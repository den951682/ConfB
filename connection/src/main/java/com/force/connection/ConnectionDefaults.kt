package com.force.connection

object ConnectionDefaults {
    var log: (String, String) -> Unit = { tag, message -> }
    var logAnalytics: (String, Map<String, String>) -> Unit = { type, data -> }
}
