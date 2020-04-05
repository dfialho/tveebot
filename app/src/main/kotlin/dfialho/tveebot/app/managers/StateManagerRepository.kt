package dfialho.tveebot.app.managers

import dfialho.tveebot.app.api.models.State
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class StateManagerRepository(private val db: Database) : StateManager {

    companion object {
        private val DEFAULT_STATE = State.FOUND
    }

    private object States : Table() {
        val id = varchar("id", length = 128).primaryKey()
        val state = enumeration("state", State::class.java).default(DEFAULT_STATE)
    }

    init {
        transaction(db) {
            SchemaUtils.create(States)
        }
    }

    override fun get(id: String): State {

        return transaction(db) {

            States.select { States.id eq id }
                .map { it[States.state] }
                .firstOrNull()
                ?: DEFAULT_STATE
        }
    }

    override fun set(id: String, state: State): Unit = transaction(db) {

        val exists = States.insertIgnore {
            it[this.id] = id
            it[this.state] = state
        }.isIgnore

        if (exists) {
            States.update({ States.id eq id }) {
                it[this.id] = id
                it[this.state] = state
            }
        }
    }
}