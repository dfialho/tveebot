
val testJar by  tasks.registering(Jar::class) {
    classifier = "tests"
    from(sourceSets.test.get().output)
}

configurations {
    create("tests")
}

artifacts {
    add("tests", testJar)
}
