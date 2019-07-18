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
  "Upsert a tag row"
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

(defn delete-from [opts]
  {:pre [(map? opts)]}
  (let [{:keys [table id env id-col]} opts
        sql (format "delete from %s where %s = ?" table, id-col)]
    (log/infof "deleting tag with id %d" id)
    (jdbc/with-db-transaction
      [conn (mk-conn env)]
      (jdbc/execute! conn [sql id]))))


(comment
  (def id (db-io/next-seq-val "tags_seq" :dev))

  (db-io/upsert-tag {:id id :name "sample" :desc "sampel-desc" :env :dev})

  (db-io/delete-from {:table "tags" :env :dev :id 119 :id-col "tag_id"})
  )








