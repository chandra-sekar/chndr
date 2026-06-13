(ns micropub.handler-test
  (:require [clojure.test :refer [deftest is testing use-fixtures]]
            [clojure.string :as str]
            [micropub.auth :as auth]
            [micropub.handler :refer [app]]
            [micropub.posts :as posts]
            [ring.mock.request :as mock]
            [clojure.data.json :as json]))

(use-fixtures :each
  (fn [f]
    (with-redefs [posts/syndicate-to-mastodon! (fn [_] nil)
                  posts/update-syndication!    (fn [_ _] nil)]
      (f))))

(defn- valid-token-stub [_token] {:me "https://chndr.me"})
(defn- invalid-token-stub [_token] nil)
(defn- success-note-stub [_] {:status :created :url "https://chndr.me/notes/1234567890/" :path "notes/1234567890.md"})
(defn- success-article-stub [_] {:status :created :url "https://chndr.me/posts/my-title/" :path "posts/my-title.md"})
(defn- success-bookmark-stub [_] {:status :created :url "https://chndr.me/notes/1234567890/" :path "notes/1234567890.md"})
(defn- github-error-stub [_] {:status :error :http-status 422})
(defn- success-media-stub [_] {:status :created :url "https://chndr.me/img/uploads/123-photo.jpg"})
(defn- media-error-stub [_] {:status :error :http-status 422})

(defn- media-request [auth file-map]
  (-> (mock/request :post "/micropub/media")
      (mock/header "Authorization" auth)
      (assoc :multipart-params {"file" file-map})))

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

(deftest config-query-contains-media-endpoint
  (let [body (json/read-str (:body (app (mock/request :get "/micropub?q=config"))))]
    (is (contains? body "media-endpoint"))
    (is (str/includes? (get body "media-endpoint") "/micropub/media"))))

(deftest config-query-with-extra-params-still-returns-config
  (let [response (app (mock/request :get "/micropub?q=config&me=https://chndr.me"))]
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
                                       "https://chndr.me/notes/")))))

(deftest form-article-location-header-points-to-posts
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-article-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&name=My+Title&content=Body")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (clojure.string/starts-with? (get-in response [:headers "Location"])
                                       "https://chndr.me/posts/")))))

(deftest form-post-missing-content-returns-400
  (with-redefs [auth/validate-token valid-token-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&name=Title+Only")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 400 (:status response))))))

(deftest bookmark-without-content-returns-201
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-bookmark-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&bookmark-of=https%3A%2F%2Fexample.com")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 201 (:status response))))))

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

;; ---------------------------------------------------------------------------
;; Media endpoint
;; ---------------------------------------------------------------------------

(def fake-file {:filename "photo.jpg" :content-type "image/jpeg"
                :tempfile (java.io.File. "/tmp/fake-upload") :size 100})

(deftest media-post-without-token-returns-401
  (let [request (-> (mock/request :post "/micropub/media")
                    (assoc :multipart-params {"file" fake-file}))
        response (app request)]
    (is (= 401 (:status response)))))

(deftest media-post-with-invalid-token-returns-403
  (with-redefs [auth/validate-token invalid-token-stub]
    (is (= 403 (:status (app (media-request "Bearer bad" fake-file)))))))

(deftest media-post-missing-file-returns-400
  (with-redefs [auth/validate-token valid-token-stub]
    (let [request (-> (mock/request :post "/micropub/media")
                      (mock/header "Authorization" "Bearer valid-token")
                      (assoc :multipart-params {}))
          response (app request)]
      (is (= 400 (:status response))))))

(deftest media-post-returns-201
  (with-redefs [auth/validate-token valid-token-stub
                posts/commit-media  success-media-stub]
    (is (= 201 (:status (app (media-request "Bearer valid-token" fake-file)))))))

(deftest media-post-location-header-points-to-img-uploads
  (with-redefs [auth/validate-token valid-token-stub
                posts/commit-media  success-media-stub]
    (let [response (app (media-request "Bearer valid-token" fake-file))]
      (is (str/starts-with? (get-in response [:headers "Location"])
                            "https://chndr.me/img/uploads/")))))

(deftest media-github-failure-returns-500
  (with-redefs [auth/validate-token valid-token-stub
                posts/commit-media  media-error-stub]
    (is (= 500 (:status (app (media-request "Bearer valid-token" fake-file)))))))

;; ---------------------------------------------------------------------------
;; Bookmark / link posts
;; ---------------------------------------------------------------------------

(deftest form-bookmark-returns-201
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-bookmark-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&bookmark-of=https%3A%2F%2Fexample.com&content=Great+read")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 201 (:status response))))))

(deftest json-bookmark-returns-201
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-bookmark-stub]
    (let [body (json/write-str {:type ["h-entry"]
                                :properties {:bookmark-of ["https://example.com"]
                                             :content ["Great read"]}})
          request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/json")
                      (mock/body body)
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 201 (:status response))))))

(deftest bookmark-location-header-points-to-notes
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-bookmark-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&bookmark-of=https%3A%2F%2Fexample.com&content=Great+read")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (str/starts-with? (get-in response [:headers "Location"])
                            "https://chndr.me/notes/")))))

