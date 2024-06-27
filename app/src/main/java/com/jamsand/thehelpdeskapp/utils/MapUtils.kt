package com.jamsand.thehelpdeskapp.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.android.gms.maps.model.LatLng
import kotlin.math.abs
import kotlin.math.atan


//will return a list of LatLng that will correspond to path
fun getLocations(): ArrayList<LatLng> {

    val locationList = ArrayList<LatLng>()
    locationList.add(LatLng(28.4356, 77.11498))
    locationList.add(LatLng(28.435660000000002, 77.11519000000001))
    locationList.add(LatLng(28.43568, 77.11521))
    locationList.add(LatLng(28.436580000000003, 77.11499))
    locationList.add(LatLng(28.436590000000002, 77.11507))
    locationList.add(LatLng(28.436970000000002, 77.11272000000001))
    locationList.add(LatLng(28.43635, 77.11289000000001))
    locationList.add(LatLng(28.4353, 77.11317000000001))
    locationList.add(LatLng(28.435280000000002, 77.11332))
    locationList.add(LatLng(28.435350000000003, 77.11368))

    return locationList
}

//draw a path between the Origin and the Destination
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
//returns an angle between them,from the the angle it will return
// we will determine how the car icon should be directed
fun getCarRotation(startLL: LatLng, endLL: LatLng): Float {
    val latDifference: Double = abs(startLL.latitude - endLL.latitude)
    val lngDifference: Double = abs(startLL.longitude - endLL.longitude)
    var rotation = -1F

    when {
        startLL.latitude < endLL.latitude && startLL.longitude < endLL.longitude -> {
            rotation = Math.toDegrees(atan(lngDifference / latDifference)).toFloat()
        }
        startLL.latitude >= endLL.latitude && startLL.longitude < endLL.longitude -> {
            rotation = (90 - Math.toDegrees(atan(lngDifference / latDifference)) + 90).toFloat()
        }
        startLL.latitude >= endLL.latitude && startLL.longitude < endLL.longitude -> {
            rotation = (Math.toDegrees(atan(lngDifference / latDifference)) + 180).toFloat()
        }
        startLL.latitude < endLL.latitude && startLL.longitude < endLL.longitude -> {
            rotation =(90 -  Math.toDegrees(atan(lngDifference / latDifference)) + 270).toFloat()
        }
    }
    return rotation
}