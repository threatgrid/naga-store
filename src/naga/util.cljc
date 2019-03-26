(ns naga.util
    "The ubiquitous utility namespace that every project seems to have"
    (:require [schema.core :as s :refer [=>]]
               #?(:cljs [cljs.js :as cjs :refer [eval empty-state js-eval]]))
    #?(:clj (:import [clojure.lang Var])))

;; NOTE: The ClojureScript functions can give inconsistent results, particularly when using eval

#?(:clj
   (s/defn get-fn-reference :- (s/maybe Var)
     "Looks up a namespace:name function represented in a keyword,
   and if it exists, return it. Otherwise nil"
     [kw :- (s/cond-pre s/Keyword s/Symbol)]
     (let [kns (namespace kw)
           snm (symbol (name kw))]
       (some-> kns
               symbol
               find-ns
               (ns-resolve snm))))

   :cljs
   (s/defn get-fn-reference :- (s/maybe Var)
     "Looks up a namespace:name function represented in a keyword,
      and if it exists, return it. Otherwise nil"
     [kw :- (s/cond-pre s/Keyword s/Symbol)]
     (binding [cljs.analyzer/*cljs-warning-handlers* []]
       (try
         (when-let [nms (namespace kw)]
           (when (find-ns (symbol nms))
             (let [snm (symbol nms (name kw))]
               (:value
                (cljs.js/eval (cljs.js/empty-state)
                              snm
                              {:eval cljs.js/js-eval :source-map true :context :expr}
                              identity)))))
         (catch :default _ )))))

#?(:clj
   (def c-eval clojure.core/eval)

   :cljs
   (defn c-eval
     "Equivalent to clojure.core/eval. Returns nil on error."
     [expr & {:as opts}]
     (try
       (let [def-opts {:eval cjs/js-eval :source-map true :context :expr}
             op (if opts
                  (merge def-opts opts)
                  def-opts)
             {:keys [error value]} (cjs/eval (cjs/empty-state)
                                             expr
                                             op
                                             identity)]
         (if error
           ((.-log js/console) error)
           value))
       (catch :default e ((.-log js/console) e) nil))))

#?(:cljs (def raw-lookup {'= = 'not= not= '< < '> > '<= <= '>= >=}))
#?(:cljs (def known-namespaces {'cljs.core (ns-publics 'cljs.core)
                                'clojure.core (ns-publics 'clojure.core)}))

#?(:clj
   (defn fn-for
     "Converts a symbol or string representing an operation into a callable function"
     [op]
     (or (ns-resolve (the-ns 'clojure.core) op)
         (throw (ex-info (str "Unable to resolve symbol '" op " in "
                              (or (namespace op) 'clojure.core))
                         {:op op :namespace (or (namespace op) "clojure.core")}))))

   :cljs
   (defn fn-for
     "Converts a symbol or string representing an operation into a callable function"
     [op]
     (letfn [(resolve-symbol [ns-symbol s]
               (get (get known-namespaces ns-symbol) (symbol (name s))))]
       (let [op-symbol (if (string? op) (symbol op) op)]
         (or
          (if-let [ons-str (namespace op-symbol)]
            (let [ons-symbol (symbol ons-str)]
              (if-let [ns->functions (known-namespaces ons-symbol)]
                (get ns->functions (symbol (name op-symbol)))
                (c-eval op-symbol)))
            (or (resolve-symbol 'clojure.core op-symbol)
                (resolve-symbol 'cljs.core op-symbol)))
          (raw-lookup op-symbol)
          (throw (ex-info (str "Unable to resolve symbol '" op-symbol " in "
                               (or (namespace op-symbol) 'cljs.core))
                          {:op op-symbol
                           :namespace (or (namespace op-symbol) "cljs.core")})))))))


(s/defn mapmap :- {s/Any s/Any}
  "Creates a map from functions applied to a seq.
   (mapmap (partial * 2) [1 2 3 4 5])
     => {1 2, 2 4, 3 6, 4 8, 5 10}
   (mapmap #(keyword (str \"k\" (dec %))) (partial * 3) [1 2 3])
     => {:k0 3, :k1 6, :k2 9}"
  ([valfn :- (=> s/Any s/Any)
    s :- [s/Any]] (mapmap identity valfn s))
  ([keyfn :- (=> s/Any s/Any)
    valfn :- (=> s/Any s/Any)
    s :- [s/Any]]
    (into {} (map (juxt keyfn valfn) s))))

(s/defn divide' :- [[s/Any] [s/Any]]
  "Takes a predicate and a sequence and returns 2 sequences.
   The first is where the predicate returns true, and the second
   is where the predicate returns false. Note that a nil value
   will not be returned in either sequence, regardless of the
   value returned by the predicate."
  [p
   s :- [s/Any]]
  (let [d (map (fn [x] (if (p x) [x nil] [nil x])) s)]
    [(keep first d) (keep second d)]))

(defn fixpoint
  "Applies the function f to the value a. The function is then,
   and applied to the result, over and over, until the result does not change.
   Returns the final result.
   Note: If the function has no fixpoint, then runs forever."
  [f a]
  (let [s (iterate f a)]
    (some identity (map #(#{%1} %2) s (rest s)))))
