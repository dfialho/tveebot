val guavaVersion: String by project
val exposedVersion: String by project

repositories {
    maven { url = uri("https://dl.bintray.com/kotlin/exposed") }
}

dependencies {
    compile(project(":tracker-api"))
    implementation(project(":rss-feed"))
}

dependencies {
    compile("com.google.guava:guava:$guavaVersion")
    compile("org.jetbrains.exposed:exposed:$exposedVersion")
    implementation("org.jsoup:jsoup:1.10.3")
}
