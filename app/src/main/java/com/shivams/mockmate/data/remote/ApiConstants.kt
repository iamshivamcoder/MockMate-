package com.shivams.mockmate.data.remote

/**
 * API Constants for the PDF Analysis backend.
 */
object ApiConstants {
    
    /**
     * Base URL for the PDF Analysis API.
     * Host: Render Cloud (Free Tier)
     * NOTE: Free tier spins down after inactivity. First request may take ~50s to warm up.
     */
    const val BASE_URL = "https://mockmate-backend-won4.onrender.com/"
    
    // API Endpoints
    const val ANALYZE_ENDPOINT = "analyze"
    const val SAMPLE_ENDPOINT = "analyze/sample"
    
    // Timeouts (in seconds) - Extended for large PDF processing and Cloud Cold Starts
    const val CONNECT_TIMEOUT = 300L   // 5 minutes - Render free tier cold starts can be very slow
    const val READ_TIMEOUT = 300L      // 5 minutes for Gemini processing
    const val WRITE_TIMEOUT = 300L     // 5 minutes for upload
}
