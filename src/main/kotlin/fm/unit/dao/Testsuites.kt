package fm.unit.dao

import fm.unit.model.Testsuite
import org.jdbi.v3.core.argument.AbstractArgumentFactory
import org.jdbi.v3.core.argument.Argument
import org.jdbi.v3.core.config.ConfigRegistry
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.customizer.*
import org.jdbi.v3.sqlobject.statement.GetGeneratedKeys
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import java.sql.Types


object PayloadArgumentFactory : AbstractArgumentFactory<Testsuite.Payload>(Types.OTHER) {
    class PayloadArgument(val value: Testsuite.Payload) : Argument {
        override fun apply(position: Int, statement: PreparedStatement, ctx: StatementContext?) {
            val xmlObject = PGobject()
            xmlObject.type = "xml"
            xmlObject.value = value.load
            statement.setObject(position, xmlObject)
        }
    }

    override fun build(value: Testsuite.Payload, config: ConfigRegistry): Argument {
        return PayloadArgument(value)
    }
}

interface Testsuites {

    /**
     * Insert a testsuite, ie JUnit XML file, for a report.
     *
     * @param report_id The report this testsuite is for.
     * @param testsuite The testsuite JUnit [[fm.unit.model.Testsuite.Payload]].
     * @return The id of the inserted testsuite.
     */
    @SqlUpdate("""
        INSERT INTO testsuites (report_id, filename, payload)
        VALUES (:report_id, :testsuite.filename, :testsuite.payload)
    """)
    @GetGeneratedKeys
    fun create(@Bind("report_id") report_id: Int, @BindBean("testsuite") testsuite: Testsuite): Int

    /**
     * Lists all testsuites.
     *
     * @return A summary of each testsuite.
     */
    @SqlQuery("""
        SELECT (xpath('count(//testcase)', payload))[1] AS tests,
               (xpath('count(//failure)', payload))[1] AS errors
        FROM testsuites
    """)
    fun summaries(): List<Testsuite.Summary>
}