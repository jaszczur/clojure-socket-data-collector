(ns clojure-arduino-data-collector.main
  (:gen-class)
  (:require [clojure-arduino-data-collector.core :as core]
            [clojure-arduino-data-collector.framing :as framing]))

;; TODO: use transducers

(def protocol
  {:type  :tcp
   :frame-extractor framing/text-endl
   :accumulator 0
   :consumer (fn [acc frame]
               (println frame)
               (inc acc))
   :on-closed (fn [acc]
                (println "Handled" acc "records"))})

(def addr (core/listen-address 6900))

(defn -main
  [& args]
  (println "yo")
  (let [handle (core/create-server addr protocol)]
    (core/close-on-shutdown handle)
    (core/start-server handle)))
