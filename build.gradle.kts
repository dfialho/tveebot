import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val kotlinVersion: String by project
val logbackVersion: String by project
val loggingVersion: String by project
val assertkVersion: String by project
val mockkVersion: String by project
val kotestVersion: String by project

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
        "testImplementation"("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
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
