(ns naga.test-util
  (:require [clojure.test :as t :refer [deftest is run-tests]]
            [clojure.string]
            [naga.util :refer [mapmap get-fn-reference fn-for divide' fixpoint]]))

(deftest test-mapmap
  (let [f1 (partial * 2)
        f2 #(keyword (str "k" %))
        f3 (partial * 3)
        input (set (range 5))
        map1 (mapmap f1 input)
        map2 (mapmap f2 f3 input)]
    (is (= input (set (keys map1))))
    (is (every? #(= (f1 %) (map1 %)) (keys map1)))

    (let [k2 (set (keys map2))]
      (is (= (count input) (count k2)))
      (is (every? k2 (map f2 input)))
      (is (every? #(= (map2 (f2 %)) (f3 %)) input)))))

(deftest test-get-fn-ref
  (let [m' (get-fn-reference :map)
        i' (get-fn-reference 'inc)
        m (get-fn-reference :clojure.core/map)
        i (get-fn-reference 'clojure.core/inc)
        j (get-fn-reference :clojure.string/join)]
    (is (nil? m'))
    (is (nil? i'))
    (is (= (map inc (range 10))
           (m i (range 10))))
    (is (= "1,2" (j "," [1 2])))))

(deftest test-fn-for
  (let [m' (fn-for 'map)
        i' (fn-for 'inc)
        m #?(:clj (fn-for 'clojure.core/map)
             :cljs (fn-for 'cljs.core/map))
        i #?(:clj (fn-for 'clojure.core/inc)
             :cljs (fn-for 'cljs.core/inc)) ]
    (is (= (map inc (range 10))
           (m' i' (range 10))))
    (is (= (map inc (range 10))
           (m i (range 10))))))

(deftest test-divide
  (is (= [[1 3 5 7 9] [0 2 4 6 8]] (divide' odd? (range 10)))))

(deftest test-fixpoint
  (let [f1 #(if (< % 5) (inc %) %)
        cntr (atom 0)
        f2 (fn [x] (swap! cntr inc) (if (pos? x) (dec x) x))]
    (is (= 5 (fixpoint f1 0)))
    (is (= 5 (fixpoint f1 5)))
    (is (= 15 (fixpoint f1 15)))
    (is (= 0 (fixpoint f2 5)))
    (is (= 6 @cntr))
    (is (= 0 (fixpoint f2 3)))
    (is (= 10 @cntr))))


#?(:cljs (run-tests))
