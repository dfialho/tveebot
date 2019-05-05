val ktorVersion: String by project
val kodeinVersion: String by project
val h2Version: String by project

plugins {
    application
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
    applicationName = "tveebot"
}

repositories {
    maven { url = uri("https://dl.bintray.com/kotlin/exposed") }
    maven { url = uri("https://dl.bintray.com/kotlin/ktor") }
    maven { url = uri("https://dl.bintray.com/kodein-framework/Kodein-DI") }
}

dependencies {
    implementation(project(":downloader-lib"))
    implementation(project(":tracker-lib"))
    implementation(project(":library-lib"))
    implementation(project(":application-api"))
}

dependencies {
    implementation("org.kodein.di:kodein-di-generic-jvm:$kodeinVersion")
    implementation("com.h2database:h2:$h2Version")
}

dependencies {
    compile("io.ktor:ktor-server-netty:$ktorVersion")
    compile("io.ktor:ktor-gson:$ktorVersion")
}

// Dirty hack to ensure the 'conf' directory is included in the classpath by the start script
// Not using this hack, will result in the classpath to include 'lib/conf' instead of just 'conf'
// Hack from: https://stackoverflow.com/questions/38416562/gradle-build-file-with-conf-folder-with-properties-not-in-jar-but-on-classpath
tasks.startScripts {
    classpath = classpath?.plus(files("src/dist/XxxAPlaceHolderForAConfxxX"))
    doLast {
        windowsScript.writeText(
            windowsScript.readText().replace("%APP_HOME%\\lib\\XxxAPlaceHolderForAConfxxX", "%APP_HOME%\\conf")
        )
        unixScript.writeText(
            unixScript.readText()
                .replace("\$APP_HOME/lib/XxxAPlaceHolderForAConfxxX", "\$APP_HOME/conf")
                .replace("DEFAULT_JVM_OPTS=\"\"", "DEFAULT_JVM_OPTS=\"-Djava.library.path='\$APP_HOME/lib/native'\"")
        )
    }
}