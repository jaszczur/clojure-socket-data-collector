(ns clojure-socket-data-collector.main
  (:require [clojure-socket-data-collector.start :refer [start]]))

(defn main [& args]
  (start args))
