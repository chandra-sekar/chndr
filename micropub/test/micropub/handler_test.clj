(ns micropub.handler-test
  (:require [clojure.test :refer [deftest is testing]]
            [micropub.auth :as auth]
            [micropub.handler :refer [app]]
            [micropub.posts :as posts]
            [ring.mock.request :as mock]
            [clojure.data.json :as json]))

(defn- valid-token-stub [_token] {:me "https://chndr.cc"})
(defn- invalid-token-stub [_token] nil)
(defn- success-note-stub [_] {:status :created :url "https://chndr.cc/notes/1234567890/"})
(defn- success-article-stub [_] {:status :created :url "https://chndr.cc/posts/my-title/"})
(defn- github-error-stub [_] {:status :error :http-status 422})

;; ---------------------------------------------------------------------------
;; Health check
;; ---------------------------------------------------------------------------

(deftest head-root-returns-200
  (let [response (app (mock/request :head "/"))]
    (is (= 200 (:status response)))))

(deftest get-root-returns-200
  (let [response (app (mock/request :get "/"))]
    (is (= 200 (:status response)))))

(deftest unknown-path-returns-404
  (let [response (app (mock/request :get "/does-not-exist"))]
    (is (= 404 (:status response)))))

;; ---------------------------------------------------------------------------
;; Micropub config
;; ---------------------------------------------------------------------------

(deftest config-query-returns-200
  (let [response (app (mock/request :get "/micropub?q=config"))]
    (is (= 200 (:status response)))))

(deftest config-query-returns-json-content-type
  (let [response (app (mock/request :get "/micropub?q=config"))]
    (is (= "application/json" (get-in response [:headers "Content-Type"])))))

(deftest config-query-returns-empty-object
  (let [response (app (mock/request :get "/micropub?q=config"))]
    (is (= {} (json/read-str (:body response))))))

(deftest config-query-with-extra-params-still-returns-config
  (let [response (app (mock/request :get "/micropub?q=config&me=https://chndr.cc"))]
    (is (= 200 (:status response)))
    (is (= "application/json" (get-in response [:headers "Content-Type"])))))

;; ---------------------------------------------------------------------------
;; Authentication
;; ---------------------------------------------------------------------------

(deftest post-without-token-returns-401
  (let [request (-> (mock/request :post "/micropub")
                    (mock/content-type "application/x-www-form-urlencoded")
                    (mock/body "h=entry&content=hello"))
        response (app request)]
    (is (= 401 (:status response)))))

(deftest post-with-invalid-token-returns-403
  (with-redefs [auth/validate-token invalid-token-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&content=hello")
                      (mock/header "Authorization" "Bearer bad-token"))
          response (app request)]
      (is (= 403 (:status response))))))

;; ---------------------------------------------------------------------------
;; Form-encoded post creation
;; ---------------------------------------------------------------------------

(deftest form-note-returns-201
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-note-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&content=Hello+world")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 201 (:status response))))))

(deftest form-note-location-header-points-to-notes
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-note-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&content=Hello+world")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (clojure.string/starts-with? (get-in response [:headers "Location"])
                                       "https://chndr.cc/notes/")))))

(deftest form-article-location-header-points-to-posts
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-article-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&name=My+Title&content=Body")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (clojure.string/starts-with? (get-in response [:headers "Location"])
                                       "https://chndr.cc/posts/")))))

(deftest form-post-missing-content-returns-400
  (with-redefs [auth/validate-token valid-token-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&name=Title+Only")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 400 (:status response))))))

;; ---------------------------------------------------------------------------
;; JSON post creation
;; ---------------------------------------------------------------------------

(deftest json-note-returns-201
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-note-stub]
    (let [body (json/write-str {:type ["h-entry"]
                                :properties {:content ["Hello world"]}})
          request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/json")
                      (mock/body body)
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 201 (:status response))))))

(deftest json-article-returns-201
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-article-stub]
    (let [body (json/write-str {:type ["h-entry"]
                                :properties {:name ["My Article"]
                                             :content ["Body text"]}})
          request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/json")
                      (mock/body body)
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 201 (:status response))))))

(deftest json-html-content-map-returns-201
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-note-stub]
    (let [body (json/write-str {:type ["h-entry"]
                                :properties {:content [{:html "<p>Hello</p>"}]}})
          request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/json")
                      (mock/body body)
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 201 (:status response))))))

;; ---------------------------------------------------------------------------
;; GitHub failure
;; ---------------------------------------------------------------------------

(deftest github-failure-returns-500
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   github-error-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&content=Hello")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 500 (:status response))))))
