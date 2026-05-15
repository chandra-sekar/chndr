(ns micropub.handler
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [micropub.auth :as auth]
            [micropub.posts :as posts]
            [ring.middleware.multipart-params :refer [wrap-multipart-params]]
            [ring.middleware.params :refer [wrap-params]]))

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

(defn- normalize-vec [v]
  (when v (if (vector? v) v [v])))

(defn- extract-params [request]
  (let [ct (get-in request [:headers "content-type"] "")]
    (if (str/includes? ct "application/json")
      (let [data (parse-json-body request)
            props (get data :properties {})]
        {:h           (first (:type data))
         :name        (first (:name props))
         :content     (extract-content (first (:content props)))
         :photo       (not-empty (vec (:photo props)))
         :bookmark-of (first (get props :bookmark-of))})
      (let [params (:params request)
            raw    (or (get params "photo[]") (get params "photo"))]
        {:h           (get params "h")
         :name        (get params "name")
         :content     (get params "content")
         :photo       (normalize-vec raw)
         :bookmark-of (get params "bookmark-of")}))))

(defn- handle-micropub-post [request]
  (let [token (bearer-token request)]
    (if-not token
      {:status 401 :body "Unauthorized"}
      (if-not (auth/validate-token token)
        {:status 403 :body "Forbidden"}
        (let [{:keys [h name content photo bookmark-of]} (extract-params request)]
          (if-not content
            {:status 400 :body "Bad Request: missing content"}
            (let [result (posts/create-post {:name name :content content :photo photo :bookmark-of bookmark-of})]
              (if (= :created (:status result))
                (do
                  (future (posts/syndicate-to-mastodon! {:content content :name name}))
                  {:status 201
                   :headers {"Location" (:url result)}
                   :body ""})
                {:status 500 :body "Internal Server Error"}))))))))

(defn- handle-media-post [request]
  (let [token (bearer-token request)]
    (if-not token
      {:status 401 :body "Unauthorized"}
      (if-not (auth/validate-token token)
        {:status 403 :body "Forbidden"}
        (let [file (get-in request [:multipart-params "file"])]
          (if-not file
            {:status 400 :body "Bad Request: missing file"}
            (let [result (posts/commit-media file)]
              (if (= :created (:status result))
                {:status 201 :headers {"Location" (:url result)} :body ""}
                {:status 500 :body "Internal Server Error"}))))))))

(defn handler [request]
  (let [method (:request-method request)
        path (:uri request)
        query (:query-string request)]
    (cond
      (and (#{:head :get} method) (= "/" path))
      {:status 200 :body ""}

      (and (= :get method) (= "/micropub" path))
      (if (= "config" (get (:params request) "q"))
        {:status 200
         :headers {"Content-Type" "application/json"}
         :body (json/write-str {"media-endpoint"
                               "https://chndr-micropub.apps.garden/micropub/media"})}
        {:status 200 :body ""})

      (and (= :post method) (= "/micropub" path))
      (handle-micropub-post request)

      (and (= :post method) (= "/micropub/media" path))
      (handle-media-post request)

      :else
      {:status 404 :body "Not Found"})))

(def app (-> handler wrap-params wrap-multipart-params))
