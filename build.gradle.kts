import com.bmuschko.gradle.docker.tasks.image.DockerPullImage
import com.bmuschko.gradle.docker.tasks.container.DockerCreateContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStartContainer
import com.bmuschko.gradle.docker.tasks.container.DockerStopContainer
import org.flywaydb.gradle.task.FlywayMigrateTask
import us.kirchmeier.capsule.task.FatCapsule
import java.sql.DriverManager

val logback_version: String by project
val ktor_version: String by project
val kotlin_version: String by project
val jackson_version: String by project
val hickarcp_version: String by project
val kotlin_logging_version: String by project
val postgresql_version: String by project
val jdbi3_kotlin: String by project
val otj_pg_embedded: String by project
val kotlintest_runner_junit5: String by project
val flywaydb: String by project

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
    id("us.kirchmeier.capsule") version "1.0.2"
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

    compile("ch.qos.logback:logback-classic:$logback_version")
    compile("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:$jackson_version")
    compile( "com.fasterxml.jackson.datatype:jackson-datatype-jsr310:$jackson_version")
    compile("com.zaxxer:HikariCP:$hickarcp_version")
    compile("io.github.microutils:kotlin-logging:$kotlin_logging_version")
    compile("io.ktor:ktor-server-netty:$ktor_version")
    compile("io.ktor:ktor-jackson:$ktor_version")
    compile("io.ktor:ktor-velocity:$ktor_version")
    compile("org.jdbi:jdbi3-kotlin-sqlobject:$jdbi3_kotlin")
    compile("org.jdbi:jdbi3-postgres:$jdbi3_kotlin")
    compile("org.jdbi:jdbi3-sqlobject:$jdbi3_kotlin")
    compile("org.postgresql:postgresql:$postgresql_version")

    testCompile("com.opentable.components:otj-pg-embedded:$otj_pg_embedded")
    testCompile("io.kotlintest:kotlintest-runner-junit5:$kotlintest_runner_junit5")
    testCompile("org.flywaydb:flyway-core:$flywaydb")
    testCompile("org.jdbi:jdbi3-testing:$jdbi3_kotlin")
}

//TODO: Remove but make sure that tests are auto discovered.
tasks.withType<Test> {
    useJUnitPlatform {}
}

val db_user = System.getenv("POSTGRES_USER") ?: "kjeschkies"
val db_password = System.getenv("POSTGRES_PASSWORD") ?: "1234"
val database = System.getenv("POSTGRES_DATABASE") ?: "unitfm"

tasks {

    /**
     * Produces a fat jar in build/libs/unit-capsule.jar.
     *
     * Start with `java -jar unit-capsule.jar`.
     *
     * See http://www.capsule.io for details.
     */
    val capsule by creating(FatCapsule::class ) {
        group = "Distribution"
        description = "Assemble app in fat jar."
        applicationClass("fm.unit.UnitAppKt")
    }

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
            val result = statement.executeQuery("SELECT COUNT(*) FROM pg_database WHERE datname='$database'")
            result.next()
            if (result.getInt(1) == 0) {
                statement.executeUpdate("CREATE DATABASE $database")
            }
            c.close()
        }
    }

    /**
     * Migrate a local PostgreSql database.
     */
    val migrateDatabase by creating(FlywayMigrateTask::class) {
        dependsOn(createDatabase)
        url = "jdbc:postgresql://localhost:5432/$database"
        user = db_user
        password = db_password
    }

    val populateDatabase by creating {
        description = "Populates a test database with some test data"
        dependsOn(migrateDatabase)
        doLast {
            val c = DriverManager.getConnection("jdbc:postgresql://localhost:5432/$database", db_user, db_password)
            val statement = c.createStatement()
            statement.execute("""
                INSERT INTO organizations (name) VALUES ('jeschkies');
                INSERT INTO repositories (name) VALUES ('unit');
            """.trimIndent())
            c.close()
        }
    }
}
