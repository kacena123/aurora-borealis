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

class Predpoved2Adapter(private val pocasieList : ArrayList<HodinuPoHodineModel>) : RecyclerView.Adapter<Predpoved2Adapter.Predpoved2ViewHolder>(){

    class Predpoved2ViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val datum : TextView = itemView.findViewById(R.id.hdatum)
        val cas : TextView = itemView.findViewById(R.id.hcas)
        val teplota : TextView = itemView.findViewById(R.id.hteplota)
        val pravdepodobnost : TextView = itemView.findViewById(R.id.hpravdepodobnost)
        val zrazky : TextView = itemView.findViewById(R.id.hzrazku)
        val ikona : ImageView = itemView.findViewById(R.id.hikona)

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Predpoved2ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.predpoved_item, parent,false)
        return Predpoved2ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: Predpoved2ViewHolder, position: Int) {
        val currentitem = pocasieList[position]
        /*
        var temp = (currentitem.main.temp - 273.15).roundToInt()
        var pop = currentitem.pop *100

        holder.teplota.text = temp.toString()
        holder.cas.text = currentitem.dt_txt.substring(5,10)
        holder.datum.text = currentitem.dt_txt.substring(11, 15)
        holder.zrazky.text = currentitem.rain.toString()
        holder.pravdepodobnost.text = pop.toString() + " %"
        Picasso.get()
            .load("https://pro.openweathermap.org/img/w/" + currentitem.weather[0].icon + ".png")
            .into(holder.ikona)

         */

        holder.teplota.text = currentitem.teplota
        holder.cas.text = currentitem.cas
        holder.datum.text = currentitem.datum
        holder.zrazky.text = currentitem.zrazky
        holder.pravdepodobnost.text = currentitem.pravdepodobnost
        Picasso.get()
            .load("https://pro.openweathermap.org/img/w/" + currentitem.ikona + ".png")
            .into(holder.ikona)



    }

    override fun getItemCount(): Int {
        return pocasieList.size
    }



}