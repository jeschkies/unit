package fm.unit

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.zaxxer.hikari.HikariDataSource
import fm.unit.dao.Organizations
import fm.unit.dao.PayloadArgumentFactory
import fm.unit.dao.Reports
import fm.unit.dao.Repositories
import fm.unit.model.ProjectSummary
import fm.unit.model.Report
import fm.unit.model.Testsuite
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.MultiPartData
import io.ktor.http.content.PartData
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.streamProvider
import io.ktor.jackson.jackson
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.velocity.Velocity
import io.ktor.velocity.VelocityContent
import kotlinx.coroutines.runBlocking
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.onDemand
import kotlin.random.Random


fun Application.module() {

    /**
     * For DEBUGging purposes only. Method generates a list of fake reports to be able to test template generation locally
     */
    fun testReports(random: Random = Random(1)): List<Report> {
        fun summaries(random: Random): List<Testsuite.Summary> {
            return (0..random.nextInt(10)).map {
                Testsuite.Summary(tests = random.nextInt(1, 10), errors = random.nextInt(2))
            }
        }

        return listOf("Fake News!", "Cake Is a Lie", "Magic Unicorn").map { Report(it, summaries(random)) }
    }

    fun template(summary: ProjectSummary): VelocityContent {
        val template = "templates/reports/summary.vm"
        val model = mutableMapOf<String, Any>("summary" to summary)
        return VelocityContent(template, model)
    }

    /**
     * Database setup
      */
    val db_user =  System.getenv("POSTGRES_USER") ?: "kjeschkies"
    val db_password =  System.getenv("POSTGRES_PASSWORD") ?: "1234"
    val db_name = System.getenv("POSTGRES_DATABASE") ?: "unitfm"
    val db_url = "jdbc:postgresql://localhost:5432/$db_name"

    // TODO(karsten): Fail fast when connection to database cannot be established.
    val ds = HikariDataSource()
    ds.jdbcUrl = db_url
    ds.username = db_user
    ds.password = db_password

    val jdbi = Jdbi.create(ds)
    jdbi.installPlugins()
    jdbi.registerArgument(PayloadArgumentFactory)

    /**
     * Extract posted JUnit XML files and commit hash from multi part upload data.
     */
    suspend fun readPostedReport(multipart: MultiPartData): Pair<String, List<Testsuite>> {
        var commit: String? = null
        val suites = mutableListOf<Testsuite>()
        while (true) {
            val part = multipart.readPart() ?: break

            when (part) {
                is PartData.FormItem ->
                    if (part.name == "commit") {
                        commit = part.value
                    }
                is PartData.FileItem -> {
                    val payload = Testsuite.Payload(part.streamProvider().bufferedReader().use { it.readText() })
                    val suite = Testsuite( part.originalFileName ?: "", payload)
                    suites.add(suite)
                }
            }
        }

        return Pair(commit ?: "", suites.toList())
    }

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule()) // support java.time.* types
        }
    }
    install(Velocity) {
        // Resource loader
        setProperty("resource.loader", "class");
        addProperty("class.resource.loader.class", ClasspathResourceLoader::class.java.name)
        // Strictness settings
        addProperty("runtime.log.invalid.references", "true")
        addProperty("runtime.references.strict", "true")
        addProperty("runtime.strict.math", "true")
        init()
    }
    install(Routing) {
        get("/") {
            call.respondText("<h1>Put your JUnit files to work for greater good!</h1>", ContentType.Text.Html)
        }
        static("/static") {
            files("static")
        }
        route("/reports/{organization}/{repository}/{prefix}") {

            get {
                val organization = call.parameters["organization"]!!
                val repository = call.parameters["repository"]!!
                val prefix = call.parameters["prefix"]!!

                val orgId = jdbi.onDemand<Organizations>().get(organization)
                val repoId = jdbi.onDemand<Repositories>().get(repository)

                if (orgId == null || repoId == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {
                    val reports = testReports()// TODO(karsten): fetch from database
                    val summary = ProjectSummary(reports)
                    call.respond(template(summary))
                }
            }

            post {
                val organization = call.parameters["organization"]!!
                val repository = call.parameters["repository"]!!
                val prefix = call.parameters["prefix"]!!

                val orgId = jdbi.onDemand<Organizations>().get(organization)
                val repoId = jdbi.onDemand<Repositories>().get(repository)

                if (orgId == null || repoId == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {

                    val multipart = call.receiveMultipart()
                    val (commit_hash, suites) = readPostedReport(multipart)
                    runBlocking {
                        jdbi.onDemand<Reports>().insert(orgId, repoId, prefix, commit_hash, suites)
                    }

                    call.respond(HttpStatusCode.Created)
                }
            }
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::module).start()
}
