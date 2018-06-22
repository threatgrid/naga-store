(ns ^{:doc "Storage API for Naga to talk to graph stores. Also includes some utility functions."
      :author "Paula Gearon"}
  naga.store
  (:require #?(:clj  [schema.core :as s]
               :cljs [schema.core :as s :include-macros true])))


(defprotocol Storage
  (start-tx [store] "Starts a transaction, if supported")
  (commit-tx [store] "Commits a transaction, if supported")
  (deltas [store] "Returns the latest updated subjects in the represented store")
  (new-node [store] "Allocates a node for the store")
  (node-id [store n] "Returns a id for a node. Numbers are good")
  (node-type? [store p n] "Returns true if the value refered to by a property can be a graph node")
  (data-property [store data] "Returns the property to use for given data. Must be in the naga namespace, and start with 'first'.")
  (container-property [store data] "Returns the property to use to indicate a containership relation for given data. Must be in the naga namespace")
  (resolve-pattern [store pattern] "Resolves a pattern against storage")
  (count-pattern [store pattern] "Counts the size of a pattern resolition against storage")
  (query [store output-pattern patterns] "Resolves a set of patterns (if not already resolved), joins the results, and projects the output. The output can contain constant values as well as selected variables.")
  (assert-data [store data] "Inserts new axioms")
  (retract-data [store data] "Removes existing axioms")
  (assert-schema-opts [store schema opts] "Inserts a new schema, if supported")
  (query-insert [store assertion-patterns patterns] "Resolves a set of patterns, joins them, and inserts the set of resolutions"))

(def StorageType (s/pred #(satisfies? Storage %)))

(defn assert-schema
  "Convenience function to avoid passing empty options"
  [store schema & {:as opts}]
  (assert-schema store schema opts))

(defn retrieve-contents
  "Convenience function to retrieve the contents of the entire store"
  [store]
  (resolve-pattern store '[?entity ?attribute ?value]))

(defn node-label
  "Returns a keyword label for a node"
  [s n]
  (keyword "naga" (str "id-" (node-id s n))))
