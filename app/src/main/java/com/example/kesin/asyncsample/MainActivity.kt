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

class MainActivity : AppCompatActivity() {
    lateinit var callbackButton: Button
    lateinit var rxButton: Button
    lateinit var asynctButton: Button
    lateinit var suspendButton: Button
    lateinit var progressBar: ProgressBar
    lateinit var message: TextView
    val api = DummyApi()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        callbackButton = findViewById<Button>(R.id.callback_button)
        rxButton = findViewById<Button>(R.id.rx_button)
        asynctButton = findViewById<Button>(R.id.async_button)
        suspendButton = findViewById<Button>(R.id.suspend_button)
        progressBar = findViewById<ProgressBar>(R.id.progressBar)
        message = findViewById<TextView>(R.id.message)

        callbackButton.setOnClickListener {
            renderBegin()
            processCallback { result ->
                runOnUiThread {
                    renderFinish(result)
                }
            }
        }

        rxButton.setOnClickListener {
            Observable.create( ObservableOnSubscribe<String> { subscriber ->
                // ここがioスレッドで実行される
                val result = api.fetchFive("Rx")
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
                    return@async api.fetchThree("async")
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
        message.text = "progress..."
        progressBar.visibility = View.VISIBLE
    }

    fun renderFinish(result: String) {
        message.text = result
        progressBar.visibility = View.INVISIBLE
    }

    fun processCallback(callback: (String) -> Unit) {
        val thread = Thread({
            val result = api.fetchThree("callback")
            callback.invoke(result)
        })
        thread.start()
    }

    suspend fun processAsync(): String {
        return api.fetchThree("suspend")

    }

}
