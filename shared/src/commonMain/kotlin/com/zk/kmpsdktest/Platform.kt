package com.zk.kmpsdktest

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform