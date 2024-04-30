package com.example.aurora.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aurora.Models.SkodecModel
import com.example.aurora.R
import com.google.firebase.auth.FirebaseAuth

class SkodceAdapter(private val skodceList : List<SkodecModel>) : RecyclerView.Adapter<SkodceAdapter.SkodceViewHolder>(){
    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mListener : onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        mListener = clickListener
    }

    class SkodceViewHolder(itemView : View, private val clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val skodec : TextView = itemView.findViewById(R.id.skodec)
        val lokacia : TextView = itemView.findViewById(R.id.lokacia)
        val pridal : TextView = itemView.findViewById(R.id.pridal)

        init{
            itemView.setOnClickListener{
                clickListener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SkodceViewHolder {
        firebaseAuth = FirebaseAuth.getInstance()
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.skodec_item, parent,false)
        return SkodceViewHolder(itemView, mListener)
    }

    override fun getItemCount(): Int {
        return skodceList.size
    }

    override fun onBindViewHolder(holder: SkodceViewHolder, position: Int) {
        val currentitem = skodceList[position]

        holder.skodec.text = currentitem.nazovSkodca
        holder.lokacia.text = currentitem.lokalita
        holder.pridal.text = currentitem.userName
    }
}