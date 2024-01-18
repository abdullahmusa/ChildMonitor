package hu.aut.bme.childmonitor.domain.webrtc

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

open class MyEventListener : ValueEventListener {
    override fun onDataChange(snapshot: DataSnapshot) {

    }

    override fun onCancelled(error: DatabaseError) {
    }
}