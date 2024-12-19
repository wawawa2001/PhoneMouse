package com.example.phonemouser

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.MotionEvent
import android.widget.Button
import com.example.phonemouser.databinding.ActivityMainBinding
import java.io.OutputStream
import java.net.Socket
import android.os.AsyncTask

var count = 0
var xyList: MutableList<MutableList<Int>> = mutableListOf()

class NetworkTask(private val callback: () -> Unit) : AsyncTask<String, Void, Void>() {

    override fun doInBackground(vararg params: String?): Void? {
        try {
            val serverAddress = "192.168.3.17"
            val serverPort = 11111
            val socket = Socket(serverAddress, serverPort)

            val outputStream: OutputStream = socket.getOutputStream()
            var byteArray: ByteArray = ByteArray(0)  // 初期化しておく

            // データを取得
            if (params.isNotEmpty() && params[0] == "click") {
                byteArray = "click".toByteArray(Charsets.UTF_8)
            }else {
                val jsonString =
                    xyList.map { it.joinToString(",", "[", "]") }.joinToString(",", "[", "]")
                byteArray = jsonString.toByteArray(Charsets.UTF_8)
            }
            outputStream.write(byteArray)

            socket.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return null
    }

    override fun onPostExecute(result: Void?) {
        super.onPostExecute(result)
        // 通信が完了した後にUIを更新する処理を行う
        callback.invoke()
    }
}

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var VirtualMouse: Button
    private var offsetX = 0f
    private var offsetY = 0f
    private val baseX = 390.toFloat()
    private val baseY = 560.toFloat()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val virtualMouseButton: Button = findViewById(R.id.VirtualMouse)

        var vX = 0f
        var vY = 0f

        val clickbtn: Button = findViewById(R.id.button4)
        var lastEventTime = 0L
        val minIntervalMillis = 500 // 500ミリ秒ごとに処理を実行

        clickbtn.setOnClickListener {
            NetworkTask {
                xyList = mutableListOf()
            }.execute("click")
        }

        virtualMouseButton.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    offsetX = event.rawX - virtualMouseButton.x
                    offsetY = event.rawY - virtualMouseButton.y
                }

                MotionEvent.ACTION_UP -> {
                    virtualMouseButton.x = baseX
                    virtualMouseButton.y = baseY

                    NetworkTask {
                        xyList = mutableListOf()
                    }.execute()
                }

                MotionEvent.ACTION_MOVE -> {
                    vX = event.rawX - offsetX
                    vY = event.rawY - offsetY

                    when{
                        vX < 190 -> vX = 190.toFloat()
                        vX > 600 -> vX = 600.toFloat()
                    }

                    when{
                        vY < 300 -> vY = 300.toFloat()
                        vY > 830 -> vY = 830.toFloat()
                    }

                    virtualMouseButton.x = vX
                    virtualMouseButton.y = vY

                    xyList.add(mutableListOf((virtualMouseButton.x - baseX).toInt()/30, (virtualMouseButton.y - baseY).toInt()/30))

                }


            }
            true
        }
    }
}