package com.example.aurora.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aurora.Models.PoleModel
import com.example.aurora.R
import com.google.firebase.auth.FirebaseAuth

class PoleAdapter(private val poleList : ArrayList<PoleModel>) : RecyclerView.Adapter<PoleAdapter.PoleViewHolder>() {

    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var mListener : onItemClickListener
    interface onItemClickListener{
        fun onItemClick(position: Int)
    }

    fun setOnItemClickListener(clickListener: onItemClickListener){
        mListener = clickListener
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PoleViewHolder {
        firebaseAuth = FirebaseAuth.getInstance()
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.pole_item, parent,false)
        return PoleViewHolder(itemView, mListener)
    }

    override fun getItemCount(): Int {
        return poleList.size
    }

    override fun onBindViewHolder(holder: PoleViewHolder, position: Int) {
        val currentitem = poleList[position]

        holder.nazov.text = currentitem.nazovPola
        holder.subtitle.text = currentitem.plodina


    }


    class PoleViewHolder(itemView : View, clickListener: onItemClickListener) : RecyclerView.ViewHolder(itemView){
        val nazov : TextView = itemView.findViewById(R.id.mTitle)
        val subtitle : TextView = itemView.findViewById(R.id.mSubTitle)

        init{
            itemView.setOnClickListener{
                clickListener.onItemClick(adapterPosition)
            }
        }
    }


    /*
    class PoleViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val nazov : TextView = itemView.findViewById(R.id.mTitle)
        val subtitle : TextView = itemView.findViewById(R.id.mSubTitle)
    }

     */
}