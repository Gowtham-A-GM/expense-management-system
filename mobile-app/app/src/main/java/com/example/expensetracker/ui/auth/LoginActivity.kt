package com.example.expensetracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.expensetracker.R
import com.example.expensetracker.databinding.ActivityLoginBinding
import com.example.expensetracker.network.RetrofitClient
import com.example.expensetracker.ui.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    // GOOGLE LOGIN HANDLER (NEW API)
    private val googleLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->

        Log.d("DEBUG_LOGIN", "Google result received: resultCode=${result.resultCode}")

        val data = result.data
        Log.d("DEBUG_LOGIN", "Google intent data: $data")

        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            Log.d("DEBUG_LOGIN", "Extracting Google account from intent...")
            val account = task.getResult(ApiException::class.java)

            Log.d(
                "DEBUG_LOGIN",
                "Google account success: email=${account.email}, idToken=${account.idToken}"
            )

            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            Log.d("DEBUG_LOGIN", "Signing in Firebase with Google credential...")

            auth.signInWithCredential(credential)
                .addOnSuccessListener {
                    Log.d("DEBUG_LOGIN", "Firebase Google Login SUCCESS uid=${auth.currentUser!!.uid}")
                    saveUserId(auth.currentUser!!.uid)
                    goToHome()
                }
                .addOnFailureListener {
                    Log.e("DEBUG_LOGIN", "Firebase Google Login FAILED: ${it.message}")
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Log.e("DEBUG_LOGIN", "Google Sign-In FAILED: ${e.message}")
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        Log.d("DEBUG_LOGIN", "LoginActivity opened")
        Log.d("DEBUG_LOGIN", "Current firebase user = ${auth.currentUser}")

        // Auto login
        auth.currentUser?.let {
            Log.d("DEBUG_LOGIN", "User already logged in. Redirecting to Home...")
            goToHome()
            return
        }

        setupEmailLogin()
        setupSignupRedirect()
        setupGoogleLogin()
    }

    // EMAIL + PASSWORD LOGIN
    private fun setupEmailLogin() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etUsername.text.toString().trim()
            val pass = binding.etPassword.text.toString().trim()

            Log.d("DEBUG_LOGIN", "Email login clicked: email=$email")

            if (email.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Enter email & password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, pass)
                .addOnSuccessListener {
                    Log.d("DEBUG_LOGIN", "Email Login SUCCESS uid=${auth.currentUser!!.uid}")
                    saveUserId(auth.currentUser!!.uid)
                    goToHome()
                }
                .addOnFailureListener {
                    Log.e("DEBUG_LOGIN", "Email Login FAILED: ${it.message}")
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }

        }
    }

    // SIGNUP BUTTON
    private fun setupSignupRedirect() {
        binding.btnSignup.setOnClickListener {
            Log.d("DEBUG_LOGIN", "Signup button clicked")
            startActivity(Intent(this, SignupActivity::class.java))
        }
    }

    // GOOGLE LOGIN BUTTON
    private fun setupGoogleLogin() {
        binding.googleContainer.setOnClickListener {

            Log.d("DEBUG_LOGIN", "Google login button clicked")

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            Log.d("DEBUG_LOGIN", "GoogleSignInOptions created. Launching Google UI...")

            val client = GoogleSignIn.getClient(this, gso)
            googleLauncher.launch(client.signInIntent)
        }
    }

    // SAVE USER UID LOCALLY
    private fun saveUserId(uid: String) {
        Log.d("DEBUG_LOGIN", "Saving UID -> $uid")
        getSharedPreferences("USER", MODE_PRIVATE)
            .edit()
            .putString("uid", uid)
            .apply()
    }

    // NAVIGATE TO HOME SCREEN
    private fun goToHome() {
        Log.d("DEBUG_LOGIN", "Navigating to HomeActivity...")
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
