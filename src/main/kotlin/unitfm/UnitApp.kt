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
                <html>
                <body>

                <h1>Test reports for project jeschkies/unit</h1>

                <svg width="100%" height="100%">
                <style type="text/css" >
                <![CDATA[
                    .failure {
                        stroke: white;
                        fill:   red;
                    }
                    .success {
                        stroke: white;
                        fill:   green;
                    }

                    .buildLabel {
                        font: 12px sans-serif;
                        stroke: none;
                        fill:   black;
                    }
            ]]>
                </style>

                <defs>
                <g id="build" x="0">
                <rect x="0" width="10" height="100"/>
                <text x="0" y="110" class="buildLabel">1</text>
                </g>
                </defs>

                """.trimIndent())
        summary.reportSummaries.forEachIndexed { i, summary ->
            val outcome = if (summary.errors == 0 ) "success" else "failure"
            val x = i * 10
            content.append("""
                    <use x="$x" y="0" xlink:href="#build" class="$outcome" />
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
