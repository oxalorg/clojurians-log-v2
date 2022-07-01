(ns clojurians-log.db.slack-import
  (:require
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [clojure.java.io :as io]
   [clojurians-log.db.import :as import]
   [clojurians-log.db.queries :as queries]
   [honey.sql :as sql]
   [next.jdbc :as jdbc]))

(defmulti from-event
  "Expects slack socket live events as maps in kebab case keywords"
  (fn [event ds cache]
    [(:type event) (:subtype event)]))

(defmethod from-event ["message" nil]
  [event ds cache]
  (let [query (-> event
                  (import/event->tx cache))
        sql-query (sql/format query)]
    #_(println sql-query)
    (jdbc/execute! ds sql-query)))

(defmethod from-event ["message" "message_changed"]
  [event ds cache]
  (-> (:message event)
      (assoc :channel (:channel event))
      (from-event ds cache)))

(defmethod from-event ["message" "message_deleted"]
  [event ds cache]
  (let [query (-> event
                  (import/message-deleted->tx cache))
        sql-query (sql/format query)]
    (jdbc/execute! ds sql-query)))

(defmethod from-event ["message" "tombstone"]
  [event ds cache]
  (println "TOOOOOMB")
  (println event)
  (let [query (-> event
                  (import/message-tombstone->tx cache))
        sql-query (sql/format query)]
    (jdbc/execute! ds sql-query)))

(defmethod from-event ["reaction_added" nil]
  [event ds cache]
  (let [query (-> event
                  (import/reaction->tx cache))]
    (jdbc/execute! ds (sql/format query))))

(defmethod from-event ["reaction_removed" nil]
  [event ds cache]
  (let [query (-> event
                  (import/reaction-removed->tx cache))]
    (jdbc/execute! ds (sql/format query))))

(defmethod from-event ["channel_created" nil]
  [event ds cache]
  (let [query (-> event
                  (import/channel->tx cache))]
    (jdbc/execute! ds (sql/format query))))

(defmethod from-event ["channel_rename" nil]
  [event ds cache]
  (-> event
      (assoc :type "channel_created")
      (from-event ds cache)))

(defmethod from-event :default
  [event ds cache]
  (println "Event import not caught of type: " (:type event)))

(defn from-file [f ds]
  (with-open [reader (io/reader f)]
    (doseq [line (line-seq reader)]
      (let [event (->> line
                       read-string
                       (cske/transform-keys csk/->kebab-case-keyword))]
        (from-event event ds (queries/get-cache ds))))))

(defn from-dir [dir ds]
  (let [dir (io/file dir)
        files (->> dir
                   file-seq
                   (filter #(.isFile %))
                   sort)]
    (doseq [f files]
      (from-file f ds))))


(comment
  (require '[integrant.repl.state :as ig-state])
  (def ds (:clojurians-log.db.core/datasource ig-state/system))
  (from-file "/tmp/2022-05-17.edn" ds)
  )
