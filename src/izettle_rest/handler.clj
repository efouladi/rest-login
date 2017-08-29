(ns izettle-rest.handler
  (:require [compojure.core :refer :all]
            [compojure.route :as route]
            [clojure.string :as s]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults api-defaults]]
            [ring.middleware.session :refer [wrap-session]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.middleware.json :refer [wrap-json-response wrap-json-body]]
            [ring.util.response :refer [redirect response status]]
            [buddy.auth.backends :as backends]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth :refer [authenticated? throw-unauthorized]]
            [buddy.hashers :as hashers]
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
                                   (response  {:timestamps (user-service/get-timestamps service username)})))
                            (POST "/createuser" request
                                 (let [username (get-in request [:body "username"]) password (get-in request [:body "password"])]
                                   (if (or (s/blank? username) (s/blank? password))
                                     (status (response nil) 400)
                                     (if-let [created-username (:username (user-service/add-user! service username password))]
                                       (response created-username)
                                       (status (response nil) 417)))))
                            )

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

(def auth-backend (backends/session))

(defn config-app [service]
  (routes
   (-> service
       (api-routes)
       (wrap-authorization auth-backend)
       (wrap-authentication auth-backend)
       (wrap-json-response)
       (wrap-json-body)
       (wrap-defaults defaults))
   (wrap-defaults site-routes site-defaults)))

(def app (config-app (->UserDB (atom []) hashers/derive hashers/check)))
 
