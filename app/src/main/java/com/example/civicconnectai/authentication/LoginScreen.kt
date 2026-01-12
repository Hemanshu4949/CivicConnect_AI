package com.example.civicconnectai.authentication

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.rounded.Email
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.civicconnectai.ui.theme.CivicConnectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.launch


@Composable
fun LoginScreen(
    onLoginClick: () -> Unit,
    onSignUpClick: (String) -> Unit,
    onGoogleSignInClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
) {


    // --- current context ---
    val context = LocalContext.current

    // --- coroutine ---
    val scope = rememberCoroutineScope()

    // --- for contact number  ---
    var showPhoneInput by remember { mutableStateOf(false) }
    var contactNumber by remember { mutableStateOf("") } // for storing contact number
    var contactError by remember { mutableStateOf<String?>(null) } // contact number validation



    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    // Loading State (to disable button while uploading)
    var isLoading by remember { mutableStateOf(false) }

    // --- FIREBASE INSTANCES ---
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()

    // --- validation of the contact details ---
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- keybord controller ---
    val keyboardController = LocalSoftwareKeyboardController.current




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Uses the Light Gray from Theme
            .padding(horizontal = 24.dp).padding(0.dp , 50.dp ,0.dp ,0.dp ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // --- 1. Logo Section ---
        Surface(
            modifier = Modifier.size(80.dp),
            shape = RoundedCornerShape(20.dp),
            color = MaterialTheme.colorScheme.primary // Royal Blue
        ) {
            Box(contentAlignment = Alignment.Center) {
                // Using a built-in icon as a placeholder for the hand/heart logo
                Icon(
                    imageVector = Icons.Rounded.Email,
                    contentDescription = "App Logo",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 2. Headers ---
        Text(
            text = "Welcome Back",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Sign in to report and track civic issues \nin your community.",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(32.dp))

        // --- 3. Input Fields ---

        // Email Input
        CivicTextField(
            value = email,
            onValueChange = { email = it },
            label = "Email Address",
            placeholder = "name@example.com",
            icon = Icons.Default.Email,
            keyboardType = KeyboardType.Email
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Input
        CivicTextField(
            value = password,
            onValueChange = { password = it },
            label = "Password",
            placeholder = "••••••••",
            icon = Icons.Default.Lock,
            keyboardType = KeyboardType.Password,
            isPassword = true,
            passwordVisible = passwordVisible,
            onPasswordToggle = { passwordVisible = !passwordVisible }
        )



        // Forgot Password Link
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.CenterEnd
        ) {
            TextButton(onClick = onForgotPasswordClick) {
                Text(
                    text = "Forgot?",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- 4. Main Action Button ---
        Button(
            onClick = {
                // --- VALIDATION LOGIC ---
                keyboardController?.hide()
                if (email.isBlank()) {
                    Toast.makeText(context, "Please enter your email.", Toast.LENGTH_SHORT).show()
                } else if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    // Optional: Check for valid email format
                    Toast.makeText(context, "Please enter a valid email address.", Toast.LENGTH_SHORT).show()
                } else if (password.isBlank()) {
                    Toast.makeText(context, "Please enter your password.", Toast.LENGTH_SHORT).show()
                } else {
                    // --- FIREBASE LOGIC ---
                    isLoading = true
                    errorMessage = null // Clear previous errors

                    // 4. FIREBASE LOGIN CALL
                    auth.signInWithEmailAndPassword(email.trim(), password)
                        .addOnCompleteListener { task ->
                            // Stop loading regardless of outcome
                            isLoading = false

                            if (task.isSuccessful) {
                                // 5. SUCCESS: Navigate to Home
                                onLoginClick()
                            } else {
                                // 6. FAILURE: Handle specific error messages
                                val exception = task.exception
                                errorMessage = when {
                                    exception?.message?.contains("INVALID_LOGIN_CREDENTIALS") == true -> "Incorrect email or password."
                                    exception?.message?.contains("INVALID_EMAIL") == true -> "Invalid email format."
                                    else -> "Login failed: ${exception?.localizedMessage}"
                                }
                            }

                        }
                }
            },
            enabled = !isLoading,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary
            )
        )

        {

            Text(
                text = "Sign In",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White
            )


        }
        if (errorMessage != null) {
            Text(
                text = errorMessage!!,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 5. Social Login Divider ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            Text(
                text = "OR CONTINUE WITH",
                style = MaterialTheme.typography.labelMedium,
                color = Color.Gray,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- 6. Google Button ---
        OutlinedButton(
            onClick ={keyboardController?.hide()
                isLoading = true
                scope.launch {
                    // 1. Launch Google Flow
                    val credential = getGoogleLoginCredential(context)

                    if (credential != null) {
                        // 2. Sign in to Firebase with the Google Credential
                        auth.signInWithCredential(credential)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val user = auth.currentUser
                                    val userId = user?.uid

                                    // 3. Save User Data to Database if needed
                                    if (userId != null) {
                                        val userData = User(
                                            name = user.displayName ?: "No Name",
                                            email = user.email ?: "",
                                            contactNumber = "" // Google doesn't provide phone usually
                                        )
                                        database.getReference("users").child(userId)
                                            .setValue(userData)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Google Sign-In Successful!",
                                                    Toast.LENGTH_SHORT
                                                ).show()

                                                val userId = auth.currentUser?.uid ?: ""

                                                //  CHECK DATABASE FOR EXISTING PHONE NUMBER
                                                database.getReference("users").child(userId)
                                                    .get()
                                                    .addOnSuccessListener { snapshot ->
                                                        val existingPhone =
                                                            snapshot.child("contactNumber").value as? String

                                                        if (existingPhone.isNullOrEmpty()) {
                                                            //  NO PHONE FOUND -> SHOW INPUT FIELD
                                                            isLoading = false
                                                            showPhoneInput = true
                                                        } else {
                                                            //  PHONE EXISTS -> GO TO HOME
                                                            isLoading = false
                                                            onGoogleSignInClick()
                                                        }
                                                    }
                                                    .addOnFailureListener {
                                                        // Handle error (optional: let them pass or show error)
                                                        isLoading = false
                                                        showPhoneInput =
                                                            true // Fallback to asking
                                                    }

                                            }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Firebase Auth Failed: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                    } else {
                        isLoading = false // User cancelled or error
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(30.dp), // Pill shape as per design
            border = BorderStroke(1.dp, Color.LightGray),
            colors = ButtonDefaults.outlinedButtonColors(
                containerColor = Color.White
            )
        ) {
            // Note: You normally use an Image painter resource here for the Google G logo
//             Image(painter = painterResource( R.drawable.ic_google), contentDescription = null)

            // For this code snippet, I'll use a Text placeholder
            Text(
                text = "G  Sign in with Google",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Black,
                fontWeight = FontWeight.Medium
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- 7. Footer ---
        Row(
            modifier = Modifier.padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Don't have an account? ",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "Sign Up",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable { onSignUpClick("default") }
            )
        }
    }
    if (isLoading) {
        Box(Modifier.fillMaxSize(), Alignment.Center)
        {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
    if (showPhoneInput) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background) // Solid background covers the form
                .padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "One Last Step!",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Please enter your contact number to complete your profile.")

            Spacer(modifier = Modifier.height(24.dp))


            //--- check for the contact number valid format ---
            CheckContact(contactNumber , onResult = { error -> contactError = error })

            SignUpTextField(
                value = contactNumber,
                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) contactNumber = it },
                placeholder = "Contact Number",
                icon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Default// Show "Next" arrow instead of "Enter"
                )
            )
            if (contactError != null) {
                Text(
                    text = contactError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    Checkcontactandstore(auth , database , context , contactNumber , onGoogleSignInClick , onLoadingChange = {
                        isLoading = it
                    })

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Complete Profile"  , color = Color.Black)
            }
        }
    }

}

// --- Helper Composable for Text Fields to keep code clean ---
@Composable
fun CivicTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType = KeyboardType.Text,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {}
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(placeholder, color = Color.LightGray) },
            leadingIcon = {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.Gray
                )
            },
            trailingIcon = if (isPassword) {
                {
                    IconButton(onClick = onPasswordToggle) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "Toggle password visibility",
                            tint = Color.Gray
                        )
                    }
                }
            } else null,
            visualTransformation = if (isPassword && !passwordVisible) PasswordVisualTransformation() else VisualTransformation.None,
            keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                disabledContainerColor = Color.White,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f)
            ),
            singleLine = true
        )
    }
}

fun Checkcontactandstore(auth : FirebaseAuth, database : FirebaseDatabase, context : Context, contactNumber : String, onGoogleSignInClick : () -> Unit , onLoadingChange: (Boolean) -> Unit)
{
    // 2. Get the Current User ID safely
    val currentUser = auth.currentUser
    if (currentUser != null) {
        val userId = currentUser.uid

        // 3. Update ONLY the 'contactNumber' field for this user
        // Path: users -> {userId} -> contactNumber
        database.getReference("users").child(userId).child("contactNumber")
            .setValue(contactNumber)
            .addOnSuccessListener {
                // 4. Success! Now go to Home
                onLoadingChange(false)
                onGoogleSignInClick()
            }
            .addOnFailureListener { e ->
                onLoadingChange(false)
                Toast.makeText(
                    context,
                    "Failed to save number: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
    else {
        onLoadingChange(false)
        Toast.makeText(context, "User not found. Try signing in again.", Toast.LENGTH_SHORT).show()
    }
}

@Preview(showBackground = true)
@Composable
fun LoginScreenPreview() {
    CivicConnectTheme {
        LoginScreen({}, {}, {}, {} )
    }
}