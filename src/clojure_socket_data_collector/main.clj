(ns clojure-socket-data-collector.main
  (:gen-class)
  (:require [clojure-socket-data-collector.socket.core :as core]
            [clojure-socket-data-collector.socket.framing :as framing]
            [clojure-socket-data-collector.processing :refer [process-data]]
            [clojure-socket-data-collector.logging :refer :all]
            [clojure.core.async :as async]))

(def ^:dynamic *buffer-size* 16)

(def protocol
  {:frame-extractor framing/text-endl})

(def addr (core/listen-address 6900))
(def server-handle (core/create-server addr protocol))

(defn -main
  [& args]
  (info "Starting server")
  (core/close-on-shutdown server-handle)
  (let [data-chan (async/chan (async/buffer *buffer-size*) process-data)]
    (core/start-server server-handle data-chan)
    (loop [record (async/<!! data-chan)]
      (if (nil? record)
        (info "Bye")
        (do
          (debug record)
          (recur (async/<!! data-chan)))))))
