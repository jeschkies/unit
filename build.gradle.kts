import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import org.flywaydb.gradle.task.FlywayMigrateTask
import org.jooq.util.jaxb.Jdbc
import java.sql.DriverManager



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
        classpath("org.postgresql:postgresql:42.2.5")
    }
}

plugins {
    application
    java
    kotlin("jvm") version "1.2.61"
    id("org.flywaydb.flyway") version "5.2.1"
    id("com.bmuschko.docker-remote-api") version "3.2.3"
    id("com.rohanprabhu.kotlin-dsl-jooq") version "0.3.1"
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
    compile("com.zaxxer:HikariCP:2.7.8")
    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("io.ktor:ktor-jackson:$ktor_version")
    compile("org.jooq:jooq")
    compile("org.postgresql:postgresql:42.2.5")
    //testCompile group: 'junit', name: 'junit', version: '4.12'
    jooqGeneratorRuntime("org.postgresql:postgresql:42.2.5")
}

val db_user = "kjeschkies"
val db_password = "1234" // TODO: pull from environment.
val database = "unitfm"

jooqGenerator {
    configuration("primary", project.java.sourceSets.getByName("main")) {
        configuration = org.jooq.util.jaxb.Configuration().apply {
            jdbc = Jdbc().apply {
                driver = "org.postgresql.Driver"
                user = db_user
                password = db_password
                url = "jdbc:postgresql://localhost:5432/$database"
            }
            generator = org.jooq.util.jaxb.Generator().apply {
                database = org.jooq.util.jaxb.Database().apply {
                    name = "org.jooq.util.postgres.PostgresDatabase"
                    excludes = "flyway_.*"
                    inputSchema = "public"
                }
                target = org.jooq.util.jaxb.Target().apply {
                    packageName = "unitfm.models"
                    directory = "${project.buildDir}/generated/jooq/primary"
                }
            }
        }
    }
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
