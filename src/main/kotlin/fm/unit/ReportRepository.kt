package fm.unit

import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule
import fm.unit.data.Testsuite
import java.io.File
import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.fasterxml.jackson.module.jaxb.JaxbAnnotationModule
import com.fasterxml.jackson.module.kotlin.readValue
import mu.KotlinLogging
import fm.unit.model.Report

/**
 * A repository is a S3 bucket like structure. Reports are stored with keys.
 */
object ReportRepository {
    private val logger = KotlinLogging.logger {}

    val separator = '/'
    val reposFolder = File("reports")

    val xmlMapper: XmlMapper

    init {
        // Create XML object mapper with support for JAXB annotations.
        val xmlModule = JacksonXmlModule()
        xmlModule.setDefaultUseWrapper(false)
        xmlMapper = XmlMapper(xmlModule)
        val jaxbModule = JaxbAnnotationModule()
        xmlMapper.registerModule(jaxbModule)
    }
    /**
     * Creates a report folder for given prefix in repository.
     *
     * @param key The key of the report.
     */
    fun createReport(key: String): File {
        reposFolder.mkdirs()

        // TODO(karsten) throw error if report with prefix exists
        val reportFolder: File = File(reposFolder, key)
        reportFolder.mkdirs()
        return reportFolder
    }

    /**
     * Return all reports with given keyPrefix sorted by the key.
     *
     * A report is bundled after the last separator.
     *
     * E.g
     *  /unit/build/1/deadbeef and /unit/build/1/91859a7 are included in report "/unit/build/1"
     *  /unit/build/1/deadbeef and /unit/build/2/91859a7 are two different reports.
     */
    fun getReports(keyPrefix: String): List<Report> {
        logger.info {  "Getting reports for $keyPrefix" }

        val folderWithPrefix = File(reposFolder, keyPrefix)
        val reports = folderWithPrefix.listFiles { f -> f.isDirectory }
                .map { report : File ->
                    val name = report.toRelativeString(folderWithPrefix)
                    val testsuites: List<Testsuite> = report.listFiles().map { loadJUnitReport(it) }
                    val testcases = testsuites.flatMap { it.testcase }
                    Report(name, testcases)
                }

        return reports
    }

    fun loadJUnitReport(file: File): Testsuite {
        return xmlMapper.readValue<Testsuite>(file)
    }
}