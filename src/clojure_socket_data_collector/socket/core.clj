(ns clojure-socket-data-collector.socket.core
  (:require [clojure.core.async :as async]
            [clojure-socket-data-collector.logging :refer :all])
  (:import [java.net ServerSocket InetSocketAddress SocketException]
           [java.util UUID]))

(def ^:dynamic *client-buffer-size* 32)

(defn listen-address
  "Create inet socket address"
  ([addr port]
   (InetSocketAddress. addr port))
  ([port]
   (InetSocketAddress. port)))

(defn- create-client [sock]
  {:address (-> sock .getInetAddress str)
   :uuid (.toString (UUID/randomUUID))})

(defn- wrap-frame-data [raw-frame]
  {:data raw-frame})

(defn- add-timestamp [frame]
  (assoc frame :timestamp (System/currentTimeMillis)))

(defn- add-client [client]
  (fn [frame]
    (assoc frame :client client)))

(defn- wrap-with-frame-metadata [client]
  (comp
   (map wrap-frame-data)
   (map add-timestamp)
   (map (add-client client))))

(defn- handle-client [proto sock data-chan]
  (let [input-stream (.getInputStream sock)
        client (create-client sock)
        frame-chan (async/chan (async/buffer *client-buffer-size*))
        frame-extractor (:frame-extractor proto)]

    (info client "Client connected. Waiting for messages.")
    (frame-extractor input-stream frame-chan)
    (async/pipeline 1                ; no need for much parallelism
                    data-chan        ; to
                    (wrap-with-frame-metadata client) ; via
                    frame-chan       ; from
                    false)))           ; do not close data-chan

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
  [handle data-channel]
  (.bind (:server-socket handle) (:listen-address handle))
  (info "Listening on" (-> handle :listen-address str))
  (async/thread
    (try
      (loop []
        (let [sock (.accept (:server-socket handle))]
          (handle-client (:protocol handle) sock data-channel))
        (debug "Waiting for next client")
        (recur))
      (catch SocketException e (info (.getMessage e)))
      (finally (async/close! data-channel))))
  data-channel)

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
