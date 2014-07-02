(defproject clear-channel "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [com.taoensso/sente "0.14.1"]]
  :profiles
  {:dev
   {:plugins [[com.keminglabs/cljx "0.4.0"]]
    :hooks [cljx.hooks]
    :cljx {:builds [{:source-paths ["src"]
                     :output-path "target/classes"
                     :rules :clj}

                    {:source-paths ["src"]
                     :output-path "target/classes"
                     :rules :cljs}]}}})
