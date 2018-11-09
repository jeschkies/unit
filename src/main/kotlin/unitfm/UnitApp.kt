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
        post("/") {

            call.respond(HttpStatusCode.Created)
        }
    }
}

fun main(args: Array<String>) {
    embeddedServer(Netty, 8080, module = Application::module).start()
}
