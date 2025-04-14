
package com.example.virtualcane3

import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import com.example.virtualcane3.ui.theme.Black
import com.example.virtualcane3.ui.theme.BlueGray
import android.Manifest
import android.util.Patterns
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore


@Composable
fun RequestAudioPermission() {
    val context = LocalContext.current
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (!isGranted) {
                Toast.makeText(context, "Microphone permission is required!", Toast.LENGTH_SHORT).show()
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }
}

@Composable
fun RegisterScreen(navController: NavController) {
    val context = LocalContext.current
    val ttsHelper = remember { TextToSpeechHelper(context) }
    RequestAudioPermission()
    Surface {
        Column(modifier = Modifier.fillMaxSize()) {
            TopSection()
            Spacer(modifier = Modifier.height(6.dp))

            Column(modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp)
            ) {
                RegisterSection(navController = navController, ttsHelper)
                Spacer(modifier = Modifier.height(10.dp))
                SocialMediaSection(navController = navController, ttsHelper)
                LogIn(navController = navController, ttsHelper)
            }
        }
    }
}


@Composable
private fun LogIn(navController: NavController, ttsHelper: TextToSpeechHelper) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxHeight(fraction = 0.8f)
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Have an account?",
                style = TextStyle(
                    color = uiColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Log In",
                style = TextStyle(
                    color = uiColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .clickable {
                        ttsHelper.speak("Log in to your account")
                        navController.navigate("login")
                    }
            )
        }
    }
}

@Composable
private fun SocialMediaSection(navController: NavController, ttsHelper: TextToSpeechHelper) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
        try{
            val account = task.getResult(ApiException::class.java)
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            FirebaseAuth.getInstance().signInWithCredential(credential)
                .addOnCompleteListener { authResult ->
                    if (authResult.isSuccessful){
                        Toast.makeText(context, "Google login successful", Toast.LENGTH_SHORT).show()
                        navController.navigate("home")
                    } else{
                        Toast.makeText(context, "Google login failed", Toast.LENGTH_SHORT).show()
                    }
                }
        } catch(e: ApiException){
            Toast.makeText(context, "Google sign-in failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "Or continue with",
            style = TextStyle(
                color = uiColor,
                fontSize = 12.sp,
                fontWeight = FontWeight.Normal
            ),
            modifier = Modifier.weight(1f)
        )

        SocialMediaLogIn(
            icon = R.drawable.google,
            text = "Google",
            modifier = Modifier.weight(1f),
            onClick = {
                ttsHelper.speak("Sign up with Google")

                val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(context.getString(R.string.default_web_client_id))
                    .requestEmail()
                    .build()

                val client = GoogleSignIn.getClient(context, gso)
                val signInIntent = client.signInIntent
                launcher.launch(signInIntent)
            }
        )
    }
}

/*@Composable
private fun RegisterSection(navController: NavController) {
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    LoginTextField(label = "Email", trailing = "", modifier = Modifier.fillMaxWidth(), onTrailingClick = {showForgotPasswordDialog = false})
    Spacer(modifier = Modifier.height(15.dp))
    LoginTextField(label = "Username", trailing = "", modifier = Modifier.fillMaxWidth(), onTrailingClick = {showForgotPasswordDialog = false})
    Spacer(modifier = Modifier.height(15.dp))
    LoginTextField(label = "Password", trailing = "", modifier = Modifier.fillMaxWidth(), isPassword = true, onTrailingClick = {showForgotPasswordDialog = false})
    Spacer(modifier = Modifier.height(20.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        onClick = {navController.navigate("home") },
        colors = ButtonDefaults.buttonColors(
            containerColor = BlueGray,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(size = 4.dp)
    ) {
        Text(
            text = "Create Account",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}*/

