(ns izettle-rest.user.protocol)
(defprotocol IUser
  (authenticate [this username password])
  (get-timestamps [this username])
  (add-user! [this username password]))

