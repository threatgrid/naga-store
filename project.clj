(defproject org.clojars.quoll/naga-store "0.5.2"
  :description "Protocol library for Naga storage"
  :url "https://github.com/threatgrid/naga-store"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.10.2"]
                 [prismatic/schema "1.1.12"]]
  :plugins [[lein-cljsbuild "1.1.7"]]
  :profiles {
    :dev {:dependencies [[org.clojure/clojurescript "1.10.773" :exclusions [com.google.code.findbugs/jsr305]]]} }
  :cljsbuild {
    :builds {
      :dev
      {:source-paths ["src"]
       :compiler {
         :output-to "out/naga/core.js"
         :optimizations :simple
         :pretty-print true}}
      :test
      {:source-paths ["src" "test"]
       :compiler {
         :output-to "out/naga/test_memory.js"
         :optimizations :simple
         :pretty-print true}}
      }
    :test-commands {
      "unit" ["node" "out/naga/test_memory.js"]}
    })
