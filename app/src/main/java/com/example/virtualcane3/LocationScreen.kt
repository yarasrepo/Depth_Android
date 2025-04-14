package com.example.virtualcane3


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Looper
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.app.ActivityCompat
import androidx.navigation.NavController
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.compose.ui.text.input.ImeAction


@SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
@Composable
fun LocationScreen(navController: NavController) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val textToSpeechHelper = remember { TextToSpeechHelper(context) }
    val locationHelper = remember { LocationHelper(context) }
    val sharedWithUsers = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }
    val sharedByUsers = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            locationHelper.startLocationUpdates()
        }
    }

    val searchResults = remember { mutableStateOf<List<Pair<String, String>>>(emptyList()) }



    LazyColumn {
        items(searchResults.value) { (uid, name) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = name)
                Button(onClick = { shareLocationWithUser(uid)
                    Toast.makeText(context, "Location shared with $name", Toast.LENGTH_SHORT).show() }) {
                    Text("Share")
                }
            }
        }
    }

    LaunchedEffect(Unit) {

        fetchSharedWithUsers { sharedWithUsers.value = it }

        fetchUsersSharingWithMe { sharedByUsers.value = it }

        val fineLocationGranted = ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!fineLocationGranted) {
            permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            // For Android 10 (API 29) and above, check ACCESS_BACKGROUND_LOCATION
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val backgroundLocationGranted = ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.ACCESS_BACKGROUND_LOCATION
                ) == PackageManager.PERMISSION_GRANTED

                if (!backgroundLocationGranted) {
                    permissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                } else {
                    locationHelper.startLocationUpdates()
                }
            } else {
                locationHelper.startLocationUpdates()
            }
        }
    }


    val items = listOf(
        BottomNavigationItem(
            title = "Home",
            selectedIcon = Icons.Filled.Home,
            unselectedIcon = Icons.Outlined.Home,
        ),
        BottomNavigationItem(
            title = "Location",
            selectedIcon = Icons.Filled.LocationOn,
            unselectedIcon = Icons.Outlined.LocationOn,
        ),
        BottomNavigationItem(
            title = "Profile",
            selectedIcon = Icons.Filled.Person,
            unselectedIcon = Icons.Outlined.Person,
        ),
        BottomNavigationItem(
            title = "Settings",
            selectedIcon = Icons.Filled.Settings,
            unselectedIcon = Icons.Outlined.Settings,
        )
    )

    Scaffold(
        topBar = { TopBar() },
        bottomBar = { BottomNavigationBar(navController = navController, items = items) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Scrollable content
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    SearchBar(
                        onSearchResults = { results -> searchResults.value = results }
                    )
                }

                item {
                    LiveMapView(locationHelper)
                }

                item {
                    ShareLocationSection(
                        textToSpeechHelper = textToSpeechHelper,
                        sharedWithUsers = sharedWithUsers.value
                    )
                }

                item {
                    LiveLocationSharedBy(
                        textToSpeechHelper = textToSpeechHelper,
                        sharedByUsers = sharedByUsers.value,
                        onViewLocation = { userId ->
                            navController.navigate("ViewLocationScreen/$userId")
                        }
                    )
                }

                items(searchResults.value) { (uid, name) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = name)
                        Button(
                            onClick = {
                                shareLocationWithUser(uid)
                                Toast.makeText(context, "Location shared with $name", Toast.LENGTH_SHORT).show()
                            }
                        ) {
                            Text("Share")
                        }
                    }
                }
            }
        }
    }
}


fun fetchSharedWithUsers(onResult: (List<Pair<String, String>>) -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val usersRef = FirebaseDatabase.getInstance().getReference("users")

    usersRef.child(currentUserId).child("sharedWith")
        .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Pair<String, String>>()
                val userIds = snapshot.children.mapNotNull { it.key }

                for (uid in userIds) {
                    usersRef.child(uid).child("name").addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(nameSnapshot: DataSnapshot) {
                            val name = nameSnapshot.getValue(String::class.java) ?: "Unknown"
                            list.add(uid to name)
                            onResult(list)
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
}

fun fetchUsersSharingWithMe(onResult: (List<Pair<String, String>>) -> Unit) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val usersRef = FirebaseDatabase.getInstance().getReference("users")

    usersRef.child(currentUserId).child("sharedBy")
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val list = mutableListOf<Pair<String, String>>()
                val userIds = snapshot.children.mapNotNull { it.key }

                for (uid in userIds) {
                    usersRef.child(uid).child("name")
                        .addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(nameSnapshot: DataSnapshot) {
                                val name = nameSnapshot.getValue(String::class.java) ?: "Unknown"
                                list.add(uid to name)
                                onResult(list)
                            }

                            override fun onCancelled(error: DatabaseError) {}
                        })
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
}

