(ns clojurians-log.system
  (:require [integrant.core :as ig]
            [integrant.repl :as ig-repl]
            [clojure.java.io :as io]
            [clojure.edn :as edn]))

(defn secrets []
  (-> "config/secrets.edn"
      io/resource
      slurp
      edn/read-string))

(defn get-config [profile*]
  (let [profile (fn [{:keys [prod dev]}]
                  (condp = profile*
                    :dev dev
                    prod))
        secret (fn [& in-keys]
                 (get-in (secrets) in-keys))]
    {:clojurians-log.http/server
     {:port (profile {:prod 8919 :dev 8000})
      :ds (ig/ref :clojurians-log.db.core/datasource)}
     :clojurians-log.db.core/datasource
     {:dbtype "postgres"
      :user (or (secret :db :user) "myuser")
      :port (or (secret :db :port) "5432")
      :password (or (secret :db :password) "mypass")
      :dbname (or (secret :db :dbname) "clojurians_log")
      :serverTimezone "UTC"}
     :clojurians-log.slack.socket/app
     {:ds (ig/ref :clojurians-log.db.core/datasource)
      :slack-app-token (or (secret :slack-socket :app-token) "xapp-12345-67890")
      :slack-bot-token (or (secret :slack-socket :bot-token) "xoxb-12345-67890")
      :tx-log-directory (or (secret :tx-log-directory) "/data/tx-logs")}
     :clojurians-log.db.core/migrations
     {:opts {:store                :database
             :migration-dir        "migrations/"
             ;; :init-script          "init.sql"
             ;; :init-in-transaction? false
             :migration-table-name "migrations"}
      :ds (ig/ref :clojurians-log.db.core/datasource)}}))

(defn go [& [{:keys [profile]
              :or {profile :dev}}]]
  (println "Launching with profile " profile)
  (ig-repl/set-prep! #(doto (get-config profile) ig/load-namespaces))
  (ig-repl/go))
