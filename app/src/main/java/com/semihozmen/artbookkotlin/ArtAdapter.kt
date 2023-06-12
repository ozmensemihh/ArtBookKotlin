package com.semihozmen.artbookkotlin

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.semihozmen.artbookkotlin.databinding.RecyclerRowBinding

class ArtAdapter (val list: ArrayList<Art>) :RecyclerView.Adapter<ArtAdapter.ArtHolder>() {


    class ArtHolder (val binding: RecyclerRowBinding) : RecyclerView.ViewHolder(binding.root){

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ArtHolder {
        val binding = RecyclerRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return ArtHolder(binding)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ArtHolder, position: Int) {
        holder.binding.txtRow.text = list.get(position).name

        holder.itemView.setOnClickListener{
            val intent = Intent(holder.itemView.context,ArtActivity::class.java)
            intent.putExtra("info","old")
            intent.putExtra("id",list[position].id)
            holder.itemView.context.startActivity(intent)
        }

    }
}