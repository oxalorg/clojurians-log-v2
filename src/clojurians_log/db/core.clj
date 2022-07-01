(ns clojurians-log.db.core
  (:require [integrant.core :as ig]
            [clojurians-log.db.migrations :as migrations]
            [next.jdbc :as jdbc]
            [next.jdbc.date-time :as jdbc.date-time]))

(defmethod ig/init-key ::datasource [_ config]
  ;; TODO: add connection pooling
  (let [ds (jdbc/get-datasource config)]
    (jdbc.date-time/read-as-instant)
    (jdbc/with-options ds jdbc/unqualified-snake-kebab-opts)))

(defmethod ig/halt-key! ::datasource [_ datasource]
  ())

(defmethod ig/init-key ::migrations [_ {:keys [ds opts] :as config}]
  (merge opts {:db {:datasource (jdbc/get-datasource ds)}}))

(defmethod ig/halt-key! ::migrations [_ config]
  ())
