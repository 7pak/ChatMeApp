package io.abood.messengerfacebood.recycler_view

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.squareup.okhttp.internal.http.HttpDate.format
import com.xwray.groupie.viewbinding.BindableItem
import io.abood.messengerfacebood.R
import io.abood.messengerfacebood.databinding.RecyclerViewItemBinding
import io.abood.messengerfacebood.model.GlideApp
import io.abood.messengerfacebood.model.TextMessage
import io.abood.messengerfacebood.model.Users
import java.text.Format
import java.text.SimpleDateFormat

class ChatItem(val uid:String, val users: Users,val textMessage: TextMessage, val context: Context): BindableItem<RecyclerViewItemBinding>() {

    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    private val fireStoreBase by lazy {
        FirebaseFirestore.getInstance()
    }
    private val currentUserDocRef:DocumentReference
    get() = fireStoreBase.document("Users/$uid")

    override fun bind(viewBinding: RecyclerViewItemBinding, position: Int) {

       getUserInfo {users->
           viewBinding.userNameChats.text= users.name

           val time =android.text.format.DateFormat.format("hh:mm: a",textMessage.date)
           viewBinding.lastChat.text=textMessage.text
           viewBinding.dateChats.text= time

           if (users.profilePic.isNotEmpty()){
               GlideApp.with(context)
                   .load(storageInstance.getReference(users.profilePic))
                   .into(viewBinding.circleChatsImage)
           }else viewBinding.circleChatsImage.setImageResource(R.drawable.ic_profile)
       }
    }

    override fun getLayout(): Int {
        return R.layout.recycler_view_item
    }

    override fun initializeViewBinding(view: View): RecyclerViewItemBinding {
        return RecyclerViewItemBinding.bind(view)
    }
     fun getUserInfo(onComplete:(Users)->Unit){
        currentUserDocRef.get().addOnSuccessListener {

            try {
                onComplete(it.toObject(Users::class.java)!!)
            }catch (e:NullPointerException){
                Log.e("MainActivity","nullable exception")
            }
        }

    }

}