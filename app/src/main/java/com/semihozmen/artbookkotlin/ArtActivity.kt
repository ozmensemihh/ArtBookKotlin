package com.semihozmen.artbookkotlin

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.semihozmen.artbookkotlin.databinding.ActivityArtBinding
import java.io.ByteArrayOutputStream

class ArtActivity : AppCompatActivity() {

    private lateinit var binding: ActivityArtBinding
    private lateinit var activityResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    var selectedBitmap :Bitmap? = null
    private lateinit var database: SQLiteDatabase

    @SuppressLint("Range")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtBinding.inflate(layoutInflater)
        setContentView(binding.root)

        registerLauncher()
        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)

        val intent = intent
        val info = intent.getStringExtra("info")

        if(!info.equals("new")){
            binding.btnSave.visibility = View.INVISIBLE
            val id = intent.getIntExtra("id",1)
            Toast.makeText(this,id.toString(),Toast.LENGTH_LONG).show()
            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(id.toString()))

            while (cursor.moveToNext()){
                binding.edtArtName.setText(cursor.getString(cursor.getColumnIndex("artName")))
                binding.edtArtistName.setText(cursor.getString(cursor.getColumnIndex("artistName")))
                binding.edtYear.setText(cursor.getString(cursor.getColumnIndex("year")))
                binding.imageView.setImageBitmap(
                    BitmapFactory.decodeByteArray(cursor.getBlob(cursor.getColumnIndex("image")),0,cursor.getBlob(cursor.getColumnIndex("image")).size)
                )

                cursor.close()

            }

        }else{
            binding.edtArtName.setText("")
            binding.edtArtistName.setText("")
            binding.edtYear.setText("")
            binding.btnSave.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.slect)
        }

    }



    fun save(view:View){

        if(selectedBitmap != null){
            val smallBitmap = makeSmallBitmap(selectedBitmap!!,300)
            val outputstream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG,50,outputstream)
            val byteArray = outputstream.toByteArray()


            try {

                //database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id INT PRIMARY KEY, artName VARCHAR, artistName VARCHAR,year VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO arts (artName,artistName,year,image) VALUES (?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,binding.edtArtName.text.toString())
                statement.bindString(2,binding.edtArtistName.text.toString())
                statement.bindString(3,binding.edtYear.text.toString())
                statement.bindBlob(4,byteArray)
                statement.execute()


            }catch (e : Exception){
                e.printStackTrace()
            }

            val intent = Intent(this@ArtActivity,MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)

        }


    }

    private fun makeSmallBitmap(image : Bitmap, maxSize: Int):Bitmap{

        var with = image.width
        var height = image.height
        val bitmapRetio :Double = with.toDouble()/height.toDouble() // yatay veya dikey olup olmadığı kontrol ediliyor.

        if(bitmapRetio > 1){
            with = maxSize
            val scaleHeight = with/bitmapRetio
            height = scaleHeight.toInt()
        }else{
            height = maxSize
            val scalewith = height*bitmapRetio
            with = scalewith.toInt()

        }

        return Bitmap.createScaledBitmap(image,with,height,true)


    }

    fun selectImage(view: View){

        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.TIRAMISU){
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_MEDIA_IMAGES)){
                    Snackbar.make(view,"Permission needed",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission",View.OnClickListener {
                            // Request Permission
                            permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                        }).show()
                }else{
                    // Request Permission
                    permissionLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                }
            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }
        }else{
            if(ContextCompat.checkSelfPermission(this,Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Snackbar.make(view,"Permission needed",Snackbar.LENGTH_INDEFINITE)
                        .setAction("Give Permission",View.OnClickListener {
                            // Request Permission
                            permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                        }).show()
                }else{
                    // Request Permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }else{
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)

            }
        }



    }

    private fun registerLauncher(){
        activityResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){result ->
            if(result.resultCode == RESULT_OK){
                if(result.data != null){
                    val imageData = result.data!!.data
                   // binding.imageView.setImageURI(imageData)
                    if(imageData != null){
                        try {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                                val source = ImageDecoder.createSource(this@ArtActivity.contentResolver,imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver,imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }

                        }catch (e:Exception){
                            e.printStackTrace()
                        }
                    }

                }
            }
        }

        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                Toast.makeText(this@ArtActivity,"Request Permission",Toast.LENGTH_LONG).show()
            }
        }
    }
}