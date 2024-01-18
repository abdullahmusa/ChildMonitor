package hu.aut.bme.childmonitor.data

import androidx.compose.runtime.mutableStateListOf
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import hu.aut.bme.childmonitor.data.model.DeviceRole
import hu.aut.bme.childmonitor.domain.auth.AuthService
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import hu.aut.bme.childmonitor.domain.webrtc.DataModel
import io.ktor.client.HttpClient
import io.ktor.client.request.patch
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.net.HttpURLConnection
import java.net.URL

class FirebaseClient {
    companion object {
        private const val dbRef = "" // TODO: add your Firebase Realtime Database reference here
        // should look like: "https://[Project ID]-default-rtdb.[DB Location].firebasedatabase.app"

        private const val STATUS = "status"
        private const val LATEST_EVENT = "latest_event"
        private const val CHILDREN = "children"
        private const val PARENTS = "parents"
    }

    init {
        assert(dbRef.isNotEmpty()) {
            "Firebase database reference is empty!"
        }
    }

    private val httpClient = HttpClient()
    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    val currentUID = AuthService.currentUser?.uid
    var deviceID: String = ""
    lateinit var deviceRole: DeviceRole
    var childrenStatuses = mutableStateListOf<Pair<String, DeviceStatus>>()

    private val myDbRef
        get() = currentUID?.let {
            dbRef.child(currentUID)
                .child(
                    when (deviceRole) {
                        DeviceRole.Child -> CHILDREN
                        DeviceRole.Parent -> PARENTS
                    }
                )
                .child(deviceID)
        }
    private val childrenDbRef = currentUID?.let {
        dbRef.child(currentUID).child(CHILDREN)
    }
    private val parentsDbRef = currentUID?.let {
        dbRef.child(currentUID).child(PARENTS)
    }

    fun observeChildrenStatus() {
        if (childrenDbRef == null) return
        coroutineScope.launch {
            getEventsFlow("${childrenDbRef}.json").collect { event ->
                if (event.name != "put" && event.name != "patch") return@collect
                runCatching {
                    val update = JsonParser.parseString(event.data).asJsonObject
                    if (!update.keySet().containsAll(listOf("path", "data"))) return@collect
                    val path = update.get("path")?.asString ?: return@collect

                    when (path) {
                        "/" -> {
                            val childDevices = update.get("data")?.asJsonObject
                            val childDeviceIDs = childDevices?.keySet()
                            childDeviceIDs?.forEach { childDeviceID ->
                                childDevices[childDeviceID]?.asJsonObject?.get("status")
                                    ?.asString?.runCatching {
                                        updateChildStatus(
                                            childDeviceID = childDeviceID,
                                            status = DeviceStatus.valueOf(this)
                                        )
                                    }
                            }
                        }

                        else -> {
                            val pathList = path.split("/")
                            if (pathList.size == 3 && pathList.last() == STATUS) {
                                val childDeviceID = pathList[1]
                                update.get("data")?.asString?.let {
                                    updateChildStatus(
                                        childDeviceID = childDeviceID,
                                        status = DeviceStatus.valueOf(it)
                                    )
                                }
                            } else {

                            }
                        }
                    }
                }.onFailure { exception ->
                    exception.printStackTrace()
                }
            }
        }
    }

    private fun updateChildStatus(childDeviceID: String, status: DeviceStatus) {
        childrenStatuses.find { it.first == childDeviceID }?.let {
            childrenStatuses.remove(it)
        }
        childrenStatuses.add(childDeviceID to status)
    }


    fun observeLatestEvent(listener: Listener) {
        if (myDbRef?.child(LATEST_EVENT) == null) return
        coroutineScope.launch {
            getEventsFlow("${myDbRef?.child(LATEST_EVENT)}.json").collect { event ->
                if (event.name != "put" && event.name != "patch") return@collect
                runCatching {
                    val update = JsonParser.parseString(event.data).asJsonObject
                    if (!update.keySet().containsAll(listOf("path", "data"))) return@collect
                    if (update.get("path")?.asString != "/") return@collect

                    val dataString = update.get("data")?.asString ?: return@collect
                    val data = try {
                        Gson().fromJson(dataString, DataModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                    data?.let {
                        listener.onLatestEventReceived(it)
                    }
                }.onFailure { exception ->
                    exception.printStackTrace()
                }
            }
        }
    }

    fun sendMessageToDevice(
        targetRole: DeviceRole,
        message: DataModel,
    ) {
        val convertedMessage = JsonObject().apply {
            addProperty(LATEST_EVENT, Gson().toJson(message))
        }.toString()

        when (targetRole) {
            DeviceRole.Child -> childrenDbRef
            DeviceRole.Parent -> parentsDbRef
        }?.child(message.target)
            ?.setValue(convertedMessage)
    }

    fun clearLatestEvent() {
        myDbRef?.child(LATEST_EVENT)?.setValue(null)
    }

    fun updateDeviceStatus(status: DeviceStatus) {
        myDbRef?.child(STATUS)?.setValue(status.name)
    }

    interface Listener {
        fun onLatestEventReceived(event: DataModel)
    }

    private fun String.child(child: String) = "$this/$child"
    private fun String.setValue(value: String?) {
        coroutineScope.launch {
            httpClient.patch("${this@setValue}.json") {
                contentType(ContentType.Text.Plain)
                setBody(
                    when {
                        value == null -> "null"
                        JsonParser.parseString(value).isJsonObject -> value
                        else -> "\"$value\""
                    }
                )
            }
        }
    }

    private data class Event(val name: String = "", val data: String = "")
    private data class Data(val path: String = "", val data: String = "")
    private data class ChildStatus(val child: String = "", val status: Data)

    private fun getEventsFlow(url: String): Flow<Event> = flow {
        coroutineScope {
            val conn = (URL(url).openConnection() as HttpURLConnection).also {
                it.setRequestProperty(
                    "Accept",
                    "text/event-stream"
                )
                it.doInput = true
            }

            conn.connect()
            val inputReader = conn.inputStream.bufferedReader()
            var event = Event()

            while (isActive) {
                val line = inputReader.readLine()
                with(line) {
                    when {
                        startsWith("event:") -> {
                            event = event.copy(name = line.substring(6).trim())
                        }

                        startsWith("data:") -> {
                            event = event.copy(data = line.substring(5).trim())
                        }

                        isEmpty() -> {
                            emit(event)
                            event = Event()
                        }
                    }
                }
            }
        }
    }
}