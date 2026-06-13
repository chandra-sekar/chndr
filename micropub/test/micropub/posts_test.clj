(ns micropub.posts-test
  (:require [clojure.test :refer [deftest is testing]]
            [clojure.string :as str]
            [micropub.posts :refer [build-note build-article build-media]]
            [micropub.posts]))

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
    (is (str/starts-with? url "https://chndr.me/notes/"))))

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

(deftest note-without-bookmark-has-no-bookmark-of
  (let [{:keys [body]} (build-note "Hello world")]
    (is (not (str/includes? body "bookmark-of:")))))

(deftest note-with-bookmark-contains-bookmark-of
  (let [{:keys [body]} (build-note "Great read" nil "https://example.com/article")]
    (is (str/includes? body "bookmark-of: https://example.com/article"))))

(deftest note-with-bookmark-and-photo-contains-both
  (let [{:keys [body]} (build-note "Great read" ["https://chndr.me/img/uploads/1-a.jpg"] "https://example.com")]
    (is (str/includes? body "photo:"))
    (is (str/includes? body "bookmark-of: https://example.com"))))

(deftest note-with-bookmark-name-contains-name
  (let [{:keys [body]} (build-note "Great read" nil "https://example.com" "Example Article")]
    (is (str/includes? body "name: Example Article"))))

(deftest note-without-bookmark-name-has-no-name
  (let [{:keys [body]} (build-note "Hello world" nil "https://example.com")]
    (is (not (str/includes? body "name:")))))

(deftest note-with-photo-includes-photo-frontmatter
  (let [{:keys [body]} (build-note "Hello world" ["https://chndr.me/img/uploads/123-photo.jpg"])]
    (is (str/includes? body "photo:"))
    (is (str/includes? body "  - url: https://chndr.me/img/uploads/123-photo.jpg"))
    (is (str/includes? body "    alt: \"\""))))

(deftest note-with-multiple-photos-includes-all-urls
  (let [{:keys [body]} (build-note "Hello world" ["https://chndr.me/img/uploads/1-a.jpg"
                                                   "https://chndr.me/img/uploads/2-b.jpg"])]
    (is (str/includes? body "  - url: https://chndr.me/img/uploads/1-a.jpg"))
    (is (str/includes? body "  - url: https://chndr.me/img/uploads/2-b.jpg"))))

(deftest note-with-photo-map-uses-value-as-url
  (let [{:keys [body]} (build-note "Hello world" [{:value "https://chndr.me/img/uploads/123-photo.jpg"
                                                    :alt "A description"}])]
    (is (str/includes? body "  - url: https://chndr.me/img/uploads/123-photo.jpg"))
    (is (str/includes? body "    alt: \"A description\""))))

(deftest note-with-photo-map-missing-alt-defaults-to-empty
  (let [{:keys [body]} (build-note "Hello world" [{:value "https://chndr.me/img/uploads/123-photo.jpg"}])]
    (is (str/includes? body "  - url: https://chndr.me/img/uploads/123-photo.jpg"))
    (is (str/includes? body "    alt: \"\""))))

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
    (is (str/starts-with? url "https://chndr.me/posts/"))))

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
    (is (str/starts-with? url "https://chndr.me/img/uploads/"))))

(deftest media-url-does-not-end-with-slash
  (let [{:keys [url]} (build-media "photo.jpg")]
    (is (not (str/ends-with? url "/")))))

;; ---------------------------------------------------------------------------
;; add-syndication
;; ---------------------------------------------------------------------------

(deftest add-syndication-inserts-field-in-frontmatter
  (let [content "---\ndate: 2026-01-01\n---\nHello world\n"
        result  (#'micropub.posts/add-syndication content "https://mastodon.social/@chander/123")]
    (is (str/includes? result "syndication: https://mastodon.social/@chander/123"))))

(deftest add-syndication-preserves-existing-frontmatter
  (let [content "---\ndate: 2026-01-01\n---\nHello world\n"
        result  (#'micropub.posts/add-syndication content "https://mastodon.social/@chander/123")]
    (is (str/includes? result "date: 2026-01-01"))))

(deftest add-syndication-preserves-body
  (let [content "---\ndate: 2026-01-01\n---\nHello world\n"
        result  (#'micropub.posts/add-syndication content "https://mastodon.social/@chander/123")]
    (is (str/includes? result "Hello world"))))

(deftest add-syndication-starts-with-frontmatter-delimiter
  (let [content "---\ndate: 2026-01-01\n---\nHello world\n"
        result  (#'micropub.posts/add-syndication content "https://mastodon.social/@chander/123")]
    (is (str/starts-with? result "---\n"))))
