(ns clear-channel.core
  (:require
   [taoensso.sente :as sente]
   [clojure.core.async :as async :refer [go chan <! >!]]))

(defn set-catch-all-handler! [f])

(def registered-calls (atom nil))

(def ^:dynamic *uid*)

(defn- event-msg-handler
  [{:as ev-msg :keys [ring-req event ?reply-fn]} _]
  (let [session (:session ring-req)
        uid (:uid session)
        [id data :as ev] event
        call-handler (get @registered-calls id)]
    (when call-handler
      (binding [*uid* uid]
        (call-handler data ?reply-fn)))))

(defn- wrap-call [call-fn data]
  (let [res-ch (chan)]
    (call-fn data (fn cb-fn-wrapper [res] (go (>! res-ch res))))
    res-ch))

(defmacro defcall [call-name args & body]
  (let [body `(clojure.core/binding [clear-channel.core/*uid* :repl-caller] ~@body)
        arg-count (count args)
        cb-sym (gensym "cb")
        fn-2 (case arg-count
               0 `([_# ~cb-sym] (~cb-sym ~body))

               1 `(~(conj args cb-sym) (~cb-sym ~body))

               2 `(~args ~body)

               (throw (ex-info "Wrong number of args for defcall. Expected 1 or 2"
                               {:call-name call-name :args args})))
        fn-01 (if (= 1 arg-count)
                `([data#] (#'clear-channel.core/wrap-call ~call-name data#))
                `([] (#'clear-channel.core/wrap-call ~call-name nil)))
        kw (keyword (str *ns*) (name call-name))]
    `(do
       (defn ~call-name
         ~fn-2
         ~fn-01)
       (clojure.core/swap! clear-channel.core/registered-calls
                           clojure.core/assoc ~kw ~call-name))))

(defn call [call-target & {:as opts :keys [data success error timeout]}]
  (let [call-target
        (cond
         (fn? call-target) call-target
         (keyword? call-target) (get registered-calls call-target)
         :else (throw (ex-info "Expected function or keyword" {:call-target call-target})))

        res-ch (chan)

        cb-fn (fn [res] (go (<! res-ch res)))]
    (call-target data cb-fn)))

(defn init! [opts]
  (let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
                connected-uids]}
        (sente/make-channel-socket! opts)]
    (def ring-ajax-post                ajax-post-fn)
    (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
    (def ch-chsk                       ch-recv) 
    (def chsk-send!                    send-fn)
    (def connected-uids                connected-uids)
    (def chsk-router
      (sente/start-chsk-router-loop! event-msg-handler ch-chsk))))


