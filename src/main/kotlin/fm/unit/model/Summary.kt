package fm.unit

import mu.KotlinLogging
import fm.unit.model.Report

/**
 * Summary of the project's testsuites.
 */
data class ProjectSummary(val reports: List<Report>)
