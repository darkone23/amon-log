(ns amon-log.test.core
  (:require [clj-http.client :as http])
  (:use [clj-http.fake])
  (:use [amon-log.core])
  (:use [expectations]))

(defn with-fake-amon 
  [func & args]
    (with-fake-routes
      {#"http:\/\/(.*)\/api\/log" (fn [req] {:status 200 :body ""})}
      (apply func args)))

; An api url can be constructed from default dynamic bindings
(expect 
  "http://127.0.0.1:2464/api/log" 
  (api-url))

; The host of amon can be changed to a different address
(expect 
  "http://amon.mysite.com/api/log" 
  (binding 
    [*amon-host* "amon.mysite.com"] 
      (api-url)))

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
