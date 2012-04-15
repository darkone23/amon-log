(defproject amon-log "0.1.0"
  :description "A simple client to log data structures and exceptions to amon"
  :dependencies [[org.clojure/clojure "1.3.0"]
                 [cheshire "4.0.0"]
                 [clj-http "0.3.6"]]
  :dev-dependencies [[lein-autoexpect "0.1.1"]
                     [clj-http-fake "0.3.0"]
                     [expectations "1.3.7"]])
