(ns clojure-socket-data-collector.processing
  (:require [clojure-socket-data-collector.logging :refer :all]
            [clojure.spec.alpha :as s]))

;; Input data spec

(def numbers-regex #"^-?[0-9]+\.?[0-9]*$")

(s/def ::str-with-number
  (s/and string? #(re-matches numbers-regex %)))

(def csv-data-spec
  (s/cat :luminosity   ::str-with-number
         :temperature  ::str-with-number
         :humidity     ::str-with-number))

;; Processing steps building blocks

(defn split-csv-line [frame]
  (update frame :data
    #(clojure.string/split % #",")))

(defn contains-valid-csv-data [frame]
  (let [csv-data (:data frame)
        valid (s/valid? csv-data-spec csv-data)]
    (if (not valid)
      (do
        (warn (:client frame ) "Invalid CSV data:" csv-data)
        (debug (:client frame) (clojure.string/trim (s/explain-str csv-data-spec csv-data)))))
    valid))

(defn str-to-num [string]
  (Double/parseDouble string))

(defn parse-numbers [frame]
  (update frame
          :data
          #(map str-to-num %)))

(defn create-record [{
                      ts :timestamp
                      {client-id :uuid} :client
                      [lum temp hum] :data}]
  {:luminosity  lum
   :temperature temp
   :humidity    hum
   :timestamp   ts
   :source      client-id})

;; Processing definition

(def process-data
  (comp
   (filter #(not (empty? (:data %))))
   (map split-csv-line)
   (filter contains-valid-csv-data)
   (map parse-numbers)
   (map create-record)))
