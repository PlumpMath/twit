(ns twit.core
  (:use
   [twitter.oauth]
   [twitter.callbacks]
   [twitter.callbacks.handlers]
   [twitter.api.restful]
   [seesaw.core]
   [seesaw.mig]
   [environ.core])
  (:require
   [clojure.data.json :as json]
   [http.async.client :as ac])
  (:import
   (twitter.callbacks.protocols AsyncStreamingCallback)))

;; Get twitter credentials ............................................

(def ^:dynamic *username* "eigenhombre")

(def my-creds (make-oauth-creds (env :twitter-app-consumer-key)
                                (env :twitter-app-consumer-secret)
                                (env :twitter-user-access-token)
                                (env :twitter-user-access-token-secret)))

(defn ids [screen-name]
  (:ids (:body (friends-ids :oauth-creds my-creds
                            :params {:screen-name screen-name}))))

;; Get IDs, then usernames, for people *username* follows ............

(defn friends [screen-name]
  (apply concat
         (for [p (partition-all 50 (ids screen-name))]
           (map :name (:body (users-lookup :oauth-creds my-creds
                                           :params {:user_id p}))))))

(def eigfriends (friends *username*))

;; Display usernames .................................................

(def f (frame :title "RSTwit",
              :size [300 :by 300]
              :on-close :exit))

(defn display [content]
  (config! f :content content)
  content)

(def lb (listbox :model (sort (fn [a b] (compare (.toLowerCase a)
                                                (.toLowerCase b)))
                              (vec eigfriends))
                 :font "ARIAL-ITALIC-14"))

(display (mig-panel
          :constraints ["wrap 2"
                        "[shrink 0]10px[600, grow, fill]"
                        "[]5px[]"]
          :items [ [(scrollable lb) "height 100:600:"]
                   [(label :text "hiss" :background :grey) "width 10:50:, grow"]
                   [(label :text "hi" :background :pink) "grow"]
                   ["there.."]]))

(-> f pack! show!)
