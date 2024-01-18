package hu.aut.bme.childmonitor.data


import androidx.compose.runtime.mutableStateListOf
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.gson.Gson
import hu.aut.bme.childmonitor.data.model.DeviceRole
import hu.aut.bme.childmonitor.data.model.DeviceStatus
import hu.aut.bme.childmonitor.domain.webrtc.DataModel
import hu.aut.bme.childmonitor.domain.webrtc.MyEventListener

class FirebaseClient(
    private val dbRef: DatabaseReference = FirebaseDatabase.getInstance().reference,
    private val firebaseAuth: FirebaseAuth = FirebaseAuth.getInstance(),
) {
    companion object {
        private const val STATUS = "status"
        private const val LATEST_EVENT = "latest_event"
        private const val CHILDREN = "children"
        private const val PARENTS = "parents"
    }

    var childrenStatuses = mutableStateListOf<Pair<String, DeviceStatus>>()

    val currentUID = firebaseAuth.currentUser?.uid
    var deviceID: String = ""
    lateinit var deviceRole: DeviceRole

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
        if (currentUID == null) return
        dbRef.addValueEventListener(object : MyEventListener() {
            override fun onDataChange(snapshot: DataSnapshot) {
                childrenStatuses.clear()
                childrenStatuses.addAll(snapshot.child(currentUID).child(CHILDREN).children.map {
                    runCatching {
                        it.key!! to DeviceStatus.valueOf(it.child(STATUS).value.toString())
                    }.getOrDefault(it.key!! to DeviceStatus.OFFLINE)
                })
            }
        })
    }

    fun observeLatestEvent(listener: Listener) {
        myDbRef?.child(LATEST_EVENT)?.addValueEventListener(
            object : MyEventListener() {
                override fun onDataChange(snapshot: DataSnapshot) {
                    super.onDataChange(snapshot)
                    val event = try {
                        Gson().fromJson(snapshot.value.toString(), DataModel::class.java)
                    } catch (e: Exception) {
                        e.printStackTrace()
                        null
                    }
                    event?.let {
                        listener.onLatestEventReceived(it)
                    }
                }
            }
        )
    }

    fun sendMessageToDevice(
        targetRole: DeviceRole,
        message: DataModel,
        success: (Boolean) -> Unit = {},
    ) {
        val convertedMessage = Gson().toJson(message)
        when (targetRole) {
            DeviceRole.Child -> childrenDbRef
            DeviceRole.Parent -> parentsDbRef
        }?.child(message.target)
            ?.child(LATEST_EVENT)
            ?.setValue(convertedMessage)
            ?.addOnCompleteListener {
                success(true)
            }?.addOnFailureListener {
                success(false)
            }
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
}