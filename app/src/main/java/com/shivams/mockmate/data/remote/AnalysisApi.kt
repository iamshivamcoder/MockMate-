package com.shivams.mockmate.data.remote

import com.shivams.mockmate.model.analysis.AnalysisResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

/**
 * Retrofit API interface for PDF Analysis endpoints.
 */
interface AnalysisApi {
    
    /**
     * Upload a PDF file for cognitive analysis.
     * 
     * @param file The PDF file as multipart form data
     * @return Response containing the analysis result
     */
    @Multipart
    @POST(ApiConstants.ANALYZE_ENDPOINT)
    suspend fun uploadPdf(
        @Part file: MultipartBody.Part
    ): Response<AnalysisResponse>
    
    /**
     * Get sample analysis without uploading a file.
     * Useful for testing the Android app without a real PDF.
     */
    @GET(ApiConstants.SAMPLE_ENDPOINT)
    suspend fun getSampleAnalysis(): Response<AnalysisResponse>
}
