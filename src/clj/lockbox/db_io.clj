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

(defn upsert-tag
  "Upsert a new tag row"
  [opts]
  {:pre [(map? opts)]}
  (let [{:keys [name desc id env]} opts]
    (log/infof "updating tag with id %d" id)
    (jdbc/with-db-transaction
      [conn (mk-conn env)]
      (jdbc/execute! conn
                     ["MERGE INTO tags(tag_id, name, description)
                      KEY(tag_ID)
                      VALUES (?, ?, ?)"
                      id
                      name
                      desc]))))


(comment
  (def id (db-io/next-seq-val "tags_seq" :dev))

  (db-io/upsert-tag {:id id :name "sample" :desc "sampel-desc" :env :dev})

  )








