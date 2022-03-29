package com.devlee.logintest

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

object LoginService {
    const val BASE_URL = "https://www.googleapis.com/"


    var retrofit: Retrofit
    private var apiService: ApiService

    init {
        val okHttpClient = OkHttpClient().newBuilder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient.addInterceptor(loggingInterceptor)

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    fun getToken(): Call<LoginGoogleResponse> {

        val loginGoogleRequest = LoginGoogleRequest(
            grantType = "authorization_code",
            clientId = "398000055767-8fcbq060e80t3fk50hcroe0blartlk03.apps.googleusercontent.com",
//            clientId = "sj90947@fixelsoft.com",
            clientSecret = "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",
//            clientSecret = "dnflwlq1!",
            redirectUri = "https://www.logintestfixelsoft.com",
            code = "4/P7q7W91a-oMsCeLvIaQm6bTrgtp7"
        )

        return apiService.oauth2Google(loginGoogleRequest)
    }

    interface ApiService {
        @POST("oauth2/v4/token")
        fun oauth2Google(
            @Body request: LoginGoogleRequest
        ): Call<LoginGoogleResponse>
    }
}

object LoginAccessToken {
    const val BASE_URL1 = "https://accounts.google.com/"
    var retrofit: Retrofit
    private var apiService: LoginServiceInterface

    init {
        val okHttpClient = OkHttpClient().newBuilder()
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.level = HttpLoggingInterceptor.Level.BODY
        okHttpClient.addInterceptor(loggingInterceptor)

        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL1)
            .client(okHttpClient.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(LoginServiceInterface::class.java)
    }

    fun getOauth(): Call<LoginGoogleResponse> {
        val clientId = "398000055767-8fcbq060e80t3fk50hcroe0blartlk03.apps.googleusercontent.com"
        val redirectUri = "https://www.logintestfixelsoft.com"
        val scope = "email%20profile"
        val responseType = "code"
        val state = "security_token%3D138r5719ru3e1%26url%3Dhttps%3A%2F%2Foauth2.example.com%2Ftoken"
        return apiService.oauth2(scope, responseType, state, redirectUri, clientId)
    }

    interface LoginServiceInterface {
        @GET("o/oauth2/v2/auth")
        fun oauth2(
            @Query("scope") scope: String,
            @Query("response_type") response_type: String,
            @Query("state") state: String,
            @Query("redirect_uri") redirect_uri: String,
            @Query("client_id") client_id: String
        ): Call<LoginGoogleResponse>
    }
}