package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

object GeminiClient {
    private const val TAG = "GeminiClient"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun generateContent(
        prompt: String,
        systemInstruction: String? = null,
        mode: ChatMode = ChatMode.LITE
    ): GeminiResponse = withContext(Dispatchers.IO) {
        val apiKey = BuildConfig.GEMINI_API_KEY
        if (apiKey.isEmpty() || apiKey == "MY_GEMINI_API_KEY") {
            Log.e(TAG, "Gemini API key is not configured!")
            return@withContext GeminiResponse.Error("API Key is not configured. Please enter your GEMINI_API_KEY in the AI Studio Secrets panel.")
        }

        val modelName = when (mode) {
            ChatMode.LITE -> "gemini-3.1-flash-lite-preview"
            ChatMode.STANDARD_GROUNDING -> "gemini-3.5-flash"
            ChatMode.PRO_THINKING -> "gemini-3.1-pro-preview"
        }

        val url = "$BASE_URL/models/$modelName:generateContent?key=$apiKey"

        try {
            val jsonPayload = JSONObject()

            // Contents array
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            contentObj.put("role", "user")
            
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            
            jsonPayload.put("contents", contentsArray)

            // System instructions
            if (systemInstruction != null) {
                val sysInstObj = JSONObject()
                val sysPartsArray = JSONArray()
                val sysPartObj = JSONObject()
                sysPartObj.put("text", systemInstruction)
                sysPartsArray.put(sysPartObj)
                sysInstObj.put("parts", sysPartsArray)
                jsonPayload.put("systemInstruction", sysInstObj)
            }

            // Tools (Search Grounding)
            if (mode == ChatMode.STANDARD_GROUNDING) {
                val toolsArray = JSONArray()
                val toolObj = JSONObject()
                toolObj.put("googleSearch", JSONObject()) // Enable Google Search
                toolsArray.put(toolObj)
                jsonPayload.put("tools", toolsArray)
            }

            // Generation Config
            val generationConfig = JSONObject()
            if (mode == ChatMode.PRO_THINKING) {
                val thinkingConfig = JSONObject()
                thinkingConfig.put("thinkingLevel", "high") // Request high reasoning depth
                generationConfig.put("thinkingConfig", thinkingConfig)
                // Note: Do not set maxOutputTokens for high thinking mode
            } else {
                generationConfig.put("temperature", 0.7)
                generationConfig.put("maxOutputTokens", 2048)
            }
            jsonPayload.put("generationConfig", generationConfig)

            val requestBody = jsonPayload.toString().toRequestBody("application/json; charset=utf-8".toMediaType())
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            val response = okHttpClient.newCall(request).execute()
            val responseBodyString = response.body?.string()

            if (!response.isSuccessful || responseBodyString == null) {
                Log.e(TAG, "Request failed with code: ${response.code}, payload: $responseBodyString")
                return@withContext GeminiResponse.Error("API call failed with code ${response.code}: ${response.message}")
            }

            val responseJson = JSONObject(responseBodyString)
            val candidates = responseJson.optJSONArray("candidates")
            val firstCandidate = candidates?.optJSONObject(0)
            val content = firstCandidate?.optJSONObject("content")
            val parts = content?.optJSONArray("parts")
            val text = parts?.optJSONObject(0)?.optString("text")

            if (text != null) {
                // Check if search grounding metadata is present
                val groundingMetadata = firstCandidate.optJSONObject("groundingMetadata")
                val searchQueries = groundingMetadata?.optJSONArray("webSearchQueries")
                val queriesList = mutableListOf<String>()
                if (searchQueries != null) {
                    for (i in 0 until searchQueries.length()) {
                        queriesList.add(searchQueries.getString(i))
                    }
                }
                
                val groundingChunks = groundingMetadata?.optJSONArray("groundingChunks")
                val sourcesList = mutableListOf<GroundingSource>()
                if (groundingChunks != null) {
                    for (i in 0 until groundingChunks.length()) {
                        val chunk = groundingChunks.getJSONObject(i)
                        val web = chunk.optJSONObject("web")
                        if (web != null) {
                            sourcesList.add(
                                GroundingSource(
                                    title = web.optString("title", "Reference"),
                                    url = web.optString("uri", "")
                                )
                            )
                        }
                    }
                }

                GeminiResponse.Success(
                    text = text,
                    queriesUsed = queriesList,
                    sources = sourcesList
                )
            } else {
                GeminiResponse.Error("No output generated by Gemini model.")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during generation", e)
            GeminiResponse.Error("Connection error: ${e.message}")
        }
    }
}

enum class ChatMode {
    LITE,               // gemini-3.1-flash-lite (fast response, low latency)
    STANDARD_GROUNDING, // gemini-3.5-flash with googleSearch grounding
    PRO_THINKING        // gemini-3.1-pro-preview with thinking level HIGH
}

sealed class GeminiResponse {
    data class Success(
        val text: String,
        val queriesUsed: List<String> = emptyList(),
        val sources: List<GroundingSource> = emptyList()
    ) : GeminiResponse()
    
    data class Error(val message: String) : GeminiResponse()
}

data class GroundingSource(
    val title: String,
    val url: String
)
