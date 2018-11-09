package unitfm

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.http.ContentType
import io.ktor.jackson.JacksonConverter
import io.ktor.request.receive
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import unitfm.data.Testsuite
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import com.zaxxer.hikari.HikariDataSource
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import org.jooq.impl.DSL
import org.jooq.SQLDialect
import unitfm.data.Testcase
//import unitfm.models.enums.Teststatus
//import unitfm.models.tables.Testcases
//import unitfm.models.tables.records.TestcasesRecord


//fun statusOf(testcase: Testcase): Teststatus {
//    if (testcase.error.isNotEmpty() || testcase.failure.isNotEmpty()) {
//        return Teststatus.failure
//    } else if (!testcase.skipped.isNullOrBlank()) {
//        return Teststatus.skipped
//    }
//
//    return Teststatus.success
//}

fun Application.module() {
    val db_user =  "kjeschkies" //System.getenv("POSTGRES_USER")
    val db_password =  "1234" //System.getenv("POSTGRES_PASSWORD")
    val db_url = "jdbc:postgresql://localhost:5432/unitfm"

    val ds = HikariDataSource()
    ds.jdbcUrl = db_url
    ds.username = db_user
    ds.password = db_password

    install(DefaultHeaders)
    install(CallLogging)
    install(ContentNegotiation) {

        // Create XML object mapper with support for JAXB annotations.
        val xmlModule = JacksonXmlModule()
        xmlModule.setDefaultUseWrapper(false)
        val xmlMapper = XmlMapper(xmlModule)
        val jaxbModule = JaxbAnnotationModule()
        xmlMapper.registerModule(jaxbModule)

        register(ContentType.Application.Xml, JacksonConverter(xmlMapper))
    }
    install(Routing) {
        get("/") {
            call.respondText("My Example Blog", ContentType.Text.Html)
        }
        post("/") {
            val testSuite = call.receive<Testsuite>()

//            DSL.using(ds, SQLDialect.POSTGRES).use { ctx ->
//                testSuite.testcase.forEach { testcase ->
//                    val record: TestcasesRecord = ctx.newRecord(Testcases.TESTCASES)
//                    record.commit = "deadbeef"
//                    record.status = statusOf(testcase)
//                    record.repository = "jeschkies/unit"
//                    record.name = testcase.name
//                    record.store()
//                }
//            }

            call.respond(HttpStatusCode.Created)
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::module).start()
}
