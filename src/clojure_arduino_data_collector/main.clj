(ns clojure-arduino-data-collector.main
  (:gen-class)
  (:require [clojure-arduino-data-collector.socket.core :as core]
            [clojure-arduino-data-collector.socket.framing :as framing]
            [clojure-arduino-data-collector.processing :refer [data-collector-reduction]]))

(def protocol
  {:type  :tcp
   :frame-extractor framing/text-endl
   :reduction data-collector-reduction})

(def addr (core/listen-address 6900))

(defn -main
  [& args]
  (println "Starting server")
  (let [handle (core/create-server addr protocol)]
    (core/close-on-shutdown handle)
    (core/start-server handle)))
