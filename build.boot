(def project
  {:project     'netpyoung/cljpyoung.spels
   :version     "0.1.0"
   :description "FIXME: write description"
   :url         "https://github.com/netpyoung/cljpyoung.spels"
   :scm         {:url "https://github.com/netpyoung/cljpyoung.spels"}
   :license     {"GNU Free Documentation License 1.3"
                 "https://www.gnu.org/licenses/fdl.txt"}})

(set-env!
 :source-paths #{"src" }
 :resource-paths #{"src"}
 :dependencies
 '[[org.clojure/clojure "1.10.0"]
   [adzerk/boot-test "RELEASE" :scope "test"]])


(deftask check []
  ;; ref: https://github.com/tolitius/boot-check
  (set-env! :dependencies #(conj % '[tolitius/boot-check "0.1.11" :scope "test"]))
  (require '[tolitius.boot-check])
  (let [with-yagni (resolve 'tolitius.boot-check/with-yagni)
        with-eastwood (resolve 'tolitius.boot-check/with-eastwood)
        with-kibit (resolve 'tolitius.boot-check/with-kibit)
        with-bikeshed (resolve 'tolitius.boot-check/with-bikeshed)]
    (comp
     (with-yagni)
     (with-eastwood)
     (with-kibit)
     (with-bikeshed))))

(deftask bat-test
  []
  (set-env! :dependencies #(conj % '[metosin/bat-test "0.4.0" :scope "test"]))
  (set-env! :dependencies #(conj % '[org.clojure/tools.namespace "0.3.0-alpha4" :exclusions [org.clojure/clojure] :scope "test"]))
  (require '[metosin.bat-test])
  (let [bat-test (resolve 'metosin.bat-test/bat-test)]
    (bat-test)))


(deftask build
  [_ snapshot LOCAL boolean "build local"]
  (task-options!
   pom (if snapshot
         (update-in project [:version] (fn [x] (str x "-SNAPSHOT")))
         project))
  (comp (pom)))

(deftask local
  []
  (comp (build)
        (jar)
        (install)))

(deftask local-snapshot
  []
  (comp (build :snapshot true)
        (jar)
        (install)))

(deftask prepare-push
  []
  (set-env!
   :repositories
   #(conj % ["clojars" {:url "https://clojars.org/repo/"
                        :username (get-sys-env "CLOJARS_USER" :required)
                        :password (get-sys-env "CLOJARS_PASS" :required)}]))
  identity)

(deftask push-release
  []
  (comp (prepare-push)
        (build)
        (jar)
        (push :repo "clojars" :ensure-release true)))

(deftask push-snapshot
  []
  (comp (prepare-push)
        (build :snapshot true)
        (jar)
        (push :repo "clojars" :ensure-snapshot true)))

(deftask run
  "Run the project."
  [a args ARG [str] "the arguments for the application."]
  (with-pass-thru fs
    (require '[cljpyoung.spels :as app])
    (apply (resolve 'app/-main) args)))

(require '[adzerk.boot-test :refer [test]])
