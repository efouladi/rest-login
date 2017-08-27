(ns izettle-rest.user.protocol)
(defprotocol IUser
  (authenticate [this username password])
  (delete [this username])
  (get-timestamps [this username])
  (add-user [this username password]))

