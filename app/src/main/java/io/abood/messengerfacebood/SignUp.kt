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
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import io.abood.messengerfacebood.databinding.ActivitySignUpBinding
import io.abood.messengerfacebood.model.Users

class SignUp : AppCompatActivity(), TextWatcher {
    private lateinit var binding: ActivitySignUpBinding

    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }

    private val fireStoreInstance: FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentUserDoc: DocumentReference
        get()= fireStoreInstance.document("Users/${mAuth.currentUser?.uid.toString()}")

    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivitySignUpBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        textWatcher(this)

        binding.btnRegister.setOnClickListener {
            val name=binding.edtNameSignUp.text.toString().trim()
            val email=binding.edtEmailSignUp.text.toString().trim()
            val password=binding.edtPassSignUp.text.toString().trim()


            if (name.isEmpty()){
                binding.edtNameSignUp.error="Name is required"
                binding.edtNameSignUp.requestFocus()
                return@setOnClickListener
            }
            if (email.isEmpty()) {
                binding.edtEmailSignUp.error = "Email is required"
                binding.edtEmailSignUp.requestFocus()
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()){
                binding.edtEmailSignUp.error="Email is not formatted right"
                binding.edtEmailSignUp.requestFocus()
                return@setOnClickListener
            }
            if (password.length<6){
                binding.edtPassSignUp.error="Password should be more than 6 characters"
                binding.edtPassSignUp.requestFocus()
                return@setOnClickListener
            }
            createAccount(name,email,password, profilePic = "")
        }

    }

    override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {

    }
    override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
        binding.btnRegister.isEnabled= binding.edtNameSignUp.text.trim().isNotEmpty()
                &&binding.edtEmailSignUp.text.trim().isNotEmpty()
                &&binding.edtPassSignUp.text.trim().isNotEmpty()
    }
    override fun afterTextChanged(s: Editable?) {
        val str: String = binding.edtPassSignUp.text.toString()
        if (str.isNotEmpty() && str.contains(" ")) {
            binding.edtPassSignUp.setText(binding.edtPassSignUp.text.toString().replace(" ", ""))
            binding.edtPassSignUp.setSelection(binding.edtPassSignUp.text.length)
        }
    }
    private fun textWatcher(watcher: TextWatcher){
        binding.edtNameSignUp.addTextChangedListener(watcher)
        binding.edtEmailSignUp.addTextChangedListener(watcher)
        binding.edtPassSignUp.addTextChangedListener(watcher)
    }

    private fun createAccount(name:String,email:String,password:String,profilePic:String){
        binding.progressBar.visibility= View.VISIBLE
        mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener {
            currentUserDoc.set(Users(name,profilePic,""))
            if (it.isSuccessful){
                binding.progressBar.visibility= View.INVISIBLE
                Toast.makeText(this, "Successfully registered", Toast.LENGTH_SHORT).show()
                val toMainActivity= Intent(this, MainActivity::class.java)
                toMainActivity.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(toMainActivity)
            }else{
                binding.progressBar.visibility= View.INVISIBLE
                Toast.makeText(this, "${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}