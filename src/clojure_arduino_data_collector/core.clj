(ns clojure-arduino-data-collector.core
  (:require [clojure.core.async :as async])
  (:import [java.net ServerSocket]
           [java.net InetSocketAddress]))


(defn listen-address
  ([addr port]
   (InetSocketAddress. addr port))
  ([port]
   (InetSocketAddress. port)))

(defn handle-client [proto sock]
  (let [input-stream (.getInputStream sock)
        client (-> sock .getInetAddress .toString)
        frame-chan (async/chan)
        frame-extractor (:frame-extractor proto)
        consumer (:consumer proto)]
    (println "Client connected" client)
    (frame-extractor input-stream frame-chan)
    (async/go-loop [acc (:accumulator proto)]
      (if-let [frame (async/<! frame-chan)]
        (recur (consumer acc frame))
        (do
          (println "Client disconnected" client)
          (if (:on-closed proto)
            ((:on-closed proto) acc)))))))

(defn create-server [addr proto]
  (let [ss (ServerSocket.)]
    {:server-socket ss
     :listen-address addr
     :protocol proto}))

(defn start-server [handle]
    (.bind (:server-socket handle) (:listen-address handle))
    (loop []
      (let [sock (.accept (:server-socket handle))]
        (handle-client (:protocol handle) sock))
      (recur)))

(defn stop-server [handle]
  (println "Closing server")
  (.close (:server-socket handle)))

(defn register-sigterm-handler [handler]
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread. handler)))

(defn close-on-shutdown [handle]
  (register-sigterm-handler #(stop-server handle)))
