(ns clojure-socket-data-collector.processing)

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

(def data-collector-reduction
  {:xform process-data
   :init 0
   :reducer +
   :consumer (fn [result]
               (println "Handled" result "records"))})
