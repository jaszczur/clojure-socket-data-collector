(ns clojure-socket-data-collector.main
  (:gen-class)
  (:require [clojure-socket-data-collector.start :refer [start]]))

(defn -main [& args]
  (start args))
