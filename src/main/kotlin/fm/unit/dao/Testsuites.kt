package fm.unit.dao

import org.jdbi.v3.sqlobject.statement.SqlUpdate

data class Testsuite(val repot_id: Int, val filename: String)

interface Testsuites {
    @SqlUpdate("insert into testsuites (commit, key) values (:report.commit, :report.key)")
    fun insert(testsuite: Testsuite, payload: Object)

    fun testsuites(): List<Testsuite>
}