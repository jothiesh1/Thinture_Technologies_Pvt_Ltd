package com.example.thinturetechnologiespvtltd.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.thinturetechnologiespvtltd.R
import com.example.thinturetechnologiespvtltd.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf("Select Role") }
    var expanded by remember { mutableStateOf(false) }

    val roles = listOf("Super Admin", "Admin", "Dealer", "Client", "User")

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.imagelogin),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp)
                .height(540.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFFF9F9F9)) // Neutral off-white background
                .padding(24.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceEvenly,
                modifier = Modifier.fillMaxSize()
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.thinlogo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(70.dp)
                        .padding(bottom = 12.dp)
                )

                // Role Dropdown
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Role") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.White,
                            unfocusedContainerColor = Color(0xFFEFEFEF), // light grey
                            disabledContainerColor = Color.LightGray,
                            focusedIndicatorColor = Color.Gray,
                            unfocusedIndicatorColor = Color.LightGray
                        )
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        roles.forEach { role ->
                            DropdownMenuItem(
                                text = { Text(role) },
                                onClick = {
                                    selectedRole = role
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Username
                TextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Username") },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFEFEFEF),
                        focusedIndicatorColor = Color.Gray,
                        unfocusedIndicatorColor = Color.LightGray
                    )
                )

                // Password
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.White,
                        unfocusedContainerColor = Color(0xFFEFEFEF),
                        focusedIndicatorColor = Color.Gray,
                        unfocusedIndicatorColor = Color.LightGray
                    )
                )

                if (errorMessage != null) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                // Login Button
                Button(
                    onClick = {
                        if (username == "abc" && password == "111") {
                            errorMessage = null
                            navController.navigate(Screen.Dashboard.route)
                        } else {
                            errorMessage = "Invalid credentials"
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .padding(top = 12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.DarkGray,
                        contentColor = Color.White
                    )
                ) {
                    Text("Log In")
                }
            }
        }
    }
}


