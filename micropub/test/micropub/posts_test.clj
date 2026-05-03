(ns micropub.posts-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [micropub.posts :refer [build-note build-article]]))

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
