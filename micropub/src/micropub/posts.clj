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

(defn- github-token []
  (or (System/getenv "GITHUB_TOKEN")
      (throw (ex-info "GITHUB_TOKEN env var not set" {}))))

(defn build-note
  ([content] (build-note content nil nil nil))
  ([content photos] (build-note content photos nil nil))
  ([content photos bookmark-of] (build-note content photos bookmark-of nil))
  ([content photos bookmark-of bookmark-name]
   (let [ts   (quot (System/currentTimeMillis) 1000)
         date (iso-now)
         photo-yaml    (when (seq photos)
                         (str "photo:\n" (str/join "\n" (map #(str "  - url: " % "\n    alt: \"\"") photos)) "\n"))
         bookmark-yaml (when bookmark-of (str "bookmark-of: " bookmark-of "\n"))
         name-yaml     (when bookmark-name (str "name: " bookmark-name "\n"))
         body (str "---\ndate: " date "\n" (or photo-yaml "") (or bookmark-yaml "") (or name-yaml "") "---\n" content "\n")
         path (str "notes/" ts ".md")
         url  (str "https://chndr.cc/notes/" ts "/")]
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
  (let [github-token (github-token)
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

(defn create-post [{:keys [name content photo bookmark-of]}]
  (let [github-token (github-token)
        {:keys [path body url message]} (cond
                                          bookmark-of       (build-note content photo bookmark-of name)
                                          (str/blank? name) (build-note content photo)
                                          :else             (build-article name content))
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
      {:status :created :url url :path path}
      {:status :error :http-status status})))

(defn- add-syndication [file-content mastodon-url]
  (let [second-delim (str/index-of file-content "---\n" 4)]
    (str (subs file-content 0 second-delim)
         "syndication: " mastodon-url "\n"
         (subs file-content second-delim))))

(defn update-syndication! [path mastodon-url]
  (let [github-token (github-token)
        {:keys [body]} @(http/get (str github-api "/repos/" repo "/contents/" path)
                                  {:headers {"Authorization" (str "Bearer " github-token)
                                             "Accept" "application/vnd.github+json"
                                             "X-GitHub-Api-Version" "2022-11-28"}
                                   :timeout 10000})
        {:keys [sha content]} (json/read-str body :key-fn keyword)
        decoded  (String. (.decode (Base64/getMimeDecoder) content) "UTF-8")
        updated  (add-syndication decoded mastodon-url)
        payload  (json/write-str {:message "Add syndication link"
                                  :content (base64 updated)
                                  :sha     sha
                                  :branch  "master"})
        {:keys [status]} @(http/put (str github-api "/repos/" repo "/contents/" path)
                                    {:headers {"Authorization" (str "Bearer " github-token)
                                               "Content-Type" "application/json"
                                               "Accept" "application/vnd.github+json"
                                               "X-GitHub-Api-Version" "2022-11-28"}
                                     :body    payload
                                     :timeout 10000})]
    (when-not (#{200 201} status)
      (println (str "update-syndication! failed: HTTP " status)))))

(defn syndicate-to-mastodon! [{:keys [content photo]}]
  (when-let [token (System/getenv "BRIDGY_MASTODON_TOKEN")]
    (let [props   (cond-> {:content [content]}
                    (seq photo) (assoc :photo (vec photo)))
          payload (json/write-str {:type ["h-entry"] :properties props})
          {:keys [status body]} @(http/post "https://brid.gy/micropub"
                                            {:headers {"Authorization" (str "Bearer " token)
                                                       "Content-Type"  "application/json"}
                                             :body    payload
                                             :timeout 15000})]
      (println (str "Bridgy micropub response: HTTP " status " — " body))
      (when (= 201 status)
        (:url (json/read-str body :key-fn keyword))))))
