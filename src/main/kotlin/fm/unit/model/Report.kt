package fm.unit.model

/**
 * A report belongs to a build of a repo in an organization.
 */
data class Report(val prefix: String, val testsuites: List<TestsuiteSummary>) {

    val errors: Int = testsuites.sumBy { it.errors }
}
