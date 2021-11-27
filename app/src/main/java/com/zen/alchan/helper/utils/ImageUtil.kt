package com.zen.alchan.helper.utils

import android.content.Context
import android.content.res.ColorStateList
import android.net.Uri
import android.os.Build
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.content.ContextCompat
import androidx.core.view.setPadding
import coil.Coil
import coil.ImageLoader
import coil.decode.GifDecoder
import coil.decode.ImageDecoderDecoder
import coil.decode.SvgDecoder
import coil.imageLoader
import coil.load
import coil.size.Scale
import coil.transform.CircleCropTransformation
import com.zen.alchan.BuildConfig
import com.zen.alchan.R
import com.zen.alchan.helper.extensions.getAttrValue

object ImageUtil {

    fun init(context: Context) {
        val imageLoader = ImageLoader.Builder(context)
            .componentRegistry {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    add(ImageDecoderDecoder(context))
                else
                    add(GifDecoder())

                add(SvgDecoder(context))
            }
            .build()

        Coil.setImageLoader(imageLoader)
    }

    fun loadImage(context: Context, url: String, imageView: AppCompatImageView) {
        imageView.load(url)
    }

    fun loadImage(context: Context, resourceId: Int, imageView: AppCompatImageView) {
        imageView.load(resourceId)
    }

    fun loadImage(context: Context, uri: Uri, imageView: AppCompatImageView) {
        imageView.load(uri)
    }

    fun loadCircleImage(context: Context, url: String, imageView: AppCompatImageView)  {
        imageView.background = ContextCompat.getDrawable(context, R.drawable.shape_oval_with_border)
        imageView.backgroundTintList = ColorStateList.valueOf(context.getAttrValue(R.attr.themeContentColor))
        imageView.setPadding(context.resources.getDimensionPixelSize(R.dimen.lineWidth))
        imageView.load(url) {
            transformations(CircleCropTransformation())
        }
    }

    fun loadRectangleImage(context: Context, url: String, imageView: AppCompatImageView) {
        imageView.background = ContextCompat.getDrawable(context, R.drawable.shape_rectangle)
        imageView.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, android.R.color.transparent))
        imageView.load(url)
    }
}