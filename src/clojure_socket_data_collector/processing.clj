(ns clojure-socket-data-collector.processing
  (:require [clojure-socket-data-collector.logging :refer :all]
            [clojure.spec.alpha :as s]))

;; Input data spec

(def numbers-regex #"^[0-9]+\.?[0-9]*$")

(s/def ::str-with-number
  (s/and string? #(re-matches numbers-regex %)))

(def csv-data-spec
  (s/cat :luminosity   ::str-with-number
         :temperature  ::str-with-number
         :humidity     ::str-with-number))

;; Processing steps building blocks

(defn log [x]
  (debug x)
  x)

(defn split-csv-line [csv-line]
  (clojure.string/split csv-line #","))

(defn is-valid-csv-data [csv-data]
  (let [valid (s/valid? csv-data-spec csv-data)]
    (if (not valid)
      (do
        (warn "Invalid CSV data:" csv-data)
        (debug (clojure.string/trim (s/explain-str csv-data-spec csv-data)))))
    valid))

(defn parse-numbers [csv-data]
  (map #(Double/parseDouble %)
       csv-data))

(defn create-record [[lum temp hum]]
  {:luminosity  lum
   :temperature temp
   :humidity    hum})

;; Result

(defn count-records
  ([num record]
   (debug record)
   (inc num))
  ([num]
   (info "Handled" num "records")))

;; Processing definition

(def process-data
  (comp
   (filter #(not (empty? %)))
   (map split-csv-line)
   (filter is-valid-csv-data)
   (map parse-numbers)
   (map create-record)))

(def data-collector-reduction
  {:xform process-data
   :init 0
   :reducer count-records})
