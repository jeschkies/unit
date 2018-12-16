package fm.unit.model

import mu.KotlinLogging
import fm.unit.model.Report

/**
 * Summary of the project's* testsuites.
 *
 * * aka org/repo.
 */
data class ProjectSummary(val reports: List<Report>)
