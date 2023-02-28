package com.cemalipek.artbook2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import com.cemalipek.artbook2.databinding.RecyclerRowBinding
import kotlinx.android.synthetic.main.recycler_row.view.*

class ListRecyclerAdapter(val artList : ArrayList<String>,val idList : ArrayList<Int>) : RecyclerView.Adapter<ListRecyclerAdapter.ArtHolder>() {

    class ArtHolder(val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)

    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.recyclerRowText.text = artList[position]
        holder.binding.recyclerRowText.setOnClickListener {
            val action = ArtListDirections.actionArtListToUpload2("recyclerdangeldim", idList[position])
            Navigation.findNavController(it).navigate(action)
        }
    }

    override fun getItemCount(): Int {
        return artList.size
    }


}