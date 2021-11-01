(ns clojurians-log.db.bulk-import
  (:require [clojure.java.io :as io]
            [clojurians-log.db.import :as import]
            [clojurians-log.utils :as utils]
            [clojurians-log.slack.api :as clj-slack]
            [clojurians-log.system :as system]
            [honey.sql :as sql]
            [integrant.repl.state :as ig-state]
            [next.jdbc :as jdbc]
            [clojurians-log.db.queries :as queries]))

(def ds (:clojurians-log.db.core/datasource ig-state/system))

(defn channels
  "Imports channels idempotently based on the slack_id"
  [ds path-or-data]
  (cond
    (string? path-or-data) (channels ds (utils/read-json-from-file (io/file path-or-data "channels.json")))
    (seq path-or-data)
    (let [slack-data path-or-data
          data (into []
                     (comp
                      (map #(utils/select-keys-nested-as
                             % [{:keys :id
                                 :rename :slack-id}
                                :name
                                {:keys [:topic :value]
                                 :rename :topic}
                                {:keys [:purpose :value]
                                 :rename :purpose}])))
                     slack-data)
          sqlmap {:insert-into [:channel]
                  :values data
                  :on-conflict :slack-id
                  :do-update-set {:fields [:name :topic :purpose]}}]
      (jdbc/execute! ds (sql/format sqlmap)))))

(defn members
  "Imports members idempotently based on the (slack) id"
  [ds path-or-data]
  (cond
    (string? path-or-data) (channels ds (utils/read-json-from-file (io/file path-or-data "users.json")))
    (seq path-or-data)
    (let [slack-data path-or-data
          data (into []
                     (comp
                      (map #(utils/select-keys-nested-as
                             % [:name
                                {:keys [:id]
                                 :rename :slack-id}
                                :team-id
                                [:profile :real-name]
                                [:profile :real-name-normalized]
                                [:profile :display-name]
                                [:profile :display-name-normalized]
                                [:profile :first-name]
                                [:profile :last-name]
                                [:profile :title]
                                [:profile :skype]
                                [:profile :phone]
                                ;;[:profile :image-original]
                                [:profile :image-24]
                                [:profile :image-32]
                                [:profile :image-48]
                                [:profile :image-72]
                                [:profile :image-192]
                                [:profile :image-512]
                                :is-admin
                                :is-bot
                                :tz
                                :tz-offset
                                :tz-label
                                :deleted
                                :bot-id
                                :is-email-confirmed])))
                     slack-data)
          sqlmap {:insert-into [:member]
                  :values data
                  :on-conflict :slack-id
                  :do-update-set {:fields [:name
                                           :team-id
                                           :real-name
                                           :real-name-normalized
                                           :display-name
                                           :display-name-normalized
                                           :first-name
                                           :last-name
                                           :title
                                           :skype
                                           :phone
                                           :is-admin
                                           :image-24
                                           :image-32
                                           :image-48
                                           :image-72
                                           :image-192
                                           :image-512
                                           :is-bot
                                           :tz
                                           :tz-offset
                                           :tz-label
                                           :deleted
                                           :bot-id
                                           :is-email-confirmed]}}
          sqlquery (sql/format sqlmap)]
      (jdbc/execute! ds sqlquery))))

(defn messages
  "Imports messages idempotently based on the slack_id"
  [ds path channel cache]
  (let [channel-dir (io/file path channel)
        msg-files (sort (file-seq channel-dir))
        file-count (atom 0)]
    (doseq [msg-file msg-files]
      (when (.isFile msg-file)
        (swap! file-count inc)
        (let [data (utils/read-json-from-file msg-file)
              data (into []
                         (comp
                          (map #(utils/select-keys-nested-as
                                 % [:text
                                    :type
                                    :subtype
                                    :purpose
                                    :user
                                    :ts
                                    :thread-ts]))
                          (map #(assoc %
                                       :channel-id
                                       (get-in cache [:chan-name->id channel]))))
                         data)
              sqlvals (remove nil? (mapv #(import/event->tx % cache) data))
              sqlmap {:insert-into [:message]
                      :values sqlvals
                      ;;:on-conflict []
                      :on-conflict {:on-constraint :message_channel_id_ts_key}
                      :do-update-set {:fields [:text :channel-id]}
                      ;;:do-nothing true
                      }
              ]
          (when (seq sqlvals)
            (jdbc/execute! ds (sql/format sqlmap))))))
    (println (format "%-10s -> %4d files imported." channel @file-count))))


(defn chan-cache [ds]
  (let [sqlmap {:select [:id :name]
                :from [:channel]}
        data (jdbc/execute! ds (sql/format sqlmap))]
    (into {}
          (map (juxt :name :id))
          data)))

(defn member-cache [ds]
  (let [sqlmap {:select [:id :slack-id]
                :from [:member]}
        data (jdbc/execute! ds (sql/format sqlmap))]
    (into {}
          (map (juxt :slack-id :id))
          data)))

(defn log-bot-channels []
  (let [slack-conn (clj-slack/conn (:slack-api-token (system/secrets)))
        bot-chans (clj-slack/get-users-conversations slack-conn {:user "U055W814A"})]
    (mapv :name bot-chans)))

(defn channel-member-import
  "Import channel data and member data from path of slack data directory.
  If path is nil, imports from the slack api."
  [& [{:keys [path]
       :or {path nil}}]]
  (let [slack-conn (clj-slack/conn (:slack-api-token (system/secrets)))
        chan-file (io/file path "channels.json")
        members-file (io/file path "users.json")
        ds (queries/repl-ds)
        imported-channels
        (if (.exists chan-file)
          ;; TODO: filter allowed channels for file import too
          (channels ds chan-file)
          (let [slack-channels (clj-slack/get-channels slack-conn)
                allowed-chans (set (log-bot-channels))
                slack-channels-to-log (filter #(contains? allowed-chans (:name %)) slack-channels)]
            (channels ds slack-channels-to-log)))
        imported-members-list
        (if (.exists members-file)
          (members ds members-file)
          (let [slack-users (clj-slack/get-users slack-conn)]
            (for [p-users (partition-all 100 slack-users)]
              (members ds p-users))))
        stats {:imported-channels-count
               (-> imported-channels first :next.jdbc/update-count)
               :imported-members-count
               (apply + (map #(-> % first :next.jdbc/update-count) imported-members-list))}]
    (println stats)
    stats))

(defn- get-cache []
  {:chan-name->id (chan-cache ds)
   :member-slack->db-id (member-cache ds)})

(defn messages-all [path]
  (for [chan (queries/all-channels (queries/repl-ds))]
    (messages ds path (:name chan) (get-cache))))

(comment
  (do
    ;; eval buffer then eval this do form to populate db
    ;; make sure slack archive is stored in src/sample_data
    (defn exec [sqlmap]
      (println (sql/format sqlmap))
      (jdbc/execute! ds (sql/format sqlmap)))

    (def slack-conn (clj-slack/conn (:slack-api-token (system/secrets))))

    (channel-member-import)

    (def path "../clojurians-log-data/sample_data")

    (messages ds path "cider" (get-cache))

    (messages-all path)

    ,)
  ,)
