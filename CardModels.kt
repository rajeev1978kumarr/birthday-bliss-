package com.example.data.model

import androidx.annotation.DrawableRes
import androidx.compose.ui.graphics.Color
import com.example.R
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

enum class CardThemeStyle(
    val displayName: String,
    val gradientStart: Color,
    val gradientEnd: Color,
    val accentColor: Color,
    val textColor: Color,
    val subtitleColor: Color,
    val isDarkCard: Boolean,
    @DrawableRes val backgroundDrawable: Int? = null,
) {
    GOLDEN_CONFETTI(
        displayName = "Botanical Gold",
        gradientStart = Color(0xFFFFF8EE),
        gradientEnd = Color(0xFFFFE7D1),
        accentColor = Color(0xFFD81B60),
        textColor = Color(0xFF2D1622),
        subtitleColor = Color(0xFF704B5A),
        isDarkCard = false,
        backgroundDrawable = R.drawable.img_card_template_floral,
    ),
    BERRY_BASH(
        displayName = "Party Streamers",
        gradientStart = Color(0xFFFFF5F7),
        gradientEnd = Color(0xFFFFE0EA),
        accentColor = Color(0xFF0D9488),
        textColor = Color(0xFF2B1420),
        subtitleColor = Color(0xFF6B4657),
        isDarkCard = false,
        backgroundDrawable = R.drawable.img_card_template_party,
    ),
    MIDNIGHT_SPARKLER(
        displayName = "Midnight Sparkler",
        gradientStart = Color(0xFF1D1128),
        gradientEnd = Color(0xFF3A1638),
        accentColor = Color(0xFFFCD34D),
        textColor = Color(0xFFFFF7FB),
        subtitleColor = Color(0xFFE7C7D8),
        isDarkCard = true,
        backgroundDrawable = null,
    ),
    CORAL_SUNSET(
        displayName = "Coral Sunset",
        gradientStart = Color(0xFFFF758C),
        gradientEnd = Color(0xFFFFB199),
        accentColor = Color(0xFFFFF8E7),
        textColor = Color(0xFFFFFFFF),
        subtitleColor = Color(0xFFFFEFEA),
        isDarkCard = true,
        backgroundDrawable = null,
    ),
    ROYAL_MARIGOLD(
        displayName = "Royal Marigold",
        gradientStart = Color(0xFFFFF4D2),
        gradientEnd = Color(0xFFFFD699),
        accentColor = Color(0xFFB42318),
        textColor = Color(0xFF3A1E04),
        subtitleColor = Color(0xFF7A4B16),
        isDarkCard = false,
        backgroundDrawable = null,
    ),
}

enum class PhotoFrameShape(val displayName: String) {
    CIRCLE("Circle"),
    POLAROID("Polaroid"),
    ARCH("Arch"),
}

enum class StickerAccent(val label: String, val emoji: String) {
    CAKE("Cake", "🎂"),
    BALLOONS("Balloons", "🎈"),
    SPARKLES("Sparkles", "✨"),
    GIFT("Gift", "🎁"),
    CROWN("Crown", "👑"),
    CONFETTI("Party", "🎉"),
}

data class WishPreset(
    val id: String,
    val languageTag: String,
    val title: String,
    val greeting: String,
    val message: String,
)

object BirthdayWishPresets {
    val all: List<WishPreset> = listOf(
        WishPreset(
            id = "en_heartfelt",
            languageTag = "English",
            title = "Heartfelt",
            greeting = "Happy Birthday!",
            message = "May your year ahead glow with laughter, unforgettable adventures, and every dream your heart quietly holds.",
        ),
        WishPreset(
            id = "hi_warm",
            languageTag = "हिंदी",
            title = "शुभकामनाएँ",
            greeting = "जन्मदिन मुबारक!",
            message = "ईश्वर करे आपका आने वाला हर दिन खुशियों, सेहत और सफलता से भरा रहे। मुस्कुराते रहिए और हर पल का जश्न मनाइए!",
        ),
        WishPreset(
            id = "hinglish_party",
            languageTag = "Hinglish",
            title = "Party Vibes",
            greeting = "Happy B'Day Superstar!",
            message = "Aaj ka din sirf tumhara hai! Cake khao, doston ke saath dhoom machao aur zindagi ke har pal ko khul ke jeeyo!",
        ),
        WishPreset(
            id = "en_funny",
            languageTag = "English",
            title = "Playful",
            greeting = "Level Up Today!",
            message = "You're not getting older, you're just unlocking vintage bonus XP! Don't count the candles — just enjoy the glow.",
        ),
        WishPreset(
            id = "hi_family",
            languageTag = "हिंदी",
            title = "स्नेह भरा संदेश",
            greeting = "बहुत-बहुत बधाई!",
            message = "आप हमारे जीवन की सबसे प्यारी रोशनी हैं। जन्मदिन के इस खास अवसर पर ढेर सारा प्यार और अनगिनत दुआएँ।",
        ),
        WishPreset(
            id = "hinglish_friend",
            languageTag = "Hinglish",
            title = "Bestie Special",
            greeting = "Janamdin Mubarak Yaar!",
            message = "Mere sabse best dost ko happy birthday! Hamesha aise hi mast raho, aur party toh abhi baaqi hai!",
        ),
    )
}

