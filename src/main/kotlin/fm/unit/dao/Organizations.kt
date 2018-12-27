package fm.unit.dao

import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface Organizations {
    @SqlUpdate("INSERT INTO organizations (name) VALUES (?)")
    @GetGeneratedKeys
    fun insert(name: String): Int
}