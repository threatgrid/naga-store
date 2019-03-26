(ns naga.schema.test-store-structs
   (:require [clojure.test :as t :refer [testing is run-tests]]
             [naga.schema.store-structs
              :refer [EntityPropertyElt EntityPropertyPattern EPVPattern epv-pattern?
                      Var vartest? FilterPattern filter-pattern? EvalPattern eval-pattern?]]
             #?(:clj  [schema.core :as s]
                :cljs [schema.core :as s :include-macros true])
             #?(:clj  [schema.test :as st :refer [deftest]]
                :cljs [schema.test :as st :refer-macros [deftest]]))
   #?(:clj (:import [clojure.lang ExceptionInfo])))

(defmacro ex-with-msg? [msg form]
  (list `is
        (list 'thrown-with-msg?
              #?(:clj `ExceptionInfo :cljs :default)
              (re-pattern msg)
              `~form)))

(defn schema-throws? [type form]
  (is (thrown-with-msg?
        #?(:clj ExceptionInfo :cljs :default)
        #"Value does not match schema"
        (do (s/validate type form) (println form)))))

(deftest test-var
  (s/validate Var '?x)
  (s/validate Var '?bar)
  (schema-throws? Var 'x)
  (schema-throws? Var :x)
  (is (vartest? '?x))
  (is (vartest? '?bar))
  (is (not (vartest? 'x)))
  (is (not (vartest? :x))))

(deftest test-entity-property-element
  (s/validate EntityPropertyElt '?x)
  (s/validate EntityPropertyElt :type)
  (s/validate EntityPropertyElt 5))

(deftest test-entity-property-pattern
  (s/validate EntityPropertyPattern '[:node :value "value"])
  (s/validate EntityPropertyPattern '[:node :value 5])
  (s/validate EntityPropertyPattern '[?x :value 5])
  (s/validate EntityPropertyPattern '[:node ?x 5])
  (s/validate EntityPropertyPattern '[:node :value ?x])
  (schema-throws? EntityPropertyPattern '[:node "String-Property" "value"]))

(deftest test-epv-pattern
  (s/validate EPVPattern '[:node :value "value"])
  (s/validate EPVPattern '[:node :value])
  (s/validate EPVPattern '[?x])
  (is (epv-pattern? '[:node :value "value"]))
  (is (epv-pattern? '[:node :value]))
  (is (epv-pattern? '[:node]))
  (schema-throws? EPVPattern '[:node :value "value" :extra])
  (schema-throws? EPVPattern '(:node "String-Property" "value"))
  (schema-throws? EPVPattern '[])
  (is (not (epv-pattern? '[]))))

(deftest test-filter
  (s/validate FilterPattern '[(= ?x ?y)])
  (s/validate FilterPattern '[(not ?bar)])
  (schema-throws? FilterPattern '[[= ?x ?y]])
  (schema-throws? FilterPattern '[true])
  (schema-throws? FilterPattern '[(= (not ?x) ?y)])
  (schema-throws? FilterPattern '[(= ?x ?y) ?z])
  (schema-throws? FilterPattern '[])
  (is (filter-pattern? '[(= ?x ?y)]))
  (is (filter-pattern? '[(not ?bar)]))
  (is (not (filter-pattern? '[[= ?x ?y]])))
  (is (not (filter-pattern? '[true])))
  (is (not (filter-pattern? '[(= ?x ?y) ?z])))
  (is (not (filter-pattern? '[]))))

(deftest test-eval-pattern
  (s/validate EvalPattern '[(str ?x ?y) ?z])
  (s/validate EvalPattern '[(- ?bar) ?a])
  (schema-throws? EvalPattern '[(str ?a ?b) ?c ?d])
  (schema-throws? EvalPattern '[[+ ?x ?y] ?z])
  (schema-throws? EvalPattern '[true ?x])
  (schema-throws? EvalPattern '[(+ (* 2 ?x) ?y) ?z])
  (schema-throws? EvalPattern '[(+ ?x ?y) 5])
  (schema-throws? EvalPattern '[])
  (is (eval-pattern? '[(str ?x ?y) ?z]))
  (is (eval-pattern? '[(- ?bar) ?a]))
  (is (not (eval-pattern? '[[+ ?x ?y] ?z])))
  (is (not (eval-pattern? '[true ?x])))
  (is (not (eval-pattern? '[(+ ?x ?y) 5])))
  (is (not (eval-pattern? '[(+ ?x ?y) (- ?x ?y)])))
  (is (not (eval-pattern? '[(+ ?x ?y)])))
  (is (not (eval-pattern? '[]))))

#?(:cljs (run-tests))
