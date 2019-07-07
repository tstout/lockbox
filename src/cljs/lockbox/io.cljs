(ns lockbox.io
  (:require [cljs.core.async :refer [<! put! pub sub chan <! >! timeout close!]]
            [cljs-http.client :as http]))

(defn next-tag-id []
  ;; TODO - call DB to get next sequence
  (rand-int 1000))

(defn next-account-id []
  (rand-int 1000))
