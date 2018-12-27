package fm.unit

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.zaxxer.hikari.HikariDataSource
import fm.unit.model.ProjectSummary
import fm.unit.model.Report
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
import io.ktor.http.content.PartData
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.content.streamProvider
import io.ktor.jackson.jackson
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.routing.route
import org.jdbi.v3.core.Jdbi
import java.io.File
import java.io.FileOutputStream


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
    val db_user =  "kjeschkies" //System.getenv("POSTGRES_USER")
    val db_password =  "1234" //System.getenv("POSTGRES_PASSWORD")
    val db_url = "jdbc:postgresql://localhost:5432/fm.unit.unitfm"

    val ds = HikariDataSource()
    ds.jdbcUrl = db_url
    ds.username = db_user
    ds.password = db_password

    val jdbi = Jdbi.create(ds)
    jdbi.installPlugins()

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
        route("/reports") {
            get("{key...}") {
                // TODO(karsten): Lots of error handling and we don't support arbitrary prefix depth.
                val key = call.parameters.getAll("key")?.joinToString("/") ?: ""

                val reports = emptyList<Report>()// TODO(karsten): fetch from database
                val summary = ProjectSummary(reports)
                call.respondText(template(summary), ContentType.Text.Html)
            }
            post("{key...}") {
                val key = call.parameters.getAll("key")?.joinToString("/") ?: ""

                val multipart = call.receiveMultipart()

                var commit: String? = null
                while (true) {
                    val part = multipart.readPart() ?: break

                    when(part) {
                        is PartData.FormItem ->
                            if (part.name == "commit") {
                                commit = part.value
                            }
                        is PartData.FileItem -> {
                            // TODO(karsten): Insert into database
                            //val file = File(reportFolder, part.originalFileName)
                            //part.streamProvider().copyTo(FileOutputStream(file))
                        }
                    }
                }
                call.respond(HttpStatusCode.Created)
            }
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::module).start()
}
