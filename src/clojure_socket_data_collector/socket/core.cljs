(ns clojure-socket-data-collector.socket.core)

(defn listen-address
  "Create inet socket address"
  ([addr port]
   (str addr ":" port))
  ([port]
   (str "0.0.0.0:" port)))

(defn create-server [addr proto])
(defn start-server [handle data-chan])
(defn close-on-shutdown [handle])
