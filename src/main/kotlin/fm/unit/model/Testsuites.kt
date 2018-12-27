package fm.unit.model

data class Testsuite(val report_id: Int, val filename: String)
data class Payload(val load: String)
data class TestsuiteSummary(val tests: Int, val errors: Int)