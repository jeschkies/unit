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
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.readAllParts
import io.ktor.http.content.streamProvider
import io.ktor.request.receiveMultipart
import io.ktor.response.respond

fun Application.module() {

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
        post("/reports/{prefix...}") {
            val prefix = call.parameters.getAll("prefix")?.joinToString("/") ?: ""

            val multipart = call.receiveMultipart()

            var commit: String? = null
            val reports = mutableListOf<String>()
            while (true) {
                val part = multipart.readPart() ?: break

                when(part) {
                    is PartData.FormItem ->
                            if (part.name == "commit") {
                                commit = part.value
                            }
                    is PartData.FileItem ->
                            reports += part.streamProvider().bufferedReader().use { it.readText() }
                }
            }
            if (commit != null) print("Commit: ${commit}")
            print("Reports: $reports")

            ReportRepository.createReport(prefix)
            call.respondText("Prefix: $prefix", ContentType.Text.Plain)
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::module).start()
}
