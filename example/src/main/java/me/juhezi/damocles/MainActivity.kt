package me.juhezi.damocles

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import me.juhezi.utils.buildTypeface
import me.juhezi.utils.view.Property
import me.juhezi.utils.view.transform

class MainActivity : AppCompatActivity() {

    val FONT_ASSET_PATH = "fonts/STZHONGS.TTF"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        var recyclerView = findViewById(R.id.recycler_view) as RecyclerView
        var textView = findViewById(R.id.tv_content) as TextView
        textView.typeface = buildTypeface {
            path = FONT_ASSET_PATH
            assetManager = assets
        }.build()
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = object : RecyclerView.Adapter<ViewHolder>() {
            override fun onCreateViewHolder(parent: ViewGroup?, viewType: Int): ViewHolder {
                val itemView = LayoutInflater.from(parent?.context).inflate(R.layout.item_sample, parent, false)
                return ViewHolder(itemView)
            }

            override fun onBindViewHolder(holder: ViewHolder?, position: Int) {}

            override fun getItemCount(): Int = 100
        }
        recyclerView.transform(Property())
    }
}

class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)