(ns clojure-socket-data-collector.processing
  (:require [clojure-socket-data-collector.logging :refer :all]))

(defn log [frame]
  (debug frame)
  frame)

(defn one [frame]
  1)

(defn split-csv-line [csv-line]
  (clojure.string/split csv-line #","))

(defn is-valid-csv-data [csv-data]
  (let [valid (= (count csv-data) 3)]
    (if (not valid)
      (warn "Invalid CSV data:" csv-data))
    valid))

(defn parse-record [csv-data]
  csv-data)

(def process-data
  (comp
   (filter #(not (empty? %1)))
   (map split-csv-line)
   (filter is-valid-csv-data)
   (map parse-record)
   (map one)))

(def data-collector-reduction
  {:xform process-data
   :init 0
   :reducer +
   :consumer (fn [result]
               (info "Handled" result "records"))})
