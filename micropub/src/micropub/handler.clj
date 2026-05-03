(ns micropub.handler
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [micropub.auth :as auth]
            [micropub.posts :as posts]
            [org.httpkit.client :as http]
            [ring.middleware.params :refer [wrap-params]]))

(def ^:private base-url "https://chndr-micropub.apps.garden")
(def ^:private me-url "https://chndr.cc/")

(defn- handle-token-start [_request]
  {:status 302
   :headers {"Location" (str "https://indieauth.com/auth"
                             "?me=" (java.net.URLEncoder/encode me-url "UTF-8")
                             "&redirect_uri=" (java.net.URLEncoder/encode (str base-url "/token/callback") "UTF-8")
                             "&client_id=" (java.net.URLEncoder/encode (str base-url "/") "UTF-8")
                             "&scope=create+post"
                             "&response_type=code")}
   :body ""})

(defn- handle-token-callback [request]
  (let [code (get (:params request) "code")]
    (if-not code
      {:status 400 :body "Missing code"}
      (let [{:keys [status body]} @(http/post "https://tokens.indieauth.com/token"
                                              {:form-params {"grant_type"   "authorization_code"
                                                             "code"         code
                                                             "redirect_uri" (str base-url "/token/callback")
                                                             "client_id"    (str base-url "/")
                                                             "me"           me-url}
                                               :headers {"Accept" "application/json"}
                                               :timeout 10000})]
        (if (= 200 status)
          (let [data (json/read-str body :key-fn keyword)
                token (:access_token data)]
            {:status 200
             :headers {"Content-Type" "text/html"}
             :body (str "<html><body style='font-family:monospace;padding:2em'>"
                        "<h2>Your Bearer token</h2>"
                        "<p>Copy this token for use in your iOS Shortcut:</p>"
                        "<textarea rows='4' style='width:100%;font-size:14px'>" token "</textarea>"
                        "</body></html>")})
          {:status 500 :body (str "Token exchange failed: " body)})))))

(defn- bearer-token [request]
  (some-> (get-in request [:headers "authorization"])
          (str/replace #"(?i)^bearer\s+" "")))

(defn- parse-json-body [request]
  (when-let [body (:body request)]
    (try
      (json/read-str (slurp body) :key-fn keyword)
      (catch Exception _ nil))))

(defn- extract-content [raw]
  (cond
    (string? raw) raw
    (map? raw)    (or (:html raw) (:text raw))
    :else         nil))

(defn- extract-params [request]
  (let [ct (get-in request [:headers "content-type"] "")]
    (if (str/includes? ct "application/json")
      (let [data (parse-json-body request)
            props (get data :properties {})]
        {:h       (first (:type data))
         :name    (first (:name props))
         :content (extract-content (first (:content props)))})
      (let [params (:params request)]
        {:h       (get params "h")
         :name    (get params "name")
         :content (get params "content")}))))

(defn- handle-micropub-post [request]
  (let [token (bearer-token request)]
    (if-not token
      {:status 401 :body "Unauthorized"}
      (if-not (auth/validate-token token)
        {:status 403 :body "Forbidden"}
        (let [{:keys [h name content]} (extract-params request)]
          (if-not content
            {:status 400 :body "Bad Request: missing content"}
            (let [result (posts/create-post {:name name :content content})]
              (if (= :created (:status result))
                {:status 201
                 :headers {"Location" (:url result)}
                 :body ""}
                {:status 500 :body "Internal Server Error"}))))))))

(defn- log-request [request response]
  (println (str ">>> " (str/upper-case (name (:request-method request)))
                " " (:uri request)
                (when-let [q (:query-string request)] (str "?" q))))
  (println (str "    headers: " (select-keys (:headers request) ["authorization" "content-type" "accept" "user-agent"])))
  (println (str "    params: " (:params request)))
  (println (str "    -> " (:status response)))
  response)

(defn handler [request]
  (let [method (:request-method request)
        path (:uri request)
        query (:query-string request)]
    (log-request request
    (cond
      (#{:head :get} method)
      (if (= "/micropub" path)
        (if (= "config" (get (:params request) "q"))
          {:status 200
           :headers {"Content-Type" "application/json"}
           :body "{}"}
          {:status 200 :body ""})
        {:status 200 :body ""})

      (and (= :post method) (= "/micropub" path))
      (handle-micropub-post request)

      (and (= :get method) (= "/token" path))
      (handle-token-start request)

      (and (= :get method) (= "/token/callback" path))
      (handle-token-callback request)

      :else
      {:status 404 :body "Not Found"}))))

(def app (wrap-params handler))
