package com.example.test_gitlad.Adapter

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.example.test_gitlad.R

class CustomSpinnerAdapter(context: Context, items: Array<String>) :
    ArrayAdapter<String>(context, R.layout.spinner_item, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getView(position, convertView, parent)
        val textView = view.findViewById<TextView>(R.id.spinner_item_text)
        textView.text = getItem(position)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = super.getDropDownView(position, convertView, parent)
        val textView = view.findViewById<TextView>(R.id.spinner_item_text)
        textView.text = getItem(position)
        return view
    }
}
