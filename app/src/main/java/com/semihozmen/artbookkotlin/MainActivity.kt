package com.semihozmen.artbookkotlin

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.semihozmen.artbookkotlin.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding:ActivityMainBinding
    private lateinit var artList: ArrayList<Art>
    private lateinit var artAdapter: ArtAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "Art Book"
        artList = ArrayList()
        binding.rv.layoutManager = LinearLayoutManager(this)
        artAdapter = ArtAdapter(artList)
        binding.rv.adapter = artAdapter


        addMenuProvider(object : MenuProvider{
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                menuInflater.inflate(R.menu.menu,menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                if(menuItem.itemId == R.id.menu_add){
                    val intent = Intent(this@MainActivity,ArtActivity::class.java)
                    intent.putExtra("info","new")
                    startActivity(intent)
                }

                return true
            }

        })

        try {

            val  database = this.openOrCreateDatabase("Arts", MODE_PRIVATE,null)
            val cursor = database.rawQuery("Select * from arts",null)
            val artNameIX = cursor.getColumnIndex("artName")
            val artIdIX = cursor.getColumnIndex("id")

            while (cursor.moveToNext()){
                val name = cursor.getString(artNameIX)
                val id = cursor.getInt(artIdIX)
                val art = Art(id,name)
                artList.add(art)
            }
            artAdapter.notifyDataSetChanged()
            cursor.close()

        }catch (e:Exception){
            e.printStackTrace()
        }
    }
}