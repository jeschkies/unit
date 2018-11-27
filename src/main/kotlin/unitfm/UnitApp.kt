package unitfm

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
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
import java.io.File
import java.io.FileOutputStream

fun Application.module() {

    fun template(summary: Summary): String {
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
        summary.reportSummaries.forEachIndexed { i, summary ->
            val outcome = if (summary.errors == 0 ) "success" else "failure"
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

                val reports = ReportRepository.getReports(key).sortedBy { it.name }
                val summary = Summary(reports)
                call.respondText(template(summary), ContentType.Text.Html)
            }
            post("{key...}") {
                val key = call.parameters.getAll("key")?.joinToString("/") ?: ""

                val multipart = call.receiveMultipart()

                var commit: String? = null
                val reportFolder = ReportRepository.createReport(key)
                while (true) {
                    val part = multipart.readPart() ?: break

                    when(part) {
                        is PartData.FormItem ->
                            if (part.name == "commit") {
                                commit = part.value
                            }
                        is PartData.FileItem -> {
                            // TODO(karsten): Check if file exists
                            val file = File(reportFolder, part.originalFileName)
                            part.streamProvider().copyTo(FileOutputStream(file))
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
