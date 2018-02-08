package com.example.kesin.asyncsample

/**
 * Created by kesin on 2018/02/09.
 */
class DummyApi {
    fun fetchThree(message: String): String {
        Thread.sleep(3000)
        return message + " Done!"
    }

    fun fetchFive(message: String): String {
        Thread.sleep(5000)
        return message + " Done!"
    }
}