(ns izettle-rest.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [izettle-rest.handler :refer :all]
            [izettle-rest.user.inmemory-user-service :refer (->UserDB)]
            [izettle-rest.user.protocol :as user-service]))


(defn get-app []
  (config-app (->UserDB (atom [{:username "test" :password "testpassplushash"}]) (fn [x] (str x "plushash")))))

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
    (let [response ((get-app) (mock/request :post "/api/login" {:username "test", :password "testpass"}))]
      (is (= (:status response) 200)))))

(deftest creatUser
  (def server (get-app))
  (testing "create user and expecting to get username"
    (let [response (server (-> (mock/request :post "/api/users/createuser")
                               (mock/content-type "application/json")
                               (mock/body "{\"username\": \"test2\", \"password\":\"testpass\"}")))]
      
      (is (= (:status response) 200))
      (is (= (:body response) "test2"))))
  (testing "create existing user"
    (let [response (server (-> (mock/request :post "/api/users/createuser")
                                  (mock/content-type "application/json")
                                  (mock/body "{\"username\": \"test2\", \"password\":\"testpass\"}")))]
      
      (is (= (:status response) 417))
      (is (= (:body response) nil)))))

(deftest timestamps
  (def server (get-app))
  (testing "getting timestamps - work in progress"
    (def login (server (mock/request :post "/api/login" {:username "test2", :password "testpass"})))
    (server (-> (mock/request :post "/api/users/createuser")
                 (mock/content-type "application/json")
                 (mock/body "{\"username\": \"test2\", \"password\":\"testpass\"}")))
    (dotimes [_ 5] login)))
