package com.example.virtualcane3

import android.location.Location
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

@Composable
fun ViewLocationScreen(userId: String) {
    val context = LocalContext.current
    val locationState = remember { mutableStateOf<Location?>(null) }
    val googleMap = remember { mutableStateOf<GoogleMap?>(null) }

    // Fetch location from Firebase
    LaunchedEffect(userId) {
        val ref = FirebaseDatabase.getInstance().getReference("live_locations/$userId")
        ref.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val lat = snapshot.child("latitude").getValue(Double::class.java)
                val lon = snapshot.child("longitude").getValue(Double::class.java)
                if (lat != null && lon != null) {
                    locationState.value = Location("").apply {
                        latitude = lat
                        longitude = lon
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    Column(Modifier.fillMaxSize()) {
        Text(
            "Live Location of $userId",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(16.dp)
        )

        AndroidView(
            factory = { ctx ->
                val mapView = MapView(ctx).apply {
                    onCreate(null)
                    onResume()
                }
                mapView.getMapAsync { map ->
                    googleMap.value = map
                }
                mapView
            },
            modifier = Modifier.fillMaxSize()
        )

        LaunchedEffect(locationState.value) {
            locationState.value?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                googleMap.value?.apply {
                    clear()
                    addMarker(MarkerOptions().position(latLng).title("User Location"))
                    moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
                }
            }
        }
    }
}