(ns clojure-socket-data-collector.start
  (:require [clojure-socket-data-collector.socket.core :as sock]
            [clojure-socket-data-collector.socket.framing :as framing]
            [clojure-socket-data-collector.processing :refer [process-data]]
            [clojure-socket-data-collector.writer :refer [store-data-sync]]
            [clojure-socket-data-collector.logging :refer [info]]
            [clojure.core.async :as async]))

(def ^:dynamic *data-buffer-size* 128)

(def protocol
  {:frame-extractor framing/text-endl})

(def addr (sock/listen-address 6900))
(def server-handle (sock/create-server addr protocol))

(defn start
  [args]
  (info "Starting server")
  (sock/close-on-shutdown server-handle)
  (let [data-chan (async/chan (async/buffer *data-buffer-size*) process-data)]
    (sock/start-server server-handle data-chan)
    (store-data-sync "./data/" data-chan)))
