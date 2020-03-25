(ns clojure-socket-data-collector.writer
  (:require [clojure.java.io :as io]
            [clojure.core.async :as async]
            [clojure.data.json :as json]
            [clojure-socket-data-collector.logging :refer :all])
  (:import [java.io BufferedWriter]))

(def ^:dynamic *write-buffer-size* 32)

(defn- dataset-file [directory]
  (io/file directory (str (System/currentTimeMillis) ".json")))

(def ^:private jsonize-data-xf
  (comp
   (map json/write-str)))

(defn- write-json-objects-to-file [file json-chan]
  (with-open [writer (io/writer file)]
    (debug "Opened output file" (str file))
    (.write writer "[\n")
    (loop [first-line true]
      (let [line (async/<!! json-chan)]
        (if-not (nil? line)
          (do
            (.write writer (if first-line " " ","))
            (.write writer line)
            (.newLine writer)
            (.flush writer)
            (recur false)))))
    (debug "Closing output file" (str file))
    (.write writer "]\n")))

(defn store-data-sync [directory data-chan]
  (let [file (dataset-file directory)
        json-chan (async/chan (async/buffer *write-buffer-size*))]
    (async/pipeline 1 json-chan jsonize-data-xf data-chan)
    (write-json-objects-to-file file json-chan)))
