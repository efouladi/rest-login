(ns izettle-rest.handler-test
  (:require [clojure.test :refer :all]
            [ring.mock.request :as mock]
            [izettle-rest.handler :refer :all]))

(deftest test-app
  (testing "main route"
    (let [response (app (mock/request :get "/index.html"))]
      (is (= (:status response) 200))
      (is (= (slurp (:body response)) (slurp "./resources/public/index.html")))))

  (testing "not-found route"
    (let [response (app (mock/request :get "/invalid"))]
      (is (= (:status response) 404)))))
