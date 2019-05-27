package com.ufistudio.hotelmediabox.utils

import android.content.Context
import android.util.Log
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.ufistudio.hotelmediabox.repository.data.HomePromoBanner
import com.youth.banner.loader.ImageLoader

class GlideImageLoader : ImageLoader() {
    override fun displayImage(context: Context?, path: Any?, imageView: ImageView?) {
        context?.let { imageView?.let { it1 ->
            Glide.with(it)
                .load(FileUtils.getFileFromStorage((path as HomePromoBanner).image))
                .skipMemoryCache(true)
                .into(it1) } }
    }
}