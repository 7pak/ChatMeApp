package io.abood.messengerfacebood

import android.app.SearchManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.OnItemClickListener
import com.xwray.groupie.Section
import com.xwray.groupie.viewbinding.BindableItem
import io.abood.messengerfacebood.databinding.ActivitySearchBinding
import io.abood.messengerfacebood.databinding.SearchItemBinding
import io.abood.messengerfacebood.model.Users
import io.abood.messengerfacebood.recycler_view.SearchItem

class SearchActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySearchBinding
    val firebaseFirestore:FirebaseFirestore by lazy {
        FirebaseFirestore.getInstance()
    }
    private lateinit var sectionItem:Section
    private var isRecycleInit=true
    override fun onCreate(savedInstanceState: Bundle?) {
        binding=ActivitySearchBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar).apply {
            title =""
        }
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home->finish()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.search_menu,menu)

        val searchManager=getSystemService(Context.SEARCH_SERVICE) as SearchManager

        (menu?.findItem(R.id.action_search)?.actionView as SearchView).apply {

            setSearchableInfo(searchManager.getSearchableInfo(componentName))

            setOnQueryTextListener(object : SearchView.OnQueryTextListener{
                override fun onQueryTextSubmit(query: String?): Boolean {
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    if(newText!!.isEmpty()){
                        return false
                    }
                    val query=firebaseFirestore.collection("Users")
                        .orderBy("name")
                        .startAt(newText.trim())
                        .endAt(newText.trim() + "\uf8ff")

                    showRecyclerResult(::updateRecyclerView,query)
                    return true
                }

            })
        }
        return true
    }
    fun updateRecyclerView(item:List<BindableItem<SearchItemBinding>>){

        fun init(){

            binding.searchRecycler.apply {
                layoutManager=LinearLayoutManager(this@SearchActivity)
                adapter = GroupAdapter<GroupieViewHolder>().apply {
                    sectionItem=Section(item)
                    add(sectionItem)
                    setOnItemClickListener(onclick)
                }
                isRecycleInit=false
            }
        }

       fun updateItem()=sectionItem.update(item)

        if (isRecycleInit){
            init()
        } else  updateItem()
    }
    private val onclick= OnItemClickListener{ item, _ ->
        item as SearchItem
        val toChatActivity = Intent(this, ChatActivity::class.java)
        toChatActivity.putExtra("profilePic", item.users.profilePic)
        toChatActivity.putExtra("username", item.users.name)
        toChatActivity.putExtra("other_UserUid",item.uid)
        startActivity(toChatActivity)
    }
    fun showRecyclerResult(onListen:(item:List<BindableItem<SearchItemBinding>>)->Unit,query: Query){
//        Log.d("MainActivity","the method worked")
        val items = mutableListOf<BindableItem<SearchItemBinding>>()

        query.get().addOnSuccessListener {
            it.documents.forEach {docs->
//                Log.d("MainActivity1","this/;   "+docs.toObject(Users::class.java)!!.name)
                items.add(SearchItem(docs.toObject(Users::class.java)!!,docs.id,this@SearchActivity))
            }
            onListen(items)
        }

    }
}