package fm.unit.model

import com.sun.org.apache.xpath.internal.operations.Bool
import org.jdbi.v3.core.mapper.Nested

/**
 * A report belongs to a build of a repo in an organization.
 */
data class Report(val prefix: String) {


    /**
     * A summary of the last n reports.
     */
    data class Summary(val report_id: Int, @Nested("ts") val testsuiteSummary: Testsuite.Summary) {

        /**
         * Indicates whether all testsuites succeeded.
         */
        val successful: Boolean = testsuiteSummary.errors == 0
    }
}
