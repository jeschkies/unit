package unitfm

import mu.KotlinLogging

data class ReportSummary(val name: String, val tests: Int, val errors: Int) {

    /**
     * Constructs a summary for a report.
     *
     * @param report the report this instance summarizes.
     */
    constructor(report: Report): this(report.name, report.tests.size, report.errors)
}

/**
 * Summary of the project reports.
 */
class Summary(reports: List<Report>) {

    private val logger = KotlinLogging.logger {}

    val reportSummaries: List<ReportSummary>

    init {
        this.reportSummaries = reports.map { ReportSummary(it) }
        logger.info { "Report summaries ${this.reportSummaries} for reports ${reports}"}
    }
}