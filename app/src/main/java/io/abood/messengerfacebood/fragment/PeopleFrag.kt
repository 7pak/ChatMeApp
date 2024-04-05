package io.abood.messengerfacebood.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import io.abood.messengerfacebood.R


class PeopleFrag : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val titleToolbar= activity?.findViewById<TextView>(R.id.toolbar_title)
        titleToolbar?.text="People"
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_people, container, false)
    }

}