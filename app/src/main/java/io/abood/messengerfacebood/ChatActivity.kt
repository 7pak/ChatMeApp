package io.abood.messengerfacebood

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Section
import io.abood.messengerfacebood.databinding.ActivityChatBinding
import io.abood.messengerfacebood.model.*
import io.abood.messengerfacebood.notification.NotificationData
import io.abood.messengerfacebood.notification.PushNotification
import io.abood.messengerfacebood.notification.RetrofitInstance
import io.abood.messengerfacebood.recycler_view.RecipientTxtMessageItem
import io.abood.messengerfacebood.recycler_view.RecipientImageMessageItem
import io.abood.messengerfacebood.recycler_view.SenderImageMessageItem
import io.abood.messengerfacebood.recycler_view.SenderTxtMessageItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.util.*


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
        private val storageInstance:FirebaseStorage by lazy {
            FirebaseStorage.getInstance()
        }
    private val fireStoreBase by lazy {
        FirebaseFirestore.getInstance()
    }

    private val messageAdapter by lazy {
        GroupAdapter<GroupieViewHolder>()
    }
    private lateinit var currentChatId:String
    private val currentImageUploaded:StorageReference
    get() = storageInstance.reference
    private lateinit var recipientUid:String
    private val currentUser=FirebaseAuth.getInstance().currentUser?.uid

    private val currentDataDoc: DocumentReference
        get() = fireStoreBase.document("Users/${FirebaseAuth.getInstance().currentUser?.uid.toString()}")

    private val currentRecipientDoc: DocumentReference
        get() = fireStoreBase.document("Users/$recipientUid")

    private lateinit var usercurrent: Users
    private lateinit var recipientcurrent:Users

    override fun onCreate(savedInstanceState: Bundle?) {

       // val recNoty=intent.extras?.getString("senderId").toString()

        binding = ActivityChatBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        title=""

        binding.backButton.setOnClickListener {
            onBackPressed()
        }


        val profilePicInChat=intent.extras?.getString("profilePic")
        val userNameInChat= intent.extras?.getString("username")
        recipientUid= intent.extras?.getString("other_UserUid").toString()



        binding.recyclerChatMessages.apply {
            adapter=messageAdapter
        }


        starChatChannel{chatId->
            getMessage(chatId)
            currentChatId=chatId
            binding.sentMessage.setOnClickListener {
                val message=binding.editTextMessage.text.toString()
                val textMessage=TextMessage(message,currentUser!!,recipientUid,usercurrent.name,recipientcurrent.name,Calendar.getInstance().time)
                if (!message.isNullOrEmpty()) {
                    sendMessage(textMessage, chatId, message)
                    sendNotification(message)
                    binding.editTextMessage.text.clear()
                }

            }
        }

        binding.userNameInsideChat.text=userNameInChat
        try {
            GlideApp.with(this)
                .load(storageInstance.getReference(profilePicInChat!!))
                .into(binding.profilePicInChat)
        } catch (e: Exception) {
            binding.profilePicInChat.setImageResource(R.drawable.ic_profile)
        }
        getCurrentUserInfo { users ->
            usercurrent=users
        }

        getRecipientInfo { users ->
            recipientcurrent=users
        }

        val sendImage=registerForActivityResult(ActivityResultContracts.GetContent()) {uri->

            val outputStream=ByteArrayOutputStream()
            if (uri!=null){
            val selectedImage= if (Build.VERSION.SDK_INT<28){
                val bitmap= MediaStore.Images.Media.getBitmap(this.contentResolver,uri)
                bitmap.compress(Bitmap.CompressFormat.JPEG,25,outputStream)
                outputStream.toByteArray()
            }else{
                val source= ImageDecoder.createSource(this.contentResolver,uri)
                val bitmap= ImageDecoder.decodeBitmap(source)
                bitmap.compress(Bitmap.CompressFormat.JPEG,25,outputStream)
                outputStream.toByteArray()
            }
                uploadImage(selectedImage){path->
                    val imageUpload=ImageMessage(path,currentUser!!,recipientUid,usercurrent.name,recipientcurrent.name,Calendar.getInstance().time)
                 sendMessage(imageUpload,currentChatId,"PHOTO")
                }
            }
        }
        binding.sentImage.setOnClickListener {
            sendImage.launch("image/*")
        }
    }

    private fun starChatChannel(onComplete:(String)->Unit) {

        fireStoreBase.collection("Users").document(currentUser!!)
            .collection("SharedChat").document(recipientUid)
            .get().addOnSuccessListener {
                if (it.exists()) {
                    onComplete(it["chatId"] as String)
                    return@addOnSuccessListener
                }

                val chatChannelCode = fireStoreBase.collection("Users").document()

                fireStoreBase.collection("Users").document(currentUser)
                    .collection("SharedChat").document(recipientUid)
                    .set(mapOf("chatId" to chatChannelCode.id))

                fireStoreBase.collection("Users").document(recipientUid)
                    .collection("SharedChat").document(currentUser)
                    .set(mapOf("chatId" to chatChannelCode.id))
                onComplete(chatChannelCode.id)
            }
    }
    private fun sendMessage(message: Message,chatCode:String,text:String){

        val contentMessage= mutableMapOf<String,Any>()
        contentMessage["senderId"]=message.senderId
        contentMessage["recipientId"]=message.recipientId
        contentMessage["senderName"]=message.senderName
        contentMessage["recipientName"]=message.recipientName
        contentMessage["date"]=message.date
        contentMessage["type"]=message.type
        contentMessage["text"]=text

        fireStoreBase.collection("Users").document(currentUser!!).collection("SharedChat")
            .document(recipientUid).update(contentMessage)
        fireStoreBase.collection("Users").document(recipientUid).collection("SharedChat")
            .document(currentUser).update(contentMessage)

        fireStoreBase.collection("ChatChannel").document(chatCode)
            .collection("messages")
            .add(message)
    }
    private fun getMessage(chatId:String) {
        fireStoreBase.collection("ChatChannel").document(chatId)
            .collection("messages").orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, _ ->

                messageAdapter.clear()

                value?.documents?.forEach {
                    val message = it.toObject(TextMessage::class.java)
                    val image = it.toObject(ImageMessage::class.java)
                    if (it["type"]=="TEXT"){
                    if (message?.senderId == currentUser) {
                        messageAdapter.add(Section(SenderTxtMessageItem(it.toObject(TextMessage::class.java)!!, it.id, this)))
                    } else {
                        messageAdapter.add(Section(RecipientTxtMessageItem(it.toObject(TextMessage::class.java)!!, it.id, this)))
                    }}
                    else{
                        if (image?.senderId==currentUser){
                        messageAdapter.add(Section(SenderImageMessageItem(it.toObject(ImageMessage::class.java)!!,it.id,this)))
                    }else messageAdapter.add(Section(RecipientImageMessageItem(it.toObject(ImageMessage::class.java)!!,it.id,this)))
                    }
                }
            }
    }

    private fun uploadImage(selectedImage: ByteArray,onSuccess:(String)->Unit){
       val ref= currentImageUploaded.child("$currentUser/images/${UUID.nameUUIDFromBytes(selectedImage)}")

            ref.putBytes(selectedImage).addOnCompleteListener{
                if (it.isSuccessful){
                    onSuccess(ref.path)
                }else Toast.makeText(this, "error: ${it.exception?.message.toString()}", Toast.LENGTH_SHORT).show()
            }
    }
    private fun sendNotification(message:String){
        if (message.isNotEmpty()) {
            getCurrentUserInfo {sender->
                getRecipientInfo {recipient->
                    PushNotification(NotificationData(sender.name, message),recipient.token,currentUser!!).also {
                        pushNotification(it)
                    }
                }
            }
        }
    }
    private fun pushNotification(notification: PushNotification)=CoroutineScope(Dispatchers.IO).launch{
        try {
            val response = RetrofitInstance.api.pushNotification(notification)
            if (response.isSuccessful) {
                Log.d("MainActivity","response is successful")
            }else Log.e("MainActivity","response is not successful")
        }catch (e:Exception){
           Log.e("MainActivity","an error occurred")
        }
    }


    private fun getCurrentUserInfo(onComplete:(Users)->Unit){
        currentDataDoc.get().addOnSuccessListener {
            onComplete(it.toObject(Users::class.java)!!)
        }
    }
    private fun getRecipientInfo(onComplete:(Users) -> Unit){
        currentRecipientDoc.get().addOnSuccessListener {
            onComplete(it.toObject(Users::class.java)!!)
        }
    }



}