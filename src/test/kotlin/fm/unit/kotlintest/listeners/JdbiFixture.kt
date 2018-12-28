package fm.unit.kotlintest.listeners

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import fm.unit.dao.PayloadArgumentFactory
import io.kotlintest.Description
import io.kotlintest.Spec
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
class JdbiFixture : TestListener {
    val builder = EmbeddedPostgres.builder()
    val epg: EmbeddedPostgres = builder.start()
    val dataSource = epg.postgresDatabase
    val flyway = Flyway.configure().dataSource(dataSource).load()
    val jdbi: Jdbi = Jdbi.create(dataSource)

    override fun beforeSpec(description: Description, spec: Spec) {
        flyway.migrate()
        jdbi.installPlugins()
        jdbi.registerArgument(PayloadArgumentFactory)

        super.beforeSpec(description, spec)
    }

    override fun afterSpec(description: Description, spec: Spec) {
        try {
            epg.close()
        } catch (e: IOException) {
            throw AssertionError(e)
        }

        super.afterSpec(description, spec)
    }
}