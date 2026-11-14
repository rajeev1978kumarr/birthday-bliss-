package com.example.data.repository

import com.example.base.FirestoreEmulatorTestBase
import com.example.data.model.BirthdayCard
import com.example.data.model.CardThemeStyle
import com.example.data.model.PartyGameIds
import com.example.data.model.QuizQuestionItem
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.UUID

class BirthdayRepositoryRuleTest : FirestoreEmulatorTestBase() {

    @Test
    fun saveAndObserveBirthdayCard_authenticatedOwner_roundTripsQuizLetterAndGift() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = BirthdayRepository(firestore)

        val card = BirthdayCard(
            recipientName = "Priya",
            senderName = "Aarav",
            milestone = "25th",
            greetingTitle = "जन्मदिन मुबारक!",
            message = "Wishing you endless joy!",
            themeStyle = CardThemeStyle.BERRY_BASH.name,
            letterSalutation = "Mere dost,",
            letterBody = "Tumhari dosti sabse anmol hai.",
            letterClosing = "Pyaar se,",
            giftSurprise = "Dinner Treat on Me!",
            quizQuestions = listOf(
                QuizQuestionItem(
                    id = "q_${UUID.randomUUID()}",
                    question = "Favorite cake?",
                    options = listOf("Chocolate", "Vanilla"),
                    correctIndex = 0,
                    revealNote = "Always chocolate!",
                )
            ),
        )

        val createResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.saveCard(card) }
        assertTrue("Create card failed: ${createResult.exceptionOrNull()}", createResult.isSuccess)
        val cardId = createResult.getOrThrow()
        assertTrue(cardId.isNotEmpty())

        val cards = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeCards(aliceUid).first { list -> list.any { it.id == cardId } }
        }
        val saved = cards.first { it.id == cardId }
        assertEquals("Priya", saved.recipientName)
        assertEquals("Dinner Treat on Me!", saved.giftSurprise)
        assertEquals(1, saved.quizQuestions.size)
        assertEquals("Favorite cake?", saved.quizQuestions.first().question)

        // Update existing card
        val updateResult = withTimeout(DEFAULT_TIMEOUT_MS) {
            repository.saveCard(saved.copy(message = "Updated birthday wish!"))
        }
        assertTrue("Update card failed: ${updateResult.exceptionOrNull()}", updateResult.isSuccess)

        val fetched = withTimeout(DEFAULT_TIMEOUT_MS) { repository.getCard(aliceUid, cardId).getOrThrow() }
        assertEquals("Updated birthday wish!", fetched?.message)

        val deleteResult = withTimeout(DEFAULT_TIMEOUT_MS) { repository.deleteCard(saved) }
        assertTrue("Delete card failed: ${deleteResult.exceptionOrNull()}", deleteResult.isSuccess)
    }

    @Test
    fun recordGameScore_authenticatedOwner_createsAndUpdatesBestScore() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val repository = BirthdayRepository(firestore)
        val gameId = "${PartyGameIds.BALLOON_POP}_${UUID.randomUUID().toString().take(8)}"

        val r1 = withTimeout(DEFAULT_TIMEOUT_MS) { repository.recordGameScore(gameId, 30) }
        assertTrue("First score failed: ${r1.exceptionOrNull()}", r1.isSuccess)

        val r2 = withTimeout(DEFAULT_TIMEOUT_MS) { repository.recordGameScore(gameId, 15) }
        assertTrue("Second score failed: ${r2.exceptionOrNull()}", r2.isSuccess)

        val r3 = withTimeout(DEFAULT_TIMEOUT_MS) { repository.recordGameScore(gameId, 48) }
        assertTrue("Third score failed: ${r3.exceptionOrNull()}", r3.isSuccess)

        val scores = withTimeout(FLOW_TIMEOUT_MS) {
            repository.observeAllGameScores(aliceUid).first { list ->
                list.any { it.gameId == gameId && it.gamesPlayed == 3L }
            }
        }
        val match = scores.first { it.gameId == gameId }
        assertEquals(48L, match.bestScore)
        assertEquals(3L, match.gamesPlayed)
    }

    @Test
    fun getCard_crossUserAccess_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        val aliceRepo = BirthdayRepository(firestore)
        val cardId = withTimeout(DEFAULT_TIMEOUT_MS) {
            aliceRepo.saveCard(
                BirthdayCard(
                    recipientName = "Mom",
                    greetingTitle = "Happy Birthday!",
                    message = "Best Mom ever!",
                )
            ).getOrThrow()
        }

        signInTestUser(BOB_EMAIL)
        val bobRepo = BirthdayRepository(firestore)
        val crossResult = withTimeout(DEFAULT_TIMEOUT_MS) { bobRepo.getCard(aliceUid, cardId) }
        assertTrue(crossResult.isFailure)
        val firestoreEx = crossResult.exceptionOrNull()?.unwrapFirestoreException()
        assertNotNull("Expected FirebaseFirestoreException", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    @Test
    fun observeCards_unauthenticatedCaller_failsWithPermissionDenied() = runBlocking {
        val aliceUid = signInTestUser(ALICE_EMAIL)
        auth.signOut()
        val repository = BirthdayRepository(firestore)

        var caught: Throwable? = null
        try {
            withTimeout(FLOW_TIMEOUT_MS) {
                repository.observeCards(aliceUid).first()
            }
        } catch (e: Exception) {
            caught = e
        }

        assertNotNull("Expected PERMISSION_DENIED exception when signed out", caught)
        val firestoreEx = caught?.unwrapFirestoreException()
        assertNotNull("Expected nested FirebaseFirestoreException, got $caught", firestoreEx)
        assertEquals(FirebaseFirestoreException.Code.PERMISSION_DENIED, firestoreEx?.code)
    }

    private companion object {
        const val ALICE_EMAIL = "alice_bliss@test.com"
        const val BOB_EMAIL = "bob_bliss@test.com"
        const val DEFAULT_TIMEOUT_MS = 5000L
        const val FLOW_TIMEOUT_MS = 3000L
    }
}
