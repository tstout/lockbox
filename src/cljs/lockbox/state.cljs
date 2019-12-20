(ns lockbox.state
  (:require [reagent.core :as r]
            [lockbox.io :refer [next-tag-id next-account-id save-tag rm-tag fetch-tags]]))

(def app-state (r/atom
                 {:env      :dev
                  :tags     {}
                  :accounts {}}))


;{1 {:name "work" :desc "work-related stuff" :dirty false}
; 2 {:name "banking" :desc "bank-related stuff" :dirty false}}}))


(defn tags []
  (:tags @app-state))

(defn tag-keys []
  (-> @(r/track tags)
      keys
      sort))

(defn tag [id]
  (-> @(r/track tags)
      (get id)))

(defn accounts []
  (:accounts @app-state))

(defn account-keys []
  (-> @(r/track accounts)
      keys
      sort))

(defn account [id]
  (-> @(r/track accounts)
      (get id)))

(declare event-handler)

(defn emit [e]
  (js/console.log "Handling event" (str e))
  (r/rswap! app-state event-handler e))

;;This app is simple enough that this type of event handling might be just barely good
;; enough. A multi-method approach might scale better visually."
(defn event-handler
  "Process events fired from UI components - updating state as appropriate."
  [state [event-name id value]]
  (case event-name
    :tags-loaded (assoc-in state [:tags] value)
    :accounts-loaded (assoc-in state [:accounts] value)
    :maybe-load-tags (do
                       (when (empty? (state :tags))
                         (fetch-tags (fn [response]
                                       (emit [:tags-loaded 1 response]))))
                       state)
    :maybe-load-accounts (do
                           (when (empty? (state :accounts))
                             (fetch-tags (fn [response]
                                           (emit [:accounts-loaded 1 response]))))
                           state)
    :tag-seq-avail (assoc-in state [:tags id] {:name "" :description "" :dirty true})
    :add-tag (do
               (next-tag-id (fn [val] (emit [:tag-seq-avail val])))
               state)
    :set-tag-name (->
                    state
                    (assoc-in [:tags id :name] value)
                    (assoc-in [:tags id :dirty] true))
    :set-tag-desc (->
                    state
                    (assoc-in [:tags id :description] value)
                    (assoc-in [:tags id :dirty] true))
    :rm-tag (do
              (rm-tag id (fn [id] (emit [:tag-removed id])))
              state)
    :tag-removed (update-in state [:tags] dissoc id)
    :tag-blur (do
                ;;(prn (str "onBlur " id))
                (if (get-in state [:tags id :dirty])
                  (do
                    ;;(prn (str "Should update/save" (get-in state [:tags id])))
                    ;; TODO - this should likely follow the state-fn pattern
                    (save-tag (merge
                                {:id id :env (:env state)}
                                (get-in state [:tags id])))
                    (assoc-in state [:tags id :dirty] false))
                  state))
    state))

