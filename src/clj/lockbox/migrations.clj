;;
;; inspired by https://github.com/technomancy/syme/blob/master/src/syme/db.clj#L66-L119
;;
(ns lockbox.migrations
  (:require [clojure.java.jdbc :as jdbc]
            [lockbox.db :refer [h2-server db-conn]]
            [lockbox.conf :refer [load-res]])
  (:import (java.sql Timestamp)))

(defn load-sql [res]
  (load-res (str "sql/migrations/" res ".sql")))

(defn initial-schema [conn]
  (->>
    (load-sql "initial-schema")
    (jdbc/db-do-commands conn)))

;; add additional functions as needed to run-migration

(defn run-and-record [conn migration]
  (migration conn)
  (jdbc/insert! conn "migrations" [:name :created_at]
               [(str (:name (meta migration)))
                (Timestamp. (System/currentTimeMillis))]))

(defn migrate [conn & migrations]
  (try
    (->>
      (jdbc/create-table-ddl "migrations"
                            [[:name :varchar "NOT NULL"]
                             [:created_at :timestamp
                              "NOT NULL" "DEFAULT CURRENT_TIMESTAMP"]])
      (jdbc/db-do-commands conn))
    (catch Exception _))
  (jdbc/with-db-transaction
    [db-conn conn]
    (let [has-run? (jdbc/query db-conn ["SELECT name FROM migrations"]
                              {:result-set-fn #(set (map :name %))})]
      (doseq [m migrations
              :when (not (has-run? (str (:name (meta m)))))]
        (run-and-record db-conn m)))))

(defn run-migration [env]
  (migrate (db-conn env)
           #'initial-schema))

(comment
  (in-ns 'lockbox.migrations)
  (run-migration :test)

  (def conn (lockbox.db/db-conn :test))
  (clojure.java.`jdbc/query conn ["select * from migrations"])
  )