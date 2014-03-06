import play.Project._

resolvers += "webjars" at "http://webjars.github.com/m2"
                                       
resolvers += "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/"

name := "imgproc"

version := "1.0-SNAPSHOT"

libraryDependencies ++= Seq(
	javaJdbc,
	javaEbean,
	cache,
	"org.webjars" %% "webjars-play" % "2.2.0", 
	"org.webjars" % "webjars-locator" % "0.5",
	"org.webjars" % "bootstrap" % "2.3.1",
	"com.amazonaws" % "aws-java-sdk" % "1.3.11",
	"postgresql" % "postgresql" % "9.1-901-1.jdbc4")


playJavaSettings
