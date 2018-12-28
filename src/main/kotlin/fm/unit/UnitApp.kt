package fm.unit

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.zaxxer.hikari.HikariDataSource
import fm.unit.dao.*
import fm.unit.model.Payload
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
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.*
import io.ktor.jackson.jackson
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.route
import kotlinx.coroutines.runBlocking
import org.jdbi.v3.core.Jdbi
import org.jdbi.v3.sqlobject.kotlin.attach
import org.jdbi.v3.sqlobject.kotlin.onDemand
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception


fun Application.module() {

    fun template(summary: ProjectSummary): String {
        val content = StringBuilder()
        content.append("""
                <!DOCTYPE html>
                <html>
                <head>
                    <link rel="stylesheet" href="/static/style.css">
                </head>
                <body>

                <h1>Test reports for project jeschkies/unit</h1>

                <svg width="100%" height="100%">
                """.trimIndent())
        summary.reports.forEachIndexed { i, report ->
            val outcome = if (report.errors == 0 ) "success" else "failure"
            val x = i * 10
            content.append("""
                    <g class="build" x="10">
		                <rect x="$x" y="10" width="10" height="100" class="$outcome buildBar"/>
		                <text x="$x" y="10" class="buildLabel">Build #$i</text>
	 	            </g>
                    """.trimIndent())
        }
        content.append("""
              </svg>

            </body>
            </html>
        """.trimIndent())
        return content.toString()
    }

    // Database setup
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
                    val payload = Payload(part.streamProvider().bufferedReader().use { it.readText() })
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
    install(Routing) {
        get("/") {
            call.respondText("My Example Blog", ContentType.Text.Html)
        }
        static("/static") {
            files("static")
        }
        route("/reports/{organization}/{repository}/{prefix}") {

            get {
                val organization = call.parameters["organization"]
                val repository = call.parameters["repository"]
                val prefix = call.parameters["prefix"]

                val reports = emptyList<Report>()// TODO(karsten): fetch from database
                val summary = ProjectSummary(reports)
                call.respondText(template(summary), ContentType.Text.Html)
            }

            post {
                val organization = call.parameters["organization"] ?: ""
                val repository = call.parameters["repository"] ?: ""
                val prefix = call.parameters["prefix"] ?: ""

                val org_id = jdbi.onDemand<Organizations>().get(organization)
                val repo_id = jdbi.onDemand<Repositories>().get(repository)

                if (org_id == null || repo_id == null) {
                    call.respond(HttpStatusCode.NotFound)
                } else {

                    val multipart = call.receiveMultipart()
                    val (commit_hash, suites) = readPostedReport(multipart)
                    runBlocking {
                        jdbi.onDemand<Reports>().insert(org_id, repo_id, prefix, commit_hash, suites)
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
