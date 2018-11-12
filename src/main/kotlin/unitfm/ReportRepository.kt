package unitfm

import unitfm.data.Testsuite
import java.io.File

data class Report(val name: String, val tests: List<Testsuite>)

/**
 * A repository is a S3 bucket like structure. Reports are stored with keys.
 */
object ReportRepository {
    val separator = '/'
    val reposFolder = File("reports")

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
     * E.g /unit/build/1/deadbeef and /unit/build/1/91859a7 are included in report "/unit/build/1".
     */
    fun getReports(keyPrefix: String): List<Report> {
        println("Getting reports")
        val reports = File(reposFolder, keyPrefix).walk().forEach { println(it) }
                //.filter { it.startsWith(keyPrefix) }.forEach { println(it) }
                //.map { it.toString() }
                //.groupBy {it.substringAfter(keyPrefix) }
                //.map {
                //    println("Group ${it.key}")
                //    Report(it.key, emptyList())
                //}

        //reports.forEach { println(it) }
        return emptyList()
    }
}