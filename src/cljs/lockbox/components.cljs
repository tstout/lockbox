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

(defn cell-edit
  ""
  [opts]
  {:pre [map? opts]}
  (let [{:keys [event id tag-row tag-element]} opts]
    [:input {:value     (tag-element tag-row)
             :on-change #(emit [event id (.-target.value %)])
             :on-blur   #(emit [:tag-blur id])}]))

(defn tag-edit [id]
  (let [t @(r/track tag id)]
    [:tr
     [:td id]
     [:td
      [cell-edit {:event :set-tag-name :id id :tag-row t :tag-element :name}]]
     [:td
      [cell-edit {:event :set-tag-desc :id id :tag-row t :tag-element :description}]]
     [:td
      [:input {:type 'button :value "-" :on-click #(emit [:rm-tag id])}]]]))

(defn edit-tags []
  (let [ids @(r/track tag-keys)]
    [:div
     [:table
      [:thead
       [:tr
        [:th "Id"]
        [:th "Name"]
        [:th "Description"]]]
      [:tbody
       (for [i ids]
         ^{:key i} [tag-edit i])]]
     [:input {:type     'button
              :value    "+"
              :on-click #(emit [:add-tag])}]]))