(deftest bookmark-with-name-still-routes-to-notes
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-bookmark-stub]
    (let [request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/x-www-form-urlencoded")
                      (mock/body "h=entry&name=Article+Title&bookmark-of=https%3A%2F%2Fexample.com&content=Great+read")
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (str/starts-with? (get-in response [:headers "Location"])
                            "https://chndr.me/notes/")))))

;; ---------------------------------------------------------------------------
;; Mastodon syndication
;; ---------------------------------------------------------------------------

(deftest note-syndication-uses-content-as-is
  (let [syndicate-args (promise)]
    (with-redefs [auth/validate-token           valid-token-stub
                  posts/create-post             success-note-stub
                  posts/syndicate-to-mastodon!  (fn [args] (deliver syndicate-args args) nil)]
      (app (-> (mock/request :post "/micropub")
               (mock/content-type "application/x-www-form-urlencoded")
               (mock/body "h=entry&content=Hello+world")
               (mock/header "Authorization" "Bearer valid-token")))
      (let [args (deref syndicate-args 500 :timeout)]
        (is (not= :timeout args))
        (is (= "Hello world" (:content args)))))))

(deftest article-syndication-uses-title-and-post-url
  (let [syndicate-args (promise)]
    (with-redefs [auth/validate-token           valid-token-stub
                  posts/create-post             success-article-stub
                  posts/syndicate-to-mastodon!  (fn [args] (deliver syndicate-args args) nil)]
      (app (-> (mock/request :post "/micropub")
               (mock/content-type "application/x-www-form-urlencoded")
               (mock/body "h=entry&name=My+Title&content=Body+text")
               (mock/header "Authorization" "Bearer valid-token")))
      (let [args (deref syndicate-args 500 :timeout)]
        (is (not= :timeout args))
        (is (= "New post: My Title\n\nhttps://chndr.me/posts/my-title/" (:content args)))))))

(deftest bookmark-syndication-uses-content-and-bookmark-url
  (let [syndicate-args (promise)]
    (with-redefs [auth/validate-token           valid-token-stub
                  posts/create-post             success-bookmark-stub
                  posts/syndicate-to-mastodon!  (fn [args] (deliver syndicate-args args) nil)]
      (app (-> (mock/request :post "/micropub")
               (mock/content-type "application/x-www-form-urlencoded")
               (mock/body "h=entry&bookmark-of=https%3A%2F%2Fexample.com&content=Great+read")
               (mock/header "Authorization" "Bearer valid-token")))
      (let [args (deref syndicate-args 500 :timeout)]
        (is (not= :timeout args))
        (is (= "Great read\n\nhttps://example.com" (:content args)))))))

(deftest bookmark-with-name-syndication-uses-content-and-bookmark-url
  (let [syndicate-args (promise)]
    (with-redefs [auth/validate-token           valid-token-stub
                  posts/create-post             success-bookmark-stub
                  posts/syndicate-to-mastodon!  (fn [args] (deliver syndicate-args args) nil)]
      (app (-> (mock/request :post "/micropub")
               (mock/content-type "application/x-www-form-urlencoded")
               (mock/body "h=entry&name=Article+Title&bookmark-of=https%3A%2F%2Fexample.com&content=Great+read")
               (mock/header "Authorization" "Bearer valid-token")))
      (let [args (deref syndicate-args 500 :timeout)]
        (is (not= :timeout args))
        (is (= "Great read\n\nhttps://example.com" (:content args)))))))

(deftest bookmark-without-content-does-not-syndicate
  (let [called (atom false)]
    (with-redefs [auth/validate-token           valid-token-stub
                  posts/create-post             success-bookmark-stub
                  posts/syndicate-to-mastodon!  (fn [_] (reset! called true))]
      (app (-> (mock/request :post "/micropub")
               (mock/content-type "application/x-www-form-urlencoded")
               (mock/body "h=entry&bookmark-of=https%3A%2F%2Fexample.com")
               (mock/header "Authorization" "Bearer valid-token")))
      (is (false? @called)))))

