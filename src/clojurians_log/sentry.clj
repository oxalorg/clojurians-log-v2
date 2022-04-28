(ns clojurians-log.sentry
  (:require [integrant.core :as ig])
  (:import (io.sentry Sentry)))

(defmethod ig/init-key ::alerts [_ {:keys [sentry-dsn]}]
  (.init Sentry (fn [opts]
                  (doto opts
                    (.setDsn sentry-dsn)
                    (.setTracesSampleRate 1.0)
                    (.setDebug true)))))

(defmethod ig/halt-key! ::server [_ server]
  (.stop server))
