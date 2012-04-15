(ns amon-log.core)
(require '[cheshire.core :as json])
(require '[clj-http.client :as http])

(def ^:dynamic *amon-host* "127.0.0.1:2464")
(def ^:dynamic *amon-throws-exceptions* false)

(def api-url #(str "http://" *amon-host* "/api/log"))

(defn to-amon
  "Logs a clojure data structure as json to amon,
   can optionally be passed a vector of :keywords as tags
   (log-data {:data '(hello world])
              :tags [:simple :success])"
  [{data :data tags :tags :or {tags ""}}]
    (try 
      (http/post (api-url)
        {:body (json/generate-string 
                 {:message {:data data
                            :timestamp (long (. (java.util.Date.) getTime))}
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
