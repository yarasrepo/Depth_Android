package com.example.virtualcane3

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.virtualcane3.ui.theme.Black
import com.example.virtualcane3.ui.theme.focusedTextFieldText
import com.example.virtualcane3.ui.theme.textFieldContainer
import com.example.virtualcane3.ui.theme.unfocusedTextFieldText

@OptIn(ExperimentalMaterial3Api::class)
/*@Composable
fun LoginTextField(modifier: Modifier = Modifier, label: String, trailing: String, isPassword: Boolean = false, onTrailingClick: () -> Unit){
    val uiColor = if (isSystemInDarkTheme()) Color.White else Black
    var textValue by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        modifier = modifier,
        value = textValue,
        onValueChange = {newValue -> textValue = newValue},
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = uiColor
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        colors = TextFieldDefaults.colors(
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.unfocusedTextFieldText,
            focusedPlaceholderColor = MaterialTheme.colorScheme.focusedTextFieldText,
            unfocusedContainerColor = MaterialTheme.colorScheme.textFieldContainer,
            focusedContainerColor = MaterialTheme.colorScheme.textFieldContainer
        ),
        visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            TextButton(onClick = onTrailingClick) {
                Text(text = trailing,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = uiColor)
            }
        }
        )
} */

@Composable
fun LoginTextField(
    modifier: Modifier = Modifier,
    label: String,
    text: String,
    isPassword: Boolean = false,
    isListening: Boolean,
    trailing: String,
    onTrailingClick: () -> Unit
) {
    val uiColor = if (isSystemInDarkTheme()) Color.White else Black
    var textValue by remember { mutableStateOf(text) }

    TextField(
        modifier = modifier,
        value = textValue,
        onValueChange = { newValue -> textValue = newValue },
        label = {
            Text(
                text = label,
                fontSize = 15.sp ,
                color = uiColor
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
        colors = TextFieldDefaults.colors(
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.unfocusedTextFieldText,
            focusedPlaceholderColor = MaterialTheme.colorScheme.focusedTextFieldText,
            unfocusedContainerColor = MaterialTheme.colorScheme.textFieldContainer,
            focusedContainerColor = MaterialTheme.colorScheme.textFieldContainer
        ),
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        trailingIcon = {
            TextButton(onClick = onTrailingClick) {
                Text(text = trailing,
                    fontSize = 10.sp,
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = uiColor)
            }
        }
    )
}
