package fm.unit.dao

import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface Organizations {
    @SqlUpdate("INSERT INTO organizations (name) VALUES (?)")
    fun insert(name: String)
}