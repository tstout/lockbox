(ns lockbox.components
  (:require [lockbox.state :refer [emit tags tag tag-keys]]
            [reagent.core :as r]))

(defn tag-comp [id]
  (let [p @(r/track tag id)]
    [:li
     (:name p)]))

(defn tag-list []
  (let [ids @(r/track tag-keys)]
    [:ul
     (for [i ids]
       ^{:key i} [tag-comp i])]))

(defn tag-edit [id]
  (let [p @(r/track tag id)]
    [:div
     [:input {:value     (:name p)
              :on-change #(emit [:set-tag id (.-target.value %)])}]]))

(defn edit-tags []
  (let [ids @(r/track tag-keys)]
    [:div
     [tag-list]
     (for [i ids]
       ^{:key i} [tag-edit i])
     [:input {:type     'button
              :value    "Add Tag"
              :on-click #(emit [:add-tag])}]]))


