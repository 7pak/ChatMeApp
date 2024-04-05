package io.abood.messengerfacebood.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.toObject
import com.xwray.groupie.*
import com.xwray.groupie.viewbinding.BindableItem
import io.abood.messengerfacebood.ChatActivity
import io.abood.messengerfacebood.R
import io.abood.messengerfacebood.SearchActivity
import io.abood.messengerfacebood.databinding.FragmentChatsBinding
import io.abood.messengerfacebood.databinding.RecyclerViewItemBinding
import io.abood.messengerfacebood.model.TextMessage
import io.abood.messengerfacebood.recycler_view.ChatItem
import io.abood.messengerfacebood.model.Users


class ChatsFrag : Fragment() {
    private lateinit var binding: FragmentChatsBinding

    private val fireStoreBase by lazy {
        FirebaseFirestore.getInstance()
    }
    private val mAuth by lazy {
        FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        val titleToolbar= activity?.findViewById<TextView>(R.id.toolbar_title)
        titleToolbar?.text="Chats"
        addChatListener(::initRecycler)

         binding=FragmentChatsBinding.inflate(inflater, container, false)
        return binding.root
    }
    private fun addChatListener(onListen:(item:List<BindableItem<RecyclerViewItemBinding>>)->Unit):ListenerRegistration {
        return fireStoreBase.collection("Users").document(mAuth.currentUser!!.uid)
            .collection("SharedChat").orderBy("date", Query.Direction.DESCENDING)
            .addSnapshotListener { value, error ->
            if (error != null) {
                return@addSnapshotListener
            }
            val items = mutableListOf<BindableItem<RecyclerViewItemBinding>>()
            value?.documents?.forEach {

                if (isAdded) {//Return true if the fragment is currently added to its activity.
                    items.add(ChatItem(it.id,it.toObject(Users::class.java)!!,it.toObject(TextMessage::class.java)!!, requireContext()))
                }
            }
            onListen(items)
        }
    }

    private fun initRecycler(item:List<BindableItem<RecyclerViewItemBinding>>){
        binding.myRecycler.apply {
            if (isAdded) {
                layoutManager = LinearLayoutManager(requireContext())
                adapter = GroupAdapter<GroupieViewHolder>().apply {
                    add(Section(item))
                    setOnItemClickListener(onClick)
                }
            }
        }
    }
      private val onClick=OnItemClickListener{ item, view ->
          item as ChatItem
          item.getUserInfo {users->

              val toChatActivity = Intent(requireContext(), ChatActivity::class.java)
              toChatActivity.putExtra("profilePic", users.profilePic)
              toChatActivity.putExtra("username", users.name)
              toChatActivity.putExtra("other_UserUid",item.uid)
              activity?.startActivity(toChatActivity)
          }
      }
}