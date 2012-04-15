(ns amon-log.test.core
  (:require [clj-http.client :as http])
  (:use [clj-http.fake])
  (:use [amon-log.core])
  (:use [expectations]))

(defn with-fake-amon 
  [func & args]
    (with-fake-routes
      {#"http:\/\/(.*)\/api\/(.*)" (fn [req] {:status 200 :body ""})}
      (apply func args)))

; An api url can be constructed from default dynamic bindings and action type
(expect 
  "http://127.0.0.1:2464/api/log" 
  (api-url "log"))

; api-url can generate exception endpoints 
(expect 
  "http://127.0.0.1:2464/api/exceptions"
  (api-url "exceptions"))

; The host of amon can be changed to a different address
(expect 
  "http://amon.mysite.com/api/log" 
  (binding 
    [*amon-host* "amon.mysite.com"] 
      (api-url "log")))

; Can create timestamps via the (current-time) function
(expect
  long
  (current-time))

; Will not raise an error if the amon host is not available
(expect
  nil
  (binding [*amon-host* "garbagehost.fake"]
    (to-amon {:data "Hello" :tags [:test]})))

; Can rebind config variables to throw exceptions
(expect
  Exception
  (binding [*amon-host* "garbagehost.fake"
            *amon-throws-exceptions* true]
    (to-amon {:data "Hello" :tags [:test]})))

; Can log data to amon
(expect
  200
  (:status 
    (with-fake-amon 
      to-amon {:data "Hello"})))

; Can optionally associate tags with logged data
(expect
  200
  (:status 
    (with-fake-amon 
      to-amon {:data "Hello" :tags [:hello :world]})))

; Can wrap a function with logging functionality
(expect
  4
  (with-fake-amon
    with-amon {:data "Simple addition"} 
      + 2 2))

; Can turn an exception into a json object
(expect
  str ; a json string
  (exception-to-json 
    (try (/ 1 0) (catch Exception e e))))

; Can optionally add a data structure to the json response
(expect
  str ; a json string
  (exception-to-json 
    (try (/ 1 0) (catch Exception e e))
    {:context "Test" 
     :fun-fact "Never divide by zero!"}))

; Can turn log an exception to amon
(expect
  200
  (:status 
    (with-fake-amon
      exception-to-amon (try (/ 1 0) (catch Exception e e)))))

; Can optionally accept a clojure data structure with the exception
(expect
  200
  (:status 
    (with-fake-amon
      exception-to-amon 
        (try (/ 1 0) (catch Exception e e))
        {:extra "An error."})))

; TODO:
; Can wrap an exception class with logging capabilities
