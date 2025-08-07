package com.example.gpsapp.ui.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gpsapp.R
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ForgotPasswordScreen(navController: NavController) {
    val context = LocalContext.current
    var email by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFEFEFEF)) // light grey background
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(Color.White)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.thinlogo),
                contentDescription = "Logo",
                modifier = Modifier
                    .size(80.dp)
                    .padding(bottom = 16.dp)
            )

            Text(
                text = "Reset Password",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(20.dp))

            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email or Username") },
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color(0xFFF5F5F5),
                    unfocusedContainerColor = Color(0xFFF5F5F5),
                    disabledContainerColor = Color(0xFFF5F5F5),
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent
                ),
                        keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    if (email.trim().isEmpty()) {
                        Toast.makeText(context, "Enter your email or username", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    isLoading = true
                    coroutineScope.launch {
                        delay(2000) // simulate backend call
                        isLoading = false
                        Toast.makeText(context, "Password reset link sent!", Toast.LENGTH_LONG).show()
                        navController.popBackStack()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                enabled = !isLoading,
                shape = RoundedCornerShape(10.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text("Send Reset Link")
                }
            }

            TextButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.padding(top = 12.dp)
            ) {
                Text("Back to Login")
            }
        }
    }
}
