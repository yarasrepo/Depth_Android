package com.example.virtualcane3

import android.widget.Toast
import androidx.annotation.OptIn
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.media3.common.util.Log
import androidx.media3.common.util.UnstableApi
import androidx.navigation.NavController
import com.example.virtualcane3.ui.theme.BlueGray
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(navController: NavController) {
    val context = LocalContext.current
    val ttsHelper = remember { TextToSpeechHelper(context) }
    val userId = FirebaseAuth.getInstance().currentUser?.uid

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
        ){
            Spacer(modifier = Modifier.height(16.dp))

            userId?.let { uid ->
                SettingsCategory("Sensitivity Settings", ttsHelper) {
                    RangeSettingsItem(ttsHelper, userId)
                }

                SettingsCategory("Feedback Preferences", ttsHelper) {
                    FeedbackPreferencesItem(ttsHelper, userId)
                }

                SettingsCategory("Obstacle Customization", ttsHelper) {
                    ObstacleCustomizationItem(ttsHelper, userId)
                }

                SettingsCategory("Environmental Modes", ttsHelper) {
                    EnvironmentalModesItem(ttsHelper, userId)
                }

                SettingsCategory("Accessibility Features", ttsHelper) {
                    AccessibilityFeaturesItem(ttsHelper, userId)
                }

                SettingsCategory("Device Management", ttsHelper) {
                    DeviceManagementItem(ttsHelper, userId)
                }

                SettingsCategory("Safety Features", ttsHelper) {
                    SafetyFeaturesItem(ttsHelper, userId)
                }

                SettingsCategory("Data Settings", ttsHelper) {
                    DataPrivacySettingsItem(ttsHelper, userId)
                }

                SettingsCategory("General Settings", ttsHelper) {
                    GeneralSettingsItem(ttsHelper, userId)
                }
            }

            Spacer(modifier = Modifier.weight(1f))
        }
    }
}

@OptIn(UnstableApi::class)
fun updateUserSetting(userId: String, key: String, value: Any) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId)
        .update("settings.$key", value)
        .addOnSuccessListener { Log.d("Settings", "$key updated to $value") }
        .addOnFailureListener { e -> Log.e("Settings", "Failed to update $key", e) }
}

@Composable
fun SettingsCategory(title: String, ttsHelper: TextToSpeechHelper, content: @Composable () -> Unit) {
    val expanded = remember { mutableStateOf(false) }

    DropdownOptionItem(
        title = title,
        leadingIcon = Icons.Default.Settings,
        expanded = expanded,
        onClick = {
            expanded.value = !expanded.value
            if(expanded.value){
                ttsHelper.speak("$title settings expanded")
            }
        }
    ) {
        if (expanded.value) {
            Column(modifier = Modifier.padding(horizontal = 16.dp)) {
                content()
            }
        }
    }
}

@OptIn(UnstableApi::class)
fun loadUserSettings(
    userId: String,
    onSettingsLoaded: (Map<String, Any>) -> Unit
) {
    val db = FirebaseFirestore.getInstance()
    db.collection("users").document(userId).get()
        .addOnSuccessListener { document ->
            val settings = document.get("settings") as? Map<String, Any>
            if (settings != null) {
                onSettingsLoaded(settings)
            } else {
                onSettingsLoaded(emptyMap())
            }
        }
        .addOnFailureListener { e ->
            Log.e("Settings", "Failed to load settings", e)
            onSettingsLoaded(emptyMap())
        }
}

@Composable
fun RangeSettingsItem(ttsHelper: TextToSpeechHelper, userId: String) {
    val detectionRange = remember { mutableStateOf("Medium") }
    val sensitivityLevel = remember { mutableStateOf("Normal") }

    LaunchedEffect(Unit) {
        loadUserSettings(userId) { settings ->
            detectionRange.value = settings["detectionRange"] as? String ?: "Medium"
            sensitivityLevel.value = settings["sensitivityLevel"] as? String ?: "Normal"
        }
    }

    TextRow(label = "Object Detection Range", value = detectionRange.value)
    OptionsRow(
        label = "Range",
        options = listOf("Short", "Medium", "Long"),
        selectedOption = detectionRange.value,
        onOptionSelected = {
            detectionRange.value = it
            ttsHelper.speak("Detection range set to $it, but this feature is coming soon")
            updateUserSetting(userId, "detectionRange", it)
        }
    )

    TextRow(
        label = "Sensitivity Level",
        value = sensitivityLevel.value
    )
    OptionsRow(
        label = "Sensitivity",
        options = listOf("Low", "Normal", "High"),
        selectedOption = sensitivityLevel.value,
        onOptionSelected = {
            sensitivityLevel.value = it
            ttsHelper.speak("Sensitivity level set to $it, but this feature is coming soon")
            updateUserSetting(userId, "sensitivityLevel", it)
        }
    )
}

