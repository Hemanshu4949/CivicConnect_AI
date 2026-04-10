package com.example.civicconnectai.authentication

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
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
import com.example.civicconnectai.Models.CountryUtils
import com.example.civicconnectai.Models.CountryUtils.globalCountries
import com.example.civicconnectai.SupabaseManager
import com.example.civicconnectai.ui.theme.CivicConnectTheme
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.FirebaseDatabase
import io.github.jan.supabase.auth.OtpType
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.builtin.OTP
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


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

    // Loading State (to disable button while uploading)
    var isLoading by remember { mutableStateOf(false) }

    // --- FIREBASE INSTANCES ---
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()

    // The 3-Strike  for otp
    var failedAttempts by remember { mutableIntStateOf(0) }

// --- UPDATED: Country Code State ---
    var expandedCountryDropdown by remember { mutableStateOf(false) }
    // We now store the whole Country object instead of just a String!
    var selectedCountry by remember { mutableStateOf(CountryUtils.globalCountries[0]) }


    // SMS based OTP verification
    var otpCode by remember { mutableStateOf("") }

    // Tracks if we are showing the Phone input or OTP input
    var isOtpSent by remember { mutableStateOf(false) }

    // Holds the unique ID Firebase gives us when it sends the SMS
    var storedVerificationId by remember { mutableStateOf("") }
    val activity = context as? Activity

