package com.cemalipek.artbook2

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.cemalipek.artbook2.databinding.FragmentArtListBinding
import kotlinx.android.synthetic.main.fragment_art_list.*

class ArtList : Fragment() {

    var artNameList = ArrayList<String>()
    var artIdList = ArrayList<Int>()
    private lateinit var listAdapter : ListRecyclerAdapter
    private var _binding : FragmentArtListBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentArtListBinding.inflate(inflater , container , false)
        val view = binding.root
        return view

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        listAdapter = ListRecyclerAdapter(artNameList,artIdList)
        binding.recyclerView.layoutManager = LinearLayoutManager(context)
        binding.recyclerView.adapter = listAdapter
        sqlDataImport()

    }


    private fun sqlDataImport(){
        try {
            activity?.let {
                val database = it.openOrCreateDatabase("arts", Context.MODE_PRIVATE, null)

                val cursor = database.rawQuery("SELECT * FROM arts", null)
                val artNameIx = cursor.getColumnIndex("artName")
                val artIdIx = cursor.getColumnIndex("id")

                artNameList.clear()
                artIdList.clear()

                while (cursor.moveToNext()){
                    artNameList.add(cursor.getString(artNameIx))
                    artIdList.add(cursor.getInt(artIdIx))

                }

                listAdapter.notifyDataSetChanged()

                cursor.close()
            }
        }catch (e : Exception){
            e.printStackTrace()
        }
    }


}