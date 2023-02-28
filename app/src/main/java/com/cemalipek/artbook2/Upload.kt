package com.cemalipek.artbook2

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.navigation.Navigation
import com.cemalipek.artbook2.databinding.FragmentUploadBinding
import kotlinx.android.synthetic.main.fragment_upload.*
import java.io.ByteArrayOutputStream


class Upload : Fragment() {

    var selectedImage : Uri? = null
    var selectedBitmap : Bitmap? = null
    private var _binding : FragmentUploadBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentUploadBinding.inflate(inflater, container, false)
        val view = binding.root
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.imageView.setOnClickListener {
            imageSelect(it)

        }

        binding.saveButton.setOnClickListener {
            save(it)

        }

        arguments?.let {
            var gelenBilgi = UploadArgs.fromBundle(it).bilgi

            if (gelenBilgi.equals("menudengeldim")){
                //yeni bir foto ekleme
                binding.ArtNameText.setText("")
                binding.ArtistNameText.setText("")
                binding.ArtYearText.setText("")
                binding.saveButton.visibility = View.VISIBLE

                val selectImageBackground = BitmapFactory.decodeResource(context?.resources,R.drawable.selectimage)
                imageView.setImageBitmap(selectImageBackground)
            }else{
                //daha önce oluşturulan fotoyu görmeye geldi
                binding.saveButton.visibility = View.INVISIBLE

                val selectedId = UploadArgs.fromBundle(it).id

                context?.let {
                    try {
                        val db = it.openOrCreateDatabase("arts",Context.MODE_PRIVATE,null)
                        val cursor = db.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

                        val artNameIx = cursor.getColumnIndex("artName")
                        val artistNameIx = cursor.getColumnIndex("artistName")
                        val yearIx = cursor.getColumnIndex("year")
                        val artImage = cursor.getColumnIndex("image")

                        while (cursor.moveToNext()){

                            binding.ArtNameText.setText(cursor.getString(artNameIx))
                            binding.ArtistNameText.setText(cursor.getString(artistNameIx))
                            binding.ArtYearText.setText(cursor.getString(yearIx))

                            val byteArray = cursor.getBlob(artImage)
                            val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                            imageView.setImageBitmap(bitmap)

                        }
                        cursor.close()

                    }catch (e: java.lang.Exception){
                        e.printStackTrace()
                    }
                }

            }
        }
    }

    fun save(view: View){
        //SQLite save
        val artName = binding.ArtNameText.text.toString()
        val artistName = binding.ArtistNameText.text.toString()
        val year = binding.ArtYearText.text.toString()


        if (selectedBitmap != null){
            val smallBitmap = makeSmallerBitmap(selectedBitmap!!,300)

            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                context?.let{
                    val database = it.openOrCreateDatabase("arts", Context.MODE_PRIVATE,null)
                    database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INTEGER PRIMARY KEY, artName VARCHAR, artistName VARCHAR, year VARCHAR, image BLOB)")

                    val sqlString = "INSERT INTO arts (artName, artistName, year, image) VALUES(?, ?, ?, ?)"
                    val statement = database.compileStatement(sqlString)
                    statement.bindString(1,artName)
                    statement.bindString(2,artistName)
                    statement.bindString(3,year)
                    statement.bindBlob(4,byteArray)
                    statement.execute()
                }


            }catch (e : Exception){
                e.printStackTrace()
            }

            val action = UploadDirections.actionUpload2ToArtList()
            Navigation.findNavController(view).navigate(action)


        }
    }

    fun imageSelect(view: View){
        activity?.let {
            if(ContextCompat.checkSelfPermission(it.applicationContext,android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),1)
            }else{
                val galeryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeryIntent, 2)

            }

        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == 1){
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                //izni aldık
                val galeryIntent = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                startActivityForResult(galeryIntent, 2)

            }
        }


        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        if (requestCode == 2 && resultCode == Activity.RESULT_OK && data != null){

            selectedImage = data.data

            try {

                context?.let {
                    if (selectedImage != null ){
                        if (Build.VERSION.SDK_INT >= 28){
                            val source = ImageDecoder.createSource(it.contentResolver,selectedImage!!)
                            selectedBitmap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitmap)
                        }else{
                            selectedBitmap = MediaStore.Images.Media.getBitmap(it.contentResolver, selectedImage)
                            binding.imageView.setImageBitmap(selectedBitmap)

                        }

                    }


                }

            }catch (e: Exception){
                e.printStackTrace()
            }



        }
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun makeSmallerBitmap(image: Bitmap, maximumSize : Int) : Bitmap {
        var width = image.width
        var height = image.height

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if (bitmapRatio > 1){
            //landscape
            width = maximumSize
            val  scaledHeight = width / bitmapRatio
            height = scaledHeight.toInt()
        }else{
            //portrait
            height = maximumSize
            val scaledWidth = height * bitmapRatio
            width = scaledWidth.toInt()

        }
        return Bitmap.createScaledBitmap(image, width, height, true)

    }


}