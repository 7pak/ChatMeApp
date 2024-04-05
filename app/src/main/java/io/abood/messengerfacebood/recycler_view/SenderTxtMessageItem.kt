package io.abood.messengerfacebood.recycler_view

import android.content.Context
import android.view.View
import com.xwray.groupie.viewbinding.BindableItem
import io.abood.messengerfacebood.R
import io.abood.messengerfacebood.databinding.SenderChatMessageBinding
import io.abood.messengerfacebood.model.TextMessage

class SenderTxtMessageItem(
      private val textMessage: TextMessage
      ,private val messageId:String,
      private val context: Context): BindableItem<SenderChatMessageBinding>(){

    override fun bind(viewBinding: SenderChatMessageBinding, position: Int) {
       viewBinding.messageSent.text=textMessage.text
        viewBinding.messageTime.text= android.text.format.DateFormat.format("hh:mm: a",textMessage.date)
    }

    override fun getLayout()= R.layout.sender_chat_message

    override fun initializeViewBinding(view: View): SenderChatMessageBinding {
        return SenderChatMessageBinding.bind(view)
    }
}

