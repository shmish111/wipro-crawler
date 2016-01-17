(ns wipro-crawler.core
  (:require [hickory.core :as h]
            [clojurewerkz.urly.core :as urly]
            [manifold.stream :as s]
            [manifold.deferred :as d]
            [aleph.http :as http]
            [clojure.set :refer [union difference]]
            [taoensso.timbre :as log]
            [clj-simple-config.core :refer [read-config]]
            [compojure.api.sweet :refer :all]
            [cats.monad.either :refer :all])
  (:import (java.net URI URL))
  (:gen-class))

(defn- href-or-src
  [{:keys [href src]}]
  (or href src))

(defn build-url
  [{:keys [protocol host port]} url]
  (str protocol "://" host (when-not (or (nil? port) (= -1 port)) (str ":" port)) (when-not (= \/ (first url)) "/") url))

(defn normalize-url
  [site url]
  (let [uri (URI. url)]
    (cond
      (contains? #{"http" "https"} (.getScheme uri)) url
      (nil? (.getScheme uri)) (build-url site url)
      :else nil)))

(defn same-site
  [{:keys [host port]} url]
  (let [{link-host :host link-port :port} (urly/as-map url)]
    (and (or (nil? link-port) (= -1 link-port) (= port link-port))
         (or (nil? link-host) (= host link-host)))))

(defn get-links
  [site html-string]
  (->> html-string
       (h/parse)
       (h/as-hickory)
       (tree-seq identity :content)
       (sequence
         (comp (filter #(contains? #{:a :img} (:tag %)))
               (map (comp href-or-src :attrs))
               (remove nil?)
               (map (partial normalize-url site))
               (remove nil?)
               (filter (partial same-site site))))
       set))

(defn crawl-page!
  [links-stream root-url url]
  (-> (d/chain (http/get (urly/normalize-url url) {:connection-timeout 1000})
               :body
               slurp
               (partial get-links (urly/as-map root-url))
               (partial s/put! links-stream))
      (d/catch Exception (fn [e]
                           (log/infof "failed to crawl site %s page %s with error %s" root-url url (.getMessage e))
                           (s/put! links-stream #{})))))

(defn crawl-site!
  "crawl a website, find all internal links and recursively crawl them. Pages are crawled asynchronously"
  [root-url]
  (let [crawl-stream (s/stream)
        links-stream (s/stream)
        _ (s/consume (partial crawl-page! links-stream root-url) crawl-stream)
        _ (s/put! crawl-stream root-url)]
    (loop [started 1
           finished 0
           urls #{}]
      (let [new-links (->> @(s/take! links-stream) (remove urls) set)
            started (+ started (count new-links))
            finished (inc finished)
            urls (union urls new-links)]
        (if (= started finished)
          (do (s/close! links-stream)
              (s/close! crawl-stream)
              urls)
          (do (doseq [link new-links] (s/put! crawl-stream link))
              (recur started finished urls)))))))

(defn valid-url?
  "take a url and return cats.monad.either a valid url or an error"
  [url]
  (try
    (let [url-object (URL. url)]
      (if (contains? #{"http" "https"} (.getProtocol url-object))
        (right url)
        (left (format "invalid url provided %s invalid protocol" url))))
    (catch Exception e
      (left (format "invalid url provided %s %s" url (.getMessage e))))))

(def handler
  (api
    (GET* "/ping" [] {:status 200 :body "pong!"})
    (GET* "/" {{url :url} :params} (branch (valid-url? url)
                                           (fn [e] {:status 400 :body e})
                                           (fn [v] {:status 200 :body (crawl-site! v)})))))

(defn -main
  [& args]
  (let [conf (read-config)]
    (http/start-server handler (-> conf :http))
    (log/info "server started on port " (-> conf :http :port))))