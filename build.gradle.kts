plugins { id("io.vacco.oss.gitflow") version "0.9.8" }

group = "io.vacco.cpiohell"
version = "0.1.0"

configure<io.vacco.oss.gitflow.GsPluginProfileExtension> {
  addJ8Spec()
  addClasspathHell()
  sharedLibrary(true, false)
}

val api by configurations

dependencies {
  api("org.apache.commons:commons-compress:1.20")
}
