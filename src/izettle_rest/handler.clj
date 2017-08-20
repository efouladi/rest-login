(ns izettle-rest.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [ring.util.response :refer [redirect]]))

(defn app-routes [user-service]
  (routes (GET "/" [] (redirect "index.html"))
          (route/not-found "Not Found")
          (route/resources "/")))


(defn config-app [user-service]
  (wrap-defaults (app-routes user-service)  site-defaults))

(def app (config-app nil))
