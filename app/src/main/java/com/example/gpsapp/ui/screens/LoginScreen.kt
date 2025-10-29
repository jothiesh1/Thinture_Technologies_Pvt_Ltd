package com.example.gpsapp.ui.screens

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import android.widget.Toast
import com.example.gpsapp.R
import com.example.gpsapp.data.local.UserPreferences
import com.example.gpsapp.data.model.LoginRequest
import com.example.gpsapp.network.RetrofitClient
import com.example.gpsapp.ui.navigation.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var selectedRole by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var passwordVisible by remember { mutableStateOf(false) }
    var rememberMe by remember { mutableStateOf(false) }
    var isAutoLoggingIn by remember { mutableStateOf(false) }

    // Field validation errors
    var usernameError by remember { mutableStateOf(false) }
    var passwordError by remember { mutableStateOf(false) }
    var roleError by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val sharedPrefs = remember {
        context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
    }

    val roles = listOf("Super Admin", "Admin", "Dealer", "Client", "User")
    val coroutineScope = rememberCoroutineScope()
    val passwordFocusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current

    // Location Permission Launcher
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineLocationGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] ?: false
        val coarseLocationGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] ?: false

        if (!fineLocationGranted && !coarseLocationGranted) {
            Toast.makeText(
                context,
                "Location permission denied. Some features may not work properly.",
                Toast.LENGTH_LONG
            ).show()
        } else {
            Toast.makeText(
                context,
                "Location permission granted!",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    // Request Location Permissions on First Launch
    LaunchedEffect(Unit) {
        val fineLocationPermission = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        )

        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            locationPermissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    // Auto-login check on screen launch
    LaunchedEffect(Unit) {
        // Load saved credentials
        val (savedUsername, savedPassword, savedRememberMe) = userPrefs.getLogin()
        val savedRole = sharedPrefs.getString("user_role", null)

        if (savedRememberMe &&
            !savedUsername.isNullOrEmpty() &&
            !savedPassword.isNullOrEmpty() &&
            !savedRole.isNullOrEmpty()) {

            // Auto-login
            username = savedUsername
            password = savedPassword
            rememberMe = true
            isAutoLoggingIn = true
            isLoading = true

            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(username.trim(), password.trim())
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    val role = response.body()?.role?.trim()?.lowercase()
                    navigateToRoleDashboard(navController, role)
                } else {
                    // Auto-login failed - clear everything
                    userPrefs.clearLogin()
                    sharedPrefs.edit().clear().apply()
                    isAutoLoggingIn = false
                    isLoading = false
                    errorMessage = "Session expired. Please login again."
                    username = ""
                    password = ""
                    rememberMe = false
                }
            } catch (e: Exception) {
                // Network error - clear everything
                userPrefs.clearLogin()
                sharedPrefs.edit().clear().apply()
                isAutoLoggingIn = false
                isLoading = false
                errorMessage = "Unable to auto-login. Please login manually."
                username = ""
                password = ""
                rememberMe = false
            }
        } else if (savedRememberMe && !savedUsername.isNullOrEmpty() && !savedPassword.isNullOrEmpty()) {
            // Load credentials but don't auto-login (missing role)
            username = savedUsername
            password = savedPassword
            rememberMe = true
        }
    }

    fun validateFields(): Boolean {
        usernameError = username.trim().isEmpty()
        passwordError = password.trim().isEmpty()
        roleError = selectedRole.isEmpty()

        return !usernameError && !passwordError && !roleError
    }

    fun handleLogin() {
        // Clear previous errors
        errorMessage = null

        // Validate all fields
        if (!validateFields()) {
            errorMessage = "Please fill in all required fields"
            return
        }

        focusManager.clearFocus()
        isLoading = true

        coroutineScope.launch {
            try {
                val response = RetrofitClient.apiService.login(
                    LoginRequest(username.trim(), password.trim())
                )
                if (response.isSuccessful && response.body()?.success == true) {
                    errorMessage = null

                    val role = response.body()?.role?.trim()?.lowercase()

                    if (rememberMe) {
                        userPrefs.saveLogin(username.trim(), password.trim(), true)
                        sharedPrefs.edit().putString("user_role", role ?: "").apply()
                    } else {
                        userPrefs.clearLogin()
                        sharedPrefs.edit().clear().apply()
                    }

                    navigateToRoleDashboard(navController, role)
                } else {
                    errorMessage = response.body()?.message ?: "Login failed"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    // Show loading screen during auto-login
    if (isAutoLoggingIn) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1A237E),
                            Color(0xFF0D47A1)
                        )
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator(
                    color = Color.White,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(48.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Signing you in...",
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
        return
    }

    // Main login UI
    Box(modifier = Modifier.fillMaxSize()) {
        // Background with blur
        Image(
            painter = painterResource(id = R.drawable.imagelogin),
            contentDescription = "Background",
            modifier = Modifier
                .fillMaxSize()
                .blur(2.dp),
            contentScale = ContentScale.Crop
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.3f),
                            Color.Black.copy(alpha = 0.5f)
                        )
                    )
                )
        )

        // Login Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .align(Alignment.Center),
            shape = RoundedCornerShape(28.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.95f)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp)
            ) {
                // Logo
                Image(
                    painter = painterResource(id = R.drawable.thinlogo),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .height(90.dp)
                        .padding(bottom = 16.dp)
                )

                // Sign in text
                Text(
                    text = "Sign in to continue",
                    fontSize = 16.sp,
                    color = Color(0xFF616161),
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // Role Dropdown (REQUIRED)
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedRole,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Role *", fontSize = 14.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color(0xFFF5F5F5),
                            unfocusedContainerColor = Color(0xFFF5F5F5),
                            disabledContainerColor = Color.LightGray,
                            focusedIndicatorColor = if (roleError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                            unfocusedIndicatorColor = if (roleError) Color(0xFFD32F2F) else Color(0xFFE0E0E0),
                            focusedLabelColor = if (roleError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                            unfocusedLabelColor = if (roleError) Color(0xFFD32F2F) else Color(0xFF757575)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        isError = roleError
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
                                    roleError = false
                                    expanded = false
                                }
                            )
                        }
                    }
                }
                if (roleError) {
                    Text(
                        text = "Please select a role",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 0.dp)
                    )
                }

                // Username TextField (REQUIRED)
                TextField(
                    value = username,
                    onValueChange = {
                        username = it
                        usernameError = false
                    },
                    label = { Text("Username *", fontSize = 14.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = if (usernameError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                        unfocusedIndicatorColor = if (usernameError) Color(0xFFD32F2F) else Color(0xFFE0E0E0),
                        focusedLabelColor = if (usernameError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                        unfocusedLabelColor = if (usernameError) Color(0xFFD32F2F) else Color(0xFF757575)
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(
                        onNext = { passwordFocusRequester.requestFocus() }
                    ),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    isError = usernameError
                )
                if (usernameError) {
                    Text(
                        text = "Username is required",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 0.dp)
                    )
                }

                // Password TextField (REQUIRED)
                TextField(
                    value = password,
                    onValueChange = {
                        password = it
                        passwordError = false
                    },
                    label = { Text("Password *", fontSize = 14.sp) },
                    visualTransformation = if (passwordVisible)
                        VisualTransformation.None else PasswordVisualTransformation(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(passwordFocusRequester),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF5F5F5),
                        unfocusedContainerColor = Color(0xFFF5F5F5),
                        focusedIndicatorColor = if (passwordError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                        unfocusedIndicatorColor = if (passwordError) Color(0xFFD32F2F) else Color(0xFFE0E0E0),
                        focusedLabelColor = if (passwordError) Color(0xFFD32F2F) else Color(0xFF1976D2),
                        unfocusedLabelColor = if (passwordError) Color(0xFFD32F2F) else Color(0xFF757575)
                    ),
                    keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { handleLogin() }),
                    singleLine = true,
                    trailingIcon = {
                        val icon = if (passwordVisible)
                            Icons.Filled.VisibilityOff else Icons.Filled.Visibility
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = icon,
                                contentDescription = if (passwordVisible)
                                    "Hide password" else "Show password",
                                tint = Color(0xFF757575)
                            )
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    isError = passwordError
                )
                if (passwordError) {
                    Text(
                        text = "Password is required",
                        color = Color(0xFFD32F2F),
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(start = 16.dp, top = 0.dp)
                    )
                }

                // Error message
                if (errorMessage != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFEBEE)
                        ),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = errorMessage!!,
                            color = Color(0xFFC62828),
                            modifier = Modifier.padding(12.dp),
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Remember Me & Forgot Password Row
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF1976D2),
                                uncheckedColor = Color(0xFF757575)
                            )
                        )
                        Text(
                            text = "Remember Me",
                            fontSize = 14.sp,
                            color = Color(0xFF424242)
                        )
                    }

                    TextButton(
                        onClick = { navController.navigate(Screen.ForgotPassword.route) }
                    ) {
                        Text(
                            "Forgot Password?",
                            fontSize = 14.sp,
                            color = Color(0xFF1976D2),
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Login Button
                Button(
                    onClick = { handleLogin() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF1976D2),
                        contentColor = Color.White,
                        disabledContainerColor = Color(0xFFBBDEFB),
                        disabledContentColor = Color.White
                    ),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    elevation = ButtonDefaults.buttonElevation(
                        defaultElevation = 4.dp,
                        pressedElevation = 8.dp
                    )
                ) {
                    if (isLoading) {
                        Row(
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                color = Color.White,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(
                                "Signing In...",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    } else {
                        Text(
                            "Sign In",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

private fun navigateToRoleDashboard(navController: NavController, role: String?) {
    when (role) {
        "superadmin" -> navController.navigate(Screen.Dashboard.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
            launchSingleTop = true
        }
        "admin" -> navController.navigate(Screen.AdminDashboard.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
            launchSingleTop = true
        }
        "dealer" -> navController.navigate(Screen.DealerDashboard.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
            launchSingleTop = true
        }
        "client" -> navController.navigate(Screen.ClientDashboard.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
            launchSingleTop = true
        }
        "user" -> navController.navigate(Screen.UserDashboard.route) {
            popUpTo(Screen.Login.route) { inclusive = true }
            launchSingleTop = true
        }
    }
}