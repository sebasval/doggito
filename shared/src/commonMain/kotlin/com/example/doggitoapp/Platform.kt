package com.example.doggitoapp

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform