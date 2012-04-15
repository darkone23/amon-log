# amon-log

A simple [amon](http://amon.cx) client in clojure, uses [chesire](http://github.com/dakrone/cheshire) to convert data structures to json and [clj-http](http://github.com/dakrone/clj-http) to hit amon's api; supports tagging functionality to associate logged data into groups.

## Usage

Add `[amon-log "0.1.1"]` to your project.clj file under `:dependencies` and run `lein deps`.

To send the data directly to amon, use `to-amon`.  You must provide a hashmap containing a clojure structure to be logged named `:data` and optionally a vector of classification keywords named `:tags`.

```clojure
(require '[amon-log.core :as log])

;; (log/to-amon my-log-data)
(log/to-amon {:data my-clojure-data})
(log/to-amon {:data my-clojure-data :tags [:vector :of :keywords]})
```

You can also wrap your function with a log to retain a single clojure form.

```clojure
(require '[amon-log.core :as log])

;; (log/with-amon my-log-data my-func my-args)
(log/with-amon
  {:data '(Im adding numbers together!) 
   :tags [:math :simple]}
  + 1 2)
;; returns 3, and logs "Im adding numbers together!" with the tags of "math" and "simple".
```

You can log your exceptions with `exception-to-amon`.  By default it will include the stacktrace, the error class, a timestamp and the exception message.  

You can optionally give amon richer error context by providing a hashmap containing any clojure data structure that can be converted to json.

```clojure
(require '[amon-log.core :as log])

;; (log/exception-to-amon my-exception)
;; (log/exception-to-amon my-exception my-hashmap)
(defn divide-with-amon
  [numerator denominator]
    (try 
      (/ numerator denominator) 
      (catch Exception e 
        (do
          (log/exception-to-amon e {:context "Caused by 'divide-with-amon'" 
                                    :args [numerator, denominator])
          (handle-exception e)))))

(divide-with-amon 10 5) ; returns 2
(divide-with-amon 10 0) ; logs the error to amon and passes the exception to 'handle-error'
```

Note: if your amon instance lives at an address other than `127.0.0.1:2464` you can change it by binding the `*amon-host*` var.

```clojure
;; logs to amon.mysite.com
(binding [*amon-host* "amon.mysite.com"] 
  (with-amon {:data '(Hello world)} println "Hello world."))
```

Amon will fail silently if the amon host is not available.  If you need different behavior you can bind *amon-throws-exceptions* to true.

```clojure
;; will raise an exception due to logging failure
(binding [*amon-host* "amon.badhost.fake"
          *amon-throws-exceptions* true] 
  (with-amon {:data '(Hello world)} println "Hello world."))
```

## TODO

Add functionality to wrap an exception class with amon logging

## License

Copyright (C) 2012

Distributed under the Eclipse Public License, the same as Clojure.
