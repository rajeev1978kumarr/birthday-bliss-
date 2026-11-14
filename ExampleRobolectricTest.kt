package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.BirthdayCard
import com.example.data.model.CardThemeStyle
import com.example.data.model.QuizCodec
import com.example.data.model.QuizQuestionItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `read app name from context`() {
        val appName = context.getString(R.string.app_name)
        assertEquals("Birthday Bliss", appName)
    }

    @Test
    fun `quiz codec and card helpers resolve cleanly`() {
        val questions = listOf(
            QuizQuestionItem(
                id = "q1",
                question = "Favorite dessert?",
                options = listOf("Gulab Jamun", "Chocolate Cake"),
                correctIndex = 1,
                revealNote = "Always chocolate cake!",
            )
        )
        val encoded = QuizCodec.encode(questions)
        val decoded = QuizCodec.decode(encoded)
        assertEquals(1, decoded.size)
        assertEquals("Favorite dessert?", decoded.first().question)

        val card = BirthdayCard(
            id = "card_1",
            recipientName = "Aarav",
            senderName = "Priya",
            milestone = "25th",
            greetingTitle = "जन्मदिन मुबारक!",
            message = "Wishing you endless joy!",
            themeStyle = CardThemeStyle.BERRY_BASH.name,
            letterBody = "Tumhari dosti sabse anmol hai.",
            giftSurprise = "Dinner Treat on Me!",
            quizQuestions = questions,
        )
        assertEquals(CardThemeStyle.BERRY_BASH, card.resolveTheme())
        assertTrue(card.hasLetter())
        assertTrue(card.hasGiftSurprise())
        assertEquals(1, card.resolveQuizQuestions().size)
    }
}