@Composable
fun LiveMapView(locationHelper: LocationHelper) {
    val userLocation by locationHelper.userLocation
    val sharedLocations by locationHelper.sharedLocations
    val googleMap = remember { mutableStateOf<GoogleMap?>(null) }


    AndroidView(
        factory = { context ->
            val mapView = MapView(context).apply {
                onCreate(null)
                onResume()
            }
            mapView.getMapAsync { googleMapInstance ->
                googleMap.value = googleMapInstance
            }
            mapView
        },
        modifier = Modifier
            .fillMaxWidth()
            .height(300.dp)
    )

    // After the map is initialized, update markers
    LaunchedEffect(userLocation, sharedLocations) {
        googleMap.value?.apply {
            clear()  // Clear previous markers

            // Show user's live location
            userLocation?.let { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                addMarker(
                    MarkerOptions().position(latLng)
                        .title("Your Location")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
                moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 15f))
            }

            // Show other users' shared locations
            sharedLocations.forEach { (userId, location) ->
                val latLng = LatLng(location.latitude, location.longitude)
                addMarker(
                    MarkerOptions().position(latLng)
                        .title("User: $userId")
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    onSearchResults: (List<Pair<String, String>>) -> Unit
) {
    var searchText by remember { mutableStateOf(TextFieldValue("")) }
    val keyboardController = LocalSoftwareKeyboardController.current

    OutlinedTextField(
            value = searchText,
            onValueChange = {
                searchText = it
                if (it.text.length >= 2) { // optional threshold
                    searchUsersByName(it.text) { results ->
                        onSearchResults(results)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            placeholder = {Text("Search by name")},
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {},
            singleLine = true,
            keyboardOptions = KeyboardOptions.Default.copy(
                imeAction = ImeAction.Search
            ),
            keyboardActions = KeyboardActions(
                onSearch = {
                    keyboardController?.hide()
                    if(searchText.text.length >= 2){
                        searchUsersByName(searchText.text){ results ->
                            onSearchResults(results)
                        }
                    }
                }
            )
    )

}


fun searchUsersByName(query: String, onResult: (List<Pair<String, String>>) -> Unit) {
    val usersRef = FirebaseDatabase.getInstance().getReference("users")

    usersRef.orderByChild("name")
        .startAt(query)
        .endAt(query + "\uf8ff") // Unicode trick for prefix search
        .addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val results = mutableListOf<Pair<String, String>>()
                for (userSnapshot in snapshot.children) {
                    val name = userSnapshot.child("name").getValue(String::class.java) ?: continue
                    val uid = userSnapshot.key ?: continue
                    results.add(uid to name)
                }
                onResult(results)
            }

            override fun onCancelled(error: DatabaseError) {}
        })
}

@Composable
fun ShareLocationSection(
    textToSpeechHelper: TextToSpeechHelper,
    sharedWithUsers: List<Pair<String, String>> // (uid, name)
) {
    Column {
        Text(text = "Live Location Shared With:", style = MaterialTheme.typography.titleMedium)
        for ((uid, name) in sharedWithUsers) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(name)
                Row {
                    IconButton(onClick = { textToSpeechHelper.speak("Live location shared with $name") }) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Speak")
                    }
                    Button(onClick = {
                        stopSharingLocationWithUser(uid)
                    }) {
                        Text("Stop Sharing")
                    }
                }
            }
        }
    }
}


@Composable
fun LiveLocationSharedBy(
    textToSpeechHelper: TextToSpeechHelper,
    sharedByUsers: List<Pair<String, String>>,
    onViewLocation: (String) -> Unit
) {
    Column {
        Text(text = "Live Location Shared By:", style = MaterialTheme.typography.titleMedium)
        sharedByUsers.forEach { (uid, name) ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(name)
                Row {
                    IconButton(onClick = {
                        textToSpeechHelper.speak("Live location shared by $name")
                    }) {
                        Icon(Icons.Filled.KeyboardArrowUp, contentDescription = "Speak")
                    }
                    Button(onClick = {
                        onViewLocation(uid)
                    }) {
                        Text("View Live Location")
                    }
                }
            }
        }
    }
}

fun stopSharingLocationWithUser(targetUserId: String) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val usersRef = FirebaseDatabase.getInstance().getReference("users")

    usersRef.child(currentUserId).child("sharedWith").child(targetUserId).removeValue()
    usersRef.child(targetUserId).child("sharedBy").child(currentUserId).removeValue()
}

fun shareLocationWithUser(targetUserId: String) {
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
    val usersRef = FirebaseDatabase.getInstance().getReference("users")

    // Add the target user to the current user's sharedWith
    usersRef.child(currentUserId).child("sharedWith").child(targetUserId).setValue(true)

    // Add the current user to the target user's sharedBy
    usersRef.child(targetUserId).child("sharedBy").child(currentUserId).setValue(true)
}



class LocationHelper(context: Context) {
    private val database = FirebaseDatabase.getInstance().reference.child("live_locations")
    private val fusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    private val _userLocation = mutableStateOf<Location?>(null)
    val userLocation: State<Location?> = _userLocation

    private val _sharedLocations = mutableStateOf<Map<String, Location>>(emptyMap())
    val sharedLocations: State<Map<String, Location>> = _sharedLocations

    @SuppressLint("MissingPermission")
    fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000
            fastestInterval = 2000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    locationResult.lastLocation?.let { location ->
                        _userLocation.value = location
                        uploadLocationToFirebase(location)
                    }
                }
            },
            Looper.getMainLooper()
        )

        fetchSharedLocations()
    }


    private fun uploadLocationToFirebase(location: Location) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        database.child(userId).setValue(
            mapOf("latitude" to location.latitude, "longitude" to location.longitude)
        )
    }

    private fun fetchSharedLocations() {
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val usersRef = FirebaseDatabase.getInstance().getReference("users")

        usersRef.child(currentUserId).child("sharedBy")
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val sharedUserIds = snapshot.children.mapNotNull { it.key }
                    database.addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val locations = mutableMapOf<String, Location>()
                            for (userId in sharedUserIds) {
                                val userSnapshot = snapshot.child(userId)
                                val lat =
                                    userSnapshot.child("latitude").getValue(Double::class.java)
                                val lon =
                                    userSnapshot.child("longitude").getValue(Double::class.java)
                                if (lat != null && lon != null) {
                                    val location = Location("").apply {
                                        latitude = lat
                                        longitude = lon
                                    }
                                    locations[userId] = location
                                }
                            }
                            _sharedLocations.value = locations
                        }

                        override fun onCancelled(error: DatabaseError) {}
                    })
                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }
}

