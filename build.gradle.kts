import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val logbackVersion: String by project
val loggingVersion: String by project
val assertkVersion: String by project
val mockkVersion: String by project
val junitVersion: String by project
val kotlintestVersion: String by project

plugins {
    kotlin("jvm") version "1.3.30" apply false
    id("fr.brouillard.oss.gradle.jgitver") version "0.8.0"
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "idea")
    apply(plugin = "fr.brouillard.oss.gradle.jgitver")

    group = "dfialho.tveebot"

    jgitver {
        nonQualifierBranches = "develop"
        mavenLike(true)
    }

    repositories {
        mavenLocal()
        mavenCentral()
        jcenter()
    }

    dependencies {
        "implementation"("ch.qos.logback:logback-classic:$logbackVersion")
        "implementation"("io.github.microutils:kotlin-logging:$loggingVersion")
        "implementation"("org.jetbrains.kotlin:kotlin-reflect:$kotlinVersion")
    }

    dependencies {
        "testImplementation"("io.kotlintest:kotlintest-runner-junit5:$kotlintestVersion")
        "testImplementation"("org.junit.jupiter:junit-jupiter-api:$junitVersion")
        "testRuntime"("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
        "testImplementation"("com.willowtreeapps.assertk:assertk-jvm:$assertkVersion")
        "testImplementation"("io.mockk:mockk:$mockkVersion")
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "1.8"
    }

    tasks.withType<Test> {
        useJUnitPlatform { }
    }
}
