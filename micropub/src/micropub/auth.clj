(ns micropub.auth
  (:require [clojure.data.json :as json]
            [org.httpkit.client :as http]))

(def me-url "https://chndr.me")

(defn- normalize-url [url]
  (some-> url clojure.string/trim (clojure.string/replace #"/$" "")))

(defn validate-token [token]
  (let [{:keys [status body]} @(http/get "https://tokens.indieauth.com/token"
                                         {:headers {"Accept" "application/json"
                                                    "Authorization" (str "Bearer " token)}
                                          :timeout 5000})]
    (when (= 200 status)
      (let [data (json/read-str body :key-fn keyword)]
        (when (= me-url (normalize-url (:me data)))
          data)))))
