(ns micropub.posts-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [micropub.posts :refer [build-note build-article build-media]]))

;; ---------------------------------------------------------------------------
;; Note structure
;; ---------------------------------------------------------------------------

(deftest note-path-is-under-notes-directory
  (let [{:keys [path]} (build-note "Hello world")]
    (is (str/starts-with? path "notes/"))))

(deftest note-path-ends-with-md
  (let [{:keys [path]} (build-note "Hello world")]
    (is (str/ends-with? path ".md"))))

(deftest note-path-filename-is-numeric-timestamp
  (let [{:keys [path]} (build-note "Hello world")
        filename (-> path (str/split #"/") last (str/replace #"\.md$" ""))]
    (is (re-matches #"\d+" filename))))

(deftest note-url-points-to-notes
  (let [{:keys [url]} (build-note "Hello world")]
    (is (str/starts-with? url "https://chndr.cc/notes/"))))

(deftest note-url-ends-with-slash
  (let [{:keys [url]} (build-note "Hello world")]
    (is (str/ends-with? url "/"))))

(deftest note-body-contains-content
  (let [{:keys [body]} (build-note "Hello world")]
    (is (str/includes? body "Hello world"))))

(deftest note-frontmatter-contains-date
  (let [{:keys [body]} (build-note "Hello world")]
    (is (re-find #"(?m)^date: " body))))

(deftest note-frontmatter-does-not-contain-title
  (let [{:keys [body]} (build-note "Hello world")]
    (is (not (str/includes? body "title:")))))

(deftest note-frontmatter-does-not-contain-layout
  (let [{:keys [body]} (build-note "Hello world")]
    (is (not (str/includes? body "layout:")))))

(deftest note-without-photos-has-no-photo-frontmatter
  (let [{:keys [body]} (build-note "Hello world")]
    (is (not (str/includes? body "photo:")))))

(deftest note-with-photo-includes-photo-frontmatter
  (let [{:keys [body]} (build-note "Hello world" ["https://chndr.cc/img/uploads/123-photo.jpg"])]
    (is (str/includes? body "photo:"))
    (is (str/includes? body "  - url: https://chndr.cc/img/uploads/123-photo.jpg"))
    (is (str/includes? body "    alt: \"\""))))

(deftest note-with-multiple-photos-includes-all-urls
  (let [{:keys [body]} (build-note "Hello world" ["https://chndr.cc/img/uploads/1-a.jpg"
                                                   "https://chndr.cc/img/uploads/2-b.jpg"])]
    (is (str/includes? body "  - url: https://chndr.cc/img/uploads/1-a.jpg"))
    (is (str/includes? body "  - url: https://chndr.cc/img/uploads/2-b.jpg"))))

;; ---------------------------------------------------------------------------
;; Article structure
;; ---------------------------------------------------------------------------

(deftest article-path-is-under-posts-directory
  (let [{:keys [path]} (build-article "My Article" "Body")]
    (is (str/starts-with? path "posts/"))))

(deftest article-path-ends-with-md
  (let [{:keys [path]} (build-article "My Article" "Body")]
    (is (str/ends-with? path ".md"))))

(deftest article-url-points-to-posts
  (let [{:keys [url]} (build-article "My Article" "Body")]
    (is (str/starts-with? url "https://chndr.cc/posts/"))))

(deftest article-url-ends-with-slash
  (let [{:keys [url]} (build-article "My Article" "Body")]
    (is (str/ends-with? url "/"))))

(deftest article-body-contains-content
  (let [{:keys [body]} (build-article "My Article" "Body text")]
    (is (str/includes? body "Body text"))))

(deftest article-frontmatter-contains-title
  (let [{:keys [body]} (build-article "My Article" "Body")]
    (is (re-find #"(?m)^title: My Article" body))))

(deftest article-frontmatter-contains-date
  (let [{:keys [body]} (build-article "My Article" "Body")]
    (is (re-find #"(?m)^date: " body))))

(deftest article-frontmatter-contains-layout
  (let [{:keys [body]} (build-article "My Article" "Body")]
    (is (re-find #"(?m)^layout: " body))))

;; ---------------------------------------------------------------------------
;; Slug generation
;; ---------------------------------------------------------------------------

(deftest slug-is-lowercased
  (let [{:keys [path]} (build-article "My Article" "Body")]
    (is (str/includes? path "my-article"))))

(deftest slug-replaces-spaces-with-hyphens
  (let [{:keys [path]} (build-article "hello world" "Body")]
    (is (str/includes? path "hello-world"))))

(deftest slug-strips-special-characters
  (let [{:keys [path]} (build-article "Hello, World!" "Body")]
    (is (str/includes? path "hello-world"))))

(deftest slug-no-leading-or-trailing-hyphens
  (let [{:keys [path]} (build-article "  spaced  " "Body")
        filename (-> path (str/split #"/") last (str/replace #"\.md$" ""))]
    (is (not (str/starts-with? filename "-")))
    (is (not (str/ends-with? filename "-")))))

;; ---------------------------------------------------------------------------
;; Media structure
;; ---------------------------------------------------------------------------

(deftest media-path-is-under-img-uploads
  (let [{:keys [path]} (build-media "photo.jpg")]
    (is (str/starts-with? path "img/uploads/"))))

(deftest media-path-has-timestamp-prefix
  (let [{:keys [path]} (build-media "photo.jpg")
        filename (-> path (str/split #"/") last)]
    (is (re-matches #"\d+-.*" filename))))

(deftest media-path-contains-original-filename
  (let [{:keys [path]} (build-media "photo.jpg")]
    (is (str/ends-with? path "photo.jpg"))))

(deftest media-url-points-to-img-uploads
  (let [{:keys [url]} (build-media "photo.jpg")]
    (is (str/starts-with? url "https://chndr.cc/img/uploads/"))))

(deftest media-url-does-not-end-with-slash
  (let [{:keys [url]} (build-media "photo.jpg")]
    (is (not (str/ends-with? url "/")))))
