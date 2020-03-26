(ns clojure-socket-data-collector.socket.framing)

(defn text-endl
  "Split frames by end-of-line characters

  Will output string values, each containing a single line from the input."
  [input-stream frame-chan])
