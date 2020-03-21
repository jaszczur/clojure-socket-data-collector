(ns clojure-socket-data-collector.socket.core
  (:require [clojure.core.async :as async]
            [clojure-socket-data-collector.logging :refer :all])
  (:import [java.net ServerSocket InetSocketAddress SocketException]))


(defn listen-address
  "Create inet socket address"
  ([addr port]
   (InetSocketAddress. addr port))
  ([port]
   (InetSocketAddress. port)))

(defn- transduce-proto [proto frame-chan]
  (let [reduction (:reduction proto)]
    (async/transduce (:xform reduction) (:reducer reduction) (:init reduction) frame-chan)))

(defn- handle-client [proto sock]
  (let [input-stream (.getInputStream sock)
        client (-> sock .getInetAddress str)
        frame-chan (async/chan)
        frame-extractor (:frame-extractor proto)
        consumer (-> proto :reduction :consumer)]

    (info "Client connected" client)
    (frame-extractor input-stream frame-chan)
    (async/go
      (let [result-chan (transduce-proto proto frame-chan)
            result (async/<! result-chan)]
        (info "Client disconnected" client)
        (if consumer
          (consumer result))))))


(defn create-server
  "Create a server handle

  Result is a description of the server"
  [addr proto]
  (let [ss (ServerSocket.)]
    {:server-socket ss
     :listen-address addr
     :protocol proto}))

(defn start-server
  "Listen for incoming connections"
  [handle]
  (.bind (:server-socket handle) (:listen-address handle))
  (info "Listening on" (-> handle :listen-address str))
  (try
    (loop []
      (let [sock (.accept (:server-socket handle))]
        (handle-client (:protocol handle) sock))
      (recur))
    (catch SocketException e (info (.getMessage e)))))

(defn stop-server [handle]
  (info "Closing server")
  (.close (:server-socket handle)))

(defn- register-sigterm-handler
  [handler]
  (.addShutdownHook
   (Runtime/getRuntime)
   (Thread. handler)))

(defn close-on-shutdown
  "(Not so) gently close server when JVM is about to terminate"
  [handle]
  (register-sigterm-handler #(stop-server handle)))
