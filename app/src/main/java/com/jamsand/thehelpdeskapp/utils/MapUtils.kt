package com.jamsand.thehelpdeskapp.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.android.gms.maps.model.LatLng

fun getLocations(): ArrayList<LatLng> {

    val locationList = ArrayList<LatLng>()
        locationList.add(LatLng(28.4356,77.1149))
        locationList.add(LatLng(28.435660000000002,77.11519000000001))

        locationList.add(LatLng(28.4358,77.11521))
        locationList.add(LatLng(28.435660000000003,77.11499))
        locationList.add(LatLng(28.4356,77.1149))





        locationList.add(LatLng(28.435660000000002,77.11519000000001))

        locationList.add(LatLng(28.4356,77.1149))
        locationList.add(LatLng(28.435660000000002,77.11519000000001))

        locationList.add(LatLng(28.4356,77.1149))
        locationList.add(LatLng(28.435660000000002,77.11519000000001))

        return locationList
}

fun getStartingLocationBitmap(): Bitmap {
    val height = 40
    val width = 40
    val bitmap = Bitmap.createBitmap(height,width, Bitmap.Config.RGB_565)
    val canvas = Canvas(bitmap)
    val paint = Paint()
    paint.color = Color.BLACK
    paint.style = Paint.Style.FILL
    paint.isAntiAlias = true
    canvas.drawRect(0F,0F, width.toFloat(),height.toFloat(),paint)

    return bitmap
}