@Composable
fun FeedbackPreferencesItem(ttsHelper: TextToSpeechHelper, userId: String) {
    val feedbackMode = remember { mutableStateOf("Vibration") }
    val vibrationIntensity = remember { mutableStateOf(50f) }
    val audioVolume = remember { mutableStateOf(75f) }

    LaunchedEffect(Unit) {
        loadUserSettings(userId) { settings ->
            feedbackMode.value = settings["feedbackMode"] as? String ?: "Vibration"
            vibrationIntensity.value = (settings["vibrationIntensity"] as? Number)?.toFloat() ?: 50f
            audioVolume.value = (settings["audioVolume"] as? Number)?.toFloat() ?: 75f
        }
    }

    OptionsRow(
        label = "Feedback Mode",
        options = listOf("Vibration", "Audio", "Both"),
        selectedOption = feedbackMode.value,
        onOptionSelected = {
            feedbackMode.value = it
            ttsHelper.speak("Feedback mode set to $it, but this feature is coming soon")
            updateUserSetting(userId, "feedbackMode", it)
        }
    )

    SliderRow(
        label = "Vibration Intensity",
        value = vibrationIntensity.value,
        onValueChange = {
            vibrationIntensity.value = it
            ttsHelper.speak("Vibration intensity set to $it, but this feature is coming soon")
            updateUserSetting(userId, "vibrationIntensity", it)
        }
    )

    SliderRow(
        label = "Audio Volume",
        value = audioVolume.value,
        onValueChange = {
            audioVolume.value = it
            ttsHelper.speak("Audio volume set to $it, but this feature is coming soon")
            updateUserSetting(userId, "audioVolume", it)
        }
    )
}

@Composable
fun ObstacleCustomizationItem(ttsHelper: TextToSpeechHelper, userId: String) {
    val obstacleCategories = remember { mutableStateOf(setOf("Low Objects", "Walls")) }
    val alertDistance = remember { mutableStateOf(50f) }

    LaunchedEffect(Unit) {
        loadUserSettings(userId) { settings ->
            val categories = settings["obstacleCategories"] as? List<String> ?: listOf()
            obstacleCategories.value = categories.toSet()
            alertDistance.value = (settings["alertDistance"] as? Number)?.toFloat() ?: 50f
        }
    }

    MultiSelectRow(
        label = "Obstacle Categories",
        options = listOf("Low Objects", "Walls", "Moving Objects"),
        selectedOptions = obstacleCategories.value,
        onOptionSelected = { selected ->
            if (selected in obstacleCategories.value) {
                obstacleCategories.value = obstacleCategories.value - selected
                ttsHelper.speak("$selected deselected")
            } else {
                obstacleCategories.value = obstacleCategories.value + selected
                ttsHelper.speak("$selected selected, but this feature is coming soon")
            }
            updateUserSetting(userId, "obstacleCategories", obstacleCategories.value.toList())
        }
    )

    SliderRow(
        label = "Object Distance Alerts",
        value = alertDistance.value,
        onValueChange = {
            alertDistance.value = it
            ttsHelper.speak("Object distance alert set to $it, but this feature is coming soon")
            updateUserSetting(userId, "alertDistance", it)
        }
    )
}

