package com.example.expensetracker.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.example.expensetracker.R
import com.example.expensetracker.databinding.ActivitySignupBinding
import com.example.expensetracker.ui.home.HomeActivity
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private lateinit var auth: FirebaseAuth

    private val googleSignupLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)

            auth.signInWithCredential(credential)
                .addOnSuccessListener {

                    // SAVE GOOGLE NAME
                    val uid = auth.currentUser!!.uid
                    val googleName = account.displayName ?: "User"

                    val userMap = mapOf(
                        "profileName" to googleName,
                        "email" to account.email
                    )

                    com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .setValue(userMap)

                    saveProfileName(googleName)
                    saveUserId(uid)

                    goToHome()

                }
                .addOnFailureListener {
                    Toast.makeText(this, it.message, Toast.LENGTH_SHORT).show()
                }

        } catch (e: Exception) {
            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = FirebaseAuth.getInstance()

        setupSignup()
        setupGoogleSignup()
        setupLoginRedirect()
    }

    private fun setupSignup() {
        binding.btnSignup.setOnClickListener {

            val profileName = binding.etProfileName.text.toString().trim()
            val email = binding.etUsername.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConformPassword.text.toString().trim()

            if (profileName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                binding.tvErrorHint.text = "All fields are required"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                binding.tvErrorHint.text = "Passwords do not match"
                return@setOnClickListener
            }

            binding.tvErrorHint.text = ""

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener {

                    val uid = auth.currentUser!!.uid

                    // Save to Firebase Database
                    val userMap = mapOf(
                        "profileName" to profileName,
                        "email" to email
                    )

                    com.google.firebase.database.FirebaseDatabase.getInstance()
                        .getReference("users")
                        .child(uid)
                        .setValue(userMap)

                    // Save locally
                    saveProfileName(profileName)
                    saveUserId(uid)

                    goToHome()
                }
                .addOnFailureListener {
                    binding.tvErrorHint.text = it.message
                }
        }
    }


    private fun setupGoogleSignup() {
        binding.googleSignupContainer.setOnClickListener {

            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build()

            val client = GoogleSignIn.getClient(this, gso)
            googleSignupLauncher.launch(client.signInIntent)
        }
    }

    private fun setupLoginRedirect() {
        binding.btnLogin.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun saveUserId(uid: String) {
        getSharedPreferences("USER", MODE_PRIVATE)
            .edit()
            .putString("uid", uid)
            .apply()
    }

    private fun saveProfileName(name: String) {
        getSharedPreferences("USER", MODE_PRIVATE)
            .edit()
            .putString("profile_name", name)
            .apply()
    }

    private fun goToHome() {
        startActivity(Intent(this, HomeActivity::class.java))
        finish()
    }
}
