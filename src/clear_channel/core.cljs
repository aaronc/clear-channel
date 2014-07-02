(ns clear-channel.core
  (:require
   [taoensso.sente :as sente]
   [clojure.core.async :as async :refer [go chan <! >!]]))

(defn call [call-target & {:as opts :keys [data success error timeout]}]
  (let [call-target
        (cond
         (fn? call-target) (:call-target (meta call-target))
         (keyword? call-target) call-target
         :else (throw (ex-info "Expected function or keyword" {:call-target call-target})))


        ]
    (call-target data cb-fn)))

(defmacro defcall [call-name args & body]
  )

(comment
  (defn- event-handler [[id data :as ev] _]
    (logf "Event: %s" ev)
    (match [id data]
           ;; TODO Match your events here <...>
           [:chsk/state {:first-open? true}]
           (logf "Channel socket successfully established!")
           [:chsk/state new-state] (logf "Chsk state change: %s" new-state)
           [:chsk/recv payload] (logf "Push event from server: %s" payload)
           :else (logf "Unmatched event: %s" ev))))




(defn init!
  ([path & {:as opts}]
     (let [opts (merge {:type :auto} opts)
           {:keys [chsk ch-recv send-fn state]}
           (sente/make-channel-socket! path opts)]
       (def chsk chsk)
       (def ch-chsk ch-recv) 
       (def chsk-send! send-fn) 
       (def chsk-state state)
       ;(def chsk-router (sente/start-chsk-router-loop! event-handler ch-chsk))
       )))
