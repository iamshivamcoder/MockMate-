package com.shivams.mockmate.data.remote

/**
 * API Constants for the PDF Analysis backend.
 */
object ApiConstants {
    
    /**
     * Base URL for the PDF Analysis API.
     * 
     * TODO: Change this IP address to your PC's local IP when testing on physical device!
     * 
     * How to find your IP:
     * - Windows: Run `ipconfig` in CMD, look for "IPv4 Address"
     * - Mac/Linux: Run `ifconfig` or `ip addr`
     * 
     * For Emulator: Use "10.0.2.2" (special alias to host machine)
     * For Physical Device: Use your PC's actual IP (e.g., "192.168.1.100")
     */
    // TODO: Replace with your PC's IP address for physical device testing
    const val BASE_URL = "http://10.172.231.1:8000/"
    
    // Use this for Android Emulator
    // const val BASE_URL = "http://10.0.2.2:8000/"
    
    // API Endpoints
    const val ANALYZE_ENDPOINT = "analyze"
    const val SAMPLE_ENDPOINT = "analyze/sample"
    
    // Timeouts (in seconds) - Extended for large PDF processing
    const val CONNECT_TIMEOUT = 60L
    const val READ_TIMEOUT = 300L    // 5 minutes for Gemini processing
    const val WRITE_TIMEOUT = 120L   // 2 minutes for upload
}
