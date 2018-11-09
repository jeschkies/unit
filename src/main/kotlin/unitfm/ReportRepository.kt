package unitfm

import java.io.File


object ReportRepository {
    val reposFolder = File("reports")

    fun createReport(prefix: String) {
        reposFolder.mkdirs()

        // TODO(karsten) throw error if report with prefix exists
        val reportFolder = File(reposFolder, prefix).mkdirs()
    }
}