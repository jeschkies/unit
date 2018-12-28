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
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader
import org.jdbi.v3.core.Jdbi
import kotlin.random.Random


fun Application.module() {

    /**
     * For DEBUGging purposes only. Method generates a list of fake reports to be able to test template generation locally
     */
    fun testReports(random: Random = Random(1)): List<Report> {
        fun summaries(random: Random): List<TestsuiteSummary> {
            return (0..random.nextInt(10)).map {
                TestsuiteSummary(tests = random.nextInt(1, 10), errors = random.nextInt(2))
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
    install(Velocity) { // this: VelocityEngine
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
        route("/reports") {
            get("{key...}") {
                // TODO(karsten): Lots of error handling and we don't support arbitrary prefix depth.
                val key = call.parameters.getAll("key")?.joinToString("/") ?: ""

                val reports = testReports()// TODO(karsten): fetch from database
                val summary = ProjectSummary(reports)
                call.respond(template(summary))
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
