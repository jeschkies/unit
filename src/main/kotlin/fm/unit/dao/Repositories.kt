package fm.unit.dao

import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate

interface Repositories {

    /**
     * Inserts a new repository.
     *
     * @param name The name of the new repository.
     * @return The id of the new repository.
     */
    @SqlUpdate("INSERT INTO repositories (name) VALUES (?)")
    @GetGeneratedKeys
    fun create(name: String): Int

    /**
     * Fetches a repository by name.
     *
     * @return Maybe the repository id or null.
     */
    @SqlQuery("SELECT repository_id from repositories WHERE name = ?")
    fun read(name: String): Int?
}
