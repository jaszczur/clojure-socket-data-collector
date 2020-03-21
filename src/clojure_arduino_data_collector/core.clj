(ns clojure-arduino-data-collector.core
  (:require [clojure.core.async :as async])
  (:import [java.net ServerSocket InetSocketAddress SocketException]))


(defn listen-address
  ([addr port]
   (InetSocketAddress. addr port))
  ([port]
   (InetSocketAddress. port)))

(defn- transduce-proto [proto frame-chan]
  (let [tducer (:transducer proto)]
    (async/transduce (:xform tducer) (:reducer tducer) (:init tducer) frame-chan)))

(defn handle-client [proto sock]
  (let [input-stream (.getInputStream sock)
        client (-> sock .getInetAddress .toString)
        frame-chan (async/chan)
        frame-extractor (:frame-extractor proto)
        consumer (:consumer proto)]
    (println "Client connected" client)
    (frame-extractor input-stream frame-chan)
    (async/go
      (let [result-chan (transduce-proto proto frame-chan)
            result (async/<! result-chan)]
        (println "Client disconnected" client)
        (if (:on-closed proto)
          ((:on-closed proto) result))))))


(defn create-server [addr proto]
  (let [ss (ServerSocket.)]
    {:server-socket ss
     :listen-address addr
     :protocol proto}))

(defn start-server [handle]
    (.bind (:server-socket handle) (:listen-address handle))
    (try
      (loop []
        (let [sock (.accept (:server-socket handle))]
          (handle-client (:protocol handle) sock))
        (recur))
      (catch SocketException e (println (.getMessage e)))))

(defn stop-server [handle]
  (println "Closing server")
  (.close (:server-socket handle)))

(defn register-sigterm-handler [handler]
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread. handler)))

(defn close-on-shutdown [handle]
  (register-sigterm-handler #(stop-server handle)))
