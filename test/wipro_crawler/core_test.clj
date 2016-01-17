(ns wipro-crawler.core-test
  (:require [midje.sweet :refer :all]
            [wipro-crawler.core :refer :all]
            [clojure.java.io :as io]
            [clojurewerkz.urly.core :as urly]))

(defn html-string
  [filename]
  (-> (io/resource filename)
      io/file
      slurp))

(facts "get-links"
       (fact "should produce no links for single page site"
             (->> (html-string "nolinks.html")
                  (get-links "clojure.org")) => #{})
       (fact "should produce only non-external links"
             (->> (html-string "withlinks.html")
                  (get-links (urly/as-map "clojure.org"))) => #{"http://clojure.org/#" "http://clojure.org/about/clojureclr" "http://clojure.org/about/clojurescript" "http://clojure.org/about/rationale" "http://clojure.org/api/api" "http://clojure.org/community/books" "http://clojure.org/community/companies" "http://clojure.org/community/contributing" "http://clojure.org/community/downloads" "http://clojure.org/community/events" "http://clojure.org/community/libraries" "http://clojure.org/community/license" "http://clojure.org/community/resources" "http://clojure.org/community/swag" "http://clojure.org/events/2016/clojurebridge_berlin" "http://clojure.org/events/2016/clojurebridge_london" "http://clojure.org/events/2016/clojureremote" "http://clojure.org/guides/getting_started" "http://clojure.org/guides/guides" "http://clojure.org/images/clojure-logo-120b.png" "http://clojure.org/index" "http://clojure.org/news/2014/08/06/transducers-are-coming" "http://clojure.org/news/2015/06/30/clojure-17" "http://clojure.org/news/2016/01/14/clojure-org-live" "http://clojure.org/news/news" "http://clojure.org/privacy" "http://clojure.org/reference/documentation"})
       (fact "should produce correct links from relative paths"
             (get-links (urly/as-map "clojure.org") "<html><body><a href=\"somepage\">link</a></body></html>")
             => #{"http://clojure.org/somepage"}
             (get-links (urly/as-map "clojure.org") "<html><body><img src=\"somepage\">link</a></body></html>")
             => #{"http://clojure.org/somepage"}
             (get-links (urly/as-map "clojure.org") "<html><body><a href=\"/somepage\">link</a></body></html>")
             => #{"http://clojure.org/somepage"}
             (get-links (urly/as-map "clojure.org") "<html><body><img src=\"/somepage\">link</a></body></html>")
             => #{"http://clojure.org/somepage"})
       (fact "should not recognize mailto"
             (get-links (urly/as-map "clojure.org") "<html><body><a href=\"mailto:david@david.com\">link</a></body></html>")
             => #{})
       (fact "should not recognize non-http protocols"
             (get-links (urly/as-map "clojure.org") "<html><body><a href=\"ftp://clojure.org/somepage\">link</a></body></html>")
             => #{}))