(ns izettle-rest.user.protocol)
(defprotocol IUser
  (getById [id])
  (delete [id])
  (addLoginTimeStamp [])
  (addUser [username password]))

