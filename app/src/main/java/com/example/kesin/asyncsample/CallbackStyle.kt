package com.example.kesin.asyncsample

/**
 * Created by kesin on 2018/02/11.
 */

class CallbackStyle {
    companion object {
        fun single(callback: (String) -> Unit) {
            val thread = Thread({
                val result = DummyApi.fetchThree("callback")
                callback.invoke(result)
            })
            thread.start()
        }

        fun serial(callback: (String) -> Unit) {
            fun inner(callback: (String) -> Unit) {
                val thread = Thread({
                    val result = DummyApi.fetchFive("callback serial")
                    callback.invoke(result)
                })
                thread.start()
            }

            single {
                inner {
                    callback.invoke(it)
                }
            }
        }
    }
}