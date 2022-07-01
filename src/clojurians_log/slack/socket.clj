(ns clojurians-log.slack.socket
  "Slack Socket App which maintains a websocket connection to Slack
  and receives real time events."
  (:require
   [camel-snake-kebab.core :as csk]
   [camel-snake-kebab.extras :as cske]
   [clojure.core.async :as async]
   [clojure.java.data :as jd]
   [clojure.java.io :as io]
   [clojurians-log.db.queries :as queries]
   [clojurians-log.db.slack-import :as slack-import]
   [integrant.core :as ig])
  (:import
   (com.slack.api.bolt App AppConfig)
   (com.slack.api.bolt.handler BoltEventHandler)
   (com.slack.api.bolt.socket_mode SocketModeApp)
   (com.slack.api.model.event ChannelCreatedEvent ChannelRenameEvent MessageChangedEvent MessageChannelJoinEvent MessageDeletedEvent MessageEvent MessageRepliedEvent ReactionAddedEvent ReactionRemovedEvent)))

(set! *warn-on-reflection* true)

(def cache (atom {}))

(defmulti handle-event (fn [ds event] (:type event)))

(defmethod handle-event :default [ds event]
  (println "Default event called")
  (when-not (seq @cache)
    (reset! cache (queries/get-cache ds)))
  (let [event (cske/transform-keys csk/->kebab-case-keyword event)]
    (prn event)
    #_(slack-import/from-event event ds @cache))
  (println "-------"))

(defn create-handler
  "Create a handler with dispatch-evt"
  [ds event-ch]
  (let [handle-event* (partial handle-event ds)]
    (reify BoltEventHandler
      (apply [_ payload ctx]
        (let [event (jd/from-java-deep (.getEvent payload) {})
              #_internal-event #_(evt-format/socket->internal m)]
          #_(tap> m)
          (async/go
            (async/>! event-ch event))
          (handle-event* event))
        (.ack ctx)))))

(defn create-app-conf [slack-bot-token]
  (.build
   (doto (AppConfig/builder)
     (.threadPoolSize 1)
     (.singleTeamBotToken slack-bot-token))))

(defn create-socket-app [{:keys [ds slack-bot-token slack-app-token]} event-ch]
  (let [app-conf (create-app-conf slack-bot-token)
        handler (create-handler ds event-ch)
        app (doto (App. app-conf)
              (.event MessageEvent handler)
              (.event MessageChangedEvent handler)
              (.event MessageRepliedEvent handler)
              (.event MessageDeletedEvent handler)
              (.event MessageChannelJoinEvent handler)
              (.event ChannelRenameEvent handler)
              (.event ChannelCreatedEvent handler)
              (.event ReactionAddedEvent handler)
              (.event ReactionRemovedEvent handler))]
    (doto (SocketModeApp. ^String slack-app-token app)
      (.startAsync))))

(defn create-event-tx-logger [{:keys [tx-log-directory]} event-ch]
  (async/go-loop []
    (when-let [event (async/<! event-ch)]
      (let [tx-log-file (io/file tx-log-directory
                                 (str (java.time.LocalDate/now) ".edn"))]
        (spit tx-log-file (prn-str event) :append true))
      (recur))))

(defmethod ig/init-key ::app [_ config]
  ;; (log/info :socket-app/starting :now)
  (let [event-ch (async/chan 100)]
    {:socket (create-socket-app config event-ch)
     :tx-log (create-event-tx-logger config event-ch)
     :event-ch event-ch}))

(defmethod ig/halt-key! ::app [_ {:keys [^SocketModeApp socket-app event-ch]}]
  ;; (log/info :socket-app/stoping :now)
  (.stop ^SocketModeApp socket-app)
  (async/close! event-ch))

(comment
  (def c (async/chan 10))
  (def tx-logger (create-event-tx-logger {:tx-log-directory "/tmp/tx"} c))
  (time
   (doseq [i (range 30)]
     (async/>!! c {:data (range (* i 20))})))
  (async/close! c)
  ,)
