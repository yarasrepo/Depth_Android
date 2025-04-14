package com.example.virtualcane3

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.KeyboardArrowDown
import androidx.compose.material.icons.outlined.KeyboardArrowUp
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.virtualcane3.ui.theme.Black
import com.example.virtualcane3.ui.theme.BlueGray
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.auth.FirebaseAuth

@Composable
fun ProfileScreen(navController: NavController) {
    val context = LocalContext.current
    val ttsHelper = remember { TextToSpeechHelper(context) }
    val emergencyContacts = remember { mutableStateListOf<Pair<String, String>>() }


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
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {

            Spacer(modifier = Modifier.height(16.dp))

            PersonalInformationItem(ttsHelper)

            EmergencyContactsItem(emergencyContacts = emergencyContacts, ttsHelper)

            Spacer(modifier = Modifier.weight(1f))

            OptionItem(
                title = "Logout",
                leadingIcon = Icons.AutoMirrored.Filled.ExitToApp,
                trailingIcon = null,
                iconColor = Color.Red,
                onClick = {
                   ttsHelper.speak("Logging out")
                    logout(navController) }
            )

        }
    }
}

fun logout(navController: NavController) {
    FirebaseAuth.getInstance().signOut()
    navController.navigate("login") {
        popUpTo("profile") { inclusive = true }
        launchSingleTop = true
    }
}

@Composable
fun TextRow(label: String, value: String) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Black
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyLarge,
            color = uiColor
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge,
            color = uiColor
        )
    }
}

@Composable
fun InputField(value: String, label: String, onValueChange: (String) -> Unit, isRecording: MutableState<Boolean>, onMicClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        Image(
            painter = painterResource(id = if (isRecording.value) R.drawable.ic_stop_recording else R.drawable.ic_microphone),
            contentDescription = if (isRecording.value) "Stop Recording" else "Start Recording",
            modifier = Modifier
                .clickable { onMicClick() }
                .size(24.dp)
        )
    }
}

/*@Composable
fun PersonalInformationItem(isRecording: MutableState<Boolean>, speechToTextHelper: SpeechToTextHelper) {
    val expanded = remember { mutableStateOf(false) }
    val isEditing = remember { mutableStateOf(false) }
    val name = remember { mutableStateOf("John Doe") }
    val email = remember { mutableStateOf("john.doe@example.com") }
    val phone = remember { mutableStateOf("1234567890") }
    val bloodType = remember { mutableStateOf("O+") }
    val medicalInfo = remember { mutableStateOf("No known allergies") }

    // Dropdown Header
    DropdownOptionItem(
        title = "Personal Information",
        leadingIcon = Icons.Default.Person,
        onClick = {},
        expanded = expanded
    ) {

    if (expanded.value) {
        Column(
            modifier = Modifier
                .padding(start = 20.dp, end =  20.dp)
        ) {
            if (!isEditing.value) {
                // Display Read-Only Information
                TextRow(label = "Full Name", value = name.value)
                TextRow(label = "Email", value = email.value)
                TextRow(label = "Phone Number", value = phone.value)
                TextRow(label = "Blood Type", value = bloodType.value)
                TextRow(label = "Medical Info", value = medicalInfo.value)

                Spacer(modifier = Modifier.height(16.dp))

                // Edit Button
                Button(onClick = { isEditing.value = true },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(size = 4.dp)) {
                    Text("Edit Information",
                        style = MaterialTheme.typography.labelMedium)
                }
            } else {
                // Editable Fields
                InputField(value = name.value,
                    label = "Full Name",
                    onValueChange = { name.value = it },
                    isRecording = isRecording,
                    onMicClick = {
                    if (isRecording.value){
                        speechToTextHelper.stopListening()
                    } else{
                        speechToTextHelper.startListening("Full Name")
                    }
                    isRecording.value = !isRecording.value
                    }
                )
                InputField(value = email.value,
                    label = "Email",
                    onValueChange = { email.value = it },
                    isRecording = isRecording,
                    onMicClick = {
                        if (isRecording.value){
                            speechToTextHelper.stopListening()
                        } else{
                            speechToTextHelper.startListening("Email")
                        }
                        isRecording.value = !isRecording.value
                    }
                )
                InputField(value = phone.value,
                    label = "Phone Number",
                    onValueChange = { phone.value = it },
                    isRecording = isRecording,
                    onMicClick = {
                        if (isRecording.value){
                            speechToTextHelper.stopListening()
                        } else{
                            speechToTextHelper.startListening("Phone Number")
                        }
                        isRecording.value = !isRecording.value
                    }
                    )
                InputField(value = bloodType.value,
                    label = "Blood Type",
                    onValueChange = { bloodType.value = it },
                    isRecording = isRecording,
                    onMicClick = {
                        if (isRecording.value){
                            speechToTextHelper.stopListening()
                        } else{
                            speechToTextHelper.startListening("Blood Type")
                        }
                        isRecording.value = !isRecording.value
                    }
                )
                InputField(value = medicalInfo.value,
                    label = "Medical Information",
                    onValueChange = { medicalInfo.value = it },
                    isRecording = isRecording,
                    onMicClick = {
                        if (isRecording.value){
                            speechToTextHelper.stopListening()
                        } else{
                            speechToTextHelper.startListening("Medical Information")
                        }
                        isRecording.value = !isRecording.value
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Save Button
                 Button(onClick = { isEditing.value = false },
                     colors = ButtonDefaults.buttonColors(
                         containerColor = BlueGray,
                         contentColor = Color.White
                     ),
                     shape = RoundedCornerShape(size = 4.dp)) {
                    Text("Save",
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }
}
}*/

