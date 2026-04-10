package com.example.civicconnectai.authentication

import android.util.Log
import android.util.Patterns
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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.civicconnectai.ui.theme.CivicConnectTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


// 1. DATA MODEL: Defines what we store in the Database
data class User(
    val name: String = "",
    val email: String = "",
    val contactNumber: String = "",
    val password: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SignUpScreen(
    onRegisterClick: (String) -> Unit,
    onLoginClick: () -> Unit
) {
// --- current context ---
    val context = LocalContext.current
// --- coroutine ---
    val scope = rememberCoroutineScope()

    // --- for contact number  ---
    var showPhoneInput by remember { mutableStateOf(false) }

    // --- State Variables ---
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var contactNumber by remember { mutableStateOf("") }
    var contactError by remember { mutableStateOf<String?>(null) } // contact number validation

    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    // Loading State (to disable button while uploading)
    var isLoading by remember { mutableStateOf(false) }


//    val isFormValid = fullName.isNotBlank() &&
//            email.isNotBlank() &&
//            password.isNotBlank() &&
//            contactNumber.length == 10 &&
//            password == confirmPassword

//    val canRegister = isFormValid && !isLoading

    // --- keybord controller ---
    val keyboardController = LocalSoftwareKeyboardController.current

    // --- FIREBASE INSTANCES ---
    val auth = FirebaseAuth.getInstance()
    val database = FirebaseDatabase.getInstance()

    // Create a requester for each field you want to control
    val nameFocusRequester = remember { FocusRequester() }
    val emailFocusRequester = remember { FocusRequester() }
    val passwordFocusRequester = remember { FocusRequester() }
    val contactnumberFocusRequester = remember { FocusRequester() }
    val confirmpasswordFocusRequester = remember { FocusRequester() }


    // This makes the screen scrollable so keyboard doesn't hide buttons
    Box(modifier = Modifier.fillMaxSize()) {
        if (!showPhoneInput)
        {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFF8F9FA)) // Light Gray Background
                .padding(horizontal = 24.dp)
                .alpha(if (isLoading) 0.5f else 1f),
        ) {

            // --- 1. Back Button & Header ---
            // (Assuming you might want a back button like the design,
            // if not, you can remove this Row)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 22.dp, bottom = 32.dp),
                horizontalArrangement = Arrangement.Start
            ) {
                // Placeholder for back arrow if needed, otherwise just space
            }

            Text(
                text = "Create Account",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                ),
                color = Color.Black
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Join your community to report issues.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // --- 2. Input Fields ---

            // Full Name
            SignUpTextField(
                value = fullName,
                onValueChange = {input ->
                    // Allow letters, spaces, hyphens, and apostrophes
                    if (input.all { it.isLetter() || it.isWhitespace() || it == '-' || it == '\'' }) {
                        fullName = input
                    } },
                placeholder = "Full Name",
                icon = Icons.Default.Person,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    capitalization = KeyboardCapitalization.Words, // Capitalize every word (e.g. "John Doe")
                    imeAction = ImeAction.Next // Show "Next" arrow instead of "Enter"
                ),
                modifier = Modifier.focusRequester(nameFocusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Email Address
            SignUpTextField(
                value = email,
                onValueChange = { email = it },
                placeholder = "Email Address",
                icon = Icons.Default.Email,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Text,
                    imeAction = ImeAction.Next // Show "Next" arrow instead of "Enter"
                ),
                modifier = Modifier.focusRequester(emailFocusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- DEBOUNCE LOGIC (The 1-second check) ---

            CheckContact(contactNumber , onResult = { error -> contactError = error })

            // ---  Contact Number ---
            SignUpTextField(
                value = contactNumber,
                onValueChange = {
                    if (it.length <= 10 && it.all { c -> c.isDigit() }){
                        contactNumber = it
                        if (contactError != null) contactError = null
                    }
                },
                placeholder = "Contact Number",
                icon = Icons.Default.Phone,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Phone,
                    imeAction = ImeAction.Next // Show "Next" arrow instead of "Enter"
                ),
                modifier = Modifier.focusRequester(contactnumberFocusRequester)
            )
            if (contactError != null) {
                Text(
                    text = contactError!!,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Password
            SignUpTextField(
                value = password,
                onValueChange = { password = it },
                placeholder = "Password",
                icon = Icons.Default.Lock,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Next // Show "Next" arrow instead of "Enter"
                ),
                isPassword = true,
                passwordVisible = passwordVisible,
                onPasswordToggle = { passwordVisible = !passwordVisible },
                modifier = Modifier.focusRequester(passwordFocusRequester)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Confirm Password
            SignUpTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                placeholder = "Confirm Password",
                icon = Icons.Default.Refresh, // Using Refresh icon to distinguish
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Default // Show "Next" arrow instead of "Enter"
                ),
                isPassword = true,
                passwordVisible = confirmPasswordVisible,
                onPasswordToggle = { confirmPasswordVisible = !confirmPasswordVisible },
                modifier = Modifier.focusRequester(confirmpasswordFocusRequester)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // --- 3. Sign Up Button ---
            Button(
                onClick = {
                    // --- 1. VALIDATION LOGIC ---
                    keyboardController?.hide()
                    if (fullName.isBlank()) {
                        Toast.makeText(context, "Please fill in all fields", Toast.LENGTH_SHORT)
                            .show()
                    } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches() || email.isBlank()) {
                        Toast.makeText(context, "Invalid email format", Toast.LENGTH_SHORT).show()
                        emailFocusRequester.requestFocus()
                    }
                    else if (contactNumber.length != 10 || contactNumber.isBlank()) { // <--- CONTACT VALIDATION
                        Toast.makeText(
                            context,
                            "Please enter a valid 10-digit contact number",
                            Toast.LENGTH_SHORT
                        ).show()
                        contactnumberFocusRequester.requestFocus()
                    }
                    else if (password.length < 1 || password.isBlank()) {
                        Toast.makeText(
                            context,
                            "Password must be at least 6 characters",
                            Toast.LENGTH_SHORT
                        ).show()
                        passwordFocusRequester.requestFocus()
                    } else if (password != confirmPassword || confirmPassword.isBlank()) {
                        Toast.makeText(context, "Passwords do not match or is empty", Toast.LENGTH_SHORT).show()
                        confirmpasswordFocusRequester.requestFocus()
                    } else {
                        // --- 2. FIREBASE LOGIC ---
                        isLoading = true

                        auth.createUserWithEmailAndPassword(email, password)
                            .addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val userId = auth.currentUser?.uid

                                    if (userId != null) {
                                        // Save Name, Email AND Contact Number
                                        val user = User(fullName, email, contactNumber, password)

                                        database.getReference("users").child(userId).setValue(user)
                                            .addOnSuccessListener {
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Account Created Successfully!",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                onRegisterClick("registration") // Navigate to Login Screen

                                            }
                                            .addOnFailureListener { e ->
                                                isLoading = false
                                                Toast.makeText(
                                                    context,
                                                    "Failed to save data: ${e.message}",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                } else {
                                    isLoading = false
                                    Toast.makeText(
                                        context,
                                        "Sign Up Failed: ${task.exception?.message}",
                                        Toast.LENGTH_SHORT
                                    ).show()
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
                    containerColor = Color(0xFF5B75E6) // Royal Blue from design
                )
            ) {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 4. OR Divider ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
                Text(
                    text = "OR CONTINUE WITH",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color.LightGray)
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- 5. Google Button ---
            OutlinedButton(
                onClick = {
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
                                        Log.d("userid" , userId.toString());

                                        // 3. Save User Data to Database if needed
                                        val userref = database.getReference("users").child(userId.toString())
                                        if (userId != null) {
                                            val userData = User(
                                                name = user.displayName ?: "No Name",
                                                email = user.email ?: "",
                                                // Google doesn't provide phone usually
                                            )
                                            userref
                                                .get()
                                                .addOnSuccessListener {snapshot->
                                                    if(!snapshot.exists())
                                                    {
                                                        userref.setValue(userData)
                                                        isLoading = true
                                                        Log.d("userid" , userId.toString()+"1");
                                                    }
                                                    else
                                                    {
                                                        isLoading = false
                                                        Toast.makeText(
                                                            context,
                                                            "Google Sign-In Successful!",
                                                            Toast.LENGTH_SHORT
                                                        ).show()
                                                    }
                                                    val userId = auth.currentUser?.uid ?: ""

                                                    //  CHECK DATABASE FOR EXISTING PHONE NUMBER
                                                    database.getReference("users").child(userId).child("contactNumber")
                                                        .get()
                                                        .addOnSuccessListener { snapshot ->
                                                            var existingPhone: String? = null

                                                            if(snapshot.exists())
                                                            {
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
                                                                    onRegisterClick("SignInWithGoogle")
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
                shape = RoundedCornerShape(30.dp),
                border = BorderStroke(1.dp, Color.LightGray.copy(alpha = 0.5f)),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = Color.White
                )
            ) {
                // Using a colored Text 'G' to mimic the logo since we don't have the asset
                Text(
                    text = "G ",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFDB4437) // Google Red-ish color
                )
                Text(
                    text = " Google",
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- 6. Footer Link ---
            Row(
                modifier = Modifier.padding(top = 90.dp).fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Already have an account? ",
                    color = Color.Gray
                )
                Text(
                    text = "Login",
                    color = Color(0xFF5B75E6), // Royal Blue
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable { onLoginClick() }
                )
            }
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
    }
    // 3. INSERT THE PHONE INPUT UI HERE (The overlay)
    // This sits "on top" of the box stack
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
                                isLoading = false
                                showPhoneInput = true ;
                                onRegisterClick("SignInWithGoogle")
                            }
                            .addOnFailureListener { e ->
                                isLoading = false
                                Toast.makeText(
                                    context,
                                    "Failed to save number: ${e.message}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                    else {
                        isLoading = false
                        Toast.makeText(context, "User not found. Try signing in again.", Toast.LENGTH_SHORT).show()
                    }

                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Complete Profile"  , color = Color.Black)
            }
        }
    }
}


@Composable
public fun CheckContact(
    contactNumber: String,
    onResult: (String?) -> Unit // Pass a function to update the error
) {
    LaunchedEffect(contactNumber) {
        // 1. If empty, clear error immediately
        if (contactNumber.isEmpty()) {
            onResult(null)
            return@LaunchedEffect
        }

        // 2. Wait for 1 second (Debounce)
        // If 'contactNumber' changes before this finishes, this block restarts.
        delay(1000)

        // 3. Perform Validation
        val error = if (contactNumber.length != 10 || !contactNumber.all { it.isDigit() }) {
            "Contact number must be exactly 10 digits"
        } else {
            null // Valid
        }

        // 4. Update the parent state
        onResult(error)
    }
}


// --- Helper Component to style the text fields exactly like the design ---
@Composable
public fun SignUpTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    isPassword: Boolean = false,
    passwordVisible: Boolean = false,
    onPasswordToggle: () -> Unit = {} ,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default ,
    modifier: Modifier = Modifier
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier.fillMaxWidth().then(modifier),
        placeholder = {
            Text(text = placeholder, color = Color.Gray)
        },
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
        keyboardOptions = keyboardOptions,
        shape = RoundedCornerShape(12.dp), // Rounded corners like the image
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = Color.White,
            unfocusedContainerColor = Color.White,
            disabledContainerColor = Color.White,
            focusedBorderColor = Color(0xFF5B75E6), // Blue when clicked
            unfocusedBorderColor = Color.LightGray.copy(alpha = 0.5f), // Light gray when idle
            cursorColor = Color(0xFF5B75E6)
        ),
        singleLine = true
    )
}

@Preview(showBackground = true)
@Composable
fun SignUpScreenPreview() {
    CivicConnectTheme {
        SignUpScreen({""}, {})
    }
}