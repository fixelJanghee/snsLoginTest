package com.devlee.logintest

import android.content.Intent
import android.content.IntentSender
import android.net.Uri
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.activity.result.ActivityResult
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.devlee.logintest.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.identity.*
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.CommonStatusCodes
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {
    companion object {
        private const val clientSecret = "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"

        private const val TAG = "LoginTest"
    }

    private val REQ_ONE_TAP = 2  // Can be any integer unique to the Activity
    private var showOneTapUI = true

    private lateinit var binding: ActivityMainBinding

    private lateinit var oneTapClient: SignInClient
    private lateinit var signInRequest: BeginSignInRequest
    private lateinit var signUpRequest: BeginSignInRequest

    private val oneTapResult = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result: ActivityResult ->
        try {
            // ActivityResult --> resultCode: Int?, data: Intent? 가지고 있음

            val credential: SignInCredential = oneTapClient.getSignInCredentialFromIntent(result.data)
            // credential에서 사용할 수 있는 데이터
            val idToken: String? = credential.googleIdToken
            val uniqueId: String = credential.id
            val password: String? = credential.password
            val displayName: String? = credential.displayName
            val familyName: String? = credential.familyName
            val givenName: String? = credential.givenName
            val profilePhoto: Uri? = credential.profilePictureUri
            when {
                idToken != null -> {
                    // Got an ID token from Google.
                    // 토큰을 내가 사용하는 서버로 보내거나 사용할 수 있다.
                    // 백엔드에서는 Google API 클라이언트 라이브러리 또는
                    // JWT 라이브러리를 사용하여 토큰을 확인
                    val message = """
                        idToken: $idToken,
                        uniqueId: $uniqueId,
                        password: $password,
                        displayName: $displayName,
                        familyName: $familyName
                        givenName: $givenName
                        profilePhoto: $profilePhoto
                    """.trimIndent()
                    binding.loginText = message
                    Log.d(TAG, message)
                }
                else -> {
                    // Shouldn't happen.
                    Log.d(TAG, "No ID token!")
                }
            }
        } catch (e: ApiException) {
            when (e.statusCode) {
                CommonStatusCodes.CANCELED -> {
                    binding.loginText = "사용자가 닫음"
                    Log.e(TAG, "One-tap dialog was closed.")
                    // Don't re-prompt the user.
                }
                CommonStatusCodes.NETWORK_ERROR -> {
                    binding.loginText = "네트워크 오류"
                    Log.e(TAG, "One-tap encountered a network error.")
                    // Try again or just ignore.
                }
                else -> {
                    binding.loginText = e.localizedMessage
                    Log.e(TAG, "Couldn't get credential from result. (${e.localizedMessage})")
                }
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.textView.movementMethod = ScrollingMovementMethod()

        binding.googleLogin.setOnClickListener { googleLogin() }
        binding.googleLogout.setOnClickListener { googleLogout() }
        binding.googleServer.setOnClickListener { googleServer() }

        // 애플 로그인 클릭
        binding.appleLoginActivity.setOnClickListener {
            val intent = Intent(this, AppleLogin::class.java)
            startActivity(intent)
        }

        oneTapClient = Identity.getSignInClient(this)
        signInRequest = BeginSignInRequest.builder()
            .setPasswordRequestOptions(
                // 비밀번호 입력 지원
                BeginSignInRequest.PasswordRequestOptions.builder()
                    .setSupported(true)
                    .build()
            )
            .setGoogleIdTokenRequestOptions(
                // GoogleIdToken 방식
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))   // server's client ID, not your Android client ID
                    .setFilterByAuthorizedAccounts(true)                    // 로그인한 아이디만 보이도록 설정
                    .build()
            )
            .setAutoSelectEnabled(true)
            .build()


        signUpRequest = BeginSignInRequest.builder()
            /** One-tap 가입은 idToken 방식만 지원함 */
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId(getString(R.string.web_client_id))
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .build()
    }


    /** 구글 로그인 */
    private fun googleLogin() {
        oneTapClient.beginSignIn(signInRequest)
            .addOnSuccessListener(this) { result: BeginSignInResult ->
                try {
                    Log.i(TAG, "beginSignIn success")
                    val ib = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    oneTapResult.launch(ib)
                } catch (e: IntentSender.SendIntentException) {
                    e.printStackTrace()
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                } catch (e: Exception) {
                    e.printStackTrace()
                    Log.e(TAG, "Exception:: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e: Exception ->
                binding.loginText = e.localizedMessage
                e.printStackTrace()
                if (e is ApiException) {
                    Log.v(TAG, "status code: ${e.status}")
                }
                // 구글아이디를 찾을 수 없어 로그아웃 된 UI 상태 유지
                Log.e(TAG, "로그인 실패 리스너:: ${e.localizedMessage}")
                googleSignUp()
            }
    }

    /** 구글 로그아웃 */
    private fun googleLogout() {
        Log.i(TAG, "googleLogout-()")
        oneTapClient.signOut()
            .addOnCompleteListener {
                binding.loginText = "로그아웃"
                Log.d(TAG, "googleLogout: complete ")
            }
    }

    private fun revokeAccess() {
        Log.i(TAG, "revokeAccess:-()")

    }

    private fun googleServer() {
        Log.i(TAG, "googleServer-()")
        LoginService.getToken().enqueue(object : Callback<LoginGoogleResponse> {
            override fun onResponse(call: Call<LoginGoogleResponse>, response: Response<LoginGoogleResponse>) {
                Log.d(TAG, "로그인 성공")
                Log.d(TAG, "response body:${response.body()} code:${response.code()} error:${response.message()}")

            }

            override fun onFailure(call: Call<LoginGoogleResponse>, t: Throwable) {
                t.printStackTrace()
                Log.e(TAG, "로그인 오류: ${t.localizedMessage}")
            }

        })
//        LoginAccessToken.getOauth().enqueue(object : Callback<LoginGoogleResponse> {
//            override fun onResponse(call: Call<LoginGoogleResponse>, response: Response<LoginGoogleResponse>) {
//                Log.d(TAG, "로그인 성공")
//                Log.d(TAG, "response ${response.body()}")
//            }
//
//            override fun onFailure(call: Call<LoginGoogleResponse>, t: Throwable) {
//                t.printStackTrace()
//                Log.e(TAG, "로그인 오류: ${t.localizedMessage}")
//            }
//
//        })
    }


    private fun googleSignUp() {
        oneTapClient.beginSignIn(signUpRequest)
            .addOnSuccessListener(this) { result ->
                Log.i(TAG, "beginSignUp success")
                try {
                    val ib = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                    oneTapResult.launch(ib)
//                    )
                } catch (e: IntentSender.SendIntentException) {
                    Log.e(TAG, "Couldn't start One Tap UI: ${e.localizedMessage}")
                }
            }
            .addOnFailureListener(this) { e ->
                // No Google Accounts found. Just continue presenting the signed-out UI.
                Log.e(TAG, "" + e.localizedMessage)
            }
    }

}