@Composable
fun PersonalInformationItem(
    ttsHelper: TextToSpeechHelper
) {
    val context = LocalContext.current
    val isEditing = remember { mutableStateOf(false) }
    val name = remember { mutableStateOf("John Doe") }
    val email = remember { mutableStateOf("john.doe@example.com") }
    val phone = remember { mutableStateOf("1234567890") }
    val bloodType = remember { mutableStateOf("O+") }
    val medicalInfo = remember { mutableStateOf("No allergies") }

    val currentUser = FirebaseAuth.getInstance().currentUser
    val db = FirebaseFirestore.getInstance()

    val isNameListening = remember { mutableStateOf(false) }
    val isEmailListening = remember { mutableStateOf(false) }
    val isPhoneListening = remember { mutableStateOf(false) }
    val isBloodTypeListening = remember { mutableStateOf(false) }
    val isMedicalInfoListening = remember { mutableStateOf(false) }
    val speechToTextHelper = remember {
        SpeechToTextHelper(context) { result ->
            when {
                isNameListening.value -> {
                    name.value = result
                    isNameListening.value = false
                }
                isEmailListening.value -> {
                    email.value = result
                    isEmailListening.value = false
                }
                isPhoneListening.value -> {
                    phone.value = result
                    isPhoneListening.value = false
                }
                isBloodTypeListening.value -> {
                    bloodType.value = result
                    isBloodTypeListening.value = false
                }
                isMedicalInfoListening.value -> {
                    medicalInfo.value = result
                    isMedicalInfoListening.value = false
                }
            }
        }
    }

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            getPersonalInfo(
                userId = userId,
                onSuccess = { data ->
                    name.value = data["name"] ?: "John Doe"
                    email.value = data["email"] ?: "john.doe@example.com"
                    phone.value = data["phone"] ?: "1234567890"
                    bloodType.value = data["bloodType"] ?: "O+"
                    medicalInfo.value = data["medicalInfo"] ?: "No known allergies"
                },
                onFailure = { exception ->
                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    Column(modifier = Modifier.padding(20.dp)) {
        Text(
            text = "Personal Information",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (!isEditing.value) {
            // Display Read-Only Information
            TextRow(label = "Full Name", value = name.value)
            TextRow(label = "Email", value = email.value)
            TextRow(label = "Phone Number", value = phone.value)
            TextRow(label = "Blood Type", value = bloodType.value)
            TextRow(label = "Medical Info", value = medicalInfo.value)

            Spacer(modifier = Modifier.height(16.dp))

            // Edit Button
            Button(
                onClick = {
                    isEditing.value = true
                    ttsHelper.speak("Edit personal information")
                    },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueGray,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(size = 4.dp)
            ) {
                Text("Edit Information", style = MaterialTheme.typography.labelMedium)
            }
        } else {
            // Editable Fields
            InputField(
                value = name.value,
                label = "Full Name",
                onValueChange = { name.value = it },
                isRecording = isNameListening,
                onMicClick = {
                    // Stop other field listeners
                    isEmailListening.value = false
                    isPhoneListening.value = false
                    isBloodTypeListening.value = false
                    isMedicalInfoListening.value = false

                    if (isNameListening.value) {
                        speechToTextHelper.stopListening()
                    } else {
                        speechToTextHelper.startListening("Full Name")
                    }
                    isNameListening.value = !isNameListening.value
                }
            )

            InputField(value = email.value, label = "Email", onValueChange = { email.value = it }, isRecording = isEmailListening, onMicClick = {
                isNameListening.value = false
                isPhoneListening.value = false
                isBloodTypeListening.value = false
                isMedicalInfoListening.value = false
                isEmailListening.value = !isEmailListening.value
                if (isEmailListening.value) speechToTextHelper.startListening("Email") else speechToTextHelper.stopListening()
            })
            InputField(value = phone.value, label = "Phone Number", onValueChange = { phone.value = it }, isRecording = isPhoneListening, onMicClick = {
                isNameListening.value = false
                isEmailListening.value = false
                isBloodTypeListening.value = false
                isMedicalInfoListening.value = false
                isPhoneListening.value = !isPhoneListening.value
                if (isPhoneListening.value) speechToTextHelper.startListening("Phone Number") else speechToTextHelper.stopListening()
            })
            InputField(value = bloodType.value, label = "Blood Type", onValueChange = { bloodType.value = it }, isRecording = isBloodTypeListening, onMicClick = {
                isNameListening.value = false
                isPhoneListening.value = false
                isEmailListening.value = false
                isMedicalInfoListening.value = false
                isBloodTypeListening.value = !isBloodTypeListening.value
                if (isBloodTypeListening.value) speechToTextHelper.startListening("Blood Type") else speechToTextHelper.stopListening()
            })
            InputField(value = medicalInfo.value, label = "Medical Information", onValueChange = { medicalInfo.value = it }, isRecording = isMedicalInfoListening, onMicClick = {
                isNameListening.value = false
                isPhoneListening.value = false
                isBloodTypeListening.value = false
                isEmailListening.value = false
                isMedicalInfoListening.value = !isMedicalInfoListening.value
                if (isMedicalInfoListening.value) speechToTextHelper.startListening("Medical Information") else speechToTextHelper.stopListening()
            })

            Spacer(modifier = Modifier.height(16.dp))

            // Save Button
            Button(
                onClick = {
                    isEditing.value = false
                    ttsHelper.speak("Saving personal information")

                    currentUser?.uid?.let { userId ->
                        val personalInfo = mapOf(
                            "name" to name.value,
                            "email" to email.value,
                            "phone" to phone.value,
                            "bloodType" to bloodType.value,
                            "medicalInfo" to medicalInfo.value
                        )

                        savePersonalInfo(
                            userId = userId,
                            personalInfo = personalInfo,
                            onSuccess = {
                                Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                            },
                            onFailure = { exception ->
                                Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                            }
                        )

                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = BlueGray,
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(size = 4.dp)
            ) {
                Text("Save", style = MaterialTheme.typography.labelMedium)
            }
        }
    }
}

fun getPersonalInfo(userId: String, onSuccess: (Map<String, String>) -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val data = mapOf(
                    "name" to (document.getString("name") ?: "John Doe"),
                    "email" to (document.getString("email") ?: "john.doe@example.com"),
                    "phone" to (document.getString("phone") ?: "1234567890"),
                    "bloodType" to (document.getString("bloodType") ?: "O+"),
                    "medicalInfo" to (document.getString("medicalInfo") ?: "No known allergies")
                )
                onSuccess(data)
            }
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

fun savePersonalInfo(userId: String, personalInfo: Map<String, String>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId).set(personalInfo)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
}

/*@Composable
fun EmergencyContactsItem(emergencyContacts: MutableList<Pair<String, String>>, sharingState: MutableMap<String, Boolean>) {
    val expanded = remember { mutableStateOf(false) }
    val showDialog = remember { mutableStateOf(false) }
    val isEditing = remember { mutableStateOf(false) }
    val selectedContactIndex = remember { mutableStateOf(-1) }
    val newContactName = remember { mutableStateOf("") }
    val newContactPhone = remember { mutableStateOf("") }
    val showEditOptions = remember { mutableStateOf(false) } // To show delete or update options
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val isRecording = remember { mutableStateOf(false) }
    val context = LocalContext.current
    val speechToTextHelper = remember { SpeechToTextHelper(context) { result ->
        when{
            isRecording.value && newContactName.value.isEmpty() -> newContactName.value = result
            isRecording.value && newContactPhone.value.isEmpty() -> newContactPhone.value = result
        }
        isRecording.value = !isRecording.value
    } }

    DropdownOptionItem(
        title = "Emergency Contacts",
        leadingIcon = Icons.Default.Phone,
        expanded = expanded,
        onClick = {}
    ) {
        if (expanded.value) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Add Emergency Contact Button
                Button(
                    onClick = {
                        showDialog.value = true
                        isEditing.value = false
                        newContactName.value = ""
                        newContactPhone.value = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(size = 4.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Add Emergency Contact",
                        style = MaterialTheme.typography.labelMedium)
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Display existing contacts
                emergencyContacts.forEachIndexed { index, contact ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${contact.first} - ${contact.second}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = uiColor
                        )
                        Button(
                            onClick = {
                                selectedContactIndex.value = index
                                newContactName.value = contact.first
                                newContactPhone.value = contact.second
                                showEditOptions.value = true
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = BlueGray,
                                contentColor = Color.White
                            ),
                            shape = RoundedCornerShape(size = 4.dp)
                        ) {
                            Text("Edit",
                                style = MaterialTheme.typography.labelMedium)
                        }
                    }
                }
            }
        }
    }

    // Edit Options Dialog (Delete or Update)
    if (showEditOptions.value) {
        AlertDialog(
            onDismissRequest = { showEditOptions.value = false },
            title = { Text("Edit Emergency Contact") },
            text = {
                Column {
                    Button(
                        onClick = {
                            // Proceed to delete the contact
                            emergencyContacts.removeAt(selectedContactIndex.value)
                            showEditOptions.value = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlueGray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(size = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Delete Contact",
                            style = MaterialTheme.typography.labelMedium)
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            // Proceed to update contact, show the edit dialog
                            showDialog.value = true
                            isEditing.value = true
                            showEditOptions.value = false
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = BlueGray,
                            contentColor = Color.White
                        ),
                        shape = RoundedCornerShape(size = 4.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update Contact",
                            style = MaterialTheme.typography.labelMedium)
                    }
                }
            },
            confirmButton = {},
            dismissButton = {}
        )
    }

    // Dialog for Adding or Editing a Contact
    if (showDialog.value) {
        AlertDialog(
            onDismissRequest = { showDialog.value = false },
            title = { Text(if (isEditing.value) "Edit Emergency Contact" else "Add Emergency Contact") },
            text = {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        TextField(
                            value = newContactName.value,
                            onValueChange = { newContactName.value = it },
                            label = { Text("Full Name") },
                            colors = TextFieldDefaults.colors(
                                unfocusedPlaceholderColor = MaterialTheme.colorScheme.unfocusedTextFieldText,
                                focusedPlaceholderColor = MaterialTheme.colorScheme.focusedTextFieldText,
                                unfocusedContainerColor = MaterialTheme.colorScheme.textFieldContainer,
                                focusedContainerColor = MaterialTheme.colorScheme.textFieldContainer
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Image(
                            painter = painterResource(id = if (isRecording.value) R.drawable.ic_stop_recording else R.drawable.ic_microphone),
                            contentDescription = if (isRecording.value) "Stop Recording" else "Start Recording",
                            modifier = Modifier
                                .clickable {
                                    if (isRecording.value) {
                                        speechToTextHelper.stopListening()
                                    } else {
                                        speechToTextHelper.startListening("Full Name")
                                    }
                                    isRecording.value = !isRecording.value
                                }
                                .size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    TextField(
                        value = newContactPhone.value,
                        onValueChange = { newContactPhone.value = it },
                        label = { Text("Phone Number") },
                        colors = TextFieldDefaults.colors(
                            unfocusedPlaceholderColor = MaterialTheme.colorScheme.unfocusedTextFieldText,
                            focusedPlaceholderColor = MaterialTheme.colorScheme.focusedTextFieldText,
                            unfocusedContainerColor = MaterialTheme.colorScheme.textFieldContainer,
                            focusedContainerColor = MaterialTheme.colorScheme.textFieldContainer
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Image(
                        painter = painterResource(id = if (isRecording.value) R.drawable.ic_stop_recording else R.drawable.ic_microphone),
                        contentDescription = if (isRecording.value) "Stop Recording" else "Start Recording",
                        modifier = Modifier
                            .clickable {
                                if (isRecording.value) {
                                    speechToTextHelper.stopListening()
                                } else {
                                    speechToTextHelper.startListening("Phone Number")
                                }
                                isRecording.value = !isRecording.value
                            }
                            .size(24.dp)
                    )

                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newContactName.value.isNotBlank() && newContactPhone.value.isNotBlank()) {
                            if (isEditing.value) {
                                // Update the contact at the selected index
                                emergencyContacts[selectedContactIndex.value] = Pair(newContactName.value, newContactPhone.value)
                            } else {
                                // Add new contact
                                val newContact = Pair(newContactName.value, newContactPhone.value)
                                emergencyContacts.add(newContact)
                                sharingState[newContact.second] = false
                            }
                            newContactName.value = ""
                            newContactPhone.value = ""
                            showDialog.value = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(size = 4.dp)
                ) {
                    Text(if (isEditing.value) "Update" else "Add",
                        style = MaterialTheme.typography.labelMedium)
                }
            },
            dismissButton = {
                Button(onClick = { showDialog.value = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(size = 4.dp)) {
                    Text("Cancel",
                        style = MaterialTheme.typography.labelMedium)
                }
            }
        )
    }
} */

@Composable
fun EmergencyContactsItem(emergencyContacts: MutableList<Pair<String, String>>, ttsHelper: TextToSpeechHelper) {
    val context = LocalContext.current
    val isEditing = remember { mutableStateOf(false) }
    val isNameListening = remember { mutableStateOf(false) }
    val isPhoneListening = remember { mutableStateOf(false) }
    val speechToTextHelper = remember {
        SpeechToTextHelper(context) { result ->
            emergencyContacts.forEachIndexed { index, contact ->
                if (isNameListening.value) {
                    emergencyContacts[index] = Pair(result, contact.second)
                    isNameListening.value = false
                } else if (isPhoneListening.value) {
                    emergencyContacts[index] = Pair(contact.first, result)
                    isPhoneListening.value = false
                }
            }
        }
    }

    val currentUser = FirebaseAuth.getInstance().currentUser

    val nameStates = remember { mutableStateListOf<MutableState<String>>()}
    val phoneStates = remember { mutableStateListOf<MutableState<String>>()}

    LaunchedEffect(currentUser?.uid) {
        currentUser?.uid?.let { userId ->
            getEmergencyContacts(
                userId = userId,
                onSuccess = { contacts ->
                    emergencyContacts.clear()
                    emergencyContacts.addAll(contacts)
                    nameStates.clear()
                    phoneStates.clear()
                    contacts.forEach{
                        nameStates.add(mutableStateOf(it.first))
                        phoneStates.add(mutableStateOf(it.second))
                    }
                },
                onFailure = { exception ->
                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    Text(
        text = "Emergency Contacts",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(16.dp)
    )

    Column(modifier = Modifier.padding(16.dp)) {
        if (!isEditing.value) {
            emergencyContacts.forEachIndexed { index, contact ->
                TextRow(label = "Name", value = contact.first)
                TextRow(label = "Phone", value = contact.second)
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (emergencyContacts.isNotEmpty()) {
                Button(
                    onClick = {
                        isEditing.value = true
                        ttsHelper.speak("Editing emergency contacts")
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BlueGray, contentColor = Color.White),
                    shape = RoundedCornerShape(size = 4.dp)
                ) {
                    Text("Edit", style = MaterialTheme.typography.labelMedium)
                }
            }
        } else {
            nameStates.forEachIndexed { index, nameState ->
                val phoneState = phoneStates[index]

                InputField(
                    value = nameState.value,
                    label = "Full Name",
                    onValueChange = { nameState.value = it },
                    isRecording = isNameListening,
                    onMicClick = {
                        if (isNameListening.value) {
                            speechToTextHelper.stopListening()
                        } else {
                            speechToTextHelper.startListening("Full Name")
                        }
                        isNameListening.value = !isNameListening.value
                    }
                )

                InputField(
                    value = phoneState.value,
                    label = "Phone Number",
                    onValueChange = { phoneState.value = it },
                    isRecording = isPhoneListening,
                    onMicClick = {
                        if (isPhoneListening.value) {
                            speechToTextHelper.stopListening()
                        } else {
                            speechToTextHelper.startListening("Phone Number")
                        }
                        isPhoneListening.value = !isPhoneListening.value
                    }
                )

                Spacer(modifier = Modifier.height(8.dp))

                Row {
                    Button(onClick = {
                        isEditing.value = false
                        ttsHelper.speak("Cancel editing emergency contact")
                    },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Gray, contentColor = Color.White),
                        shape = RoundedCornerShape(size = 4.dp)) {
                        Text("Cancel", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        emergencyContacts.clear()
                        nameStates.indices.forEach{ i ->
                            emergencyContacts.add(Pair(nameStates[i].value, phoneStates[i].value))
                        }
                        isEditing.value = false
                        currentUser?.uid?.let { userId ->
                            saveEmergencyContacts(
                                userId = userId,
                                contacts = emergencyContacts,
                                onSuccess = {
                                    Toast.makeText(context, "Saved successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { exception ->
                                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        ttsHelper.speak("Save emergency contact")
                    },
                        colors = ButtonDefaults.buttonColors(containerColor = BlueGray, contentColor = Color.White),
                        shape = RoundedCornerShape(size = 4.dp)) {
                        Text("Save", fontSize = 12.sp)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(onClick = {
                        emergencyContacts.removeAt(index)
                        nameStates.removeAt(index)
                        phoneStates.removeAt(index)

                        isEditing.value = false
                        currentUser?.uid?.let { userId ->
                            deleteEmergencyContact(
                                userId = userId,
                                index = index,
                                contacts = emergencyContacts,
                                onSuccess = {
                                    Toast.makeText(context, "Deleted successfully!", Toast.LENGTH_SHORT).show()
                                },
                                onFailure = { exception ->
                                    Toast.makeText(context, "Error: ${exception.message}", Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                        ttsHelper.speak("Delete emergency contact")
                    },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red, contentColor = Color.White),
                        shape = RoundedCornerShape(size = 4.dp)) {
                        Text("Delete", fontSize = 12.sp)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = {
            emergencyContacts.add(Pair("", ""))
            nameStates.add(mutableStateOf(""))
            phoneStates.add(mutableStateOf(""))
            ttsHelper.speak("Emergency contact added")
        },
            colors = ButtonDefaults.buttonColors(containerColor = BlueGray, contentColor = Color.White),
            shape = RoundedCornerShape(size = 4.dp),
            modifier = Modifier.fillMaxWidth()) {
            Text("Add Emergency Contact", style = MaterialTheme.typography.labelMedium)
        }
    }
}

fun getEmergencyContacts(userId: String, onSuccess: (List<Pair<String, String>>) -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            if (document != null && document.exists()) {
                val contacts = document.get("emergencyContacts") as? List<Map<String, String>>
                val contactList = contacts?.map {
                    Pair(it["name"] ?: "Unknown", it["phone"] ?: "0000000000")
                } ?: emptyList()
                onSuccess(contactList)
            }
        }
        .addOnFailureListener { exception ->
            onFailure(exception)
        }
}

fun saveEmergencyContacts(userId: String, contacts: List<Pair<String, String>>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()
    val contactList = contacts.map { mapOf("name" to it.first, "phone" to it.second) }

    db.collection("users").document(userId).update("emergencyContacts", contactList)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
}

fun deleteEmergencyContact(userId: String, index: Int, contacts: MutableList<Pair<String, String>>, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    val db = FirebaseFirestore.getInstance()

    contacts.removeAt(index)

    val contactList = contacts.map { mapOf("name" to it.first, "phone" to it.second) }

    db.collection("users").document(userId).update("emergencyContacts", contactList)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
}


@Composable
fun DropdownOptionItem(
    title: String,
    leadingIcon: ImageVector,
    expanded: MutableState<Boolean>,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded.value = !expanded.value }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "$title Icon",
                tint = Color(0xFF3EB6FF),
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = uiColor
            )
        }
        Icon(
            imageVector = if (expanded.value) Icons.Outlined.KeyboardArrowUp else Icons.Outlined.KeyboardArrowDown,
            contentDescription = "$title Arrow",
            tint = Color(0xFF3EB6FF),
            modifier = Modifier.size(24.dp)
        )
    }
    if (expanded.value) {
        content()
    }
}

@Composable
fun OptionItem(
    title: String,
    leadingIcon: ImageVector,
    trailingIcon: ImageVector? = Icons.Outlined.KeyboardArrowDown,
    iconColor: Color = Color(0xFF3EB6FF),
    onClick: () -> Unit
) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = "$title Icon",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = uiColor
            )
        }

        trailingIcon?.let {
            Icon(
                imageVector = it,
                contentDescription = "$title Arrow",
                tint = iconColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