@Composable
fun EnvironmentalModesItem(ttsHelper: TextToSpeechHelper, userId: String) {
    val environmentMode = remember { mutableStateOf("Indoor") }
    val lightingCondition = remember { mutableStateOf("Daylight") }

    LaunchedEffect(Unit) {
        loadUserSettings(userId) { settings ->
            environmentMode.value = settings["environmentMode"] as? String ?: "Indoor"
            lightingCondition.value = settings["lightingCondition"] as? String ?: "Daylight"
        }
    }

    OptionsRow(
        label = "Indoor/Outdoor Mode",
        options = listOf("Indoor", "Outdoor"),
        selectedOption = environmentMode.value,
        onOptionSelected = {
            environmentMode.value = it
            ttsHelper.speak("Environment mode set to $it, but this feature is coming soon")
            updateUserSetting(userId, "environmentMode", it)
        }
    )

    OptionsRow(
        label = "Lighting Conditions",
        options = listOf("Daylight", "Low Light", "Night"),
        selectedOption = lightingCondition.value,
        onOptionSelected = {
            lightingCondition.value = it
            ttsHelper.speak("Lighting condition set to $it, but this feature is coming soon")
            updateUserSetting(userId, "lightingCondition", it)
        }
    )
}

@Composable
fun AccessibilityFeaturesItem(ttsHelper: TextToSpeechHelper, userId: String) {
    val textToSpeechSpeed = remember { mutableStateOf(1.0f) }
    val screenReaderMode = remember { mutableStateOf(true) }
    val language = remember { mutableStateOf("English") }
    val appearance = remember{ mutableStateOf("Light") }
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        loadUserSettings(userId) { settings ->
            textToSpeechSpeed.value = (settings["textToSpeechSpeed"] as? Number)?.toFloat() ?: 1.0f
            screenReaderMode.value = settings["screenReaderMode"] as? Boolean ?: true
            language.value = settings["language"] as? String ?: "English"
            appearance.value = settings["appearance"] as? String ?: "Light"
        }
    }

    SliderRow(
        label = "Text-to-Speech Speed",
        value = textToSpeechSpeed.value,
        onValueChange = {
            textToSpeechSpeed.value = it
            ttsHelper.speak("Text-to-speech speed set to $it, but this feature is coming soon")
            updateUserSetting(userId, "textToSpeechSpeed", it)
        }
    )

    OptionsRow(
        label = "Language Settings",
        options = listOf("English", "Spanish", "French"),
        selectedOption = language.value,
        onOptionSelected = { selected ->
            if (selected != "English") {
                ttsHelper.speak("$selected is not available yet")
                Toast.makeText(context, "$selected is not available yet", Toast.LENGTH_SHORT).show()
            } else {
                language.value = selected
                ttsHelper.speak("Language set to $selected")
                updateUserSetting(userId, "language", selected)
            }
        }
    )

    ToggleRow(
        label = "Screen Reader Mode",
        checked = screenReaderMode.value,
        onCheckedChange = {
            screenReaderMode.value = it
            ttsHelper.speak("Screen reader mode set to $it, but this feature is coming soon")
            updateUserSetting(userId, "screenReaderMode", it)
        }
    )
    OptionsRow(
        label = "Appearance",
        options = listOf("Light", "Dark"),
        selectedOption = appearance.value,
        onOptionSelected = {
            appearance.value = it
            ttsHelper.speak("Appearance set to $it, but this feature is coming soon")
            updateUserSetting(userId, "appearance", it)
        }
    )
}

@Composable
fun DeviceManagementItem(ttsHelper: TextToSpeechHelper, userId: String) {
    val batteryMode = remember { mutableStateOf("Performance") }

    LaunchedEffect(Unit) {
        loadUserSettings(userId) { settings ->
            batteryMode.value = settings["batteryMode"] as? String ?: "Performance"
        }
    }

    OptionsRow(
        label = "Battery Usage Mode",
        options = listOf("Performance", "Battery Saver"),
        selectedOption = batteryMode.value,
        onOptionSelected = {
            batteryMode.value = it
            ttsHelper.speak("Battery usage mode set to $it, but this feature is coming soon")
            updateUserSetting(userId, "batteryMode", it)
        }
    )

    TextRow(
        label = "Software Updates",
        value = "Up-to-date",
    )
}

