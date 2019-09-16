package me.manulorenzo.webapp.repository

import me.manulorenzo.webapp.model.EmojiPhrase
import me.manulorenzo.webapp.model.EmojiPhrases
import me.manulorenzo.webapp.model.User
import me.manulorenzo.webapp.model.Users
import me.manulorenzo.webapp.repository.DatabaseFactory.dbQuery
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class EmojiphrasesRepository : Repository {
    override suspend fun getUserById(userId: String): User? =
        Users.select { Users.id.eq(userId) }.map {
            User(
                userId,
                it[Users.email],
                it[Users.displayName],
                it[Users.passwordHash]
            )
        }.singleOrNull()

    override suspend fun add(userId: String, emojiValue: String, phraseValue: String) {
        transaction {
            EmojiPhrases.insert {
                it[user] = userId
                it[emoji] = emojiValue
                it[phrase] = phraseValue
            }
        }
    }

    override suspend fun phrase(id: Int): EmojiPhrase? = dbQuery {
        EmojiPhrases.select { EmojiPhrases.id eq id }.mapNotNull { toEmojiPhrase(it) }.singleOrNull()
    }

    override suspend fun phrase(id: String): EmojiPhrase? = phrase(id.toInt())

    override suspend fun phrases(): List<EmojiPhrase> = dbQuery {
        EmojiPhrases.selectAll().map { toEmojiPhrase(it) }
    }

    override suspend fun remove(id: Int): Boolean {
        requireNotNull(phrase(id)) { "No phrase found for id $id." }
        return dbQuery { EmojiPhrases.deleteWhere { EmojiPhrases.id eq id } } > 0
    }

    override suspend fun remove(id: String): Boolean = remove(id.toInt())


    override suspend fun clear() {
        EmojiPhrases.deleteAll()
    }

    private fun toEmojiPhrase(row: ResultRow): EmojiPhrase =
        EmojiPhrase(
            id = row[EmojiPhrases.id].value,
            userId = row[EmojiPhrases.user],
            emoji = row[EmojiPhrases.emoji],
            phrase = row[EmojiPhrases.phrase]
        )

    private fun toUser(row: ResultRow): User = User(
        userId = row[Users.id],
        email = row[Users.email],
        displayName = row[Users.displayName],
        passwordHash = row[Users.passwordHash]
    )

    override suspend fun user(userId: String, hash: String?): User? {
        val user: User? = dbQuery { Users.select { Users.id eq userId }.mapNotNull { toUser(it) }.singleOrNull() }
        return when (hash) {
            user?.passwordHash, null -> user
            else -> null
        }
    }

    override suspend fun userByEmail(email: String): User? =
        Users.select { Users.email eq email }
            .map {
                User(
                    it[Users.id],
                    it[Users.email],
                    it[Users.displayName],
                    it[Users.passwordHash]
                )
            }.singleOrNull()

    override suspend fun createUser(user: User) = dbQuery {
        Users.insert {
            it[id] = user.userId
            it[email] = user.email
            it[displayName] = user.displayName
            it[passwordHash] = user.passwordHash
        }
        Unit
    }
}