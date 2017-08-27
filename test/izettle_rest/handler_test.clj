(ns izettle-rest.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [izettle-rest.handler :refer :all]
            [izettle-rest.user.protocol :as user-service]))


(defrecord mocked-user-service []
  user-service/IUser
  (get-timestamps [this username] nil)
  (delete [this username] nil)
  (authenticate [this username password] (when (= username "test") true))
  (add-user [this username password] nil))

(defn get-app []
  (config-app (->mocked-user-service)))

(deftest test-app
  (testing "main route"
    (let [response ((get-app) (mock/request :get "/index.html"))]
      (is (= (:status response) 200))
      (is (= (slurp (:body response)) (slurp "./resources/public/index.html")))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))

(deftest login
  (testing "login fail"
    (let [response ((get-app) (mock/request :post "/api/login" {:username "unauth", :password ""}))]
      (is (= (:status response) 401))))

  (testing "login success"
    (let [response ((get-app) (mock/request :post "/api/login" {:username "test", :password ""}))]
      (is (= (:status response) 200)))))
