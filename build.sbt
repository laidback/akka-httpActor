lazy val akkaHttpVersion = "10.1.7"
lazy val akkaVersion    = "2.5.21"

lazy val root = (project in file(".")).
  settings(
    inThisBuild(List(
      organization    := "com.example",
      scalaVersion    := "2.12.7"
    )),
    name := "asset-mgmt",
    libraryDependencies ++= Seq(
      "com.typesafe.akka" %% "akka-http"            % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "pl.iterators"      %% "kebs-spray-json"      % "1.6.2",
      "pl.iterators"      %% "kebs-akka-http"       % "1.6.2",
      "com.typesafe.akka" %% "akka-stream"          % akkaVersion,
      "com.lightbend.akka" %% "akka-stream-alpakka-s3"      % "1.0-M2",
      "com.lightbend.akka" %% "akka-stream-alpakka-slick"   % "1.0-M2",
      "pl.iterators"      %% "kebs-akka-http"       % "1.6.2",
      "pl.iterators"      %% "kebs-spray-json"      % "1.6.2",
      "com.h2database"    % "h2"                    % "1.4.192",
      "org.slf4j"         % "slf4j-nop"             % "1.6.4",

      "com.typesafe.akka" %% "akka-http-testkit"    % akkaHttpVersion % Test,
      "com.typesafe.akka" %% "akka-testkit"         % akkaVersion     % Test,
      "com.typesafe.akka" %% "akka-stream-testkit"  % akkaVersion     % Test,
      "org.scalatest"     %% "scalatest"            % "3.0.1"         % Test
    )
  )
