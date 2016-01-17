# wipro-crawler

A small service to crawl websites and return all links discovered.

## Usage

`lein run`

`curl -XGET "http://localhost:8080/?url=http://clojure.org"`

## Building

The service can be deployed as a jar:

`lein uberjar`

`java -jar xxx.jar`

`curl -XGET "http://localhost:8080/?url=http://clojure.org"`

or as a docker container:

`lein uberimage`

`docker run -e SERVICE_PORT=8080 <image id>`

## Development

To avoid whitespace and formatting changes, please use `lein cljfmt fix` before commits.

## Issues

Currently the crawler will return en empty set if the initial page failed to load. It would be better to crawl the 
initial page first and then continue if it was successful, otherwise return some sort of error to the requester.
This would also fit in nicely with the use of the either monad.

Metrics can be added so that we can identify common types of errors and improve the crawler.

## TODO

* get rid of urly?
* metrics
* separate service and crawler

## License

Copyright © 2016 David Smith

Distributed under the MIT License.
