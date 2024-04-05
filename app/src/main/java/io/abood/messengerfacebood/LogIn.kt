package io.abood.messengerfacebood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import io.abood.messengerfacebood.databinding.ActivityLogInBinding

class LogIn : AppCompatActivity(), TextWatcher {
    private lateinit var binding: ActivityLogInBinding
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStoreBase by lazy {
        FirebaseFirestore.getInstance()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityLogInBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        binding.edtEmailLogin.addTextChangedListener(this)
        binding.edtPassLogin.addTextChangedListener(this)

        binding.btnCreateNewAccount.setOnClickListener {
            startActivity(Intent(this, SignUp::class.java))
        }

        binding.btnLogin.setOnClickListener {
            val email = binding.edtEmailLogin.text.toString().trim()
            val password = binding.edtPassLogin.text.toString().trim()


            if (email.isEmpty()) {
                binding.edtEmailLogin.error = "Email is required"
                binding.edtEmailLogin.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtEmailLogin.error = "Email is not formatted right"
                binding.edtEmailLogin.requestFocus()
                return@setOnClickListener
            }
            if (password.length<6){
                binding.edtPassLogin.error="Password should be more than 6 characters"
                binding.edtPassLogin.requestFocus()
                return@setOnClickListener
            }
            logIn(email,password)
        }
    }

    override fun onStart() {
        if (mAuth.currentUser?.uid!=null){
            val toMainActivity= Intent(this, MainActivity::class.java)
            toMainActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(toMainActivity)
        }
        super.onStart()
    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }

    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        binding.btnLogin.isEnabled = binding.edtEmailLogin.text.toString().trim().isNotEmpty()
                && binding.edtPassLogin.text.toString().trim().isNotEmpty()
    }

    override fun afterTextChanged(s: Editable?) {
        val str: String = binding.edtPassLogin.text.toString()
        if (str.isNotEmpty() && str.contains(" ")) {
            binding.edtPassLogin.setText(binding.edtPassLogin.text.toString().replace(" ", ""))
            binding.edtPassLogin.setSelection(binding.edtPassLogin.text.length)
        }
    }

    private fun logIn(email:String,password:String){
        binding.progressBar.visibility = View.VISIBLE
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener {

            if (it.isSuccessful) {
                binding.progressBar.visibility = View.INVISIBLE
                FirebaseMessaging.getInstance().token.addOnCompleteListener { task->
                    if (!task.isSuccessful){
                        Toast.makeText(this, "did not successes", Toast.LENGTH_SHORT).show()
                    }else {
                        val token = task.result
                        fireStoreBase.collection("Users")
                            .document(mAuth.currentUser?.uid.toString())
                            .update(mapOf("token" to token))

                    }
                }
                Toast.makeText(this, "logged in successfully", Toast.LENGTH_SHORT).show()
                val toMainActivity= Intent(this, MainActivity::class.java)
                toMainActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(toMainActivity)
            } else {
                binding.progressBar.visibility = View.INVISIBLE
                Toast.makeText(this, it.exception?.message.toString(), Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }
}