(ns clojure-socket-data-collector.main
  (:gen-class)
  (:require [clojure-socket-data-collector.socket.core :as core]
            [clojure-socket-data-collector.socket.framing :as framing]
            [clojure-socket-data-collector.processing :refer [data-collector-reduction]]))

(def protocol
  {:frame-extractor framing/text-endl
   :reduction data-collector-reduction})

(def addr (core/listen-address 6900))
(def server-handle (core/create-server addr protocol))

(defn -main
  [& args]
  (println "Starting server")
  (core/close-on-shutdown server-handle)
  (core/start-server server-handle))
