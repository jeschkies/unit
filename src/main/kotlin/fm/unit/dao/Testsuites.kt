package fm.unit.dao

import org.jdbi.v3.sqlobject.statement.SqlUpdate

data class Testsuite(val report_id: Int, val filename: String)

interface Testsuites {
    @SqlUpdate("""
        INSERT INTO testsuites (report_id, filename, payload)
        VALUES (:testsuite.report_id, :testsuite.filename, XMLPARSE(:payload))
    """)
    fun insert(testsuite: Testsuite, payload: String)
}