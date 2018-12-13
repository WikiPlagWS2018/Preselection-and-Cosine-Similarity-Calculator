ThisBuild / scalaVersion := "2.11.0"
ThisBuild / organization := "de.htw.f4.wikiplag"


lazy val idf = (project in file("."))
  .settings(
    name := "WikiIDFApp",
    libraryDependencies++=Seq(
    "org.apache.spark" %% "spark-core" % "2.2.0",
    "org.apache.spark" %% "spark-sql" % "2.2.0",
    "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.1")
    
  )

assemblyMergeStrategy in assembly := {
      case PathList("META-INF", xs@_*) => MergeStrategy.discard
      case x => MergeStrategy.first
    }