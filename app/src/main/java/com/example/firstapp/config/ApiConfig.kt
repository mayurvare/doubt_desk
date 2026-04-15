package com.example.firstapp.config

object ApiConfig {
    
    /**
     * Get AI API Key
     * TODO: Move this to local.properties or BuildConfig
     * IMPORTANT: Do not commit API keys to public repositories
     */
    fun getAiApiKey(): String {
        // TODO: Replace with BuildConfig.AI_API_KEY in production
        // For now, add your API key in local.properties: AI_API_KEY=your_key_here
        return "YOUR_API_KEY_HERE" // Replace this with actual key locally
    }
    
    /**
     * Check if API key is configured
     */
    fun isApiKeyConfigured(): Boolean {
        return getAiApiKey().isNotEmpty()
    }
}
