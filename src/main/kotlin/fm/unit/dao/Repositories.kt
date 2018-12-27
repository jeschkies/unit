package fm.unit.dao

import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface Repositories {
    @SqlUpdate("INSERT INTO repositories (name) VALUES (?)")
    @GetGeneratedKeys
    fun insert(name: String): Int
}
