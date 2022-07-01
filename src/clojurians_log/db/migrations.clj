(ns clojurians-log.db.migrations
  (:require [migratus.core :as migratus]))

(defn init [config]
  (migratus/init config))

(defn create [config name]
  (migratus/create config name))

(defn migrate [config]
  (migratus/migrate config))

(defn rollback [config]
  "rollback the migration with the latest timestamp"
  (migratus/rollback config))

(defn up [config id]
  "bring up migrations matching the ids"
  (migratus/up config 20111206154000))

(defn down [config id]
  "bring down migrations matching the ids"
  (migratus/down config 20111206154000))


(comment
  (def mc (user/migrations-config))
  mc
  (migrate mc)
  )
