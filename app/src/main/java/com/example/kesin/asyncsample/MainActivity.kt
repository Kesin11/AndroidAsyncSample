package com.example.kesin.asyncsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.ObservableOnSubscribe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.lang.System.currentTimeMillis

class MainActivity : AppCompatActivity() {
    lateinit var callbackButton: Button
    lateinit var callbackSerialButton: Button
    lateinit var rxButton: Button
    lateinit var asynctButton: Button
    lateinit var suspendButton: Button
    lateinit var progressBar: ProgressBar
    lateinit var message: TextView
    val api = DummyApi()
    var timeCount: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        callbackButton = findViewById<Button>(R.id.callback_button)
        callbackSerialButton = findViewById<Button>(R.id.callback_s_button)
        rxButton = findViewById<Button>(R.id.rx_button)
        asynctButton = findViewById<Button>(R.id.async_button)
        suspendButton = findViewById<Button>(R.id.suspend_button)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        message = findViewById<TextView>(R.id.message)

        callbackButton.setOnClickListener {
            renderBegin()
            CallbackStyle.single {
                runOnUiThread {
                    renderFinish(it)
                }
            }
        }

        callbackSerialButton.setOnClickListener {
            renderBegin()
            CallbackStyle.serial {
                runOnUiThread {
                    renderFinish(it)
                }
            }
        }

        rxButton.setOnClickListener {
            Observable.create( ObservableOnSubscribe<String> { subscriber ->
                // ここがioスレッドで実行される
                val result = DummyApi.fetchTwo("Rx")
                subscriber.onNext(result)
            })
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe  { result ->
                // こっちがmainスレッドで実行される
                renderFinish(result)
            }

            renderBegin()
        }

        asynctButton.setOnClickListener {
            renderBegin()
            launch(UI) {
                // asyncはawaitが必要
                val task = async {
                    return@async DummyApi.fetchOne("async")
                }
                val result = task.await()
                renderFinish(result)
            }
        }

        suspendButton.setOnClickListener {
            renderBegin()
            launch(UI) {
                // suspendの場合はawait不要
                val result = processAsync()
                renderFinish(result)
            }
        }
    }

    fun renderBegin() {
        timeCount = currentTimeMillis()
        message.text = "progress..."
        progressBar.visibility = View.VISIBLE
    }

    fun renderFinish(result: String) {
        timeCount = (currentTimeMillis() - timeCount!!) / 1000
        message.text = "$result ${timeCount}s"
        progressBar.visibility = View.INVISIBLE
    }

    suspend fun processAsync(): String {
        return DummyApi.fetchOne("suspend")

    }

}
