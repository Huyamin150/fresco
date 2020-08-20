package com.facebook.samples.kotlin

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.facebook.drawee.backends.pipeline.Fresco
import com.facebook.drawee.view.SimpleDraweeView
import com.facebook.imagepipeline.common.ImageDecodeOptions
import com.facebook.imagepipeline.request.ImageRequestBuilder
import kotlinx.android.synthetic.main.item_image.view.*
import kotlinx.android.synthetic.main.item_simple.view.*
import kotlinx.android.synthetic.main.layout_simple.*

/**
 * Created by Nick.Hu at 2020/8/17 20:13.
 * Mail:huyamin@theduapp.com
 * Tel: +8618521550285
 * Description: The Class SimpleActivity
 */
class SimpleActivity :AppCompatActivity(){

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.layout_simple)
        jumpToEmpty.setOnClickListener {
            startActivity(Intent(this,EmptyActivity::class.java))
        }
        listview.layoutManager = LinearLayoutManager(this)
        val list = mutableListOf<String>(
                "http://cdn.poizon.com/algo/webp/algo_33745563_s2000e4000_w767h320.webp",
                "http://cdn.poizon.com/algo/webp/algo_34028166_s2000e5000_w1151h480.webp"
        )
        listview.adapter = InnerAdapter(list)
    }



    class InnerAdapter(val list:MutableList<String>) :RecyclerView.Adapter<InnerAdapter.VVH>(){



        class VVH(val view:View) :RecyclerView.ViewHolder(view) {

            fun setData(str:String){
                view.item_simple.controller = Fresco.newDraweeControllerBuilder()
                        .setImageRequest(
                                ImageRequestBuilder.newBuilderWithSource(Uri.parse(str))
                                        .setImageDecodeOptions(ImageDecodeOptions.newBuilder().apply {
                                            decodePreviewFrame = true
                                            setBitmapConfig(Bitmap.Config.ARGB_8888)

                                        }.build())
                                        .build())
                        .setOldController(itemView.item_simple.controller)
                        .setAutoPlayAnimations(true)
                        .build()
            }
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VVH {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_simple,parent,false)
           return VVH(view)
        }

        override fun getItemCount(): Int {
          return list.size
        }

        override fun onBindViewHolder(holder: VVH, position: Int) {
            holder.setData(list[position])
        }
    }
}
