(ns micropub.core
  (:require [micropub.handler :refer [app]]
            [org.httpkit.server :as server]))

(defonce server-instance (atom nil))

(defn start [& _]
  (let [port 7777]
    (reset! server-instance (server/run-server app {:port port}))
    (println (str "Micropub server running on port " port))))
