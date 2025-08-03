package com.force.connection

import android.net.wifi.WifiManager
import com.force.connection.ConnectionDefaults.log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.InetAddress
import javax.inject.Inject
import javax.jmdns.JmDNS
import javax.jmdns.ServiceEvent
import javax.jmdns.ServiceInfo
import javax.jmdns.ServiceListener

class InetAddressHelper @Inject constructor(
    private val wifiManager: WifiManager
) {
    private val scope = CoroutineScope(Dispatchers.IO)

    private val _resolvedAddress = MutableSharedFlow<String>()
    val resolvedAddress: Flow<String> = _resolvedAddress

    private lateinit var jmdns: JmDNS
    private lateinit var lock: WifiManager.MulticastLock
    private var serviceInfo: ServiceInfo? = null

    suspend fun init() = withContext(Dispatchers.IO) {
        if (!::jmdns.isInitialized) {
            val ip = wifiManager.connectionInfo.ipAddress
            val ipAddress = InetAddress.getByAddress(
                byteArrayOf(
                    (ip and 0xff).toByte(),
                    (ip shr 8 and 0xff).toByte(),
                    (ip shr 16 and 0xff).toByte(),
                    (ip shr 24 and 0xff).toByte()
                )
            )
            log(CONN_TAG, "Local IP used : $ipAddress")
            jmdns = JmDNS.create(ipAddress, "AndroidDevice").also {
                it.addServiceListener("_conf._udp.local.", object : ServiceListener {
                    override fun serviceAdded(event: ServiceEvent?) {
                        log(CONN_TAG, "DNS Service added: ${event?.info}")
                        jmdns.requestServiceInfo(event?.type, event?.name)
                    }

                    override fun serviceRemoved(event: ServiceEvent?) {
                        log(CONN_TAG, "DNS Service removed: ${event?.info}")
                    }

                    override fun serviceResolved(event: ServiceEvent?) {
                        log(CONN_TAG, "DNS Service resolved: ${event?.info}")
                        val serverIp = event?.info?.hostAddresses?.firstOrNull()
                        serverIp?.let { scope.launch { _resolvedAddress.emit(it) } }
                        val serverPort = event?.info?.port
                        log(CONN_TAG, "Server IP: $serverIp, Port: $serverPort")
                    }
                })
            }
        }
    }

    suspend fun startBroadcast() = withContext(Dispatchers.Default) {
        lock = wifiManager.createMulticastLock("lock")
        lock.setReferenceCounted(true)
        lock.acquire()

        serviceInfo = ServiceInfo.create(
            "_conf._udp.local.",
            "Conf Configurator",
            8888,
            "EXAMPLE TEXT"
        )

        jmdns.registerService(serviceInfo)
    }

    suspend fun stopBroadcast() = withContext(Dispatchers.Default) {
        jmdns.unregisterService(serviceInfo)
        lock.release()
    }

    suspend fun release() {
        if (::jmdns.isInitialized) {
            jmdns.close()
        }
        if (::lock.isInitialized && lock.isHeld) {
            lock.release()
        }
        log(CONN_TAG, "InetAddressHelper released")
    }
}
