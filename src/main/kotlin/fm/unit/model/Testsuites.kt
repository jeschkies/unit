package fm.unit.model

data class Payload(val load: String)
data class Testsuite(val filename: String, val payload: Payload)
data class TestsuiteSummary(val tests: Int, val errors: Int)