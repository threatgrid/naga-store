(ns naga.test-store-registry
  (:require [clojure.test :as t :refer [deftest is run-tests]]
            [naga.store-registry :refer [register-storage! get-storage-handle]])
  #?(:clj  (:import [clojure.lang ExceptionInfo])))

(defn store-factory-fn
  [t]
  (fn [c] {:config c :store-type t}))

(deftest test-registration
  (register-storage! :test-store (store-factory-fn 3))
  (register-storage! :test-store2 (store-factory-fn 2))
  (register-storage! :test-store (store-factory-fn 1))

  (let [s1 (get-storage-handle {:type :test-store :step 1})
        s2 (get-storage-handle {:type :test-store2 :step 2})
        s3 (get-storage-handle {:type :test-store :step 3 :store {:store-type 3}})]
    (is (thrown-with-msg?
        #?(:clj ExceptionInfo :cljs :default)
        #"Unknown storage configuration"
        (get-storage-handle {:type :test-store3 :step 4})))
    (is (= s1 {:config {:type :test-store :step 1} :store-type 1}))
    (is (= s2 {:config {:type :test-store2 :step 2} :store-type 2}))
    (is (= s3 {:store-type 3}))))


#?(:cljs (run-tests))
