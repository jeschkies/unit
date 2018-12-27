package fm.unit

import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.zaxxer.hikari.HikariDataSource
import fm.unit.model.ProjectSummary
import fm.unit.model.Report
import fm.unit.model.TestsuiteSummary
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.files
import io.ktor.http.content.static
import io.ktor.http.withCharset
import io.ktor.jackson.jackson
import io.ktor.request.receiveMultipart
import io.ktor.response.respond
import io.ktor.response.respondText
import io.ktor.response.respondTextWriter
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import org.apache.velocity.VelocityContext
import org.apache.velocity.app.Velocity
import org.jdbi.v3.core.Jdbi
import java.io.StringWriter
import kotlin.random.Random


fun Application.module() {

    /**
     * For DEBUGing purposes only. Method generates a list of fake reports to be able to test template generation locally
     */
    fun testReports(random: Random = Random(1)): List<Report> {
        fun summaries(random: Random): List<TestsuiteSummary> {
            return (0..random.nextInt(10)).map {
                TestsuiteSummary(tests = random.nextInt(1, 10), errors = random.nextInt(2))
            }
        }

        return listOf("Fake News!", "Cake Is a Lie", "Magic Unicorn").map { Report(it, summaries(random)) }
    }

    fun template(summary: ProjectSummary): String {
        val t = Velocity.getTemplate("templates/reports/summary.vm")

        val context = VelocityContext()
        context.put("summary", summary)

        val writer = StringWriter()
        t.merge(context, writer)
        return writer.toString()
    }

    /**
     * Database setup
     */
    val db_user =  "kjeschkies" //System.getenv("POSTGRES_USER")
    val db_password =  "1234" //System.getenv("POSTGRES_PASSWORD")
    val db_url = "jdbc:postgresql://localhost:5432/fm.unit.unitfm"

    val ds = HikariDataSource()
    ds.jdbcUrl = db_url
    ds.username = db_user
    ds.password = db_password

    val jdbi = Jdbi.create(ds)
    jdbi.installPlugins()

    /**
     * Velocity setup. Additional settings are provided via `velocity.properties` file.
     */
    Velocity.init("velocity.properties")

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule()) // support java.time.* types
        }
    }
    install(Routing) {
        get("/") {
            call.respondText("Put your JUnit files to work for greater good!", ContentType.Text.Html)
        }
        static("/static") {
            files("static")
        }
        route("/reports") {
            get("{key...}") {
                // TODO(karsten): Lots of error handling and we don't support arbitrary prefix depth.
                val key = call.parameters.getAll("key")?.joinToString("/") ?: ""

                val reports = testReports()// TODO(karsten): fetch from database
                val summary = ProjectSummary(reports)
                call.respondText(template(summary), ContentType.Text.Html.withCharset(Charsets.UTF_8))
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
