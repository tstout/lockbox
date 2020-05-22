(ns lockbox.middleware
  (:require
   [ring.middleware.content-type :refer [wrap-content-type]]
   [ring.middleware.params :refer [wrap-params]]
   [prone.middleware :refer [wrap-exceptions]]
   [ring.middleware.reload :refer [wrap-reload]]
   [ring.middleware.defaults :refer [site-defaults api-defaults wrap-defaults]]))

(def middleware
  [#(wrap-defaults % api-defaults)
   wrap-exceptions
   wrap-reload])
