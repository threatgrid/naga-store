(ns ^{:doc "Registry for Naga Storage."
      :author "Paula Gearon"}
  naga.store-registry)

(def registered-stores (atom {}))
(def shutdown-fns (atom []))

(defn register-storage!
  "Registers a new storage type"
  ([store-id factory-fn] (register-storage! store-id factory-fn nil))
  ([store-id factory-fn shutdown-fn]
   (swap! registered-stores assoc store-id factory-fn)
   (when shutdown-fn (swap! shutdown-fns conj shutdown-fn))))

(defn get-storage-handle
  "Creates a store of the configured type. Throws an exception for unknown types."
  [{type :type store :store :as config}]
  (or store
      (if-let [factory (@registered-stores (keyword type))]
        (factory config)
        (throw (ex-info "Unknown storage configuration" config)))))

(defn shutdown
  []
  (doseq [f @shutdown-fns] (f)))

