package com.example.aurora.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aurora.Models.HourlyForecast4.HodinuPoHodineModel
import com.example.aurora.R
import com.squareup.picasso.Picasso

class PredpovedDlhodobaAdapter(private val pocasieList : ArrayList<HodinuPoHodineModel>) :RecyclerView.Adapter<PredpovedDlhodobaAdapter.PredpovedDlhodobaViewHolder>() {

    class PredpovedDlhodobaViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val datum : TextView = itemView.findViewById(R.id.hdatum)
        val cas : TextView = itemView.findViewById(R.id.hcas)
        val teplota : TextView = itemView.findViewById(R.id.hteplota)
        val pravdepodobnost : TextView = itemView.findViewById(R.id.hpravdepodobnost)
        val zrazky : TextView = itemView.findViewById(R.id.hzrazku)
        val ikona : ImageView = itemView.findViewById(R.id.hikona)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PredpovedDlhodobaViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.predpoved_item, parent,false)
        return PredpovedDlhodobaViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return pocasieList.size
    }

    override fun onBindViewHolder(holder: PredpovedDlhodobaViewHolder, position: Int) {
        val currentitem = pocasieList[position]
        holder.teplota.text = currentitem.teplota
        holder.cas.text = currentitem.cas
        holder.datum.text = currentitem.datum
        holder.zrazky.text = currentitem.zrazky
        holder.pravdepodobnost.text = currentitem.pravdepodobnost
        Picasso.get()
            .load("https://pro.openweathermap.org/img/w/" + currentitem.ikona + ".png")
            .into(holder.ikona)
    }


}