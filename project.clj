(defproject wipro-crawler "0.1.0-SNAPSHOT"
  :description "A small service to crawl websites and return all links discovered"
  :url "https://github.com/shmish111/wipro-crawler"
  :license {:name "The MIT License (MIT)"
            :url "https://opensource.org/licenses/MIT"}
  :pedantic? :abort
  :dependencies [[org.clojure/clojure "1.7.0"]
                 [hickory "0.6.0"]
                 [clojurewerkz/urly "1.0.0"]
                 [aleph "0.4.1-beta3"]
                 [com.taoensso/timbre "4.2.1"]
                 [clj-simple-config "0.1.1"]
                 [metrics-clojure "2.5.1"]
                 [commons-codec "1.6"]
                 [metosin/compojure-api "0.24.3"
                  :exclusions [commons-codec potemkin]]
                 [funcool/cats "1.2.0"]]
  :main wipro-crawler.core
  :plugins [[lein-midje "3.1.3"]
            [lein-cljfmt "0.3.0"]
            [com.palletops/uberimage "0.4.1"
             :exclusions [com.fasterxml.jackson.core/jackson-core]]]
  :profiles {:dev {:dependencies [[midje "1.8.3"
                                   :exclusions [riddley commons-codec potemkin]]]}
             :uberjar {:aot :all}}
  :uberimage {:cmd ["/bin/dash" "run.sh"]
              :files {"run.sh" "resources/run.sh"}})
