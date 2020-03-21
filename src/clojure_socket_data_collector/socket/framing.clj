(ns clojure-socket-data-collector.socket.framing
  (:require [clojure.core.async :as async])
  (:import [java.io BufferedReader]))

(defn text-endl
  "Split frames by end-of-line characters

  Will output string values, each containing a single line from the input."
  [input-stream frame-chan]
  (let [input-reader (clojure.java.io/reader input-stream)]
    (async/go-loop []
      (let [line (.readLine input-reader)]
        (if (nil? line)
          (async/close! frame-chan)
          (do
            (async/>! frame-chan line)
            (recur)))))))
