package com.example.ai

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

object GeminiService {
    private const val TAG = "GeminiService"
    
    // Choose the default model recommended for basic text tasks
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    // Flag to check if we have a real user-supplied key or a placeholder
    fun isApiKeyConfigured(): Boolean {
        val key = BuildConfig.GEMINI_API_KEY
        return key.isNotEmpty() && key != "MY_GEMINI_API_KEY" && !key.contains("PLACEHOLDER")
    }

    suspend fun generateContent(prompt: String, systemInstruction: String? = null): String = withContext(Dispatchers.IO) {
        if (!isApiKeyConfigured()) {
            Log.w(TAG, "Gemini API Key is not configured. Using high-quality local tutoring simulation.")
            return@withContext getOfflineResponse(prompt)
        }

        val apiKey = BuildConfig.GEMINI_API_KEY
        val url = "$BASE_URL?key=$apiKey"

        try {
            // Build request JSON
            val requestJson = JSONObject()
            
            // Contents
            val contentsArray = JSONArray()
            val contentObj = JSONObject()
            val partsArray = JSONArray()
            val partObj = JSONObject()
            partObj.put("text", prompt)
            partsArray.put(partObj)
            contentObj.put("parts", partsArray)
            contentsArray.put(contentObj)
            requestJson.put("contents", contentsArray)

            // System Instruction if provided
            if (systemInstruction != null) {
                val systemInstructionObj = JSONObject()
                val systemPartsArray = JSONArray()
                val systemPartObj = JSONObject()
                systemPartObj.put("text", systemInstruction)
                systemPartsArray.put(systemPartObj)
                systemInstructionObj.put("parts", systemPartsArray)
                requestJson.put("systemInstruction", systemInstructionObj)
            }

            // Generation Config
            val generationConfig = JSONObject()
            generationConfig.put("temperature", 0.7)
            requestJson.put("generationConfig", generationConfig)

            val mediaType = "application/json; charset=utf-8".toMediaType()
            val requestBody = requestJson.toString().toRequestBody(mediaType)

            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use { response ->
                val bodyString = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Request failed (code ${response.code}): $bodyString")
                    return@withContext "API error: Code ${response.code}. Using smart local tutor simulation:\n\n${getOfflineResponse(prompt)}"
                }

                if (bodyString.isNullOrEmpty()) {
                    return@withContext "Error: Received empty response from Gemini API."
                }

                val responseJson = JSONObject(bodyString)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates != null && candidates.length() > 0) {
                    val firstCandidate = candidates.getJSONObject(0)
                    val content = firstCandidate.optJSONObject("content")
                    if (content != null) {
                        val parts = content.optJSONArray("parts")
                        if (parts != null && parts.length() > 0) {
                            return@withContext parts.getJSONObject(0).optString("text", "No readable text from AI.")
                        }
                    }
                }
                "AI compiled a blank statement. Try rephrasing your question!"
            }
        } catch (e: Exception) {
            Log.e(TAG, "Exception during generation", e)
            "Connection trouble. Here is a simulated response:\n\n${getOfflineResponse(prompt)}"
        }
    }

    /**
     * A rich offline fallback system representing high quality simulation responses so users
     * don't get stuck if they do not have active internet/API key.
     */
    private fun getOfflineResponse(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("help grep") || lower.contains("explain grep") -> """
                🔍 **AI Tutor Explains: 'grep' (Global Regular Expression Print)**
                
                **Purpose:**
                The `grep` command searches text files for patterns matching a query and outputs the lines that contain a match. It is like "Find in File" (Ctrl+F) for the command line.
                
                **Syntax:**
                `grep [options] "pattern" [file]`
                
                **Common Options:**
                *   `-i`: Ignore case (uppercase vs lowercase matches).
                *   `-v`: Invert search (show lines that *do not* contain the pattern).
                *   `-n`: Show line numbers alongside results.
                *   `-r` or `-R`: Search recursively inside files in all directories.
                
                **Real-World Example:**
                If you have a file `about_me.txt` and want to locate your level objective:
                `grep Objective about_me.txt`
                
                **Why it's powerful:**
                Sifting through megabytes of server logs to find errors (`grep -i "error" server.log`) is an essential DevOps and operations superpower!
            """.trimIndent()

            lower.contains("help chmod") || lower.contains("explain chmod") -> """
                🔐 **AI Tutor Explains: 'chmod' (Change Mode)**
                
                **Purpose:**
                `chmod` modifies the read (r), write (w), and execute (x) permissions of files and directories in Linux for Security Isolation.
                
                **Permissions Model:**
                Linux files have three target scopes:
                1.  **User (u)**: The owner of the file.
                2.  **Group (g)**: Users in the file's primary group.
                3.  **Others (o)**: Everyone else on the system.
                
                **Numeric Notation (Octal):**
                Permissions are calculated in binary groups:
                *   `4` = Read (r)
                *   `2` = Write (w)
                *   `1` = Execute (x)
                
                Sum them up for a scope!
                *   `7` (4+2+1) = read, write, and execute (`rwx`)
                *   `6` (4+2) = read and write (`rw-`)
                *   `5` (4+1) = read and execute (`r-x`)
                *   `4` = read-only (`r--`)
                
                Therefore, standard script permissions are often `755` (`rwxr-xr-x`).
                
                **Quick Syntax Example:**
                *   `chmod +x scripts/run_tests.sh` (Add execute permission for owner)
                *   `chmod 644 document.txt` (Owner can read/write, others read-only)
            """.trimIndent()

            lower.contains("help ls") || lower.contains("explain ls") -> """
                📁 **AI Tutor Explains: 'ls' (List Segment / List)**
                
                **Purpose:**
                Prints the files and directories inside a targeted directory path onto the screen.
                
                **Syntax:**
                `ls [options] [path]`
                
                **Popular Flags:**
                *   `-l`: Long listing format (showing size, author, date, and permissions).
                *   `-a`: Show all files, including hidden dotfiles (like `.bashrc` or `.git`).
                *   `-h`: Render file sizes in human-readable metrics (like KB, MB).
                
                **Quick Tip:**
                Running `ls -la` combines standard options to print full metadata on all files!
            """.trimIndent()

            lower.contains("explain cd") || lower.contains("help cd") -> """
                🧭 **AI Tutor Explains: 'cd' (Change Directory)**
                
                **Purpose:**
                Navigates your terminal's focus to a different subdirectory location.
                
                **Quick Paths:**
                *   `cd projects`: Move relatively into the `projects` child folder.
                *   `cd ..`: Climb up one level into the parent directory.
                *   `cd /`: Snap instantly to the absolute root directory.
                *   `cd ~`: Return path focus back home.
            """.trimIndent()

            lower.contains("help mv") || lower.contains("explain mv") || lower.contains("help cp") || lower.contains("explain cp") -> """
                📦 **AI Tutor Explains: 'mv' & 'cp' (Move / Copy)**
                
                **Move (mv):**
                Used to relocate or rename files. Running `mv old.txt new.txt` acts as a rename. Running `mv document.txt projects/` moves the file into projects without copying.
                
                **Copy (cp):**
                Generates a completely independent duplicate file.
                *   `cp note.txt copy_note.txt` (Copies file)
                *   `cp -r old_dir/ new_dir/` (Use `-r` flag to copy entire subfolders recursively!)
            """.trimIndent()

            lower.contains("why do we need") || lower.contains("different from") || lower.contains("what is linux") -> """
                🐧 **AI Tutor: Linux Architecture 101**
                
                Linux is an open-source operating system kernel created by Linus Torvalds in 1991.
                
                Unlike Windows or macOS:
                1.  **Terminal-First**: Servers, cloud containers, and mobile systems rely on text-based terminals for super low latency and powerful automated bash scripts.
                2.  **File Centricity**: Everything in Linux is managed as a file—even keyboard inputs, printers, and hard drives are represented by stateful streams under `/dev`!
                
                You're in the perfect place to learn the command lines that power over 90% of the world's commercial servers!
            """.trimIndent()

            else -> """
                🎓 **LinuxLab AI Tutor Chatbot**
                
                I am your educational AI Tutor. I can answer questions about terminal commands, explain the purpose of directories, show you how to structure parameters, and guide you on scripting error troubles!
                
                **Ask me anything like:**
                * "How do I redirect echoes?"
                * "Explain the difference between absolute and relative paths"
                * "Show me common examples of grep"
                * "What is a virtual file system?"
                
                *(Note: You can add your official Google Gemini API Key in the AI Studio Secrets panel to unlock the full power of real-time custom answers!)*
            """.trimIndent()
        }
    }
}
