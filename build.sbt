val ScalatraVersion = "3.1.0"

ThisBuild / scalaVersion := "3.5.0"
ThisBuild / organization := "com.github.gallalouche"

lazy val hello = (project in file("."))
  .settings(
    name := "HW Smoke Tester",
    version := "0.1.0-SNAPSHOT",
    libraryDependencies ++= Seq(
      "org.scalatra" %% "scalatra-jakarta" % ScalatraVersion,
      "org.scalatra" %% "scalatra-scalatest-jakarta" % ScalatraVersion % "test",
      "ch.qos.logback" % "logback-classic" % "1.5.6" % "runtime",
      "org.eclipse.jetty" % "jetty-plus" % "12.0.10" % "compile",
       "org.eclipse.jetty.ee10" % "jetty-ee10-webapp" % "12.0.13",
      "jakarta.servlet" % "jakarta.servlet-api" % "6.0.0",
      "org.eclipse.jetty.ee10" % "jetty-ee10-servlet" % "12.0.13",
      "org.typelevel" %% "cats-core" % "2.12.0",
      "org.typelevel" %% "cats-effect" % "3.5.4",
      "com.github.pathikrit" % "better-files_3" % "3.9.2",
    ),
    javaOptions ++= Seq(
      "-Xdebug",
      "-Xrunjdwp:transport=dt_socket,server=y,suspend=n,address=*:5005"
    ),
  )

enablePlugins(SbtTwirl)
enablePlugins(JettyPlugin)

Jetty / containerLibs := Seq(
  ("org.eclipse.jetty.ee10" % "jetty-ee10-runner" % "12.0.10").intransitive(),
)
Jetty / containerMain := "org.eclipse.jetty.ee10.runner.Runner"

Jetty / containerPort := 8090
Jetty / debugPort := 5005
