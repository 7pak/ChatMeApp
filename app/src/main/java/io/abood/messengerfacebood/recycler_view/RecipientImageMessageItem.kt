package io.abood.messengerfacebood.recycler_view

import android.content.Context
import android.view.View
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.viewbinding.BindableItem
import io.abood.messengerfacebood.R
import io.abood.messengerfacebood.databinding.RecipientChatImageBinding
import io.abood.messengerfacebood.model.GlideApp
import io.abood.messengerfacebood.model.ImageMessage
import io.abood.messengerfacebood.model.TextMessage

class RecipientImageMessageItem(
    private val imageMessage: ImageMessage
    ,private val messageId:String,
    private val context: Context
): BindableItem<RecipientChatImageBinding>(){
    private val storageInstance: FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }

    override fun bind(viewBinding: RecipientChatImageBinding, position: Int) {
        viewBinding.messageTime.text= android.text.format.DateFormat.format("hh:mm: a",imageMessage.date)
        try {
            GlideApp.with(context)
                .load(storageInstance.getReference(imageMessage.imagePath))
                .into(viewBinding.imageChatView)
        } catch (e: Exception) {
            viewBinding.imageChatView.setImageResource(R.drawable.ic_send_image)
        }

    }

    override fun getLayout()= R.layout.recipient_chat_image

    override fun initializeViewBinding(view: View): RecipientChatImageBinding {
        return RecipientChatImageBinding.bind(view)
    }
}