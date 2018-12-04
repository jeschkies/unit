package fm.unit.model

import fm.unit.data.Testcase

data class Report(val name: String, val tests: List<Testcase>) {

    val errors: Int = tests.count{ it.error.isNotEmpty() || it.failure.isNotEmpty() }
}
