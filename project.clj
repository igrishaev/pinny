(def MIN_JAVA_VERSION "16")

(defproject com.github.igrishaev/deed "0.1.0-SNAPSHOT"

  :description
  "Fast, flexible, 0-deps (de)serialization library for Clojure"

  :dependencies
  [[org.clojure/clojure "1.11.1"]]

  :pom-addition
  [:properties
   ["maven.compiler.source" ~MIN_JAVA_VERSION]
   ["maven.compiler.target" ~MIN_JAVA_VERSION]]

  :source-paths ["src/clj"]
  :java-source-paths ["src/main/java"]
  :javac-options ["-Xlint:unchecked"
                  "-Xlint:preview"
                  "--release" ~MIN_JAVA_VERSION]

  :profiles
  {:dev
   {:source-paths ["dev"]
    :dependencies [[criterium "0.4.6"]
                   [com.taoensso/nippy "3.4.2"]
                   [com.alpha-prosoft/jsonista "0.3.8.11"]]}

   :test
   {:source-paths ["test"]}})
