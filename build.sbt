resolvers += "Atlassian's Maven Public Repository" at "https://packages.atlassian.com/maven-public/"

lazy val root = (project in file("."))
   .enablePlugins(PlayScala)
  .settings(
    name := "play-slick-silhouette-sangria",
    version := "0.0.1",
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      guice,
      caffeine,
      "com.typesafe.play" %% "play-slick" % "5.0.0",
      "com.h2database" % "h2" % "1.4.199",
      "org.scalatest" %% "scalatest" % "3.1.0" % Test,
      "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
      "org.scalacheck" %% "scalacheck" % "1.14.3" % Test,
      "org.postgresql" % "postgresql" % "42.2.9",
      "org.sangria-graphql" %% "sangria" % "2.0.0-M3",
      "org.sangria-graphql" %% "sangria-play-json" % "2.0.0",
      "com.mohiva" %% "play-silhouette" % "7.0.0",
      "com.mohiva" %% "play-silhouette-persistence"  % "7.0.0",
      "com.mohiva" %% "play-silhouette-crypto-jca" % "7.0.0",
      "com.mohiva" %%  "play-silhouette-password-bcrypt" % "7.0.0",
      "net.codingwell" %% "scala-guice" % "4.2.6",
      "com.iheart" %% "ficus" % "1.4.7",
      "org.flywaydb" %% "flyway-play" % "6.0.0",
      specs2 % Test
    ),
    scalacOptions ++= Seq(
      "-feature",
      "-deprecation",
      "-Xfatal-warnings"
    )
  )
