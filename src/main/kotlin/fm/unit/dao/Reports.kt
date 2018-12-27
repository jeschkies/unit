package fm.unit.dao

import org.jdbi.v3.core.mapper.Nested
import org.jdbi.v3.core.mapper.reflect.ColumnName
import org.jdbi.v3.sqlobject.customizer.BindBean
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

data class Report(
        @ColumnName("report_id") val id: Int,
        val organization_id: Int,
        val repository_id: Int,
        val commit_hash: String,
        val prefix: String)

interface Reports {
    @SqlUpdate("""
        INSERT INTO reports (organization_id, repository_id, commit_hash, prefix)
        VALUES (:organization_id, :repository_id, :commit_hash, :prefix)
    """)
    @GetGeneratedKeys
    fun insert(organization_id: Int, repository_id: Int, commit_hash: String, prefix: String): Int

    // TODO(karsten): join with testsuites.
    @SqlQuery("SELECT * FROM reports ORDER BY report_id")
    fun reports(): List<Report>
}