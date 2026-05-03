(ns micropub.posts
  (:require [clojure.data.json :as json]
            [clojure.string :as str]
            [org.httpkit.client :as http])
  (:import [java.time Instant]
           [java.util Base64]
           [java.nio.file Files]))

(def github-api "https://api.github.com")
(def repo "chandra-sekar/chndr")

(defn- slugify [s]
  (-> s
      str/lower-case
      (str/replace #"[^a-z0-9]+" "-")
      (str/replace #"^-|-$" "")))

(defn- base64 [s]
  (.encodeToString (Base64/getEncoder) (.getBytes s "UTF-8")))

(defn- base64-bytes [^bytes b]
  (.encodeToString (Base64/getEncoder) b))

(defn- iso-now []
  (str (Instant/now)))

(defn build-note
  ([content] (build-note content nil))
  ([content photos]
   (let [ts (quot (System/currentTimeMillis) 1000)
         date (iso-now)
         photo-yaml (when (seq photos)
                      (str "photo:\n" (str/join "\n" (map #(str "  - url: " % "\n    alt: \"\"") photos)) "\n"))
         body (str "---\ndate: " date "\n" (or photo-yaml "") "---\n" content "\n")
         path (str "notes/" ts ".md")
         url (str "https://chndr.cc/notes/" ts "/")]
     {:path path :body body :url url
      :message "Add note via micropub"})))

(defn build-article [name content]
  (let [slug (slugify name)
        date (iso-now)
        body (str "---\ntitle: " name "\ndate: " date "\nlayout: layouts/post.njk\n---\n" content "\n")
        path (str "posts/" slug ".md")
        url (str "https://chndr.cc/posts/" slug "/")]
    {:path path :body body :url url
     :message (str "Add article via micropub: " name)}))

(defn build-media [filename]
  (let [ts   (quot (System/currentTimeMillis) 1000)
        name (str ts "-" filename)
        path (str "img/uploads/" name)
        url  (str "https://chndr.cc/img/uploads/" name)]
    {:path path :url url :message (str "Add media via micropub: " name)}))

(defn commit-media [{:keys [filename tempfile]}]
  (let [github-token (System/getenv "GITHUB_TOKEN")
        {:keys [path url message]} (build-media filename)
        payload (json/write-str {:message message
                                 :content (base64-bytes (Files/readAllBytes (.toPath tempfile)))
                                 :branch "master"})
        {:keys [status]} @(http/put (str github-api "/repos/" repo "/contents/" path)
                                    {:headers {"Authorization" (str "Bearer " github-token)
                                               "Content-Type" "application/json"
                                               "Accept" "application/vnd.github+json"
                                               "X-GitHub-Api-Version" "2022-11-28"}
                                     :body payload
                                     :timeout 10000})]
    (if (= 201 status)
      {:status :created :url url}
      {:status :error :http-status status})))

(defn create-post [{:keys [name content photo]}]
  (let [github-token (System/getenv "GITHUB_TOKEN")
        {:keys [path body url message]} (if (str/blank? name)
                                          (build-note content photo)
                                          (build-article name content))
        payload (json/write-str {:message message
                                 :content (base64 body)
                                 :branch "master"})
        {:keys [status]} @(http/put (str github-api "/repos/" repo "/contents/" path)
                                    {:headers {"Authorization" (str "Bearer " github-token)
                                               "Content-Type" "application/json"
                                               "Accept" "application/vnd.github+json"
                                               "X-GitHub-Api-Version" "2022-11-28"}
                                     :body payload
                                     :timeout 10000})]
    (if (= 201 status)
      {:status :created :url url}
      {:status :error :http-status status})))