(deftest syndication-passes-photos
  (let [syndicate-args (promise)]
    (with-redefs [auth/validate-token          valid-token-stub
                  posts/create-post            success-note-stub
                  posts/syndicate-to-mastodon! (fn [args] (deliver syndicate-args args) nil)]
      (let [body (json/write-str {:type ["h-entry"]
                                  :properties {:content ["Hello with photo"]
                                               :photo ["https://chndr.me/img/uploads/123-photo.jpg"]}})]
        (app (-> (mock/request :post "/micropub")
                 (mock/content-type "application/json")
                 (mock/body body)
                 (mock/header "Authorization" "Bearer valid-token"))))
      (let [args (deref syndicate-args 500 :timeout)]
        (is (= ["https://chndr.me/img/uploads/123-photo.jpg"] (:photo args)))))))

(deftest syndication-passes-photo-alt-text
  (let [syndicate-args (promise)]
    (with-redefs [auth/validate-token          valid-token-stub
                  posts/create-post            success-note-stub
                  posts/syndicate-to-mastodon! (fn [args] (deliver syndicate-args args) nil)]
      (let [body (json/write-str {:type ["h-entry"]
                                  :properties {:content ["A photo with alt text"]
                                               :photo [{:value "https://chndr.me/img/uploads/123-photo.jpg"
                                                        :alt   "A sunset over the sea"}]}})]
        (app (-> (mock/request :post "/micropub")
                 (mock/content-type "application/json")
                 (mock/body body)
                 (mock/header "Authorization" "Bearer valid-token"))))
      (let [args  (deref syndicate-args 500 :timeout)
            photo (first (:photo args))]
        (is (not= :timeout args))
        (is (= "https://chndr.me/img/uploads/123-photo.jpg" (:value photo)))
        (is (= "A sunset over the sea" (:alt photo)))))))

(deftest photo-only-post-returns-201
  (with-redefs [auth/validate-token valid-token-stub
                posts/create-post   success-note-stub]
    (let [body (json/write-str {:type ["h-entry"]
                                :properties {:photo ["https://chndr.me/img/uploads/123-photo.jpg"]}})
          request (-> (mock/request :post "/micropub")
                      (mock/content-type "application/json")
                      (mock/body body)
                      (mock/header "Authorization" "Bearer valid-token"))
          response (app request)]
      (is (= 201 (:status response))))))

(deftest photo-only-post-syndication-is-called
  (let [syndicate-args (promise)]
    (with-redefs [auth/validate-token          valid-token-stub
                  posts/create-post            success-note-stub
                  posts/syndicate-to-mastodon! (fn [args] (deliver syndicate-args args) nil)]
      (let [body (json/write-str {:type ["h-entry"]
                                  :properties {:photo ["https://chndr.me/img/uploads/123-photo.jpg"]}})]
        (app (-> (mock/request :post "/micropub")
                 (mock/content-type "application/json")
                 (mock/body body)
                 (mock/header "Authorization" "Bearer valid-token"))))
      (let [args (deref syndicate-args 500 :timeout)]
        (is (not= :timeout args))
        (is (= ["https://chndr.me/img/uploads/123-photo.jpg"] (:photo args)))))))

(deftest syndication-not-called-on-github-failure
  (let [called (atom false)]
    (with-redefs [auth/validate-token           valid-token-stub
                  posts/create-post             github-error-stub
                  posts/syndicate-to-mastodon!  (fn [_] (reset! called true))]
      (app (-> (mock/request :post "/micropub")
               (mock/content-type "application/x-www-form-urlencoded")
               (mock/body "h=entry&content=Hello")
               (mock/header "Authorization" "Bearer valid-token")))
      (is (false? @called)))))

(deftest update-syndication-called-with-path-and-mastodon-url
  (let [update-args (promise)]
    (with-redefs [auth/validate-token           valid-token-stub
                  posts/create-post             success-note-stub
                  posts/syndicate-to-mastodon!  (fn [_] "https://mastodon.social/@chander/999")
                  posts/update-syndication!     (fn [path url] (deliver update-args {:path path :url url}))]
      (app (-> (mock/request :post "/micropub")
               (mock/content-type "application/x-www-form-urlencoded")
               (mock/body "h=entry&content=Hello+world")
               (mock/header "Authorization" "Bearer valid-token")))
      (let [args (deref update-args 500 :timeout)]
        (is (not= :timeout args))
        (is (= "notes/1234567890.md" (:path args)))
        (is (= "https://mastodon.social/@chander/999" (:url args)))))))

(deftest update-syndication-not-called-when-bridgy-returns-nil
  (let [called (promise)]
    (with-redefs [auth/validate-token           valid-token-stub
                  posts/create-post             success-note-stub
                  posts/syndicate-to-mastodon!  (fn [_] nil)
                  posts/update-syndication!     (fn [_ _] (deliver called true))]
      (app (-> (mock/request :post "/micropub")
               (mock/content-type "application/x-www-form-urlencoded")
               (mock/body "h=entry&content=Hello+world")
               (mock/header "Authorization" "Bearer valid-token")))
      (is (= :not-called (deref called 200 :not-called))))))
