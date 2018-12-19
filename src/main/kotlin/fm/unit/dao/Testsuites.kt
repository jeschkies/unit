package fm.unit.dao

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


data class Testsuite(val report_id: Int, val filename: String)

data class Payload(val load: String)

data class TestsuiteSummary(val tests: Int, val errors: Int)

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
        VALUES (:testsuite.report_id, :testsuite.filename, :payload)
    """)
    fun insert(@BindBean("testsuite") testsuite: Testsuite, @Bind("payload") payload: Payload)

    @SqlQuery("""
        SELECT (xpath('count(//testcase)', payload))[1] AS tests,
               (xpath('count(//failure)', payload))[1] AS errors
        FROM testsuites
    """)
    fun summaries(): List<TestsuiteSummary>
}