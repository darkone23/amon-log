(ns amon-log.test.core
  (:use [amon-log.core])
  (:use [expectations]))

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

; Can log data to amon

; Can optionally associate tags with logged data

; Can wrap a function with logging functionality
