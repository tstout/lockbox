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
  (let [{:keys [name description id env]} opts]
    (log/infof "updating tag with id %d" id)
    (jdbc/with-db-transaction
      [conn (mk-conn env)]
      (jdbc/execute! conn
                     ["MERGE INTO tags(tag_id, name, description)
                      KEY(tag_ID)
                      VALUES (?, ?, ?)"
                      id
                      name
                      description]))))

(defn delete-from [opts]
  {:pre [(map? opts)]}
  (let [{:keys [table id env id-col]} opts
        sql (format "delete from %s where %s = ?" table, id-col)]
    (log/infof "deleting tag with id %d" id)
    (jdbc/with-db-transaction
      [conn (mk-conn env)]
      (jdbc/execute! conn [sql id]))))

(defn fetch-tags [env]
  (log/infof "fetching tags for env %s" env)
  (jdbc/with-db-connection
    [conn (mk-conn env)]
    (->
      conn
      (jdbc/query "select tag_id, name, description from tags"))))


(comment
  (def id (next-seq-val "tags_seq" :dev))

  (upsert-tag {:id id :name "sample" :desc "sampel-desc" :env :dev})

  (delete-from {:table "tags" :env :dev :id 119 :id-col "tag_id"})

  (fetch-tags :dev)
  )








