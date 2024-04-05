package io.abood.messengerfacebood.recycler_view

import android.content.Context
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import io.abood.messengerfacebood.R
import io.abood.messengerfacebood.databinding.RecipientChatMessageBinding
import io.abood.messengerfacebood.model.TextMessage

class RecipientTxtMessageItem (
    private val textMessage: TextMessage
    ,private val messageId:String,
    private val context: Context): BindableItem<RecipientChatMessageBinding>(){
    override fun bind(viewBinding: RecipientChatMessageBinding, position: Int) {
        viewBinding.messageSent.text=textMessage.text
        viewBinding.messageTime.text= android.text.format.DateFormat.format("hh:mm: a",textMessage.date)
    }
    override fun getLayout()= R.layout.recipient_chat_message

    override fun initializeViewBinding(view: View): RecipientChatMessageBinding {
        return RecipientChatMessageBinding.bind(view)
    }
}
