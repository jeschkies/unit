package fm.unit.dao

import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface Organizations {
    /**
     * Insert a new organization.
     *
     * @param name The name of the organization.
     * @return The id of the inserted organization.
     */
    @SqlUpdate("INSERT INTO organizations (name) VALUES (?)")
    @GetGeneratedKeys
    fun insert(name: String): Int

    /**
     * Fetches an organization by name.
     *
     * @return Maybe the organization id or null.
     */
    @SqlQuery("SELECT organization_id from organizations WHERE name = ?")
    fun get(name: String): Int?
}