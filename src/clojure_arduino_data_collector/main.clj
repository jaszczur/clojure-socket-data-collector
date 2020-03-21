(ns clojure-arduino-data-collector.main
  (:gen-class)
  (:require [clojure-arduino-data-collector.core :as core]
            [clojure-arduino-data-collector.framing :as framing]))

(defn log [frame]
  (println frame)
  frame)

(defn one [frame]
  1)

(def process-data
  (comp
   (filter #(not (empty? %1)))
   (map log)
   (map one)))

(def protocol
  {:type  :tcp
   :frame-extractor framing/text-endl
   :transducer {:xform process-data
                :init 0
                :reducer +}
   :on-closed (fn [result]
                (println "Handled" result "records"))})

(def addr (core/listen-address 6900))

(defn -main
  [& args]
  (println "yo")
  (let [handle (core/create-server addr protocol)]
    (core/close-on-shutdown handle)
    (core/start-server handle)))
