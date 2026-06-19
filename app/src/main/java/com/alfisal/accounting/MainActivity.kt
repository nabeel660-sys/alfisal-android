package com.alfisal.accounting

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.webkit.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private var filePathCallback: ValueCallback<Array<Uri>>? = null
    private var cameraImageUri: Uri? = null
    private val FILE_CHOOSER_REQUEST = 100
    private val PERMISSION_REQUEST = 101

    private val APP_URL = "https://accounting-alfisal.pages.dev/"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        webView = findViewById(R.id.webview)
        setupWebView()

        if (isOnline()) {
            webView.loadUrl(APP_URL)
        } else {
            showOfflinePage()
        }
    }

    private fun setupWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            databaseEnabled = true
            loadWithOverviewMode = true
            useWideViewPort = true
            setSupportZoom(false)
            builtInZoomControls = false
            displayZoomControls = false
            allowFileAccess = true
            allowContentAccess = true
            mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
            cacheMode = WebSettings.LOAD_DEFAULT
            mediaPlaybackRequiresUserGesture = false
            userAgentString = userAgentString + " AlFisalApp/1.0"
        }

        webView.webViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
            }

            override fun shouldOverrideUrlLoading(view: WebView?, request: WebResourceRequest?): Boolean {
                val url = request?.url?.toString() ?: return false

                // فتح Google OAuth في المتصفح الخارجي
                if (url.contains("accounts.google.com") ||
                    url.contains("oauth2.googleapis.com") ||
                    url.contains("oauth2callback")) {
                    try {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        startActivity(intent)
                    } catch (e: Exception) {
                        Toast.makeText(this@MainActivity, "افتح المتصفح يدوياً", Toast.LENGTH_SHORT).show()
                    }
                    return true
                }

                // الموقع الرئيسي يفتح داخل التطبيق
                if (url.startsWith("https://accounting-alfisal.pages.dev")) {
                    return false
                }

                // روابط خارجية تفتح في المتصفح
                try {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                } catch (e: Exception) { }
                return true
            }

            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
                if (request?.isForMainFrame == true) {
                    showOfflinePage()
                }
            }
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onShowFileChooser(
                webView: WebView?,
                filePathCallback: ValueCallback<Array<Uri>>?,
                fileChooserParams: FileChooserParams?
            ): Boolean {
                this@MainActivity.filePathCallback?.onReceiveValue(null)
                this@MainActivity.filePathCallback = filePathCallback
                openFileChooser()
                return true
            }

            override fun onPermissionRequest(request: PermissionRequest?) {
                request?.grant(request.resources)
            }
        }
    }

    private fun openFileChooser() {
        val permissions = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_EXTERNAL_STORAGE
        )
        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }
        if (missing.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, missing.toTypedArray(), PERMISSION_REQUEST)
            return
        }
        launchFileChooser()
    }

    private fun launchFileChooser() {
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        val photoFile = createImageFile()
        cameraImageUri = FileProvider.getUriForFile(this, "$packageName.fileprovider", photoFile)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri)

        val galleryIntent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        }

        val chooser = Intent.createChooser(galleryIntent, "اختر ملفاً")
        chooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(cameraIntent))
        startActivityForResult(chooser, FILE_CHOOSER_REQUEST)
    }

    private fun createImageFile(): File {
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        return File.createTempFile("IMG_${timeStamp}_", ".jpg", storageDir)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == FILE_CHOOSER_REQUEST) {
            if (resultCode == RESULT_OK) {
                val results = when {
                    data?.clipData != null -> {
                        (0 until data.clipData!!.itemCount).map { data.clipData!!.getItemAt(it).uri }.toTypedArray()
                    }
                    data?.data != null -> arrayOf(data.data!!)
                    cameraImageUri != null -> arrayOf(cameraImageUri!!)
                    else -> null
                }
                filePathCallback?.onReceiveValue(results)
            } else {
                filePathCallback?.onReceiveValue(null)
            }
            filePathCallback = null
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == PERMISSION_REQUEST && grantResults.isNotEmpty()) {
            launchFileChooser()
        }
    }

    override fun onBackPressed() {
        if (webView.canGoBack()) {
            webView.goBack()
        } else {
            super.onBackPressed()
        }
    }

    private fun isOnline(): Boolean {
        val cm = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork ?: return false
        val capabilities = cm.getNetworkCapabilities(network) ?: return false
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun showOfflinePage() {
        val html = """
            <!DOCTYPE html>
            <html dir="rtl" lang="ar">
            <head><meta charset="UTF-8"><meta name="viewport" content="width=device-width,initial-scale=1">
            <style>
              body{font-family:Arial,sans-serif;display:flex;align-items:center;justify-content:center;
                   min-height:100vh;margin:0;background:#f0f4f8;color:#1a1a2e}
              .box{text-align:center;padding:40px;background:white;border-radius:20px;
                   box-shadow:0 4px 20px rgba(0,0,0,0.1);max-width:320px;margin:20px}
              .icon{font-size:64px;margin-bottom:16px}
              h2{color:#2563eb;margin-bottom:8px}
              p{color:#666;line-height:1.6}
              button{margin-top:20px;padding:12px 28px;background:#2563eb;color:white;
                     border:none;border-radius:10px;font-size:16px;cursor:pointer}
            </style></head>
            <body>
              <div class="box">
                <div class="icon">📡</div>
                <h2>لا يوجد اتصال بالإنترنت</h2>
                <p>يرجى التحقق من اتصالك بالإنترنت والمحاولة مرة أخرى</p>
                <button onclick="window.location.reload()">إعادة المحاولة</button>
              </div>
            </body></html>
        """.trimIndent()
        webView.loadData(html, "text/html; charset=utf-8", "UTF-8")
    }
}
