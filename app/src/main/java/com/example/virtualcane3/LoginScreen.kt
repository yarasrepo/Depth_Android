package com.example.virtualcane3


import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.virtualcane3.ui.theme.Black
import com.example.virtualcane3.ui.theme.BlueGray
import com.example.virtualcane3.ui.theme.focusedTextFieldText
import com.example.virtualcane3.ui.theme.textFieldContainer
import com.example.virtualcane3.ui.theme.unfocusedTextFieldText
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.GoogleAuthProvider


@Composable
fun LoginScreen(navController: NavController) {
    val context = LocalContext.current
    val ttsHelper = remember { TextToSpeechHelper(context) }
    DisposableEffect(Unit) {
        onDispose {
            ttsHelper.shutdown()
        }
    }

}

@Composable
private fun CreateAccount(navController: NavController, ttsHelper: TextToSpeechHelper) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Color.Black

    Box(
        modifier = Modifier
            .fillMaxHeight(fraction = 0.8f)
            .fillMaxWidth(),
        contentAlignment = Alignment.BottomCenter
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Don't have an account?",
                style = TextStyle(
                    color = uiColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Normal
                )
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "Create now",
                style = TextStyle(
                    color = uiColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                ),
                modifier = Modifier
                    .clickable {
                        ttsHelper.speak("Create an account")
                        navController.navigate("register")
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
                ttsHelper.speak("Sign in with Google")

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
private fun LoginSection(navController: NavController) {
    var showForgotPasswordDialog by remember { mutableStateOf(false)}
    LoginTextField(
        label = "Email",
        trailing = "",
        modifier = Modifier.fillMaxWidth(),
        onTrailingClick = {showForgotPasswordDialog = false}
    )
    Spacer(modifier = Modifier.height(15.dp))
    LoginTextField(
        label = "Password",
        trailing = "Forgot?",
        modifier = Modifier.fillMaxWidth(),
        isPassword = true,
        onTrailingClick = {showForgotPasswordDialog = true}
    )

    if (showForgotPasswordDialog){
        ForgotPasswordDialog(onDismiss = { showForgotPasswordDialog = false})
    }
    Spacer(modifier = Modifier.height(20.dp))
    Button(
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp),
        onClick = {navController.navigate("home") },
        colors = ButtonDefaults.buttonColors(
            containerColor = if (isSystemInDarkTheme()) BlueGray else Black,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(size = 4.dp)
    ) {
        Text(
            text = "Log in",
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
        )
    }
}*/

@Composable
private fun LoginSection(navController: NavController, ttsHelper: TextToSpeechHelper){
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    //var isListening by remember { mutableStateOf(false) }
    var isPasswordVisible by remember { mutableStateOf(false)}
    var showForgotPasswordDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    var isEmailListening by remember { mutableStateOf(false) }
    var isPasswordListening by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()

    fun loginUser(email: String, password: String) {
        val auth = FirebaseAuth.getInstance()
        if (email.isNotEmpty() && password.isNotEmpty()) {
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        // Login successful, navigate to home or other screen
                        navController.navigate("home")
                    } else {
                        // Handle login failure
                        val exception = task.exception
                        val errorMessage = when (exception){
                            is FirebaseAuthInvalidCredentialsException -> "Invalid Credentials"
                            else -> "Login failed: ${exception?.message}"
                        }
                        // Show error message as a Toast
                        Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            // Show an error message if fields are empty
            Toast.makeText(context, "Please enter both email and password", Toast.LENGTH_SHORT).show()
        }
    }


    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            LoginTextField(
                label = "Email",
                text = email,
                trailing = "",
                onTrailingClick = { },
                modifier = Modifier
                    .weight(1f)
                    .clickable { ttsHelper.speak("Email field") }
            )

            Spacer(modifier = Modifier.width(1.dp))

        }

        Spacer(modifier = Modifier.height(15.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            LoginTextField(
                label = "Password",
                text = password,
                isPassword = !isPasswordVisible,
                trailing = "Forgot?",
                onTrailingClick = { showForgotPasswordDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .clickable { ttsHelper.speak("Password field") }
            )
            if (showForgotPasswordDialog){
                ForgotPasswordDialog(
                    onDismiss = { showForgotPasswordDialog = false },
                    onEmailSent = { showForgotPasswordDialog = false }
                )
            }

        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { ttsHelper.speak("Login Button") }
                .height(40.dp),
            onClick = { loginUser(email, password) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isSystemInDarkTheme()) BlueGray else Black,
                contentColor = Color.White
            ),
            shape = RoundedCornerShape(size = 4.dp)
        ) {
            Text(
                text = "Log In",
                fontSize = 12.sp,
                style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold)
            )
        }
    }
}


@Composable
fun ForgotPasswordDialog(
    onDismiss: () -> Unit,
    onEmailSent: () -> Unit
) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var showError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Reset Password") },
        text = {
            Column {
                TextField(
                    value = email,
                    onValueChange = {
                        email = it
                        showError = false
                    },
                    label = { Text("Enter your email") },
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        unfocusedPlaceholderColor = MaterialTheme.colorScheme.unfocusedTextFieldText,
                        focusedPlaceholderColor = MaterialTheme.colorScheme.focusedTextFieldText,
                        unfocusedContainerColor = MaterialTheme.colorScheme.textFieldContainer,
                        focusedContainerColor = MaterialTheme.colorScheme.textFieldContainer
                    )
                )
                if (showError) {
                    Text(text = errorMessage, color = Color.Red)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (email.isNotBlank()) {
                    FirebaseAuth.getInstance().sendPasswordResetEmail(email)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Toast.makeText(context, "Reset email sent!", Toast.LENGTH_SHORT).show()
                                onEmailSent()
                            } else {
                                showError = true
                                errorMessage = task.exception?.message ?: "Error sending email."
                            }
                        }
                } else {
                    showError = true
                    errorMessage = "Email cannot be empty"
                }
            }) {
                Text("Send")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
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
            text = stringResource(id = R.string.login),
            style = MaterialTheme.typography.headlineLarge,
            color = uiColor
        )
    }
}

