package com.example.kesin.asyncsample

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.functions.BiFunction
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.experimental.android.UI
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.launch
import java.lang.System.currentTimeMillis

class MainActivity : AppCompatActivity() {
    lateinit var callbackButton: Button
    lateinit var callbackSerialButton: Button
    lateinit var rxButton: Button
    lateinit var rxSerialButton: Button
    lateinit var rxConcurrentButton: Button
    lateinit var asynctButton: Button
    lateinit var suspendButton: Button
    lateinit var progressBar: ProgressBar
    lateinit var message: TextView
    var timerStarted: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        callbackButton = findViewById<Button>(R.id.callback_button)
        callbackSerialButton = findViewById<Button>(R.id.callback_s_button)
        rxButton = findViewById<Button>(R.id.rx_button)
        rxSerialButton = findViewById<Button>(R.id.rx_s_button)
        rxConcurrentButton = findViewById<Button>(R.id.rx_c_button)
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
            // fetchOneは別スレッドで実行される
            RxStyle.fetchOne("Rx")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe  { result ->
                    // こっちはmainスレッドで実行される
                    renderFinish(result)
                }

            renderBegin()
        }

        rxSerialButton.setOnClickListener {
            RxStyle.fetchOne("Rx")
                .flatMap {
                    RxStyle.fetchTwo("Rx")
                }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe  { result ->
                    renderFinish(result)
                }

            renderBegin()
        }

        rxConcurrentButton.setOnClickListener {
            // 単にsubscribeOn(Schedulers.io())とzipの組み合わせでは2つが別々のスレッドで実行されない
            // https://medium.com/@Joseph82/concurrent-tasks-with-zip-operator-99773466039f
            Observable.zip<String, String, String>(
                RxStyle.fetchOne("Rx").subscribeOn(Schedulers.newThread()),
                RxStyle.fetchTwo("Rx2").subscribeOn(Schedulers.newThread()),
                BiFunction { message1, message2 ->
                    message1
                })
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe  { result ->
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
        timerStarted = currentTimeMillis()
        message.text = "progress..."
        progressBar.visibility = View.VISIBLE
    }

    fun renderFinish(result: String) {
        var timeCount = (currentTimeMillis() - timerStarted!!) / 1000.0
        message.text = "$result ${timeCount}s"
        progressBar.visibility = View.INVISIBLE
    }

    suspend fun processAsync(): String {
        return DummyApi.fetchOne("suspend")

    }

}
