(ns amon-log.core)
(require '[cheshire.core :as json])
(require '[clj-http.client :as http])

(def ^:dynamic *amon-host* "127.0.0.1:2464")
(def ^:dynamic *amon-throws-exceptions* false)
(def ^:dynamic *amon-secret-key* nil)

(def api-url #(str "http://" *amon-host* "/api/" % (when *amon-secret-key* (str "/" *amon-secret-key*))))

(defn current-time []
  (long (. (java.util.Date.) getTime)))

(defn to-amon
  "Logs a clojure data structure as json to amon,
   can optionally be passed a vector of :keywords as tags
   (log-data {:data '(hello world])
              :tags [:simple :success])"
  [{data :data tags :tags :or {tags ""}}]
    (try 
      (http/post (api-url "log")
        {:body (json/generate-string 
                 {:message {:data data
                            :timestamp (current-time)}
                  :tags tags})
        :conn-timeout 1000
        :socket-timeout 1000
        :content-type :json})
      (catch Exception e 
        (if *amon-throws-exceptions* (throw e) nil))))
  
(defn with-amon
  "Allows you to call your function while logging a data structure
   (with-amon-log my-log-data my-function my-args)"
  [data func & args]
    (to-amon data)
    (apply func args))

(defn exception-to-json
  "Converts a java exception to a json dict"
  ([e] ; Case one, no data is provided
    (exception-to-json e {}))
  ([e data] ; Case two, data is provided
    (let [cls (str (. e getClass))
          url "" ; what should this be?
          stack (into [] (. e getStackTrace))
          env (. (get stack 0) getMethodName)
          trace (map (fn [trace] (. trace toString)) stack)
          msg (. e getMessage)
          data (assoc data :thrown-from env :timestamp (current-time))]
      (json/generate-string 
        {:exception_class cls
         :url url
         :backtrace trace
         :message msg
         :data data}))))

(defn exception-to-amon
  "Logs an exception to amon"
  ([e]
    (exception-to-amon e {}))
  ([e data]
    (try
      (http/post (api-url "exception")
        {:body (exception-to-json e data)
        :conn-timeout 1000
        :socket-timeout 1000
        :content-type :json})
      (catch Exception e
        (if *amon-throws-exceptions* (throw e) nil)))))
