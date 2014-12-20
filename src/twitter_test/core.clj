(ns twitter-test.core
  (:require [twitter-test.creds :as cr]
            [clojure.java.io :as io]
            [clojure.string :as st]
            [twitter.oauth :as o]
            [twitter.callbacks :as c]
            [twitter.callbacks.handlers :as h]
            [twitter.api.restful :as r]
            [twitter.api.search :as s])
  (:import java.io.FileWriter
           java.io.StringWriter))

;; (take 50 (map :word (word-freq (get-words (from-file)))))

(def creds (apply o/make-oauth-creds cr/oauth-creds))

(def res-file "/tmp/twitter_result.edn")

#_(defn to-file [f input]
  (let [sw (StringWriter.)]
    (with-open [w (FileWriter. "/tmp/twitter_result.edn")]
      (clojure.pprint/pprint (f input) sw)
      (.write w (.toString sw)))))

(defn to-file [input & {:keys [file] :or {file res-file}}]
  (let [sw (StringWriter.)]
    (clojure.pprint/pprint input sw)
    (spit "/tmp/twitter_result.edn" (.toString sw))))

(defn from-file [& {:keys [file] :or {file res-file}}]
  (read-string (slurp file)))

(defn make-call [f params]
  (f :oauth-creds creds :params params))

(defn show-user [username]
  (make-call r/users-show {:screen-name username}))

(defn search [term & {:keys [count] :or {count "25"}}]
  (make-call s/search {:q term :count count}))

(defn tweet-texts [data]
  (map :text (get-in data [:body :statuses])))

(defn get-words [data]
  (mapcat #(st/split % #" ") (tweet-texts data)))

(defn lexical-diversity [words]
  (/ (* 1.0 (count (into #{} words))) (count words)))

(defn avg-words-tweet [data]
  (/ (* 1.0 (count (get-words data))) (count (tweet-texts data))))

(defn word-freq [words]
  (reverse (sort-by (juxt :freq :word) (reduce #(conj % {:word %2 :freq (count (filter #{%2} words))}) [] (into #{} words)))))
