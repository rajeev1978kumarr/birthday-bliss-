package com.example.data.repository

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import com.example.R
import com.example.data.model.BirthdayCard
import com.example.data.model.GameScore
import com.example.data.remote.OperationType
import com.example.data.remote.handleFirestoreError
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class BirthdayRepository(
    private val db: FirebaseFirestore,
    private val appContext: Context? = null,
) {
    constructor(context: Context) : this(
        db = FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        ),
        appContext = context.applicationContext,
    )

    private val auth = Firebase.auth

    private fun requireUserId(): String =
        auth.currentUser?.uid
            ?: throw IllegalStateException("User must be signed in with Google before accessing Firestore.")

    private fun cardsCollection(userId: String) =
        db.collection("users").document(userId).collection("cards")

    private fun gameScoresCollection(userId: String) =
        db.collection("users").document(userId).collection("gameScores")

    fun observeCards(userId: String): Flow<List<BirthdayCard>> {
        val path = "users/$userId/cards"
        return cardsCollection(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    BirthdayCard::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                ).sortedByDescending { it.updatedAt ?: it.createdAt ?: Timestamp(0, 0) }
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    fun observeCard(userId: String, cardId: String): Flow<BirthdayCard?> {
        val docRef = cardsCollection(userId).document(cardId)
        return docRef
            .snapshots()
            .map { snapshot ->
                if (!snapshot.exists()) null
                else snapshot.toObject(
                    BirthdayCard::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.GET, docRef.path)
                }
                throw error
            }
    }

    suspend fun getCard(userId: String, cardId: String): Result<BirthdayCard?> {
        val docRef = cardsCollection(userId).document(cardId)
        return try {
            val snap = docRef.get().await()
            val card = if (!snap.exists()) null else snap.toObject(
                BirthdayCard::class.java,
                DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
            )
            Result.success(card)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.GET, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun saveCard(card: BirthdayCard): Result<String> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/unauthenticated/cards")
            return Result.failure(e)
        }

        val quizList = card.quizQuestions.take(25).map { q ->
            mapOf(
                "id" to q.id,
                "question" to q.question,
                "options" to q.options,
                "correctIndex" to q.correctIndex,
                "revealNote" to q.revealNote,
            )
        }

        return if (card.id.isBlank()) {
            val docRef = cardsCollection(uid).document()
            val payload = buildMap<String, Any> {
                put("userId", uid)
                put("recipientName", card.recipientName.trim().take(100))
                put("senderName", card.senderName.trim().take(100))
                put("milestone", card.milestone.trim().take(40))
                put("greetingTitle", card.greetingTitle.trim().ifBlank { "Happy Birthday!" }.take(120))
                put("message", card.message.trim().take(1000))
                put("themeStyle", card.themeStyle)
                card.photoPath?.trim()?.takeIf { it.isNotEmpty() }?.let {
                    put("photoPath", it.take(500))
                }
                put("photoFrameShape", card.photoFrameShape)
                put("stickerAccent", card.stickerAccent)
                put("letterSalutation", card.letterSalutation.trim().take(120))
                put("letterBody", card.letterBody.trim().take(4000))
                put("letterClosing", card.letterClosing.trim().take(120))
                put("giftSurprise", card.giftSurprise.trim().take(500))
                put("quizQuestions", quizList)
                put("createdAt", FieldValue.serverTimestamp())
                put("updatedAt", FieldValue.serverTimestamp())
            }
            try {
                docRef.set(payload).await()
                Result.success(docRef.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.CREATE, docRef.path)
                Result.failure(e)
            }
        } else {
            val docRef = cardsCollection(uid).document(card.id)
            val updates = mapOf<String, Any>(
                "recipientName" to card.recipientName.trim().take(100),
                "senderName" to card.senderName.trim().take(100),
                "milestone" to card.milestone.trim().take(40),
                "greetingTitle" to card.greetingTitle.trim().ifBlank { "Happy Birthday!" }.take(120),
                "message" to card.message.trim().take(1000),
                "themeStyle" to card.themeStyle,
                "photoPath" to (card.photoPath?.trim()?.takeIf { it.isNotEmpty() }?.take(500) ?: FieldValue.delete()),
                "photoFrameShape" to card.photoFrameShape,
                "stickerAccent" to card.stickerAccent,
                "letterSalutation" to card.letterSalutation.trim().take(120),
                "letterBody" to card.letterBody.trim().take(4000),
                "letterClosing" to card.letterClosing.trim().take(120),
                "giftSurprise" to card.giftSurprise.trim().take(500),
                "quizQuestions" to quizList,
                "updatedAt" to FieldValue.serverTimestamp(),
            )
            try {
                docRef.update(updates).await()
                Result.success(card.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                handleFirestoreError(e, OperationType.UPDATE, docRef.path)
                Result.failure(e)
            }
        }
    }

    suspend fun deleteCard(card: BirthdayCard): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, "users/unauthenticated/cards/${card.id}")
            return Result.failure(e)
        }
        val docRef = cardsCollection(uid).document(card.id)
        return try {
            docRef.delete().await()
            withContext(Dispatchers.IO) {
                card.photoPath?.let { path ->
                    runCatching { File(path).delete() }
                }
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docRef.path)
            Result.failure(e)
        }
    }

    fun observeAllGameScores(userId: String): Flow<List<GameScore>> {
        val path = "users/$userId/gameScores"
        return gameScoresCollection(userId)
            .snapshots()
            .map { snapshot ->
                snapshot.toObjects(
                    GameScore::class.java,
                    DocumentSnapshot.ServerTimestampBehavior.ESTIMATE,
                )
            }
            .catch { error ->
                val firestoreError = generateSequence(error) { it.cause }
                    .filterIsInstance<FirebaseFirestoreException>()
                    .firstOrNull()
                if (firestoreError == null && error is CancellationException) throw error
                val exceptionToLog = firestoreError ?: (error as? Exception)
                if (exceptionToLog != null && auth.currentUser?.uid == userId) {
                    handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                }
                throw error
            }
    }

    suspend fun recordGameScore(gameId: String, score: Int): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, "users/unauthenticated/gameScores/$gameId")
            return Result.failure(e)
        }
        val safeScore = score.coerceAtLeast(0)
        val docRef = gameScoresCollection(uid).document(gameId)
        return try {
            val snap = docRef.get().await()
            if (!snap.exists()) {
                val payload = mapOf(
                    "userId" to uid,
                    "gameId" to gameId,
                    "bestScore" to safeScore,
                    "gamesPlayed" to 1,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                docRef.set(payload).await()
            } else {
                val existingBest = (snap.getLong("bestScore") ?: 0L).toInt()
                val newBest = maxOf(existingBest, safeScore)
                val played = (snap.getLong("gamesPlayed") ?: 0L) + 1L
                val updates = mapOf(
                    "bestScore" to newBest,
                    "gamesPlayed" to played,
                    "updatedAt" to FieldValue.serverTimestamp(),
                )
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.WRITE, docRef.path)
            Result.failure(e)
        }
    }

    suspend fun persistPickedPhoto(uri: Uri): String? = withContext(Dispatchers.IO) {
        val ctx = appContext ?: return@withContext null
        runCatching {
            val bitmap = decodeScaledBitmap(ctx, uri, maxDimension = 1080) ?: return@withContext null
            val dir = File(ctx.filesDir, "card_photos").apply { mkdirs() }
            val outFile = File(dir, "photo_${UUID.randomUUID()}.jpg")
            FileOutputStream(outFile).use { stream ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 88, stream)
            }
            bitmap.recycle()
            outFile.absolutePath
        }.getOrNull()
    }

    private fun decodeScaledBitmap(ctx: Context, uri: Uri, maxDimension: Int): Bitmap? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val source = ImageDecoder.createSource(ctx.contentResolver, uri)
            return ImageDecoder.decodeBitmap(source) { decoder, info, _ ->
                decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                val width = info.size.width
                val height = info.size.height
                val largest = maxOf(width, height)
                if (largest > maxDimension) {
                    val scale = maxDimension.toFloat() / largest.toFloat()
                    decoder.setTargetSize(
                        (width * scale).toInt().coerceAtLeast(1),
                        (height * scale).toInt().coerceAtLeast(1),
                    )
                }
            }
        } else {
            val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
            ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, boundsOpts)
            }
            var sampleSize = 1
            while (maxOf(boundsOpts.outWidth, boundsOpts.outHeight) / sampleSize > maxDimension) {
                sampleSize *= 2
            }
            val decodeOpts = BitmapFactory.Options().apply { inSampleSize = sampleSize }
            return ctx.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, decodeOpts)
            }
        }
    }
}
