package io.abood.messengerfacebood

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.abood.messengerfacebood.databinding.ActivityProfileBinding
import io.abood.messengerfacebood.model.GlideApp
import io.abood.messengerfacebood.model.Users
import java.io.ByteArrayOutputStream
import java.util.*

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding
    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private val mAuth: FirebaseAuth by lazy {
        FirebaseAuth.getInstance()
    }
    private val fireStoreBase by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentStorageDoc: StorageReference
        get() = storageInstance.reference.child(mAuth.currentUser?.uid.toString())
    private val currentDataDoc: DocumentReference
        get() = fireStoreBase.document("Users/${mAuth.currentUser?.uid.toString()}")

    private lateinit var selectedBitmap:ByteArray
    private lateinit var userName:String

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityProfileBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Me"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setHomeButtonEnabled(true)
        binding.signOut.setOnClickListener {
            mAuth.signOut()
            val toLogIn= Intent(this, LogIn::class.java)
            toLogIn.flags= Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(toLogIn)
        }
//
        getData { users ->
            userName=users.name
//            you should reBuild the project before use glideApp
            try {
            GlideApp.with(this)
                .load(storageInstance.getReference(users.profilePic))
                .into(binding.profileChangeImage)
            binding.usernameProfile.text=userName
                }catch (e:Exception){
                    binding.profileChangeImage.setImageResource(R.drawable.ic_profile)
                }
        }


        val imageSrc = registerForActivityResult(ActivityResultContracts.GetContent()){ uri->

            if (uri!=null){
//            here we compress the image inorder to reduce its size
                val outputStream= ByteArrayOutputStream()
                selectedBitmap = if (Build.VERSION.SDK_INT<28) {
                    val bimap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    bimap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)
                    outputStream.toByteArray()
                }else{
                    val source = ImageDecoder.createSource(this.contentResolver, uri)
                    val bitmap = ImageDecoder.decodeBitmap(source)
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 25, outputStream)
                    outputStream.toByteArray()
                }

                uploadImageFile(selectedBitmap,uri) { path ->
                    val userField = mutableMapOf<String, Any>()
                    userField["name"] = userName
                    userField["profilePic"] = path
                    currentDataDoc.update(userField)
                }
            }
        }
        binding.profileChangeImage.setOnClickListener {
            imageSrc.launch("image/*")
        }
    }

    private fun uploadImageFile(selectedBitmap: ByteArray, uri: Uri, onSuccess:(String)->Unit) {
        val ref= currentStorageDoc.child("Profile picture/${UUID.nameUUIDFromBytes(selectedBitmap)}")
        binding.progressBar.visibility= View.VISIBLE
        ref.putBytes(selectedBitmap).addOnCompleteListener {
            if (it.isSuccessful){
                onSuccess(ref.path)
                binding.progressBar.visibility= View.INVISIBLE
                binding.profileChangeImage.setImageURI(uri)
            }else{
                Toast.makeText(this, "error: ${it.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId==android.R.id.home){
            finish()
            return true
        }
        return false
    }
    private fun getData(onComplete:(Users)->Unit){
        currentDataDoc.get().addOnSuccessListener {
            onComplete(it.toObject(Users::class.java)!!)
        }
    }
}