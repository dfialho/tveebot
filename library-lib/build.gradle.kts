val guavaVersion: String by project

dependencies {
    compile(project(":library-api"))
    implementation("com.google.guava:guava:$guavaVersion")

    testCompile(project(":app-api", configuration = "tests"))
}
