import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import org.flywaydb.gradle.task.FlywayMigrateTask
import java.sql.DriverManager

val ktor_version = "0.9.5"
val jackson_version = "2.9.4"

buildscript {
    repositories {
        gradlePluginPortal()
        mavenCentral()
        maven {
            url = uri("https://plugins.gradle.org/m2/")
        }
    }
    dependencies {
        classpath("org.postgresql:postgresql:42.2.5")
    }
}

plugins {
    application
    java
    kotlin("jvm") version "1.3.0"
    id("org.flywaydb.flyway") version "5.2.3"
    id("com.bmuschko.docker-remote-api") version "3.2.3"
}

tasks.withType(org.jetbrains.kotlin.gradle.tasks.KotlinCompile::class.java).all {
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

application {
    mainClassName = "fm.unit.UnitAppKt"
}

repositories {
    jcenter()
    maven { url = uri("http://kotlin.bintray.com/ktor") }
    mavenCentral()
}

dependencies {
    compile(kotlin("stdlib"))
    compile(kotlin("stdlib-jdk8"))
    compile(kotlin("reflect"))
    compile("ch.qos.logback:logback-classic:1.2.3")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson_version")
    compile( "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")
    compile("com.zaxxer:HikariCP:2.7.8")
    compile("io.github.microutils:kotlin-logging:1.6.20")
    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("io.ktor:ktor-jackson:$ktor_version")
    compile("org.jdbi:jdbi3-kotlin-sqlobject:3.5.1")
    compile("org.jdbi:jdbi3-postgres:3.5.1")
    compile("org.jdbi:jdbi3-sqlobject:3.5.1")
    compile("org.postgresql:postgresql:42.2.5")

    testCompile("com.opentable.components:otj-pg-embedded:0.11.3")
    testCompile("io.kotlintest:kotlintest-runner-junit5:3.1.10")
    testCompile("org.flywaydb:flyway-core:5.2.3")
    testCompile("org.jdbi:jdbi3-testing:3.5.1")
}

//TODO: Remove but make sure that tests are auto discovered.
tasks.withType<Test> {
    useJUnitPlatform {}
}

val db_user = "kjeschkies"
val db_password = "1234" // TODO: pull from environment.
val database = "unitfm"

tasks {

    val postgresImage by creating(DockerPullImage::class) {
        repository = "postgres"
        tag = "10.5-alpine"
    }


    val postgresContainer by creating(DockerCreateContainer::class) {
        dependsOn(postgresImage)
        targetImageId { postgresImage.getImageId() }
        portBindings = listOf("5432:5432")
        setEnv("POSTGRES_USER=$db_user", "POSTGRES_PASSWORD=$db_password")
    }

    val startPostgres by creating(DockerStartContainer::class) {
        dependsOn(postgresContainer)
        targetContainerId { postgresContainer.getContainerId() }
    }

    val stopPostgres by creating(DockerStopContainer::class) {
        targetContainerId { postgresContainer.getContainerId() }
    }

    val createDatabase by creating {
        doFirst {
            val c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/", db_user, db_password)
            val statement = c.createStatement()
            statement.executeUpdate("CREATE DATABASE $database")
        }
    }

    val migrateDatabase by creating(FlywayMigrateTask::class) {
        dependsOn(createDatabase)
        url = "jdbc:postgresql://localhost:5432/$database"
        user = db_user
        password = db_password
    }
}
