package fm.unit.model

import fm.unit.model.Report

object Project {

    /**
     * A summary of the last n reports.
     */
    data class Summary(val reports: List<Report.Summary>)
}
