package unitfm.model

import unitfm.data.Testcase

data class Report(val name: String, val tests: List<Testcase>) {

    val errors: Int = tests.count{ it.error.isNotEmpty() || it.failure.isNotEmpty() }
}
