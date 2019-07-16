(ns lockbox.db-io
  (:require [clojure.java.jdbc :as jdbc]
            [taoensso.timbre :as log]
            [lockbox.db :refer [mk-conn]]))

(defn next-seq-val
  "Get the next value from the named sequence"
  [seq-name env]
  (log/infof "next-seq-val seq-name: %s env: %s" seq-name env)
  (jdbc/with-db-connection
    [conn (mk-conn env)]
    (->
      conn
      (jdbc/query (format "select nextval('%s') as value" seq-name))
      first
      :value)))






