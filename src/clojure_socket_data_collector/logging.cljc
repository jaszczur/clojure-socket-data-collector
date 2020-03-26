(ns clojure-socket-data-collector.logging)


(defn data [& msg]
  (apply println (cons "DATA:  " msg)))


(defn debug [& msg]
  (apply println (cons "DEBUG: " msg)))

(defn info [& msg]
  (apply println (cons "INFO:  " msg)))

(defn warn [& msg]
  (apply println (cons "WARN:  " msg)))
