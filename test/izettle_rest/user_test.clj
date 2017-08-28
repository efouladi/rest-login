(ns izettle-rest.user-test
  (:require [izettle-rest.user.protocol :as user-service]
            [izettle-rest.user.inmemory-user-service :refer (->UserDB)]
            [clojure.test :refer :all]))

(use-fixtures :each
  (fn [tests]
    (def db (->UserDB (atom []) (fn [x] (str "hashedpass" x))))
    (tests)))


(deftest add-user
  (testing "add a user and expect it to be returned"
    (let [user (user-service/add-user! db "testuser" "testpass")]
      (is (= user {:username "testuser" :password "hashedpasstestpass" :timestamps []})))))

(deftest authenticate
  (testing "authenticate user"
    (let [user (user-service/add-user! db "testuser" "testpass")
          authenticated (user-service/authenticate db "testuser" "testpass" )
          wrong-user (user-service/authenticate db "testuserr" "testpass" )
          wrong-pass (user-service/authenticate db "testuser" "testpasss")
          ]
      (is (= (dissoc authenticated :timestamps) {:username "testuser" :password "hashedpasstestpass"}))
      (is (= (quot (get (:timestamps authenticated) 0) 1000) (quot (System/currentTimeMillis) 1000)))
      (is (= wrong-user nil))
      (is (= wrong-pass nil)))))

(deftest get-timestamps
  (testing "get the last auth timestamps"
    (let [user (user-service/add-user! db "testuser" "testpass")
          authenticated (nth (repeatedly #(user-service/authenticate db "testuser" "testpass" )) 6)]
      (is (= (count (:timestamps authenticated)) 5)))))