@Composable
private fun RegisterSection(navController: NavController, ttsHelper: TextToSpeechHelper) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf("") }
    //var isListening by remember { mutableStateOf(false) }
    var isEmailListening by remember { mutableStateOf(false)}
    var isUsernameListening by remember { mutableStateOf(false)}
    var isPasswordListening by remember { mutableStateOf(false)}

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    fun registerUser(email: String, password: String, username: String) {
        if (username.length < 3) {
            Toast.makeText(context, "Username must be at least 3 characters", Toast.LENGTH_SHORT).show()
            return
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(context, "Please enter a valid email address", Toast.LENGTH_SHORT).show()
            return
        }

        if (password.length < 8 ||
            !password.any { it.isDigit() } ||
            !password.any { "!@#\$%^&*()-_=+[]{}|;:'\",.<>?/`~".contains(it) }
        ) {
            Toast.makeText(
                context,
                "Password must be at least 8 characters, include a number and a special character",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Registration successful, navigate to home or other screen
                        val user = auth.currentUser
                        val userId = user?.uid?:""
                        val userRef = db.collection("users").document(userId)

                        //Full Firestore structure for the user
                        val userData = mapOf(
                            "uid" to userId,
                            "email" to email,
                            "username" to username,
                            "phoneNumber" to "",
                            "bloodType" to "",
                            "medicalInfo" to "",
                            "emergencyContacts" to emptyList<Map<String, String>>()
                        )

                        // Save user data to Firestore
                        userRef.set(userData)
                            .addOnSuccessListener {
                                navController.navigate("home")
                            }
                            .addOnFailureListener {e ->
                                errorMessage = "Failed to save user data: ${e.message}"
                            }
                    } else {
                        // Handle registration failure
                        val exception = task.exception
                        errorMessage = when (exception) {
                            is FirebaseAuthUserCollisionException -> "User already exists"
                            is FirebaseAuthInvalidCredentialsException -> "Invalid email format"
                            else -> "Registration failed: ${exception?.message}"
                        }
                    }
                }
        } else {
            errorMessage = "Please fill in all fields"
        }
    }


    val speechHelper = remember {
        SpeechToTextHelper(context) { result ->
            when {
                isEmailListening -> {
                    email = result
                    isEmailListening = false
                }
                isUsernameListening -> {
                    username = result
                    isUsernameListening = false
                }
                isPasswordListening -> {
                    password = result
                    isPasswordListening = false
                }
            }
        }
    }


    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LoginTextField(
                label = "Email",
                text = email,
                isListening = isEmailListening,
                isPassword = false,
                trailing = "",
                onTrailingClick = {},
                modifier = Modifier
                    .weight(1f)
                    .clickable { ttsHelper.speak("Email field") }
            )

            Spacer(modifier = Modifier.width(1.dp))

            IconButton(onClick = {
                ttsHelper.speak("Email")
                if (isEmailListening) {
                    speechHelper.stopListening()
                } else {
                    speechHelper.startListening("Email")
                }
                isEmailListening = !isEmailListening
                isUsernameListening = false
                isPasswordListening = false
            }) {
                Icon(
                    painter = painterResource(id = if (isEmailListening) R.drawable.ic_stop_recording else R.drawable.ic_microphone),
                    contentDescription = if (isEmailListening) "Stop Recording" else "Start Recording",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            LoginTextField(
                label = "Username",
                text = username,
                isListening = isUsernameListening,
                trailing = "",
                isPassword = false,
                onTrailingClick = {},
                modifier = Modifier
                    .weight(1f)
                    .clickable { ttsHelper.speak("Username field") }
            )

            Spacer(modifier = Modifier.width(1.dp))

            IconButton(onClick = {
                ttsHelper.speak("Username")
                if (isUsernameListening) {
                    speechHelper.stopListening()
                } else {
                    speechHelper.startListening("Username")
                }
                isUsernameListening = !isUsernameListening
                isPasswordListening = false
                isEmailListening = false
            }) {
                Icon(
                    painter = painterResource(id = if (isUsernameListening) R.drawable.ic_stop_recording else R.drawable.ic_microphone),
                    contentDescription = if (isUsernameListening) "Stop Recording" else "Start Recording",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            LoginTextField(
                label = "Password",
                text = password,
                isListening = isPasswordListening,
                trailing = "",
                isPassword = true,
                onTrailingClick = {},
                modifier = Modifier
                    .weight(1f)
                    .clickable { ttsHelper.speak("Password field") }
            )

            Spacer(modifier = Modifier.width(1.dp))

            IconButton(onClick = {
                ttsHelper.speak("Password")
                if (isPasswordListening) {
                    speechHelper.stopListening()
                } else {
                    speechHelper.startListening("Password")
                }
                isPasswordListening = !isPasswordListening
                isEmailListening = false
                isUsernameListening = false
            }) {
                Icon(
                    painter = painterResource(id = if (isPasswordListening) R.drawable.ic_stop_recording else R.drawable.ic_microphone),
                    contentDescription = if (isPasswordListening) "Stop Recording" else "Start Recording",
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Error message display
        if (errorMessage.isNotEmpty()) {
            Text(
                text = errorMessage,
                color = Color.Red,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp)
            )
        }

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(38.dp)
                .clickable { ttsHelper.speak("Create Account Button") },
            onClick = { registerUser(email, password, username) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSystemInDarkTheme()) BlueGray else Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(size = 4.dp)
        ) {
            Text(
                text = "Create Account",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}


@Composable
private fun TopSection() {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Black

    Box(
        contentAlignment = Alignment.TopCenter
    ) {
        val shapeResource = if (isSystemInDarkTheme()) {
            R.drawable.shapenight
        } else {
            R.drawable.shape
        }
        Image(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(fraction = 0.46f),
            painter = painterResource(id = shapeResource),
            contentDescription = null,
            contentScale = ContentScale.FillBounds
        )

        Row(
            modifier = Modifier.padding(top = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            val logoResource = if (isSystemInDarkTheme()) {
                R.drawable.dark
            } else {
                R.drawable.light2
            }
            Image(
                modifier = Modifier.size(250.dp),
                painter = painterResource(id = logoResource),
                contentDescription = "Virtual Cane Logo"
            )
        }
        Text(
            modifier = Modifier
                .padding(bottom = 10.dp)
                .align(alignment = Alignment.BottomCenter),
            text = "Register",
            style = MaterialTheme.typography.headlineLarge,
            color = uiColor
        )
    }
}