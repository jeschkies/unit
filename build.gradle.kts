import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer

val ktor_version = "0.9.5"

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        //classpath("com.bmuschko:gradle-docker-plugin:3.2.3")
        //classpath("gradle.plugin.com.boxfuse.client:gradle-plugin-publishing:5.2.1")
        //classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.2.51")
        classpath("org.postgresql:postgresql:42.2.5")
    }
}

plugins {
    application
    java
    kotlin("jvm") version "1.2.61"
    id("org.flywaydb.flyway") version "5.2.1"
    id("com.bmuschko.docker-remote-api") version "3.2.3"
}

application {
    mainClassName = "unitfm.UnitAppKt"
}

repositories {
    jcenter()
    maven { url = uri("http://kotlin.bintray.com/ktor") }
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib"))
    compile("ch.qos.logback:logback-classic:1.2.3")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.4")
    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("io.ktor:ktor-jackson:$ktor_version")
    //testCompile group: 'junit', name: 'junit', version: '4.12'
}

val db_user = "kjeschkies"
val db_password = "1234" // TODO: pull from environment.
val database = "unitfm"

// TODO(karsten): Create database for tests.
flyway {
    url = "jdbc:postgresql://localhost:5432/$database"
    user = db_user
    password = db_password
}

tasks {

    val postgresImage by creating(DockerPullImage::class) {
        repository = "postgres"
        tag = "10.5-alpine"
    }


    val postgresContainer by creating(DockerCreateContainer::class) {
        dependsOn(postgresImage)
        targetImageId { postgresImage.getImageId() }
        portBindings = listOf("5432:5432")
        //env = arrayOf("POSTGRES_USER=$db_user")
        //env.set(1, "POSTGRES_PASSWORD=$db_password")
    }

    val startPostgres by creating(DockerStartContainer::class) {
        dependsOn(postgresContainer)
        targetContainerId { postgresContainer.getContainerId() }
    }

    val stopPostgres by creating(DockerStopContainer::class) {
        targetContainerId { postgresContainer.getContainerId() }
    }
}
