name := "WikiIDFApp"

version := "0.1"

scalaVersion := "2.11.0"


libraryDependencies ++=Seq("org.apache.spark" %% "spark-core" % "2.2.0",
  "org.apache.spark" %% "spark-sql" % "2.2.0",
  "com.datastax.spark" %% "spark-cassandra-connector" % "2.0.1")

resolvers += Resolver.mavenLocal
