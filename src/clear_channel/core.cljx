(ns clear-channel.core
  (:require
   [taoensso.sente :as sente]))

#+clj
(defn init! [opts]
  (let [{:keys [ch-recv send-fn ajax-post-fn ajax-get-or-ws-handshake-fn
                connected-uids]}
        (sente/make-channel-socket! opts)]
    (def ring-ajax-post                ajax-post-fn)
    (def ring-ajax-get-or-ws-handshake ajax-get-or-ws-handshake-fn)
    (def ch-chsk                       ch-recv) 
    (def chsk-send!                    send-fn)
    (def connected-uids                connected-uids)))

(defn set-catch-all-handler! [f])

(def registered-calls (atom nil))

(def ^:private ^:dynamic *uid*)

(defn get-uid [] *uid*)

(defn- event-msg-handler
  [{:as ev-msg :keys [ring-req event ?reply-fn]} _]
  (let [session (:session ring-req)
        uid (:uid session)
        [id data :as ev] event
        call-handler (get @registered-calls id)]
    (when call-handler
      (binding [*uid* uid]
        (call-handler data ?reply-fn))))))

(defmacro defcall [name args & body])

;;(init! {})

