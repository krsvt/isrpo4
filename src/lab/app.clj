(ns lab.app
  (:require
   [lab.util.postgres :as pg]
   [medley.core :as medley]
   [clojure.string :as str]
   [iapetos.collector.ring :as ring]
   [lab.metrics :as metrics]))

(defn camelcase->snakecase [s]
  (-> (name s)
      (str/replace #"([a-z])([A-Z])" "$1_$2")
      str/lower-case))

(defn snakecase->camelcase [s]
  (let [words (clojure.string/split (name s) #"_")]
    (str (first words)
         (apply str (map clojure.string/capitalize (rest words))))))

(defn map-json-in [json]
  (medley/map-keys camelcase->snakecase json))

(defn map-json-out [json]
  (medley/map-keys snakecase->camelcase json))

(defn get-patients [{:keys [biff/ds]}]
  (metrics/inc-get-patient)
  {:status 200
   :body (mapv map-json-out (pg/patients ds))})

(defn add-patient [{:keys [biff/ds params]}]
  (let [params (map-json-in params)
        pat (pg/create-patient ds params)]
    (cond
      pat (do
            (metrics/inc-created-patients)
            {:status 200
             :body (map-json-out pat)})

      :else {:status 400})))

(defn get-patient [{:keys [biff/ds] :as ctx}]
  (let [id (-> ctx :path-params :id)
        pat (pg/patient-by-id ds id)]
    (cond
      pat
      (do
        (metrics/inc-get-patient)
        {:status 200
       :body (map-json-out pat)})
      :else
      {:status 404})))

(defn update-patient [{:keys [biff/ds params] :as ctx}]
  (let [id (-> ctx :path-params :id)
        params (map-json-in params)
        pat (pg/update-patient ds id params)]
    (cond
      pat
      {:status 200
       :body (map-json-out pat)}
      :else
      {:status 404})))

(defn delete-patient [{:keys [biff/ds] :as ctx}]
  (let [id (-> ctx :path-params :id)
        res (pg/delete-patient ds id)]
    (cond
      res
      {:status 204}
      :else
      {:status 404})))

(def module
  {:api-routes [["/metrics"
                 {:get metrics/metrics-handler}]

                ["/api/patient"
                 ["" {:post add-patient
                      :get get-patients}]

                 ["/:id"
                  {:get get-patient
                   :put update-patient
                   :delete delete-patient}]]]})
