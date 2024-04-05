package io.abood.messengerfacebood.recycler_view

import android.content.Context
import android.util.Log
import android.view.View
import com.google.firebase.storage.FirebaseStorage
import com.xwray.groupie.Item
import com.xwray.groupie.viewbinding.BindableItem
import io.abood.messengerfacebood.R

import io.abood.messengerfacebood.databinding.SearchItemBinding
import io.abood.messengerfacebood.model.GlideApp
import io.abood.messengerfacebood.model.Users

class SearchItem(val users: Users,val uid:String,val context: Context):BindableItem<SearchItemBinding>() {
    private val firebaseStorage:FirebaseStorage by lazy {
        FirebaseStorage.getInstance()
    }
    override fun bind(viewBinding: SearchItemBinding, position: Int) {
        viewBinding.userNameSearch.text=users.name
        if (users.profilePic.isNotEmpty()){
        GlideApp.with(context)
            .load(firebaseStorage.getReference(users.profilePic))
            .into(viewBinding.circleSearchImage)
        }else viewBinding.circleSearchImage.setImageResource(R.drawable.ic_profile)
    }

    override fun getLayout()= R.layout.search_item

    override fun initializeViewBinding(view: View):SearchItemBinding{
      return  SearchItemBinding.bind(view)
    }

    override fun isSameAs(other: Item<*>): Boolean {
        if (other !is SearchItem)
            return false
        if (this.users != other.users)
            return false

        return true
    }

    override fun hashCode(): Int {
        var result = users.hashCode()
        result=31* result + context.hashCode()
        return result
    }

    override fun equals(other: Any?): Boolean {
        return isSameAs(other as SearchItem)
    }

}