@IgnoreExtraProperties
data class QuizQuestionItem(
    val id: String = UUID.randomUUID().toString(),
    val question: String = "",
    val options: List<String> = emptyList(),
    val correctIndex: Int = 0,
    val revealNote: String = "",
)

@IgnoreExtraProperties
data class BirthdayCard(
    @DocumentId val id: String = "",
    val userId: String = "",
    val recipientName: String = "",
    val senderName: String = "",
    val milestone: String = "",
    val greetingTitle: String = "",
    val message: String = "",
    val themeStyle: String = CardThemeStyle.GOLDEN_CONFETTI.name,
    val photoPath: String? = null,
    val photoFrameShape: String = PhotoFrameShape.POLAROID.name,
    val stickerAccent: String = StickerAccent.CAKE.name,
    val letterSalutation: String = "",
    val letterBody: String = "",
    val letterClosing: String = "",
    val giftSurprise: String = "",
    val quizQuestions: List<QuizQuestionItem> = emptyList(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
) {
    fun resolveTheme(): CardThemeStyle =
        runCatching { CardThemeStyle.valueOf(themeStyle) }.getOrDefault(CardThemeStyle.GOLDEN_CONFETTI)

    fun resolveFrameShape(): PhotoFrameShape =
        runCatching { PhotoFrameShape.valueOf(photoFrameShape) }.getOrDefault(PhotoFrameShape.POLAROID)

    fun resolveSticker(): StickerAccent =
        runCatching { StickerAccent.valueOf(stickerAccent) }.getOrDefault(StickerAccent.CAKE)

    fun resolveQuizQuestions(): List<QuizQuestionItem> = quizQuestions

    fun hasLetter(): Boolean = letterBody.isNotBlank()

    fun hasGiftSurprise(): Boolean = giftSurprise.isNotBlank()
}

@IgnoreExtraProperties
data class GameScore(
    @DocumentId val id: String = "",
    val userId: String = "",
    val gameId: String = "",
    val bestScore: Long = 0L,
    val gamesPlayed: Long = 0L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null,
)

object QuizCodec {
    fun encode(items: List<QuizQuestionItem>): String {
        val arr = JSONArray()
        items.forEach { item ->
            val optsArr = JSONArray()
            item.options.forEach { optsArr.put(it) }
            val obj = JSONObject().apply {
                put("id", item.id)
                put("question", item.question)
                put("options", optsArr)
                put("correctIndex", item.correctIndex)
                put("revealNote", item.revealNote)
            }
            arr.put(obj)
        }
        return arr.toString()
    }

    fun decode(json: String?): List<QuizQuestionItem> {
        if (json.isNullOrBlank()) return emptyList()
        return runCatching {
            val arr = JSONArray(json)
            buildList {
                for (i in 0 until arr.length()) {
                    val obj = arr.getJSONObject(i)
                    val optsJson = obj.optJSONArray("options") ?: JSONArray()
                    val opts = buildList {
                        for (j in 0 until optsJson.length()) {
                            add(optsJson.optString(j))
                        }
                    }.filter { it.isNotBlank() }
                    val q = obj.optString("question").trim()
                    if (q.isNotEmpty() && opts.size >= 2) {
                        add(
                            QuizQuestionItem(
                                id = obj.optString("id").ifBlank { UUID.randomUUID().toString() },
                                question = q,
                                options = opts,
                                correctIndex = obj.optInt("correctIndex", 0).coerceIn(0, opts.lastIndex),
                                revealNote = obj.optString("revealNote"),
                            )
                        )
                    }
                }
            }
        }.getOrDefault(emptyList())
    }
}

object StarterQuizTemplates {
    val starterQuestions: List<QuizQuestionItem> = listOf(
        QuizQuestionItem(
            id = "q_fav_treat",
            question = "Aaj birthday par sabse pehle kya treat chahiye?",
            options = listOf("Chocolate Cake 🍫", "Ice Cream Party 🍨", "Long Drive & Coffee ☕", "All of the above! 🎉"),
            correctIndex = 3,
            revealNote = "Bilkul sahi! Birthday star ki har wish poori hogi aaj!",
        ),
        QuizQuestionItem(
            id = "q_smile_reason",
            question = "Humari sabse best quality kya hai jab hum saath hote hain?",
            options = listOf("Non-stop hasi mazak 😄", "Endless food cravings 🍕", "Late night talks 🌙"),
            correctIndex = 0,
            revealNote = "Tumhare saath bitaya har pal ek favourite memory hai!",
        ),
        QuizQuestionItem(
            id = "q_special_wish",
            question = "Guess karo maine tumhare liye kya secret wish maangi hai?",
            options = listOf("Health & Happiness ✨", "Big Success Ahead 🚀", "Dher saari khushiyan 💖"),
            correctIndex = 2,
            revealNote = "Sach toh ye hai — sab kuch! Health, success aur dher saara pyaar.",
        ),
    )
}

data class LetterStarterTemplate(
    val id: String,
    val label: String,
    val salutation: String,
    val body: String,
    val closing: String,
)

object LetterTemplates {
    val all: List<LetterStarterTemplate> = listOf(
        LetterStarterTemplate(
            id = "letter_hinglish",
            label = "Hinglish Dil Se",
            salutation = "Mere sabse khaas insaan,",
            body = "Aaj tumhara birthday hai, aur main bas itna kehna chahta/chahti hoon ki tumhari presence meri zindagi ko har din thoda aur bright banati hai.\n\nHumne saath mein jitni bhi hasi, masti aur chhoti-badi memories banayi hain, woh sab mere dil ke sabse kareeb hain. Dua hai ki aane wala saal tumhare liye nayi khushiyan, sukoon aur har woh cheez laaye jo tum deserve karte ho!",
            closing = "Hamesha tumhare saath,",
        ),
        LetterStarterTemplate(
            id = "letter_english",
            label = "Heartfelt English",
            salutation = "Dearest Birthday Star,",
            body = "Every year with you in my life feels like a gift worth celebrating. Thank you for the warmth, laughter, and kindness you bring to everyone around you.\n\nAs you step into a brand-new chapter today, remember how deeply you are loved and appreciated. May your days ahead be filled with peace, bold adventures, and dreams coming true.",
            closing = "With all my love,",
        ),
        LetterStarterTemplate(
            id = "letter_hindi",
            label = "हिंदी पत्र",
            salutation = "प्रिय जन्मदिन के सितारे,",
            body = "आज के इस पावन दिन पर दिल की गहराइयों से जन्मदिन की ढेर सारी शुभकामनाएँ। आपकी मुस्कान और आपका स्नेह हर किसी के जीवन में उजाला भर देता है।\n\nईश्वर से यही प्रार्थना है कि आने वाला वर्ष आपके लिए उत्तम स्वास्थ्य, अपार सफलता और अनगिनत खुशियाँ लेकर आए। हमेशा ऐसे ही मुस्कुराते रहिए।",
            closing = "सदैव आपका शुभचिंतक,",
        ),
    )

    val quickGiftIdeas: List<String> = listOf(
        "Dinner Treat on Me! 🍕",
        "Coffee & Dessert Date ☕",
        "Movie Night + Popcorn 🎬",
        "Mystery Gift Waiting for You 🎁",
        "Weekend Ice Cream Run 🍨",
    )
}

object PartyGameIds {
    const val BALLOON_POP = "balloon_pop"
    const val CANDLE_BLOW = "candle_blow"
    const val PINATA_SMASH = "pinata_smash"
}
