package fm.unit.model



/**
 * A testsuite corresponds to a JUnit XMl file. A [[fm.unit.model.Report]] can have multiple testsuites.
 *
 * @property filename The JUnit XMl file name
 * @property payload The string content of the file.
 */
data class Testsuite(val filename: String?, val payload: Payload) {

    /**
     * A wrapper of the JUnit XML content. It is used to ease the argument mapping for JDBI.
     */
    data class Payload(val load: String)

    /**
     * A testsuite summary is generated by *reading* a JUnit XML file from the database. It includes some analysis.
     *
     * @property tests The number of test cases in the file.
     * @property errors The number of errors reported.
     */
    data class Summary(val tests: Int, val errors: Int) {
        val successes: Int = tests - errors
    }
}