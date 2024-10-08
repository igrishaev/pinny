(ns demo
  (:require
   [clojure.java.io :as io]
   [deed.core :as deed]
   [deed.base64 :as b64]
   [deed.vectorz :as vz])
  (:import
   (mikera.vectorz Vectorz)))

(def file
  (io/file "dump.deed"))

(def data
  {:number 1
   :string "hello"
   :bool true
   :nil nil
   :symbol 'hello/test
   :simple-kw :test
   :complex-kw :foo/bar
   :vector [1 2 :test nil 42 "hello"]
   :map {:test {:bar {'dunno {"lol" [:kek]}}}}
   :set #{:a :b :c}
   :atom (atom {:abc 42})})

(deed/encode-to data file)

(def data-back
  (deed/decode-from file))

#_
{:number 1,
 :symbol hello/test,
 :complex-kw :foo/bar,
 :string "hello",
 :vector [1 2 :test nil 42 "hello"],
 :nil nil,
 :bool true,
 :set #{:c :b :a},
 :simple-kw :test,
 :atom #<Atom@75f6dd87: {:abc 42}>,
 :map {:test {:bar {dunno {"lol" [:kek]}}}}}


(= (dissoc data :atom) (dissoc data-back :atom))
true

;; xxd /path/to/dump.deed
;; 00000000: 0001 0001 0000 0000 0000 0000 0000 0000  ................
;; 00000010: 0000 0000 0000 0000 0000 0000 0000 0000  ................
;; 00000020: 0000 003b 0000 000b 004a 0000 0006 6e75  ...;.....J....nu
;; 00000030: 6d62 6572 000e 004a 0000 0006 7379 6d62  mber...J....symb
;; 00000040: 6f6c 004b 0000 000a 6865 6c6c 6f2f 7465  ol.K....hello/te
;; 00000050: 7374 004a 0000 000a 636f 6d70 6c65 782d  st.J....complex-
;; 00000060: 6b77 004a 0000 0007 666f 6f2f 6261 7200  kw.J....foo/bar.
;; 00000070: 4a00 0000 0673 7472 696e 6700 2b00 0000  J....string.+...
;; 00000080: 0568 656c 6c6f 004a 0000 0006 7665 6374  .hello.J....vect
;; 00000090: 6f72 002e 0000 0006 000e 000c 0000 0000  or..............
;; 000000a0: 0000 0002 004a 0000 0004 7465 7374 0000  .....J....test..
;; 000000b0: 000c 0000 0000 0000 002a 002b 0000 0005  .........*.+....
;; 000000c0: 6865 6c6c 6f00 4a00 0000 036e 696c 0000  hello.J....nil..
;; 000000d0: 004a 0000 0004 626f 6f6c 0029 004a 0000  .J....bool.).J..
;; 000000e0: 0003 7365 7400 3300 0000 0300 4a00 0000  ..set.3.....J...
;; 000000f0: 0163 004a 0000 0001 6200 4a00 0000 0161  .c.J....b.J....a
;; 00000100: 004a 0000 0009 7369 6d70 6c65 2d6b 7700  .J....simple-kw.
;; 00000110: 4a00 0000 0474 6573 7400 4a00 0000 0461  J....test.J....a
;; 00000120: 746f 6d00 3000 3b00 0000 0100 4a00 0000  tom.0.;.....J...
;; 00000130: 0361 6263 000c 0000 0000 0000 002a 004a  .abc.........*.J
;; 00000140: 0000 0003 6d61 7000 3b00 0000 0100 4a00  ....map.;.....J.
;; 00000150: 0000 0474 6573 7400 3b00 0000 0100 4a00  ...test.;.....J.
;; 00000160: 0000 0362 6172 003b 0000 0001 004b 0000  ...bar.;.....K..
;; 00000170: 0005 6475 6e6e 6f00 3b00 0000 0100 2b00  ..dunno.;.....+.
;; 00000180: 0000 036c 6f6c 002e 0000 0001 004a 0000  ...lol.......J..
;; 00000190: 0003 6b65 6b                             ..kek

(deed/encode-seq-to (map inc (range 999)) file)

(deed/encode-to {:foo 123} "test.deed")

(deed/encode-to {:foo 123} (io/file "test2.deed"))

(deed/encode-to {:foo 123} (-> "test3.deed"
                               io/file
                               io/output-stream))

(deed/decode-from "test.deed")
;; {:foo 123}

(deed/decode-from (io/file "test2.deed"))
;; {:foo 123}

(deed/decode-from (-> "test3.deed"
                      io/file
                      io/input-stream))
;; {:foo 123}

(def buf
  (deed/encode-to-bytes {:test 123}))


(deed/decode-from buf)

(deed/encode-seq-to [1 2 3] "test.deed")

(deed/decode-from "test.deed")

(deed/decode-seq-from "test.deed")

(deed/with-decoder [d "test.deed"]
  (doseq [item (deed/decode-seq d)]
    (println item)))

(deed/with-decoder [d "test.deed"]
  (->> d
       (deed/decode-seq)
       (mapv inc)))

(deed/with-decoder [d "test.deed"]
  (doseq [item d]
    (println item)))


(with-open [in (io/input-stream "test.deed")
            d (deed/decoder in)]
  (doseq [item (deed/decode-seq d)]
    (println item)))

(with-open [d (deed/decoder "test.deed")]
  (doseq [item (deed/decode-seq d)]
    (println item)))

(deed/with-decoder [d "test.deed"]
  (mapv inc d))

(deed/with-decoder [d "test.deed"]
  (doseq [item d]
    (println "item is" item)))

(deed/with-encoder [e "test.deed"]
  (doseq [x (range 1 32)]
    (when (even? x)
      (deed/encode e x))))

(deed/with-decoder [d "test.deed"]
  (loop [i 0]
    (let [item (deed/decode d)]
      (if (deed/eof? item)
        (println "EOF")
        (do
          (println "item" i item)
          (recur (inc i)))))))

(deftype MyType [a b c])

(def mt (new MyType :hello "test" 42))

(deed/encode-to mt "test.deed")

(deed/decode-from "test.deed")
;; #<Unsupported@b918edf: {:content "demo.MyType@4376ae5c", :class "demo.MyType"}>

(def mt-back
  (deed/decode-from "test.deed"))

(deed/unsupported? mt-back)

@mt-back

(deftype MyType [a b c]
  Object
  (toString [_]
    (format "<MyType: %s, %s, %s>" a b c)))

(def mt (new MyType :hello "test" 42))

(deed/encode-to mt "test.deed")

(def mt-back
  (deed/decode-from "test.deed"))

(str mt-back)
"Unsupported[className=demo.MyType, content=<MyType: :hello, test, 42>]"

@mt-back
{:content "<MyType: :hello, test, 42>", :class "demo.MyType"}

(deed/encode-to mt "test.deed" {:encode-unsupported? false})


(deftype SomeType [x y z]
  )

(def SomeTypeOID 4321)

(extend-protocol deed/IEncode
  SomeType
  (-encode [this encoder]
    (deed/writeOID encoder SomeTypeOID)
    (deed/encode encoder (.-x this))
    (deed/encode encoder (.-y this))
    (deed/encode encoder (.-z this))))


(extend-type SomeType
  deed/IEncode
  (-encode [this encoder]
    (deed/writeOID encoder SomeTypeOID)
    (deed/encode encoder (.-x this))
    (deed/encode encoder (.-y this))
    (deed/encode encoder (.-z this))))

(deftype SomeType [x y z]
  deed/IEncode
  (-encode [this encoder]
    (deed/writeOID encoder SomeTypeOID)
    (deed/encode encoder x)
    (deed/encode encoder y)
    (deed/encode encoder z)))

(defmethod deed/-decode SomeTypeOID
  [_ decoder]
  (let [x (deed/decode decoder)
        y (deed/decode decoder)
        z (deed/decode decoder)]
    (new SomeType x y z)))

(def _buf
  (deed/encode-to-bytes (new SomeType 1 2 3)))

(deed/decode-from _buf)

(with-open [out (-> "dump.deed.b64"
                    (b64/base64-output-stream))]
  (deed/encode-to [1 2 3] out))

(slurp "dump.deed.b64")

(with-open [in (-> "dump.deed.b64"
                   (io/file)
                   (b64/base64-input-stream))]
  (deed/decode-from in))


(def vz
  (Vectorz/create (double-array [1.1 2.2 3.3])))

(def dump
  (deed/encode-to-bytes vz))

(deed/decode-from dump)
;; #object[mikera.vectorz.Vector3 0x5ecb1a9a "[1.1,2.2,3.3]"]
