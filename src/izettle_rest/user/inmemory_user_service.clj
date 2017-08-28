(ns izettle-rest.user.inmemory-user-service
  (:require [izettle-rest.user.protocol :as user-service]))

(defn find-user [username users]
  (first (filter #(= (:username (second %)) username)
                 (map-indexed (fn [index item] [index item]) @users))))

(defn add-timestamp [index]
  (fn [users] (update-in users [index :timestamps] #(conj (vec (take-last 4 %)) (System/currentTimeMillis)))))

(defrecord UserDB [users hash-fun]
  user-service/IUser
  (get-timestamps [this username]
    (->> (find-user username users)
         (second)
         (:timestamps)))
  (authenticate [this username password]
    (let [[index user] (find-user username users)]
      (when (= (hash-fun password) (:password user))
        (get (swap! users (add-timestamp index)) index))))

  (add-user! [this username password]
    (when-not (= username  (:username (second (find-user username users))))
      (let [new-user {:username username :password (hash-fun password) :timestamps []}]
        (swap! users #(conj % new-user))
        new-user))))
