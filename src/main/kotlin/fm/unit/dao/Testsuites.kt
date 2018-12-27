package fm.unit.dao

import fm.unit.model.Payload
import fm.unit.model.Testsuite
import fm.unit.model.TestsuiteSummary
import org.jdbi.v3.core.argument.AbstractArgumentFactory
import org.jdbi.v3.core.argument.Argument
import org.jdbi.v3.core.config.ConfigRegistry
import org.jdbi.v3.core.statement.StatementContext
import org.jdbi.v3.sqlobject.customizer.*
import org.jdbi.v3.sqlobject.statement.SqlQuery
import org.jdbi.v3.sqlobject.statement.SqlUpdate
import org.postgresql.util.PGobject
import java.sql.PreparedStatement
import java.sql.Types


object PayloadArgumentFactory : AbstractArgumentFactory<Payload>(Types.OTHER) {
    class PayloadArgument(val value: Payload) : Argument {
        override fun apply(position: Int, statement: PreparedStatement, ctx: StatementContext?) {
            val xmlObject = PGobject()
            xmlObject.type = "xml"
            xmlObject.value = value.load
            statement.setObject(position, xmlObject)
        }
    }

    override fun build(value: Payload, config: ConfigRegistry): Argument {
        return PayloadArgument(value)
    }
}

interface Testsuites {
    @SqlUpdate("""
        INSERT INTO testsuites (report_id, filename, payload)
        VALUES (:report_id, :testsuite.filename, :testsuite.payload)
    """)
    fun insert(@Bind("report_id") report_id: Int, @BindBean("testsuite") testsuite: Testsuite)

    @SqlQuery("""
        SELECT (xpath('count(//testcase)', payload))[1] AS tests,
               (xpath('count(//failure)', payload))[1] AS errors
        FROM testsuites
    """)
    fun summaries(): List<TestsuiteSummary>
}