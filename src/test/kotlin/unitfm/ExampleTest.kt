package unitfm

import unitfm.dao.Reports

import com.opentable.db.postgres.embedded.EmbeddedPostgres
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.specs.StringSpec
import org.jdbi.v3.sqlobject.kotlin.onDemand
import org.flywaydb.core.Flyway
import org.jdbi.v3.sqlobject.SqlObjectPlugin
import org.jdbi.v3.core.Jdbi
import unitfm.dao.Report





/*
class JdbiLstener : TestListener {
    val testMigration = Migration().withDefaultPath()
    val testJdbi = JdbiRule.embeddedPostgres().withPlugins().withMigration(testMigration)

    override fun beforeTest(description: Description): Unit {
        testJdbi.
    }

}*/

class ExampleTest : StringSpec() {
    // TODO(karsten): Make the rulse Kotlintest TestListeners
    // Setup database
    val builder = EmbeddedPostgres.builder()
    val epg: EmbeddedPostgres = autoClose(builder.start())
    val dataSource = epg.postgresDatabase

    init {
        "Reports DAO roundtrip" {
            val flyway = Flyway.configure().dataSource(dataSource).load()
            flyway.migrate()

            val jdbi = Jdbi.create(dataSource)
            jdbi.installPlugins()
            val dao = jdbi.onDemand<Reports>()

            dao.insert(Report(0, "deadbeaf", "/jeschkies/unit/1"))
            dao.insert(Report(0, "12345678", "/jeschkies/unit/2"))

            dao.reports() shouldHaveSize (2)
        }
    }
}