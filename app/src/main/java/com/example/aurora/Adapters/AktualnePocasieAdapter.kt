package com.example.aurora.Adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.aurora.Models.HourlyForecast4.AktualnePocasieModel
import com.example.aurora.Models.HourlyForecast4.Hour
import com.example.aurora.Models.HourlyForecast4.HourlyForecast4
import com.example.aurora.R
import com.squareup.picasso.NetworkPolicy
import com.squareup.picasso.Picasso
import kotlin.coroutines.coroutineContext
import kotlin.math.roundToInt

class AktualnePocasieAdapter(private val pocasieList : List<AktualnePocasieModel>) : RecyclerView.Adapter<AktualnePocasieAdapter.PocasieViewHolder>(){

    class PocasieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val icon: ImageView = itemView.findViewById(R.id.icon)
        val daytime : TextView = itemView.findViewById(R.id.daytime)
        val teplota : TextView = itemView.findViewById(R.id.teplota)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PocasieViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.pocasie_horizontal_item, parent,false)
        return PocasieViewHolder(view)
    }

    override fun getItemCount(): Int {
        return pocasieList.size
    }

    override fun onBindViewHolder(holder: PocasieViewHolder, position: Int) {
        val pocasie = pocasieList[position]
        holder.daytime.text = pocasie.daytime
        Picasso.get()
            .load("https://pro.openweathermap.org/img/w/" + pocasie.icon + ".png")
            .into(holder.icon)

        holder.teplota.text = pocasie.teplota + "°C"
    }
}