package fm.unit.kotlintest.listeners

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import io.kotlintest.Description
import io.kotlintest.TestResult
import io.kotlintest.extensions.TestListener
import org.flywaydb.core.Flyway
import org.jdbi.v3.core.Jdbi
import java.io.IOException

/**
 * A Kotlintest listener that initializes a PostgreSQL database with migration.
 *
 * It provide a Jdbi instance.
 */
class JdbiLstener : TestListener {
    val builder = EmbeddedPostgres.builder()
    val epg: EmbeddedPostgres = builder.start()
    val dataSource = epg.postgresDatabase
    val flyway = Flyway.configure().dataSource(dataSource).load()
    val jdbi: Jdbi = Jdbi.create(dataSource)

    override fun beforeTest(description: Description): Unit {
        flyway.migrate()
        jdbi.installPlugins()
    }

    override fun afterTest(description: Description, result: TestResult) {
        try {
            epg.close()
        } catch (e: IOException) {
            throw AssertionError(e)
        }

        super.afterTest(description, result)
    }

}