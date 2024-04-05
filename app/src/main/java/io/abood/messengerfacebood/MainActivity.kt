package io.abood.messengerfacebood

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.firebase.ui.auth.data.model.User
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import io.abood.messengerfacebood.databinding.ActivityMainBinding
import io.abood.messengerfacebood.fragment.ChatsFrag
import io.abood.messengerfacebood.fragment.MoreFrag
import io.abood.messengerfacebood.fragment.PeopleFrag
import io.abood.messengerfacebood.model.GlideApp
import io.abood.messengerfacebood.model.Users

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStoreBase by lazy {
        FirebaseFirestore.getInstance()
    }
    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private val currentDataDoc: DocumentReference
        get() = fireStoreBase.document("Users/${mAuth.currentUser?.uid.toString()}")
    private lateinit var currentUsers: Users

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""


        getData {
            try {
                GlideApp.with(this)
                    .load(storageInstance.getReference(it.profilePic))
                    .into(binding.circleImage)
            } catch (e: Exception) {
                binding.circleImage.setImageResource(R.drawable.ic_profile)
            }
        }

        setFragment(ChatsFrag())

        binding.circleImage.setOnClickListener {
            startActivity(Intent(this, ProfileActivity::class.java))
        }

        binding.searchActivity.setOnClickListener {
            startActivity(Intent(this,SearchActivity::class.java))
        }

        binding.navBottom.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.chat_menu -> {
                    setFragment(ChatsFrag())
                }
                R.id.people_menu -> {
                    setFragment(PeopleFrag())
                }
                R.id.more_menu -> {
                    setFragment(MoreFrag())
                }
            }
            true
        }
    }

    private fun setFragment(fragment: Fragment) {
        val fr = supportFragmentManager.beginTransaction()
        fr.replace(R.id.coordinator, fragment)
        fr.commit()
    }

    private fun getData(onComplete: (Users) -> Unit) {
        currentDataDoc.get().addOnSuccessListener {
            onComplete(it.toObject(Users::class.java)!!)
        }
    }


}