# Lab3

## Up
```
docker compose -d --build
```
## App
Доступно на `http://localhost:8080`. Метрики на `http://localhost:8080/metrics`

Реализация метрик
```clojure
(def registry
  (-> (prometheus/collector-registry)
      (prometheus/register
        (prometheus/counter :patients/get {:description "Number of GET requests to /patient endpoint"})
        (prometheus/counter :patients/created {:description "Number of created patients"}))))

(defn inc-created-patients []
  (prometheus/inc registry :patients/created))

(defn inc-get-patient []
  (prometheus/inc registry :patients/get))

(defn metrics-handler [_request]
  {:status 200
   :headers {"Content-Type" "text/plain"}
   :body (export/text-format registry)})

;; далее вызовы на эндпоинтах (metrics/inc-created-patients), (metrics/inc-get-patient)
```

## Prometheus
Доступен на `http://localhost:3000`
![image](https://github.com/user-attachments/assets/8a9e56cb-b266-4bad-b403-762e3347f475)


## VictoriaMetrics
Доступна на `http://localhost:8428`
![image](https://github.com/user-attachments/assets/5b40fd11-2457-4f17-9e97-d2d5bf16cf21)

## Grafana
![image](https://github.com/user-attachments/assets/ced23acf-7dc0-4c50-b4b6-cf3d48933a37)
![image](https://github.com/user-attachments/assets/a7f0cbaa-da9a-43d4-bb04-93ce95156a16)
