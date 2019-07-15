(ns lockbox.middleware
  (:require
   [ring.middleware.defaults :refer [site-defaults api-defaults wrap-defaults]]))

;(def middleware
;  [site-defaults])

(def middleware
  [api-defaults])
