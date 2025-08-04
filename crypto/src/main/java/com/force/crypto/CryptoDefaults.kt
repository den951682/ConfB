package com.force.crypto

object CryptoDefaults {
    var log: (String, String) -> Unit = { tag, message -> }
    var logAnalytics: (String, Map<String, String>) -> Unit = { type, data -> }
}
