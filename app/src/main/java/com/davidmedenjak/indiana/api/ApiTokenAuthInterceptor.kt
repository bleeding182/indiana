package com.davidmedenjak.indiana.api

import com.davidmedenjak.indiana.features.entertoken.UserSettings
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class ApiTokenAuthInterceptor @Inject constructor(val settings: UserSettings) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {

        val request = chain.request()

        if (request.url().pathSegments().last() == "me") {
            return chain.proceed(request)
        }

        val authenticatedRequest = request.newBuilder()
                .header("Authorization", "token ${settings.apiToken}")
                .build()

        return chain.proceed(authenticatedRequest)
    }

}