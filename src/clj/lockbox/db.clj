(ns lockbox.db
  (:require [taoensso.timbre :as log]
            [clojure.java.jdbc :as jdbc]
            [lockbox.conf :refer [env-var]])
  (:import (java.net InetAddress)
           (org.h2.tools Server)
           (org.h2.jdbcx JdbcConnectionPool)
           (org.h2.jdbc JdbcSQLException)))

(defn host-name []
  (.. InetAddress getLocalHost getHostName))

;; TODO - figure out why tcpSSL is not working
(def h2-server
  (delay
    (let [pwd (env-var "LOCKBOX_PWD")]
      {:classname   "org.h2.Driver"
       :subprotocol "h2"
       :subname     (format "tcp://%s/~/.lockbox/db/lockbox;jmx=true;CIPHER=AES" (host-name))
       :user        "sa"
       :password    (format "%s %s" pwd pwd)})))

(def h2-server-init
    {:classname   "org.h2.Driver"
     :subprotocol "h2"
     :subname     (format "tcp://%s/~/.lockbox/db/lockbox;jmx=true" (host-name))
     :user        "sa"
     :password    ""})

;; TODO  - this does not need to be a delay..right?
(def h2-mem
  (delay {:classname   "org.h2.Driver"
          :subprotocol "h2"
          :subname     "mem:lockbox;DB_CLOSE_DELAY=-1"
          :user        "sa"
          :password    ""}))

(defn configure-user []
  (try
    (jdbc/db-do-commands
      h2-server-init
      false
      [(format "ALTER USER SA SET PASSWORD '%s'" (env-var "LOCKBOX_PWD"))])
    (catch Exception _)))

(defn mk-h2-server
  "Create an H2 server. Returns a function which accepts the operations
  :start and :stop"
  []
  (let
    [server (->
              (into-array String ["-tcpAllowOthers" "-trace"])
              Server/createTcpServer)
     server-ops {:start (fn [] (.start server))
                 :stop  (fn [] (.stop server))}]
    (configure-user)
    (fn [operation & args] (-> (server-ops operation) (apply args)))))

(defn mk-h2-pool
  "Creates a simple H2 connection pool (supplied by H2)"
  [db-spec]
  {:datasource
   (let [{:keys [subname user password]} db-spec]
     (JdbcConnectionPool/create
       (format "jdbc:h2:%s" subname)
       user
       password))})

(defmulti db-conn "Create an env-specific DB connection" identity)

(defmethod db-conn :test [env]
  (mk-h2-pool @h2-mem))

(defmethod db-conn :prod [env]
  (mk-h2-pool @h2-server))

(defmethod db-conn :dev [env]
  (mk-h2-pool @h2-server))

;;
(def mk-conn (memoize db-conn))

(comment
  (in-ns 'lockbox.db)
  (use 'lockbox.migrations)
  (def server (mk-h2-server))

  (server :start)
  (run-migration)
  (jdbc/query (mk-conn :dev) ["select * from migrations"])
  )

