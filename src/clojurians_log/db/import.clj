(ns clojurians-log.db.import
  (:require
   [clojure.string :as string]
   [clojurians-log.db.queries :as queries]
   [clojurians-log.time-utils :as time-utils]
   [clojurians-log.utils :as utils]))

(defmulti event->tx
  (fn [message cache]
    ((juxt :type :subtype) message)))

(defmethod event->tx :default [_ _]
  ;; return nil by default, this will let us skip events we don't (yet) care
  ;; about
  nil)

(defn message->tx [{:keys [channel user text ts thread-ts] :as message}
                   {:keys [member-slack->db-id chan-slack-id->id] :as cache}]
  (let [member-id (get member-slack->db-id user)
        channel-id (get chan-slack-id->id channel)
        _ (println cache user channel member-id channel-id)
        parent-id (when (and thread-ts (not= thread-ts ts))
                    {:select [:id]
                     :from [:message]
                     :limit 1
                     :where [:and
                             [:= :ts thread-ts]
                             [:= :channel-id channel-id]]})
        value {:channel-id channel-id
               :member-id member-id
               :text (string/replace text #"\u0000" "")
               :ts ts
               :created-at (time-utils/ts->inst ts)
               :parent parent-id
               :deleted-ts nil}]
    {:insert-into [:message]
     :values [value]
     ;;:on-conflict []
     :on-conflict {:on-constraint :message_channel_id_ts_key}
     :do-update-set {:fields [:text :channel-id]}
     ;;:do-nothing true
     :returning [:ts :id]}))

(defmethod event->tx ["message" nil] [message cache]
  (message->tx message cache))

(defmethod event->tx ["message" "message_deleted"] [{:keys [deleted_ts channel] :as message} cache]
  nil)

(defmethod event->tx ["message" "message_changed"] [{:keys [message channel]} cache]
  #_(event->tx (assoc message :channel channel)))

(defmethod event->tx ["message" "thread_broadcast"] [message cache]
  nil
  #_(assoc
      (message->tx message) :message/thread-broadcast? true))

(defn message-deleted->tx [{:keys [deleted-ts channel]}
                           {:keys [chan-slack-id->id] :as cache}]
  (let [channel-id (get chan-slack-id->id channel)]
    {:delete []
     :from [:message]
     :where [:and
             [:= :channel-id channel-id]
             [:= :ts deleted-ts]]}))

(defn message-tombstone->tx [{:keys [ts channel]}
                             {:keys [chan-slack-id->id] :as cache}]
  (let [channel-id (get chan-slack-id->id channel)]
    {:update :message
     :set {:text "This message was deleted."
           :deleted-ts ts}
     :where [:and
             [:= :channel-id channel-id]
             [:= :ts ts]]}))

(defn member->tx [user]
  (let [data (-> user
                 utils/select-keys-nested-as
                 [{:keys [:id]
                   :rename :slack-id}
                  :name :team-id :is-admin :is-bot
                  :tz :tz-offset :tz-label
                  :is-email-confirmed :deleted :bot-id
                  [:profile :real-name] [:profile :real-name-normalized]
                  [:profile :display-name] [:profile :display-name-normalized]
                  [:profile :first-name] [:profile :last-name]
                  [:profile :title] [:profile :skype] [:profile :phone]
                  ;;[:profile :image-original]
                  [:profile :image-24] [:profile :image-32] [:profile :image-48]
                  [:profile :image-72] [:profile :image-192] [:profile :image-512]])]
    {:insert-into [:member]
     :values data
     :on-conflict :slack-id
     :do-update-set {:fields :name :team-id :is-admin :is-bot
                     :tz :tz-offset :tz-label
                     :deleted :bot-id :is-email-confirmed
                     :real-name :real-name-normalized
                     :display-name :display-name-normalized
                     :first-name :last-name
                     :title :skype :phone
                     :image-24 :image-32 :image-48 :image-72 :image-192 :image-512}}))

(defn channel->tx [{:keys [id name-normalized name]} cachs]
  {:insert-into [:channel]
   :values {:slack-id id
            :name (or name-normalized name)}
   :on-conflict :slack-id
   :do-update-set {:fields [:name]}})

(defn reaction-removed->tx [{:keys [item user reaction]}
                            {:keys [member-slack->db-id
                                    chan-slack-id->id] :as cache}]
  (let [channel-id (get chan-slack-id->id (:channel item))]
    {:delete []
     :from [:reaction]
     :where [:and
             [:= :channel-id channel-id]
             [:= :member-id (get member-slack->db-id user)]
             [:is :message-id {:select [:id]
                               :from [:message]
                               :limit 1
                               :where [:and
                                       [:= :ts (:ts item)]
                                       [:= :channel-id channel-id]]}]
             [:= :reaction reaction]]}))

(defn reaction->tx [{:keys [item user reaction]}
                    {:keys [member-slack->db-id
                            chan-slack-id->id] :as cache}]
  (let [channel-id (get chan-slack-id->id (:channel item))]
    {:insert-into [:reaction]
     :values [{:channel-id channel-id
               :member-id (get member-slack->db-id user)
               :message-id {:select [:id]
                            :from [:message]
                            :limit 1
                            :where [:and
                                    [:= :ts (:ts item)]
                                    [:= :channel-id channel-id]]}
               :reaction reaction}]}))

(defn reactions->tx [{:keys [channel-id user reactions ts thread-ts] :as message}
                     {:keys [member-slack->db-id message-ts->db-id] :as cache}]
  (let [member-id (get member-slack->db-id user)
        message-id (get message-ts->db-id ts)]
    (mapcat (fn reaction-val [reaction-entry]
              (map (fn reaction-for-each-user [user]
                     {:channel-id channel-id
                      :member-id (get member-slack->db-id user)
                      :message-id message-id
                      :reaction (:name reaction-entry)})
                   (:users reaction-entry)))
            reactions)))