@Composable
fun SafetyFeaturesItem(ttsHelper: TextToSpeechHelper, userId: String) {
    val emergencyAlertEnabled = remember { mutableStateOf(true) }
    val fallDetectionSensitivity = remember { mutableStateOf(2.0f) }

    LaunchedEffect(Unit) {
        loadUserSettings(userId) { settings ->
            emergencyAlertEnabled.value = settings["emergencyAlertEnabled"] as? Boolean ?: true
            fallDetectionSensitivity.value = (settings["fallDetectionSensitivity"] as? Number)?.toFloat() ?: 2.0f
        }
    }

    ToggleRow(
        label = "Emergency Alert Button",
        checked = emergencyAlertEnabled.value,
        onCheckedChange = {
            emergencyAlertEnabled.value = it
            ttsHelper.speak("Emergency alert button is set to $it, but this feature is coming soon")
            updateUserSetting(userId, "emergencyAlertEnabled", it)
        }
    )

    SliderRow(
        label = "Fall Detection Sensitivity",
        value = fallDetectionSensitivity.value,
        onValueChange = {
            fallDetectionSensitivity.value = it
            ttsHelper.speak("Fall detection sensitivity set to $it, but this feature is coming soon")
            updateUserSetting(userId, "fallDetectionSensitivity", it)
        }
    )
}

@Composable
fun DataPrivacySettingsItem(ttsHelper: TextToSpeechHelper, userId: String) {
    val dataSharingEnabled = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        loadUserSettings(userId) { settings ->
            dataSharingEnabled.value = settings["dataSharingEnabled"] as? Boolean ?: false
        }
    }

    ToggleRow(
        label = "Data Sharing",
        checked = dataSharingEnabled.value,
        onCheckedChange = {
            dataSharingEnabled.value = it
            ttsHelper.speak("Data sharing is set to $it, but this feature is coming soon")
            updateUserSetting(userId, "dataSharingEnabled", it)
        }
    )
}

@Composable
fun GeneralSettingsItem(ttsHelper: TextToSpeechHelper, userId: String) {
    val notificationsEnabled = remember { mutableStateOf(true) }
    val appVersion = "1.0.0"

    LaunchedEffect(Unit) {
        loadUserSettings(userId) { settings ->
            notificationsEnabled.value = settings["notificationsEnabled"] as? Boolean ?: true
        }
    }

    ToggleRow(
        label = "Notifications",
        checked = notificationsEnabled.value,
        onCheckedChange = {
            notificationsEnabled.value = it
            ttsHelper.speak("Notifications are set to $it, but this feature is coming soon")
            updateUserSetting(userId, "notificationsEnabled", it)
        }
    )

    TextRow(
        label = "About App",
        value = "Version $appVersion"
    )
}

@Composable
fun ToggleRow(label: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = uiColor
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.Gray,
                uncheckedThumbColor = Color.Gray,
                checkedTrackColor = BlueGray,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@Composable
fun MultiSelectRow(label: String, options: List<String>, selectedOptions: Set<String>, onOptionSelected: (String) -> Unit) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val checkedColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val uncheckedColor = if (isSystemInDarkTheme()) Color.Gray else Color.DarkGray

    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            label,
            style = MaterialTheme.typography.bodyLarge,
            color = uiColor
        )

        options.forEach { option ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    option,
                    style = MaterialTheme.typography.bodyLarge,
                    color = uiColor
                )
                Checkbox(
                    checked = selectedOptions.contains(option),
                    onCheckedChange = { onOptionSelected(option) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = checkedColor,
                        uncheckedColor = uncheckedColor,
                        checkmarkColor = if (isSystemInDarkTheme()) Color.Black else Color.White // Adjust checkmark visibility
                    )
                )
            }
        }
    }
}

@Composable
fun OptionsRow(label: String, options: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            label,
            fontSize = 15.sp,
            color = uiColor
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            options.forEach { option ->
                Button(
                    onClick = { onOptionSelected(option) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BlueGray,
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(size = 4.dp),
                ) {
                    Text(
                        option,
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}

@Composable
fun SliderRow(label: String, value: Float, onValueChange: (Float) -> Unit) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val uiColor2 = if (isSystemInDarkTheme()) Color.White else BlueGray
    Column(modifier = Modifier.padding(vertical = 8.dp)) {
        Text(
            "$label (${value.toInt()}%)",
            style = MaterialTheme.typography.bodyLarge,
            color = uiColor
        )
        Slider(
            value = value,
            onValueChange = { newValue ->
                onValueChange(newValue.roundToInt().toFloat())
            },
            colors =SliderDefaults.colors(
                thumbColor = uiColor2,
                activeTrackColor = uiColor2
            ),
            valueRange = 0f..100f,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
