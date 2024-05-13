package com.example.aurora.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aurora.Models.NotifikacieModel
import com.example.aurora.R
import com.google.firebase.auth.FirebaseAuth

class NotifikacieAdapter(private val NotifikacieList : List<NotifikacieModel>) : RecyclerView.Adapter<NotifikacieAdapter.NotifikacieViewHolder>() {

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var mListener : NotifikacieAdapter.onItemClickListener

    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        mListener = clickListener
    }

    class NotifikacieViewHolder(itemView : View, private val clickListener: NotifikacieAdapter.onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val cas : TextView = itemView.findViewById(R.id.cas)
        val teplota : TextView = itemView.findViewById(R.id.teplota)
        val notifikacie : TextView = itemView.findViewById(R.id.upozornenie)

        init{
            itemView.setOnClickListener{
                clickListener.onItemClick(adapterPosition)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotifikacieViewHolder {
        firebaseAuth = FirebaseAuth.getInstance()
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.upozornenie_item, parent,false)
        return NotifikacieViewHolder(itemView, mListener)
    }

    override fun getItemCount(): Int {
        return NotifikacieList.size
    }

    override fun onBindViewHolder(holder: NotifikacieViewHolder, position: Int) {
        val currentitem = NotifikacieList[position]

        holder.cas.text = "Hodiny: " + currentitem.hodiny
        holder.teplota.text = "Stupne: " + currentitem.teplota + "°C"
        holder.notifikacie.text = "Notifikácia: " + currentitem.nazovPola
    }
}