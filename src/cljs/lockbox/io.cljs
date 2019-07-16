(ns lockbox.io
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [cljs.tools.reader.edn :as edn]
    [cljs.core.async :refer [<! put! pub sub chan <! >! timeout close!]]
    [cljs-http.client :as http]))

(defn next-tag-id [state-fn]
  (go (let [response (<! (http/post "/next-seq" {:edn-params {:seq-name "tags_seq" :env :dev}}))
            edn-resp (edn/read-string (:body response))
            next-val (:next-seq edn-resp)]
        (js/console.log (str "response is ----->" (:body response)))
        (state-fn next-val))))

;; TODO - call DB to get next sequence
(defn next-account-id []
  (rand-int 1000))


