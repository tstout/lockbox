(ns lockbox.conf
  (:require
    [clojure.edn :as edn]
    [clojure.java.io :as io]))


(defn load-res [res]
  (-> res
      io/resource
      slurp))

(defn load-edn-resource [res]
  (->> res
       io/resource
       slurp
       edn/read-string))

(defn env-var [name]
  (if-let [val (System/getenv name)]
    val
    (throw (Exception. (format "env var %s is not defined - required!" name)))))
