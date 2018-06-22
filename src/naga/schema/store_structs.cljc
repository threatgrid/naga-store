(ns naga.schema.store-structs
  (:require #?(:clj  [schema.core :as s]
               :cljs [schema.core :as s :include-macros true])))

;; single element in a rule
(def EntityPropertyElt
  (s/cond-pre s/Keyword s/Symbol #?(:clj Long :cljs s/Num)))

;; simple pattern containing a single element. e.g. [?v]
(def EntityPattern [(s/one s/Symbol "entity")])

;; two or three element pattern.
;; e.g. [?s :property]
;;      [:my/id ?property ?value]
(def EntityPropertyPattern
  [(s/one EntityPropertyElt "entity")
   (s/one EntityPropertyElt "property")
   (s/optional s/Any "value")])

;; The full pattern definition, with 1, 2 or 3 elements
(def EPVPattern
  (s/if #(= 1 (count %))
    EntityPattern
    EntityPropertyPattern))

(s/defn epv-pattern? :- s/Bool
  [pattern :- [s/Any]]
  (and (vector? pattern)
       (not (seq? (first pattern)))))

(s/defn filter-pattern? :- s/Bool
  [pattern :- [s/Any]]
  (and (vector? pattern) (seq? (first pattern))))

(s/defn op-pattern? :- s/Bool
  [pattern :- [s/Any]]
  (seq? pattern))

(s/defn vartest? :- s/Bool
  [x]
  (and (symbol? x) (boolean (#{\? \%} (first (name x))))))

(s/defn vars :- [s/Symbol]
  "Return a seq of all variables in a pattern"
  [pattern :- EPVPattern]
  (filter vartest? pattern))

(def Operators (s/enum 'or 'not 'OR 'NOT))

;; filters are a vector with an executable list destined for eval
(def FilterPattern (s/pred #(and (vector? %) (list? (first %)))))

(def OpPattern (s/constrained (s/pred list?)
                              [(s/one Operators "operator") EPVPattern])) 

(def Pattern (s/if list? OpPattern
               (s/if (comp list? first) FilterPattern EPVPattern)))

(def Value (s/pred (complement symbol?) "Value"))

(def Results [[Value]])

(def EntityPropAxiomElt
  (s/cond-pre s/Keyword #?(:clj Long :cljs s/Num)))

(def EntityPropValAxiomElt
  (s/conditional (complement symbol?) s/Any))

(def Triple
  [(s/one s/Any "entity")
   (s/one s/Any "property")
   (s/one s/Any "value")])

(def Axiom
  [(s/one EntityPropAxiomElt "entity")
   (s/one EntityPropAxiomElt "property")
   (s/one EntityPropValAxiomElt "value")])

