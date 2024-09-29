(defproject com.github.igrishaev/deed-core "0.1.0-SNAPSHOT"

  :description
  "Fast, flexible, 0-deps (de)serialization library for Clojure"

  :scm {:dir ".."}

  :plugins
  [[lein-parent "0.3.8"]]

  :source-paths ["src/clj"]
  :java-source-paths ["src/java"]

  :dependencies
  [[org.clojure/clojure]]

  :parent-project
  {:path "../project.clj"
   :inherit [:deploy-repositories
             :license
             :release-tasks
             :managed-dependencies
             :plugins
             :repositories
             :pom-addition
             :javac-options
             :url
             [:profiles :dev]
             [:profiles :test]]})
