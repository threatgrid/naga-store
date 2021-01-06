(ns ^{:doc "Storage API for Naga to talk to graph stores. Also includes some utility functions."
      :author "Paula Gearon"}
  naga.store
  (:require #?(:clj  [schema.core :as s]
               :cljs [schema.core :as s :include-macros true])))


(defprotocol Storage
  (start-tx [store] "Starts a transaction, if supported")
  (commit-tx [store] "Commits a transaction, if supported")
  (deltas [store] "Returns the latest updated subjects in the represented store")
  (resolve-pattern [store pattern] "Resolves a pattern against storage")
  (count-pattern [store pattern] "Counts the size of a pattern resolition against storage")
  (query [store output-pattern patterns] "Resolves a set of patterns (if not already resolved), joins the results, and projects the output. The output can contain constant values as well as selected variables.")
  (assert-data [store data] "Inserts new axioms")
  (retract-data [store data] "Removes existing axioms")
  (assert-schema-opts [store schema opts] "Inserts a new schema, if supported")
  (query-insert [store assertion-patterns patterns] "Resolves a set of patterns, joins them, and inserts the set of resolutions"))

(def StorageType (s/pred #(satisfies? Storage %)))

(defprotocol ConnectionStore
  (as-store [c] "Turns a native connection into a storage object"))

;; default is to return the provided object
(extend-type #?(:clj Object :cljs object) ConnectionStore (as-store [c] c))

(defn assert-schema
  "Convenience function to avoid passing empty options"
  [store schema & {:as opts}]
  (assert-schema store schema opts))

(defn retrieve-contents
  "Convenience function to retrieve the contents of the entire store"
  [store]
  (resolve-pattern store '[?entity ?attribute ?value]))

