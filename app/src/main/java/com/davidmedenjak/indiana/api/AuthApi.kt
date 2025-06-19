package com.davidmedenjak.indiana.api

import com.davidmedenjak.indiana.model.V0UserProfileRespModel
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header

interface AuthApi {
    /**
     * Get your profile info
     * Shows the authenticated users profile info
     * Responses:
     *  - 200: OK
     *  - 401: Unauthorized
     *  - 404: Not Found
     *  - 500: Internal Server Error
     *
     * @return [V0UserProfileRespModel]
     */
    @GET("me")
    suspend fun userProfile(@Header("Authorization") token: String): Response<V0UserProfileRespModel>

}
