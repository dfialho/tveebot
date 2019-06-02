val h2Version: String by project
val exposedVersion: String by project

plugins {
    application
}

application {
    mainClassName = "dfialho.tveebot.data.migrator.MigratorKt"
    applicationName = "tveebot-migrator"
}

repositories {
    maven { url = uri("https://dl.bintray.com/kotlin/exposed") }
}

dependencies {
    implementation("com.h2database:h2:$h2Version")
    implementation("org.jetbrains.exposed:exposed:$exposedVersion")
    implementation(project(":application-api"))
}