//    val callbacks = remember {
//        object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
//            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
//                // Auto-retrieval (Some Android phones instantly read the SMS and verify)
//                signInWithPhoneAuthCredential(auth, credential, onLoginClick) {
//                    isLoading = false
//                    Toast.makeText(context, "Error: ${it.message}", Toast.LENGTH_LONG).show()
//                }
//            }
//
//            override fun onVerificationFailed(e: FirebaseException) {
//                isLoading = false
//                Toast.makeText(context, "Verification Failed: ${e.message}", Toast.LENGTH_LONG)
//                    .show()
//                Log.d("errorotp" ,"Verification Failed: ${e.message}" )
//            }
//
//            override fun onCodeSent(
//                verificationId: String,
//                token: PhoneAuthProvider.ForceResendingToken
//            ) {
//                // The SMS was sent successfully! Save the ID and switch the UI to OTP mode.
//                isLoading = false
//                storedVerificationId = verificationId
//                isOtpSent = true
//                Toast.makeText(context, "OTP Sent!", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }


    // --- validation of the contact details ---
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // --- keybord controller ---
    val keyboardController = LocalSoftwareKeyboardController.current




    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background) // Uses the Light Gray from Theme
            .padding(horizontal = 24.dp).padding(0.dp, 50.dp, 0.dp, 0.dp),
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
                    Toast.makeText(
                        context,
                        "Please enter a valid email address.",
                        Toast.LENGTH_SHORT
                    ).show()
                } else if (password.isBlank()) {
                    Toast.makeText(context, "Please enter your password.", Toast.LENGTH_SHORT)
                        .show()
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
            onClick = {
                keyboardController?.hide()
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
                                    Log.d("userid", userId.toString());

                                    // 3. Save User Data to Database if needed
                                    val userref =
                                        database.getReference("users").child(userId.toString())
                                    if (userId != null) {
                                        val userData = User(
                                            name = user.displayName ?: "No Name",
                                            email = user.email ?: "",
                                            // Google doesn't provide phone usually
                                        )
                                        userref
                                            .get()
                                            .addOnSuccessListener { snapshot ->
                                                if (!snapshot.exists()) {
                                                    userref.setValue(userData)
                                                    isLoading = true
                                                    Log.d("userid", userId.toString() + "1");
                                                } else {
                                                    isLoading = false
                                                    Toast.makeText(
                                                        context,
                                                        "Google Sign-In Successful!",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                }
                                                val userId = auth.currentUser?.uid ?: ""

                                                //  CHECK DATABASE FOR EXISTING PHONE NUMBER
                                                database.getReference("users").child(userId)
                                                    .child("contactNumber")
                                                    .get()
                                                    .addOnSuccessListener { snapshot ->
                                                        var existingPhone: String? = null

                                                        if (snapshot.exists()) {
                                                            existingPhone =
                                                                snapshot.value as? String
                                                        }
                                                        if (existingPhone.isNullOrEmpty()) {
                                                            //  NO PHONE FOUND -> SHOW INPUT FIELD
                                                            isLoading = false
                                                            showPhoneInput = true
                                                        } else {
                                                            //  PHONE EXISTS -> GO TO HOME
                                                            isLoading = false
                                                            showPhoneInput = false
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
    if (showPhoneInput ) {
        if (!isOtpSent) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background) // Solid background covers the form
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = contactNumber,
                    onValueChange = {
                        if (it.length <= 10 && it.all { c -> c.isDigit() }) contactNumber = it
                    },
                    label = { Text("Phone Number") },
                    placeholder = { Text("+91 9876543210") }, // Remind them to use Country Code!
                    leadingIcon = { // --- THE COUNTRY CODE DROPDOWN ---
                        Box(
                            modifier = Modifier
                                .clickable { expandedCountryDropdown = true }
                                .padding(horizontal = 12.dp, vertical = 8.dp)
                        ) {
                            // What it looks like when CLOSED (e.g. "🇮🇳 +91")
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${selectedCountry.flag} ${selectedCountry.dialCode}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold
                                )
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Select Country Code",
                                    tint = Color.Gray
                                )
                            }

                            // What it looks like when OPEN (e.g. "🇮🇳 India (+91)")
                            DropdownMenu(
                                expanded = expandedCountryDropdown,
                                onDismissRequest = { expandedCountryDropdown = false },
                                // Add a max height so it scrolls if the list gets too long!
                                modifier = Modifier.heightIn(max = 300.dp).background(Color.White)
                            ) {
                                globalCountries.forEach { country ->
                                    DropdownMenuItem(
                                        text = {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Text(
                                                    text = country.flag,
                                                    modifier = Modifier.padding(end = 8.dp)
                                                )
                                                Text(
                                                    text = country.name,
                                                    modifier = Modifier.weight(1f)
                                                )
                                                Text(text = country.dialCode, color = Color.Gray)
                                            }
                                        },
                                        onClick = {
                                            selectedCountry = country
                                            expandedCountryDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Phone,
                        imeAction = ImeAction.Default// Show "Next" arrow instead of "Enter"
                    ),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.headlineSmall
                )
//            Text(
//                text = "One Last Step!",
//                style = MaterialTheme.typography.headlineMedium,
//                fontWeight = FontWeight.Bold
//            )
                Spacer(modifier = Modifier.height(8.dp))
                Text("Please enter your contact number to complete your profile.")

                Spacer(modifier = Modifier.height(24.dp))

                //--- check for the contact number valid format ---
                CheckContact(contactNumber, onResult = { error -> contactError = error })

//            SignUpTextField(
//                value = contactNumber,
//                onValueChange = { if (it.length <= 10 && it.all { c -> c.isDigit() }) contactNumber = it },
//                placeholder = "Contact Number",
//                icon = Icons.Default.Phone,
//                keyboardOptions = KeyboardOptions(
//                    keyboardType = KeyboardType.Phone,
//                    imeAction = ImeAction.Default// Show "Next" arrow instead of "Enter"
//                )
//            )
                if (contactError != null) {
                    Text(
                        text = contactError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier
                            .padding(start = 16.dp, top = 4.dp)
                            .align(Alignment.Start)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (contactNumber.isBlank() || activity == null) return@Button
                        isLoading = true

                        // clearing number format
                        val cleanNumber = contactNumber.trim().replace(" ", "")
                        // Combine the dropdown code with the typed number!
                        val fullPhoneNumber = "${selectedCountry.dialCode}$contactNumber"

//                    val options = PhoneAuthOptions.newBuilder(auth)
//                        .setPhoneNumber(fullPhoneNumber)
//                        .setTimeout(60L, TimeUnit.SECONDS)
//                        .setActivity(activity)
//                        .setCallbacks(callbacks)
//                        .build()
//                    PhoneAuthProvider.verifyPhoneNumber(options)

                        scope.launch {
                            try {
                                isLoading = true

                                // 1. SUPABASE: Send the OTP
                                SupabaseManager.client.auth.signInWith(OTP) {
                                    phone = fullPhoneNumber
                                }

                                isOtpSent = true
                                Toast.makeText(
                                    context,
                                    "OTP Sent via Supabase!",
                                    Toast.LENGTH_SHORT
                                ).show()

                            } catch (e: Exception) {
                                Toast.makeText(
                                    context,
                                    "Failed to send OTP: ${e.message}",
                                    Toast.LENGTH_LONG
                                ).show()
                                Log.d("otperror", e.message.toString())
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading && contactNumber.length == 10, // Ensure valid length
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Send OTP")
                    }
                }

//
//                // --- STATE 1: PHONE NUMBER INPUT ---
//                OutlinedTextField(
//                    value = contactNumber,
//                    onValueChange = { contactNumber = it },
//                    label = { Text("Phone Number") },
//                    placeholder = { Text("+91 9876543210") }, // Remind them to use Country Code!
//                    leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
//                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
//                    modifier = Modifier.fillMaxWidth(),
//                    singleLine = true
//                )

                Spacer(modifier = Modifier.height(24.dp))

                OutlinedButton(
                    onClick = {
                        Checkcontactandstore(
                            auth,
                            database,
                            context,
                            contactNumber, // You might want to pass fullPhoneNumber here instead!
                            onGoogleSignInClick,
                            onLoadingChange = { isLoading = it }
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Complete Profile (Skip OTP)", color = Color.Gray)
                }
            }
        } else {
            // --- STATE 2: OTP INPUT BLOCK ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Enter Verification Code",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "We sent a 6-digit code to ${selectedCountry.dialCode} $contactNumber",
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = otpCode,
                    onValueChange = {
                        if (it.length <= 6 && it.all { c -> c.isDigit() }) otpCode = it
                    },
                    label = { Text("6-Digit OTP") },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        if (otpCode.length != 6) return@Button

                        val cleanNumber = contactNumber.filter { it.isDigit() }
                        val fullPhoneNumber = "${selectedCountry.dialCode}$cleanNumber"

                        scope.launch {
                            try {
                                isLoading = true

                                // 1. SUPABASE: Verify the OTP
                                SupabaseManager.client.auth.verifyPhoneOtp(
                                    type = OtpType.Phone.SMS,
                                    phone = fullPhoneNumber,
                                    token = otpCode
                                )

                                // 2. SUCCESS -> FIREBASE: Store the number in Realtime Database
                                val firebaseUser = auth.currentUser
                                if (firebaseUser != null) {
                                    database.getReference("users")
                                        .child(firebaseUser.uid)
                                        .child("contactNumber")
                                        .setValue(fullPhoneNumber)
                                        .addOnSuccessListener {
                                            onLoginClick() // Navigate to Home!
                                        }
                                }

                            } catch (e: Exception) {
                                // 3. FAILURE -> Handle the Strikes and Firebase Deletion
                                failedAttempts++

                                if (failedAttempts >= 3) {
                                    // STRIKE 3: Wipe the database and Auth profile
                                    val firebaseUser = auth.currentUser
                                    if (firebaseUser != null) {
                                        // A. Delete the Realtime DB Node
                                        database.getReference("users").child(firebaseUser.uid)
                                            .removeValue()
                                        // B. Delete the Auth Profile so they aren't stuck in limbo
                                        firebaseUser.delete()
                                        // C. Sign out
                                        auth.signOut()
                                    }

                                    Toast.makeText(
                                        context,
                                        "Too many failed attempts. Profile creation aborted and data deleted.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                    onSignUpClick("default") // Navigate back to the start of the app

                                } else {
                                    Toast.makeText(
                                        context,
                                        "Invalid OTP. You have ${3 - failedAttempts} attempts remaining.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            } finally {
                                isLoading = false
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    enabled = !isLoading && otpCode.length == 6,
                    shape = RoundedCornerShape(12.dp)
                )
                {
                    if (isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color.White
                        )
                    } else {
                        Text("Verify & Login")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextButton(onClick = { isOtpSent = false }) {
                    Text("Edit Phone Number")
                }
            }
        }
    }
}
private fun signInWithPhoneAuthCredential(
    auth: FirebaseAuth,
    credential: PhoneAuthCredential,
    onSuccess: () -> Unit,
    onFailure: (Exception) -> Unit
) {
    auth.signInWithCredential(credential)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                onSuccess()
            } else {
                if (task.exception != null) {
                    onFailure(task.exception!!)
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

