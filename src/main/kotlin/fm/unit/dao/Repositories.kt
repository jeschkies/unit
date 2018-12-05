package fm.unit.dao

import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface Repositories {
    @SqlUpdate("INSERT INTO repositories (name) VALUES (?)")
    fun insert(name: String)
}
