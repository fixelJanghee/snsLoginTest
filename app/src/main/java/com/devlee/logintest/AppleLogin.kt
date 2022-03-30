package com.devlee.logintest

import android.os.Bundle
import android.os.PersistableBundle
import android.text.method.ScrollingMovementMethod
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.devlee.logintest.databinding.ActivityAppleBinding
import com.google.firebase.auth.OAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

class AppleLogin : AppCompatActivity() {

    private val TAG = "AppleLogin"
    private val auth by lazy { Firebase.auth }
    private val provider by lazy { OAuthProvider.newBuilder("apple.com") }

    private lateinit var binding: ActivityAppleBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAppleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appleLoginText.movementMethod = ScrollingMovementMethod()

        provider.scopes = mutableListOf("email", "name")
        // 한국어로 설정
        provider.addCustomParameter("locale", "ko")

        binding.appleLogin.setOnClickListener {
            login()
        }



    }

    private fun login() {
        val pending = auth.pendingAuthResult
        if (pending != null) {
            pending.addOnSuccessListener { authResult ->
                Log.d(TAG, "pending success:: $authResult")
            }
                .addOnFailureListener { e ->
                    e.printStackTrace()
                    Log.e(TAG, "pending fail:: ${e.localizedMessage}")
                }
        } else {
            Log.d(TAG, "pending is null")
            signUp()
        }
    }

    private fun signUp() {
        auth.startActivityForSignInWithProvider(this, provider.build())
            .addOnSuccessListener { authResult ->
                // Sign-in successful
                val message = """
                    email -> ${authResult.user?.email}
                    idToken -> ${authResult.user?.getIdToken(true)}
                    provider -> ${authResult.credential?.provider}
                """.trimIndent()
                Log.d(TAG, "Sign-in success:: ${authResult.user}")
                Log.d(TAG, message)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
                Log.e(TAG, "Sign-in fail:: ${e.localizedMessage}")
            }
    }
}