package com.example.kesin.asyncsample

/**
 * Created by kesin on 2018/02/09.
 */
class DummyApi {
    companion object {
        fun fetchOne(message: String): String {
            Thread.sleep(1000)
            return "$message Done!"
        }

        fun fetchTwo(message: String): String {
            Thread.sleep(2000)
            return "$message Done!"
        }
    }
}