(ns izettle-rest.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response]]
            [ring.util.response :refer [redirect response status]]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [izettle-rest.user.protocol :as user-service]
            [izettle-rest.user.inmemory-user-service :refer (->UserDB)]
            ))


(defroutes site-routes 
  (GET "/" [] (redirect "index.html"))
  (route/not-found "Not Found")
  (route/resources "/"))

(defn api-routes [service]
  (routes (context "/api" []
                   (context "/users" []
                            (GET "/:username" [username :as request]
                                 (if-not (authenticated? request)
                                   (throw-unauthorized)
                                   username)))
                   (GET "/logout" [] (-> (response nil)
                                         (assoc :session {})))
                   (POST "/login" [username password :as request]
                         (if-let [user (user-service/authenticate service username password)]
                           (let [updated-session (assoc (:session request) :identity (keyword username))]
                            (-> (response user)
                                (assoc :session updated-session)))
                           (status (response nil) 401)))
                   (ANY "*" [] (route/not-found "")))))

(def defaults (-> api-defaults
                  (assoc :session true)
                  (assoc-in [:responses :content-types] false)))

(defn config-app [service]
  (routes
   (-> service
       (api-routes)
       (wrap-authentication (backends/session))
       (wrap-json-response)
       (wrap-defaults defaults))
   (wrap-defaults site-routes site-defaults)))

(def app (config-app (->UserDB (atom []) (fn [x] (str x "plushash")))))
 
