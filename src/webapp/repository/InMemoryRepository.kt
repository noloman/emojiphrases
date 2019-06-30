package me.manulorenzo.webapp.repository

import me.manulorenzo.webapp.EmojiPhrase
import me.manulorenzo.webapp.Repository
import java.util.concurrent.atomic.AtomicInteger

class InMemoryRepository : Repository {
    private val idCounter = AtomicInteger()
    private val phrases = mutableListOf<EmojiPhrase>()

    override suspend fun add(phrase: EmojiPhrase): EmojiPhrase {
        if (phrases.contains(phrase)) {
            return phrases.find { it == phrase }!!
        }
        phrase.id = idCounter.incrementAndGet()
        phrases.add(phrase)
        return phrase
    }

    override suspend fun phrase(id: Int): EmojiPhrase = phrase(id.toString())

    override suspend fun phrase(id: String): EmojiPhrase =
        phrases.find { it.id.toString() == id } ?: throw IllegalArgumentException("No phrase found for id $id")


    override suspend fun phrases(): List<EmojiPhrase> = phrases.toList()

    override suspend fun remove(phrase: EmojiPhrase): Boolean {
        if (!phrases.contains(phrase)) throw IllegalArgumentException("No phrase found for id $phrase.id")
        return phrases.remove(phrase)
    }

    override suspend fun remove(id: Int): Boolean = phrases.remove(phrase(id))

    override suspend fun remove(id: String): Boolean = phrases.remove(phrase(id))

    override suspend fun clear() = phrases.clear()
}