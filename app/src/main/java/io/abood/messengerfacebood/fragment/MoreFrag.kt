package io.abood.messengerfacebood.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.abood.messengerfacebood.R


class MoreFrag : Fragment() {


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val titleToolbar= activity?.findViewById<TextView>(R.id.toolbar_title)
        titleToolbar?.text="Discover"
        return inflater.inflate(R.layout.fragment_more, container, false)
    }

}