(ns lockbox.io
  (:require-macros [cljs.core.async.macros :refer [go go-loop]])
  (:require
    [cljs.tools.reader.edn :as edn]
    [cljs.core.async :refer [<! put! pub sub chan <! >! timeout close!]]
    [cljs-http.client :as http]))

;; TODO Need to fix hardcoded env

(defn err-check [response]
  ;;
  ;; TODO - need to add err handling - maybe an error event?
  ;;
  )

(defn next-tag-id [state-fn]
  (go
    (let [response (<! (http/post "/next-seq" {:edn-params {:seq-name "tags_seq" :env :dev}}))
          edn-resp (edn/read-string (:body response))
          next-val (:next-seq edn-resp)]
      (state-fn next-val))))

(defn rm-tag [id state-fn]
  (go
    (let [response (<! (http/delete "/rm-tag" {:edn-params {:id id :env :dev}}))
          edn-resp (edn/read-string (:body response))]
      (state-fn id))))

(defn save-tag [opts]
  {:pre [(map? opts)]}
  (go
    (let [response (<! (http/post "/save-tag" {:edn-params opts}))]
      (err-check response))))

(defn xfrm-tags
  ([] {})
  ([acc v]
   (assoc acc (v :tag_id) (merge {:dirty false} (select-keys v [:name :description])))))

(defn fetch-tags [state-fn]
  (go
    (let [response (<! (http/get "/fetch-tags/dev"))
          edn-resp (edn/read-string (:body response))
          x-tags (reduce xfrm-tags {} edn-resp)]
      (state-fn x-tags))))

;; TODO - call DB to get next sequence
(defn next-account-id []
  (rand-int 1000))


