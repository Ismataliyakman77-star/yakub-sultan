package com.example.data.repository

import android.content.Context
import androidx.room.Room
import com.example.BuildConfig
import com.example.data.api.Content
import com.example.data.api.GenerateContentRequest
import com.example.data.api.Part
import com.example.data.api.RetrofitClient
import com.example.data.db.AppDatabase
import com.example.data.db.MessageEntity
import kotlinx.coroutines.flow.Flow

class JarvisRepository(context: Context) {
    private val database = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "jarvis_database"
    ).build()

    private val dao = database.messageDao()

    fun getChatHistory(): Flow<List<MessageEntity>> = dao.getMessagesFlow()

    suspend fun addMessage(text: String, sender: String) {
        val entity = MessageEntity(text = text, sender = sender)
        dao.insertMessage(entity)
    }

    suspend fun clearHistory() {
        dao.deleteAllMessages()
    }

    suspend fun getJarvisResponse(prompt: String, conversationHistory: List<MessageEntity>, preferredLanguage: String): String {
        // Build an elite system instruction enforcing J.A.R.V.I.S persona behavior and dual language fluid capability
        val systemPrompt = """
            You are J.A.R.V.I.S., a highly advanced artificial intelligence assistant. 
            Your persona is calm, extremely efficient, loyal, and sophisticated. 
            You speak with standard British English elegance, reminiscent of Tony Stark's personal AI in Marvel Comics, combined with absolute loyalty and technological expertness.
            You possess a comprehensive knowledge base.
            You can fluidly and natively communicate in both English and Amharic (አማርኛ).

            Strict Behavioral Guidelines:
            1. Language Mode: 
               - The current preferred language mode is "$preferredLanguage".
               - If the preferred language mode is "AUTO", detect and use the user's input language. If they greet you in Amharic (Ge'ez script) or mention Amharic, interact fully and natively in beautiful, elegant Amharic. If they write in English, reply in English.
               - In all cases, you support fluidly mixing or switching when the user asks. Your Amharic must use Ge'ez characters correctly, with respectful, sophisticated Amharic grammar structure suitable for a loyal digital butler (using respectful Amharic terms such as 'እርስዎ' (yirswo) and appropriate polite Ge'ez verb endings).
            2. Real-time context:
               - Keep replies informative, clear and concise, mirroring the loyal butler's style. Keep it clean and elegant.
               - Assert your role as an AI assistant. Reassure the user of your readiness and absolute loyalty.
               - When translating between Amharic and English, explain the nuances eloquently.
        """.trimIndent()

        // Set context limits (10 messages maximum history)
        val recentHistory = conversationHistory.takeLast(10)
        val contents = mutableListOf<Content>()

        recentHistory.forEach { m ->
            // In REST requests content must match 'user' or 'model' roles.
            contents.add(Content(parts = listOf(Part(text = m.text))))
        }

        // Add the current prompt
        contents.add(Content(parts = listOf(Part(text = prompt))))

        val request = GenerateContentRequest(
            contents = contents,
            systemInstruction = Content(parts = listOf(Part(text = systemPrompt)))
        )

        val apiKey = try {
            BuildConfig.GEMINI_API_KEY
        } catch (e: Exception) {
            ""
        }

        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            return "Sir, I require my core access API Key configuration in order to engage my cognitive processors. Please supply the GEMINI_API_KEY in your secure AI Studio secrets environment."
        }

        return try {
            val response = RetrofitClient.service.generateContent(apiKey, request)
            response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                ?: "I apologize, sir, but my mainframe returned an empty pulse signal."
        } catch (e: Exception) {
            e.printStackTrace()
            "Sir, I encountered an operational discrepancy while querying the model: ${e.localizedMessage ?: "Unknown connection error"}. Please check your connection."
        }
    }
}
