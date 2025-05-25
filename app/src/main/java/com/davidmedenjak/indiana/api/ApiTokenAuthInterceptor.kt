package com.davidmedenjak.indiana.api

import com.davidmedenjak.indiana.session.SessionManager
import okhttp3.Interceptor
import okhttp3.Response
import retrofit2.HttpException
import javax.inject.Inject

class ApiTokenAuthInterceptor @Inject constructor(
    private val sessionManager: SessionManager,
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        val apiToken = sessionManager.apiToken
        if (apiToken.isNullOrBlank()) {
            return chain.proceed(request)
        }

        val authenticatedRequest = request.newBuilder()
            .header("Authorization", "token $apiToken")
            .build()

        try {
            return chain.proceed(authenticatedRequest)
        } catch (ex: HttpException) {
            if (ex.code() == 401) {
                sessionManager.logout()
            }
            throw ex
        }
    }
}