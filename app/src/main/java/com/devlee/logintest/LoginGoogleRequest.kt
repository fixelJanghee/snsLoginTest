package com.devlee.logintest

import com.google.gson.annotations.SerializedName

data class LoginGoogleRequest(
    @SerializedName("grant_type")
    val grantType: String,
    @SerializedName("client_id")
    val clientId: String,
    @SerializedName("client_secret")
    val clientSecret: String,
    @SerializedName("redirect_uri")
    val redirectUri: String,
    val code: String
)

data class LoginGoogleResponse(
    var access_token: String?,
    var refresh_token: String?,
    var expires_in: Int?,           // second
    var token_type: String?,
    var scope: String?,
    var id_token: String?
) {
    override fun toString(): String {
        return """
            access_token : $access_token
            refresh_token : $refresh_token
            expires_in : $expires_in
            scope : $scope
            token_type : $token_type
            id_token : $id_token
        """.trimIndent()
    }
}
