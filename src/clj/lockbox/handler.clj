(ns lockbox.handler
  (:require
    [lockbox.db-io :as db-io]
    [reitit.ring :as reitit-ring]
    [lockbox.middleware :refer [middleware]]
    [hiccup.page :refer [include-js include-css html5]]
    [config.core :refer [env]]
    [lockbox.db-io :as db-io]
    [clojure.edn :as edn]))

;; TODO - add proper logging here

(def mount-target
  [:div#app
   [:h2 "Welcome to lockbox"]
   [:p "please wait while Figwheel is waking up ..."]
   [:p "(Check the js console for hints if nothing exciting happens.)"]])

(defn head []
  [:head
   [:meta {:charset "utf-8"}]
   [:meta {:name    "viewport"
           :content "width=device-width, initial-scale=1"}]
   (include-css (if (env :dev) "/css/site.css" "/css/site.min.css"))])

(defn loading-page []
  (html5
    (head)
    [:body {:class "body-container"}
     mount-target
     (include-js "/js/app.js")]))


(defn index-handler
  [_request]
  (do
    (.println System/err (format "Request pp: %s" (-> _request :path-params)))
    {:status  200
     :headers {"Content-Type" "text/html"}
     :body    (loading-page)}))

(defn next-seq-handler [request]
  (do
    {:status 200
     ;;:headers {"Content-Type" "application/json"}
     :body   (let [{:keys [seq-name env]} (edn/read-string (-> request :body slurp))]
               (str {:next-seq (db-io/next-seq-val seq-name env)}))}))

(defn save-tag-handler [request]
  (db-io/upsert-tag (edn/read-string (-> request :body slurp)))
  {:status 200})

(defn rm-tag-handler [request]
  (db-io/delete-from (merge
                       (edn/read-string (-> request :body slurp))
                       {:table "tags" :id-col "tag_id"}))
  {:status 200})

(defn fetch-tag-handler [request]
  (let [env (-> request :path-params :env keyword)]
    {:status 200
     :body  (-> (db-io/fetch-tags env) vec str)}))

(def app
  (reitit-ring/ring-handler
    (reitit-ring/router
      [["/fetch-tags"
        ["/:env" {:get {:handler fetch-tag-handler}}]]
       ["/save-tag" {:post {:handler save-tag-handler}}]
       ["/rm-tag" {:delete {:handler rm-tag-handler}}]
       ["/next-seq" {:post {:handler next-seq-handler}}]
       ["/" {:get {:handler index-handler}}]
       ["/items"
        ["" {:get {:handler index-handler}}]
        ["/:item-id" {:get {:handler    index-handler
                            :parameters {:path {:item-id int?}}}}]]
       ["/about" {:get {:handler index-handler}}]])
    (reitit-ring/routes
      (reitit-ring/create-resource-handler {:path "/" :root "/public"})
      (reitit-ring/create-default-handler))
    {:middleware middleware}))
