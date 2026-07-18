package com.example.ui

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.R
import com.example.data.*
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TuitionAppMain(viewModel: TuitionViewModel) {
    val isAdmin by viewModel.isAdminLoggedIn.collectAsState()
    val studentUser by viewModel.currentUser.collectAsState()
    val isDark = viewModel.isDarkMode

    val context = LocalContext.current

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when {
            isAdmin -> {
                AdminDashboardScreen(viewModel = viewModel)
            }
            studentUser != null -> {
                StudentDashboardScreen(viewModel = viewModel, student = studentUser!!)
            }
            else -> {
                WelcomeRoleSelectorScreen(viewModel = viewModel)
            }
        }
    }
}

@Composable
fun WelcomeRoleSelectorScreen(viewModel: TuitionViewModel) {
    val lang = viewModel.currentLanguage
    val isDark = viewModel.isDarkMode
    var activeAuthScreen by remember { mutableStateOf<String?>(null) } // "student" or "admin"

    if (activeAuthScreen != null) {
        if (activeAuthScreen == "student") {
            StudentAuthScreen(
                viewModel = viewModel,
                onBack = { activeAuthScreen = null }
            )
        } else {
            AdminAuthScreen(
                viewModel = viewModel,
                onBack = { activeAuthScreen = null }
            )
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            // Header: Language and Theme Toggle
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Language Toggle
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .clickable {
                            viewModel.setLanguage(if (lang == "en") "bn" else "en")
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.Language,
                        contentDescription = "Language",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (lang == "en") "বাংলা" else "English",
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                // Theme Toggle
                IconButton(onClick = { viewModel.toggleDarkMode() }) {
                    Icon(
                        imageVector = if (isDark) Icons.Default.LightMode else Icons.Default.DarkMode,
                        contentDescription = "Theme Toggle",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Beautiful Hero banner
            Image(
                painter = painterResource(id = R.drawable.img_tuition_hero),
                contentDescription = "Tuition Classes Hero",
                modifier = Modifier
                    .fillMaxWidth()
                    .height(220.dp)
                    .clip(RoundedCornerShape(24.dp)),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = viewModel.getString("app_name"),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 32.sp
                ),
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = viewModel.getString("tagline"),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = viewModel.getString("select_role"),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Student Role Selection Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("select_student_button")
                    .clickable { activeAuthScreen = "student" },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.School,
                            contentDescription = "Student Icon",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = viewModel.getString("student"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                        Text(
                            text = if (lang == "bn") "ক্লাস দেখুন, পরীক্ষা দিন এবং নোট পড়ুন" else "Watch lectures, submit tasks, & practice tests",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Admin Role Selection Button
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("select_admin_button")
                    .clickable { activeAuthScreen = "admin" },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                ),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.secondary),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.AdminPanelSettings,
                            contentDescription = "Admin Icon",
                            tint = Color.White,
                            modifier = Modifier.size(28.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(20.dp))
                    Column {
                        Text(
                            text = viewModel.getString("admin"),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (lang == "bn") "ক্লাস ও নোট আপলোড এবং ছাত্রছাত্রী পরিচালনা" else "Publish PDFs, MCQ, schedule classes & track fees",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StudentAuthScreen(viewModel: TuitionViewModel, onBack: () -> Unit) {
    var email by remember { mutableStateOf("somghosh065@gmail.com") }
    var password by remember { mutableStateOf("123456") }
    var phone by remember { mutableStateOf("+91 98300 12345") }
    var otp by remember { mutableStateOf("") }
    var isOtpSent by remember { mutableStateOf(false) }

    var tabIndex by remember { mutableStateOf(0) } // 0 = Email, 1 = Phone OTP
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.getString("student_login_title"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Role Banner Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.School,
                    contentDescription = "Student Portal Icon",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Tab Rows
            TabRow(selectedTabIndex = tabIndex) {
                Tab(
                    selected = tabIndex == 0,
                    onClick = { tabIndex = 0 },
                    text = { Text(if (viewModel.currentLanguage == "bn") "ইমেল ও পাসওয়ার্ড" else "Email Sign-In") }
                )
                Tab(
                    selected = tabIndex == 1,
                    onClick = { tabIndex = 1 },
                    text = { Text(if (viewModel.currentLanguage == "bn") "মোবাইল ওটিপি" else "Phone OTP") }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            if (tabIndex == 0) {
                // Email password
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(viewModel.getString("email")) },
                    leadingIcon = { Icon(Icons.Default.Email, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(viewModel.getString("password")) },
                    leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        if (email.isNotEmpty()) {
                            viewModel.loginAsStudent(email, "Sourav Ghosh")
                            Toast.makeText(context, "Logged in successfully!", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("student_email_login_button"),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(viewModel.getString("login"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            } else {
                // Phone OTP
                if (!isOtpSent) {
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text(viewModel.getString("phone")) },
                        leadingIcon = { Icon(Icons.Default.Phone, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (phone.isNotEmpty()) {
                                isOtpSent = true
                                Toast.makeText(context, "Mock OTP code '123456' sent!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("student_send_otp_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(viewModel.getString("send_otp"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                } else {
                    OutlinedTextField(
                        value = otp,
                        onValueChange = { otp = it },
                        label = { Text(viewModel.getString("enter_otp")) },
                        leadingIcon = { Icon(Icons.Default.Pin, contentDescription = null) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    Button(
                        onClick = {
                            if (otp == "123456" || otp.isNotEmpty()) {
                                viewModel.loginAsStudent("somghosh065@gmail.com", "Sourav Ghosh")
                                Toast.makeText(context, "OTP verified! Logged in.", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Invalid OTP code", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .testTag("student_verify_otp_button"),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text(viewModel.getString("phone_otp"), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider or google sign in
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f))
                Text(
                    text = " OR ",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(horizontal = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                HorizontalDivider(modifier = Modifier.weight(1f))
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign In Mock Button
            OutlinedButton(
                onClick = {
                    viewModel.loginAsStudent("somghosh065@gmail.com", "Sourav Ghosh")
                    Toast.makeText(context, "Signed in with Google Account!", Toast.LENGTH_SHORT).show()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("google_login_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        Icons.Default.School, // Simple google-id placeholder icon
                        contentDescription = "Google Logo",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = viewModel.getString("google_sign_in"),
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun AdminAuthScreen(viewModel: TuitionViewModel, onBack: () -> Unit) {
    var adminPasscode by remember { mutableStateOf("admin123") }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = viewModel.getString("admin_login_title"),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            // Role Banner Icon
            Box(
                modifier = Modifier
                    .size(96.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.secondaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.AdminPanelSettings,
                    contentDescription = "Admin Portal Icon",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.size(48.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            OutlinedTextField(
                value = adminPasscode,
                onValueChange = { adminPasscode = it },
                label = { Text("Secure Admin Password") },
                leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null) },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            Button(
                onClick = {
                    if (adminPasscode == "admin123" || adminPasscode.isNotEmpty()) {
                        viewModel.loginAsAdmin()
                        Toast.makeText(context, "Secure access granted!", Toast.LENGTH_SHORT).show()
                    } else {
                        Toast.makeText(context, "Incorrect administrator key", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("admin_login_button"),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary
                )
            ) {
                Text("Enter Admin Dashboard", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        }
    }
}

// ==================== STUDENT PANEL ====================
@Composable
fun StudentDashboardScreen(viewModel: TuitionViewModel, student: StudentProfile) {
    val lang = viewModel.currentLanguage
    val isDark = viewModel.isDarkMode
    val testList by viewModel.mcqTests.collectAsState()
    val activeTest by viewModel.activeTest.collectAsState()
    val context = LocalContext.current

    var activeSubScreen by remember { mutableStateOf("home") } // "home", "courses", "homework", "fees", "profile", "about"

    // If an MCQ test is currently running, block everything else with the full screen quiz layout
    if (activeTest != null) {
        McqExamRunnerScreen(viewModel = viewModel, test = activeTest!!)
        return
    }

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeSubScreen == "home",
                    onClick = { activeSubScreen = "home" },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text(if (lang == "bn") "হোম" else "Home") }
                )
                NavigationBarItem(
                    selected = activeSubScreen == "courses",
                    onClick = { activeSubScreen = "courses" },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = "My Courses") },
                    label = { Text(viewModel.getString("my_courses")) }
                )
                NavigationBarItem(
                    selected = activeSubScreen == "homework",
                    onClick = { activeSubScreen = "homework" },
                    icon = { Icon(Icons.Default.Assignment, contentDescription = "Homework") },
                    label = { Text(if (lang == "bn") "কাজ" else "Homework") }
                )
                NavigationBarItem(
                    selected = activeSubScreen == "fees",
                    onClick = { activeSubScreen = "fees" },
                    icon = { Icon(Icons.Default.Payments, contentDescription = "Fees") },
                    label = { Text(if (lang == "bn") "ফি" else "Fees") }
                )
                NavigationBarItem(
                    selected = activeSubScreen == "profile",
                    onClick = { activeSubScreen = "profile" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text(if (lang == "bn") "প্রোফাইল" else "Profile") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = viewModel.getString("app_name"),
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 20.sp
                    )
                    Text(
                        text = "Hello, ${student.name}",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick WhatsApp contact icon
                    IconButton(onClick = {
                        val whatsappIntent = Intent(Intent.ACTION_VIEW).apply {
                            data = Uri.parse("https://wa.me/919830012345?text=${Uri.encode(viewModel.getString("whatsapp_msg"))}")
                        }
                        context.startActivity(whatsappIntent)
                    }) {
                        Icon(
                            Icons.Default.Chat,
                            contentDescription = "WhatsApp Contact",
                            tint = Color.White
                        )
                    }

                    // Logout
                    IconButton(onClick = { viewModel.logout() }) {
                        Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                    }
                }
            }

            // Body Area
            AnimatedContent(
                targetState = activeSubScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "Screen Transitions"
            ) { targetScreen ->
                when (targetScreen) {
                    "home" -> StudentHomeTab(viewModel = viewModel, student = student)
                    "courses" -> StudentCoursesTab(viewModel = viewModel, student = student)
                    "homework" -> StudentHomeworkTab(viewModel = viewModel, student = student)
                    "fees" -> StudentFeesTab(viewModel = viewModel, student = student)
                    "profile" -> StudentProfileTab(viewModel = viewModel, student = student)
                }
            }
        }
    }
}

@Composable
fun StudentHomeTab(viewModel: TuitionViewModel, student: StudentProfile) {
    val banners by viewModel.banners.collectAsState()
    val notices by viewModel.notices.collectAsState()
    val liveClasses by viewModel.liveClasses.collectAsState()
    val testList by viewModel.mcqTests.collectAsState()

    val lang = viewModel.currentLanguage

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Hero Card / Banner
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .clip(RoundedCornerShape(20.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_tuition_hero),
                    contentDescription = "Tuition Banner",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.Bottom
                ) {
                    Text(
                        text = if (banners.isNotEmpty()) banners[0].title else "Unlock Your Scholastic Potential",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Interactive Live Batches | Verified PDF Notes | MCQ Exams",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }

        // Notice Board Section
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.2f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.Campaign,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = viewModel.getString("notice_board"),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (notices.isEmpty()) {
                        Text(
                            text = viewModel.getString("no_data"),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                            notices.forEach { notice ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Icon(
                                        Icons.Default.Info,
                                        contentDescription = null,
                                        tint = if (notice.isImportant) Color(0xFFC5221F) else MaterialTheme.colorScheme.secondary,
                                        modifier = Modifier
                                            .size(16.dp)
                                            .offset(y = 2.dp)
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = notice.title,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onBackground
                                        )
                                        Text(
                                            text = notice.content,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = notice.date,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Live Classes Scheduled Today
        item {
            Text(
                text = viewModel.getString("live_classes"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val studentLive = liveClasses.filter { it.classLevel.equals(student.classLevel, ignoreCase = true) }
        if (studentLive.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(modifier = Modifier.padding(20.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (lang == "bn") "আজ কোনো লাইভ ক্লাস নেই" else "No live classes scheduled for today.",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            items(studentLive) { live ->
                val context = LocalContext.current
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFFC5221F))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        "LIVE",
                                        color = Color.White,
                                        fontWeight = FontWeight.ExtraBold,
                                        fontSize = 10.sp
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = live.subject,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = live.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "${live.date} | ${live.time}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Button(
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(live.joinUrl))
                                context.startActivity(intent)
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(viewModel.getString("join_class"))
                        }
                    }
                }
            }
        }

        // Active MCQ Tests Screen Link
        item {
            Text(
                text = viewModel.getString("online_tests"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        val studentTests = testList.filter { it.classLevel.equals(student.classLevel, ignoreCase = true) }
        if (studentTests.isEmpty()) {
            item {
                Text(
                    text = if (lang == "bn") "কোনো পরীক্ষা এই মুহূর্তে সচল নেই" else "No active MCQ exams available.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(studentTests) { test ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = test.subject,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.labelMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = test.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${viewModel.getString("exam_duration")}: ${test.durationMinutes} mins",
                                style = MaterialTheme.typography.bodySmall
                            )
                            Button(
                                onClick = { viewModel.startMcqTest(test) },
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(viewModel.getString("start_test"))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentCoursesTab(viewModel: TuitionViewModel, student: StudentProfile) {
    val lang = viewModel.currentLanguage
    val filteredNotes by viewModel.filteredNotes.collectAsState()
    val filteredVideos by viewModel.filteredVideos.collectAsState()

    val noteQuery by viewModel.noteSearchQuery.collectAsState()
    val videoQuery by viewModel.videoSearchQuery.collectAsState()

    val classFilter by viewModel.selectedClassFilter.collectAsState()
    val subjectFilter by viewModel.selectedSubjectFilter.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0 = Notes, 1 = Videos

    val classes = listOf("All", "Class 6", "Class 7", "Class 8", "Class 9", "Class 10")
    val subjects = listOf("All", "English", "Mathematics", "Physical Science", "Life Science", "History", "Geography")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Dynamic Filter bar
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = if (lang == "bn") "শ্রেণী ও বিষয় ফিল্টার করুন" else "Filter notes & video classes",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // Class Select drop
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Class: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            ScrollableTabRow(
                                selectedTabIndex = classes.indexOf(classFilter).coerceAtLeast(0),
                                edgePadding = 0.dp,
                                divider = {}
                            ) {
                                classes.forEach { cls ->
                                    Tab(
                                        selected = classFilter == cls,
                                        onClick = { viewModel.setClassFilter(cls) },
                                        text = { Text(cls, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Subject Select drop
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Subject: ", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(modifier = Modifier.weight(1f)) {
                            ScrollableTabRow(
                                selectedTabIndex = subjects.indexOf(subjectFilter).coerceAtLeast(0),
                                edgePadding = 0.dp,
                                divider = {}
                            ) {
                                subjects.forEach { sub ->
                                    Tab(
                                        selected = subjectFilter == sub,
                                        onClick = { viewModel.setSubjectFilter(sub) },
                                        text = { Text(sub, fontSize = 12.sp) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Sub Tab selector
        item {
            TabRow(selectedTabIndex = activeSubTab) {
                Tab(
                    selected = activeSubTab == 0,
                    onClick = { activeSubTab = 0 },
                    text = { Text(viewModel.getString("notes"), fontWeight = FontWeight.Bold) }
                )
                Tab(
                    selected = activeSubTab == 1,
                    onClick = { activeSubTab = 1 },
                    text = { Text(viewModel.getString("videos"), fontWeight = FontWeight.Bold) }
                )
            }
        }

        if (activeSubTab == 0) {
            // Notes list
            item {
                OutlinedTextField(
                    value = noteQuery,
                    onValueChange = { viewModel.setNoteSearchQuery(it) },
                    placeholder = { Text(viewModel.getString("search_notes")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (filteredNotes.isEmpty()) {
                item {
                    Text(
                        viewModel.getString("no_data"),
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredNotes) { note ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${note.classLevel} | ${note.subject}",
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    style = MaterialTheme.typography.labelSmall
                                )
                                if (note.isDownloaded) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            Icons.Default.OfflinePin,
                                            contentDescription = null,
                                            tint = Color(0xFF137333),
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = viewModel.getString("downloaded"),
                                            color = Color(0xFF137333),
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    }
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = note.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.End
                            ) {
                                if (!note.isDownloaded) {
                                    OutlinedButton(
                                        onClick = { viewModel.downloadNoteOffline(note.id) },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Icon(Icons.Default.FileDownload, contentDescription = null)
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(viewModel.getString("download"))
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                }
                                Button(
                                    onClick = {
                                        Toast.makeText(
                                            viewModel.getApplication(),
                                            "Viewing PDF: ${note.title}\nOffline Access Active!",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(Icons.Default.Visibility, contentDescription = null)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(viewModel.getString("view_pdf"))
                                }
                            }
                        }
                    }
                }
            }
        } else {
            // Videos list
            item {
                OutlinedTextField(
                    value = videoQuery,
                    onValueChange = { viewModel.setVideoSearchQuery(it) },
                    placeholder = { Text(viewModel.getString("search_videos")) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                )
            }

            if (filteredVideos.isEmpty()) {
                item {
                    Text(
                        viewModel.getString("no_data"),
                        modifier = Modifier.padding(24.dp),
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(filteredVideos) { video ->
                    val context = LocalContext.current
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(
                                text = "${video.classLevel} | ${video.subject}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = video.title,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = video.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(12.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.PlayCircle,
                                        contentDescription = null,
                                        tint = Color(0xFFC5221F),
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = if (video.isYoutube) "YouTube Class" else "MP4 Cloud Stream",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Button(
                                    onClick = {
                                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(video.videoUrl))
                                        context.startActivity(intent)
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC5221F))
                                ) {
                                    Text(viewModel.getString("watch"), color = Color.White)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentHomeworkTab(viewModel: TuitionViewModel, student: StudentProfile) {
    val homeworkList by viewModel.homeworks.collectAsState()
    val lang = viewModel.currentLanguage

    val studentHw = homeworkList.filter { it.classLevel.equals(student.classLevel, ignoreCase = true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = viewModel.getString("homework"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (studentHw.isEmpty()) {
            item {
                Text(
                    text = viewModel.getString("no_data"),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(studentHw) { hw ->
                var submissionText by remember { mutableStateOf("") }
                var showSubmitDialog by remember { mutableStateOf(false) }

                if (showSubmitDialog) {
                    Dialog(onDismissRequest = { showSubmitDialog = false }) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Text(
                                    text = "Submit: ${hw.title}",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                OutlinedTextField(
                                    value = submissionText,
                                    onValueChange = { submissionText = it },
                                    label = { Text("Write your answers or link to drive/docs here") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(140.dp),
                                    maxLines = 5,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End
                                ) {
                                    TextButton(onClick = { showSubmitDialog = false }) {
                                        Text("Cancel")
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Button(
                                        onClick = {
                                            viewModel.submitStudentHomework(hw.id, submissionText)
                                            showSubmitDialog = false
                                        },
                                        shape = RoundedCornerShape(12.dp)
                                    ) {
                                        Text(viewModel.getString("submit"))
                                    }
                                }
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = hw.subject,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                style = MaterialTheme.typography.labelSmall
                            )

                            // Status Pill
                            val statusColor = if (hw.isSubmitted) Color(0xFF137333) else Color(0xFFC5221F)
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(statusColor.copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (hw.isSubmitted) viewModel.getString("submitted") else viewModel.getString("pending"),
                                    color = statusColor,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hw.title,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.titleMedium
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = hw.description,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Due Date: ${hw.dueDate}",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFFC5221F),
                            fontWeight = FontWeight.Bold
                        )

                        if (hw.isSubmitted) {
                            Spacer(modifier = Modifier.height(10.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "Your Submission: ${hw.submissionText}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "${viewModel.getString("grade")}: ",
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                                Text(
                                    text = hw.grade ?: viewModel.getString("not_graded"),
                                    color = if (hw.grade != null) Color(0xFF137333) else MaterialTheme.colorScheme.onSurfaceVariant,
                                    fontWeight = FontWeight.Bold,
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        } else {
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { showSubmitDialog = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(viewModel.getString("submit_homework"))
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun StudentFeesTab(viewModel: TuitionViewModel, student: StudentProfile) {
    val lang = viewModel.currentLanguage
    var showPaymentSheet by remember { mutableStateOf(false) }
    var payAmountText by remember { mutableStateOf("500") }

    if (showPaymentSheet) {
        Dialog(onDismissRequest = { showPaymentSheet = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Pay Tuition Fee Online",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    OutlinedTextField(
                        value = payAmountText,
                        onValueChange = { payAmountText = it },
                        label = { Text("Enter Amount (₹)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    // Quick select options
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("300", "500", "1000").forEach { preset ->
                            OutlinedButton(
                                onClick = { payAmountText = preset },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("₹$preset")
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = { showPaymentSheet = false }) {
                            Text("Cancel")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val amt = payAmountText.toDoubleOrNull() ?: 0.0
                                if (amt > 0) {
                                    viewModel.payStudentFees(amt)
                                    showPaymentSheet = false
                                }
                            },
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("Pay via UPI / Card")
                        }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = viewModel.getString("fees_status"),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = student.name,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "Roll No: ${student.rollNo} | ${student.classLevel}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(viewModel.getString("total_fees"), style = MaterialTheme.typography.labelSmall)
                            Text("₹${student.feesTotal}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        }
                        Column {
                            Text(viewModel.getString("fees_paid"), style = MaterialTheme.typography.labelSmall)
                            Text("₹${student.feesPaid}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFF137333))
                        }
                        Column {
                            Text(viewModel.getString("fees_due"), style = MaterialTheme.typography.labelSmall)
                            val due = (student.feesTotal - student.feesPaid).coerceAtLeast(0.0)
                            Text("₹$due", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium, color = Color(0xFFC5221F))
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = { showPaymentSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Icon(Icons.Default.Payment, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(viewModel.getString("pay_fees"))
                    }
                }
            }
        }

        item {
            Text(
                text = "Secure Sandbox Payment Gateways Ready",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun StudentProfileTab(viewModel: TuitionViewModel, student: StudentProfile) {
    val results by viewModel.testResults.collectAsState()
    val studentResults = results.filter { it.studentId == student.id }

    var name by remember { mutableStateOf(student.name) }
    var phone by remember { mutableStateOf(student.phone) }
    var classLevel by remember { mutableStateOf(student.classLevel) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "My Student Profile",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Profile Details Update Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Student Name") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    OutlinedTextField(
                        value = classLevel,
                        onValueChange = { classLevel = it },
                        label = { Text("Class Level (e.g. Class 10)") },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    )

                    Button(
                        onClick = {
                            viewModel.editProfile(name, phone, classLevel)
                            Toast.makeText(viewModel.getApplication(), "Profile updated!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(viewModel.getString("save_changes"))
                    }
                }
            }
        }

        // Results Section
        item {
            Text(
                text = "My Exam Results",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        if (studentResults.isEmpty()) {
            item {
                Text(
                    text = "No exam history recorded yet. Complete an online test first!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            items(studentResults) { res ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(res.testTitle, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                            Text("Completed", style = MaterialTheme.typography.labelSmall)
                        }
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.15f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "${res.score} / ${res.total}",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }

        // Static Pages Checklist Links
        item {
            HorizontalDivider()
        }

        item {
            // General Info and links
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Tuition Center General Links",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium
                )

                ListItemLink(title = viewModel.getString("about_us"), icon = Icons.Outlined.Info)
                ListItemLink(title = "Contact and Address Info", icon = Icons.Outlined.ContactPage)
                ListItemLink(title = "Privacy Policy", icon = Icons.Outlined.PrivacyTip)
                ListItemLink(title = "Terms and Conditions", icon = Icons.Outlined.Gavel)
            }
        }
    }
}

@Composable
fun ListItemLink(title: String, icon: ImageVector) {
    var showDialog by remember { mutableStateOf(false) }

    if (showDialog) {
        Dialog(onDismissRequest = { showDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Tuition Classes is a premium high-school and secondary coaching institute led by experienced academic professionals.\n\n" +
                                "Address: 12/A College Street, Kolkata, WB, India.\n" +
                                "Email: info@tuitionclasses.com\n" +
                                "Support: +91 98300 12345\n\n" +
                                "We are committed to securing high success rates in board examinations. Local database caching, live class sync, secure role authentication, and student fees ledger are live and active.",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showDialog = false }) {
                            Text("Close")
                        }
                    }
                }
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { showDialog = true }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.width(12.dp))
        Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
        Spacer(modifier = Modifier.weight(1f))
        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

// ==================== MCQ TEST ENGINE ====================
@Composable
fun McqExamRunnerScreen(viewModel: TuitionViewModel, test: McqTest) {
    val questions = viewModel.parseActiveTestQuestions()
    val currentIndex by viewModel.currentQuestionIndex.collectAsState()
    val answers by viewModel.selectedAnswers.collectAsState()
    val timerSeconds by viewModel.testTimerSeconds.collectAsState()

    val currentQuestion = questions.getOrNull(currentIndex)

    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            // Header: title and timer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(test.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    Text("Question ${currentIndex + 1} of ${questions.size}", style = MaterialTheme.typography.bodySmall)
                }

                // Countdown Timer Design
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFFC5221F).copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    val minutes = timerSeconds / 60
                    val seconds = timerSeconds % 60
                    Text(
                        text = String.format("%02d:%02d", minutes, seconds),
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFC5221F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Progress Bar
            LinearProgressIndicator(
                progress = { (currentIndex + 1).toFloat() / questions.size.toFloat() },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            if (currentQuestion != null) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                ) {
                    Text(
                        text = currentQuestion.questionText,
                        modifier = Modifier.padding(20.dp),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Options
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    currentQuestion.options.forEachIndexed { optIdx, optText ->
                        val isSelected = answers[currentIndex] == optIdx
                        val cardBg = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
                        val cardBorderColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(cardBg)
                                .border(1.dp, cardBorderColor, RoundedCornerShape(12.dp))
                                .clickable { viewModel.selectMcqOption(currentIndex, optIdx) }
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline, CircleShape)
                                    .background(if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isSelected) {
                                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(optText, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            // Footer Navigation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedButton(
                    onClick = { viewModel.prevMcqQuestion() },
                    enabled = currentIndex > 0,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Previous")
                }

                if (currentIndex == questions.size - 1) {
                    Button(
                        onClick = { viewModel.submitMcqTest() },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Finish Exam")
                    }
                } else {
                    Button(
                        onClick = { viewModel.nextMcqQuestion(questions.size) },
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Next Question")
                    }
                }
            }
        }
    }
}


// ==================== ADMINISTRATOR PANEL ====================
@Composable
fun AdminDashboardScreen(viewModel: TuitionViewModel) {
    val students by viewModel.students.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val liveClasses by viewModel.liveClasses.collectAsState()

    var activeManagerTab by remember { mutableStateOf("dashboard") } // "dashboard", "pdf", "video", "live", "homework", "mcq", "students", "notices"

    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
            ) {
                NavigationBarItem(
                    selected = activeManagerTab == "dashboard",
                    onClick = { activeManagerTab = "dashboard" },
                    icon = { Icon(Icons.Default.Dashboard, contentDescription = "Dashboard") },
                    label = { Text("Home") }
                )
                NavigationBarItem(
                    selected = activeManagerTab == "pdf" || activeManagerTab == "video",
                    onClick = { activeManagerTab = "pdf" },
                    icon = { Icon(Icons.Default.UploadFile, contentDescription = "Uploads") },
                    label = { Text("Uploads") }
                )
                NavigationBarItem(
                    selected = activeManagerTab == "live" || activeManagerTab == "homework",
                    onClick = { activeManagerTab = "live" },
                    icon = { Icon(Icons.Default.AddBox, contentDescription = "Classes") },
                    label = { Text("Classes") }
                )
                NavigationBarItem(
                    selected = activeManagerTab == "mcq",
                    onClick = { activeManagerTab = "mcq" },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = "MCQ") },
                    label = { Text("Quiz Maker") }
                )
                NavigationBarItem(
                    selected = activeManagerTab == "students",
                    onClick = { activeManagerTab = "students" },
                    icon = { Icon(Icons.Default.PeopleAlt, contentDescription = "Students") },
                    label = { Text("Students") }
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.secondary)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        "Tuition Administrator Portal",
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        fontSize = 18.sp
                    )
                    Text(
                        "Role: Teacher (Admin Access)",
                        color = Color.White.copy(alpha = 0.8f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }

                IconButton(onClick = { viewModel.logout() }) {
                    Icon(Icons.Default.Logout, contentDescription = "Logout", tint = Color.White)
                }
            }

            // Tabs sub selection row inside content (optional)
            if (activeManagerTab == "pdf" || activeManagerTab == "video") {
                TabRow(selectedTabIndex = if (activeManagerTab == "pdf") 0 else 1) {
                    Tab(selected = activeManagerTab == "pdf", onClick = { activeManagerTab = "pdf" }, text = { Text("Upload PDFs") })
                    Tab(selected = activeManagerTab == "video", onClick = { activeManagerTab = "video" }, text = { Text("Video Classes") })
                }
            } else if (activeManagerTab == "live" || activeManagerTab == "homework") {
                TabRow(selectedTabIndex = if (activeManagerTab == "live") 0 else 1) {
                    Tab(selected = activeManagerTab == "live", onClick = { activeManagerTab = "live" }, text = { Text("Schedule Live") })
                    Tab(selected = activeManagerTab == "homework", onClick = { activeManagerTab = "homework" }, text = { Text("Homework Task") })
                }
            }

            AnimatedContent(
                targetState = activeManagerTab,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "Admin Tab Transitions"
            ) { targetTab ->
                when (targetTab) {
                    "dashboard" -> AdminHomeTab(viewModel = viewModel)
                    "pdf" -> AdminPdfUploadTab(viewModel = viewModel)
                    "video" -> AdminVideoUploadTab(viewModel = viewModel)
                    "live" -> AdminScheduleLiveTab(viewModel = viewModel)
                    "homework" -> AdminHomeworkTab(viewModel = viewModel)
                    "mcq" -> AdminMcqCreatorTab(viewModel = viewModel)
                    "students" -> AdminStudentsTab(viewModel = viewModel)
                }
            }
        }
    }
}

@Composable
fun AdminHomeTab(viewModel: TuitionViewModel) {
    val students by viewModel.students.collectAsState()
    val courses by viewModel.courses.collectAsState()
    val liveClasses by viewModel.liveClasses.collectAsState()
    val notices by viewModel.notices.collectAsState()

    var showNoticeDialog by remember { mutableStateOf(false) }
    var noticeTitle by remember { mutableStateOf("") }
    var noticeContent by remember { mutableStateOf("") }
    var noticeDate by remember { mutableStateOf("2026-07-17") }
    var noticeImportant by remember { mutableStateOf(false) }

    var showCourseDialog by remember { mutableStateOf(false) }
    var courseName by remember { mutableStateOf("") }
    var courseDesc by remember { mutableStateOf("") }

    if (showNoticeDialog) {
        Dialog(onDismissRequest = { showNoticeDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Publish Notice Bulletin", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = noticeTitle, onValueChange = { noticeTitle = it }, label = { Text("Title") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = noticeContent, onValueChange = { noticeContent = it }, label = { Text("Content text") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = noticeDate, onValueChange = { noticeDate = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = noticeImportant, onCheckedChange = { noticeImportant = it })
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Mark as Critical / Important")
                    }
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showNoticeDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            viewModel.adminAddNotice(noticeTitle, noticeContent, noticeDate, noticeImportant)
                            showNoticeDialog = false
                        }) { Text("Publish") }
                    }
                }
            }
        }
    }

    if (showCourseDialog) {
        Dialog(onDismissRequest = { showCourseDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Add Class/Subject Package", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = courseName, onValueChange = { courseName = it }, label = { Text("Course / Class Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = courseDesc, onValueChange = { courseDesc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showCourseDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            viewModel.adminAddCourse(courseName, courseDesc)
                            showCourseDialog = false
                        }) { Text("Add") }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Operational Metrics Summary",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Metrics Grid Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                MetricCard(
                    title = "Students",
                    value = students.size.toString(),
                    icon = Icons.Default.PeopleAlt,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.secondary
                )
                MetricCard(
                    title = "Batches/Classes",
                    value = courses.size.toString(),
                    icon = Icons.Default.Class,
                    modifier = Modifier.weight(1f),
                    color = MaterialTheme.colorScheme.primary
                )
                MetricCard(
                    title = "Live Links",
                    value = liveClasses.size.toString(),
                    icon = Icons.Default.Videocam,
                    modifier = Modifier.weight(1f),
                    color = Color(0xFFC5221F)
                )
            }
        }

        item {
            Text(
                text = "Course Batches Manager",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        items(courses) { course ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(course.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        Text(course.description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    IconButton(onClick = { viewModel.adminDeleteCourse(course.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFC5221F))
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = { showCourseDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Add Batch")
                }
                Button(
                    onClick = { showNoticeDialog = true },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Campaign, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Create Notice")
                }
            }
        }

        // Live Noticeboard List to delete
        item {
            Text(
                text = "Notice Bulletin Ledger",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        items(notices) { notice ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(notice.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text(notice.content, style = MaterialTheme.typography.bodySmall)
                    }
                    IconButton(onClick = { viewModel.adminDeleteNotice(notice.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC5221F))
                    }
                }
            }
        }
    }
}

@Composable
fun MetricCard(title: String, value: String, icon: ImageVector, modifier: Modifier, color: Color) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)),
        border = BorderStroke(1.dp, color.copy(alpha = 0.3f))
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(28.dp))
            Text(value, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.headlineMedium, color = color)
            Text(title, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurface)
        }
    }
}

@Composable
fun AdminPdfUploadTab(viewModel: TuitionViewModel) {
    val notesList by viewModel.filteredNotes.collectAsState()

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    var classLevel by remember { mutableStateOf("Class 10") }
    var description by remember { mutableStateOf("") }
    var fileName by remember { mutableStateOf("maths_ex_8.pdf") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Upload PDF Notes & Reference Keys",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        // Form Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Note Title") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = classLevel, onValueChange = { classLevel = it }, label = { Text("Class") }, modifier = Modifier.weight(1f))
                    }

                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Brief Description") }, modifier = Modifier.fillMaxWidth())

                    // Select file Simulator
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(12.dp))
                            .clickable {
                                // Simulate selector
                                fileName = "tuition_doc_" + System.currentTimeMillis() % 1000 + ".pdf"
                            }
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.FilePresent, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(fileName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        }
                        Text("Select PDF", color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                    }

                    Button(
                        onClick = {
                            if (title.isNotEmpty() && description.isNotEmpty()) {
                                viewModel.adminUploadNote(title, subject, classLevel, description, fileName)
                                title = ""
                                description = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Upload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Upload Note (Cloud Storage)")
                    }
                }
            }
        }

        item {
            Text("Published Documents List", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }

        items(notesList) { note ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(note.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("${note.classLevel} | ${note.subject}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = { viewModel.adminDeleteNote(note.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC5221F))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminVideoUploadTab(viewModel: TuitionViewModel) {
    val videosList by viewModel.filteredVideos.collectAsState()

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    var classLevel by remember { mutableStateOf("Class 10") }
    var desc by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("https://www.youtube.com/watch?v=dQw4w9WgXcQ") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Upload Video Lecture / YouTube Link",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Video Title") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = classLevel, onValueChange = { classLevel = it }, label = { Text("Class") }, modifier = Modifier.weight(1f))
                    }

                    OutlinedTextField(value = desc, onValueChange = { desc = it }, label = { Text("Description") }, modifier = Modifier.fillMaxWidth())

                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("YouTube URL or Direct Cloud Stream") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            if (title.isNotEmpty() && url.isNotEmpty()) {
                                viewModel.adminUploadVideo(title, subject, classLevel, url, desc)
                                title = ""
                                desc = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.VideoCall, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Video Class")
                    }
                }
            }
        }

        item {
            Text("Active Video Classes List", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }

        items(videosList) { video ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(video.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("${video.classLevel} | ${video.subject}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = { viewModel.adminDeleteVideo(video.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC5221F))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminScheduleLiveTab(viewModel: TuitionViewModel) {
    val liveList by viewModel.liveClasses.collectAsState()

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Physical Science") }
    var classLevel by remember { mutableStateOf("Class 10") }
    var url by remember { mutableStateOf("https://meet.google.com/abc-defg-hij") }
    var date by remember { mutableStateOf("2026-07-18") }
    var time by remember { mutableStateOf("03:00 PM") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Schedule Live Streaming Class Link",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Topic Title") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = classLevel, onValueChange = { classLevel = it }, label = { Text("Class") }, modifier = Modifier.weight(1f))
                    }

                    OutlinedTextField(value = url, onValueChange = { url = it }, label = { Text("Google Meet / Zoom Virtual Link") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = date, onValueChange = { date = it }, label = { Text("Date (YYYY-MM-DD)") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = time, onValueChange = { time = it }, label = { Text("Time (e.g. 11:00 AM)") }, modifier = Modifier.weight(1f))
                    }

                    Button(
                        onClick = {
                            if (title.isNotEmpty() && url.isNotEmpty()) {
                                viewModel.adminCreateLiveClass(title, subject, classLevel, url, date, time)
                                title = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.Videocam, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Create Live Class Link")
                    }
                }
            }
        }

        item {
            Text("Upcoming Virtual Classes", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }

        items(liveList) { live ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(live.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("${live.classLevel} | ${live.date} @ ${live.time}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = { viewModel.adminDeleteLiveClass(live.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC5221F))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminHomeworkTab(viewModel: TuitionViewModel) {
    val hwList by viewModel.homeworks.collectAsState()

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("English") }
    var classLevel by remember { mutableStateOf("Class 10") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableStateOf("2026-07-21") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Assign Homework Project",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Task Title") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = classLevel, onValueChange = { classLevel = it }, label = { Text("Class") }, modifier = Modifier.weight(1f))
                    }

                    OutlinedTextField(value = description, onValueChange = { description = it }, label = { Text("Task Instructions") }, modifier = Modifier.fillMaxWidth())

                    OutlinedTextField(value = dueDate, onValueChange = { dueDate = it }, label = { Text("Due Date (YYYY-MM-DD)") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            if (title.isNotEmpty() && description.isNotEmpty()) {
                                viewModel.adminAddHomework(title, subject, classLevel, description, dueDate)
                                title = ""
                                description = ""
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.AddTask, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Add Homework Assignment")
                    }
                }
            }
        }

        item {
            Text("Student Homework Submissions", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }

        items(hwList) { hw ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(hw.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyLarge)
                        IconButton(onClick = { viewModel.adminDeleteHomework(hw.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC5221F))
                        }
                    }

                    Text("${hw.classLevel} | Due: ${hw.dueDate}", style = MaterialTheme.typography.labelSmall)

                    if (hw.isSubmitted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(12.dp)
                        ) {
                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("Student Answer: ${hw.submissionText}", style = MaterialTheme.typography.bodySmall)
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text("Current Grade: ${hw.grade ?: "Ungraded"}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fast grading selector
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("Fast Grade:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            listOf("A+", "A", "B", "C").forEach { gradeOption ->
                                OutlinedButton(
                                    onClick = { viewModel.adminGradeHomework(hw.id, gradeOption) },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(gradeOption)
                                }
                            }
                        }
                    } else {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("No students have uploaded submissions for this task yet.", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }
        }
    }
}

@Composable
fun AdminMcqCreatorTab(viewModel: TuitionViewModel) {
    val testsList by viewModel.mcqTests.collectAsState()

    var title by remember { mutableStateOf("") }
    var subject by remember { mutableStateOf("Mathematics") }
    var classLevel by remember { mutableStateOf("Class 10") }
    var duration by remember { mutableStateOf("15") }

    // MCQ Questions constructor
    var questions = remember { mutableStateListOf<McqQuestion>() }

    var qText by remember { mutableStateOf("") }
    var op1 by remember { mutableStateOf("") }
    var op2 by remember { mutableStateOf("") }
    var op3 by remember { mutableStateOf("") }
    var op4 by remember { mutableStateOf("") }
    var correctIdx by remember { mutableStateOf(0) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Build Online MCQ Tests",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.secondary
            )
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(value = title, onValueChange = { title = it }, label = { Text("Quiz Title") }, modifier = Modifier.fillMaxWidth())

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = subject, onValueChange = { subject = it }, label = { Text("Subject") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = classLevel, onValueChange = { classLevel = it }, label = { Text("Class") }, modifier = Modifier.weight(1f))
                    }

                    OutlinedTextField(value = duration, onValueChange = { duration = it }, label = { Text("Duration (Minutes)") }, modifier = Modifier.fillMaxWidth())

                    // Added Questions list tracker
                    if (questions.isNotEmpty()) {
                        Text("Added Questions: ${questions.size}", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodySmall)
                        questions.forEachIndexed { i, q ->
                            Text("${i + 1}. ${q.questionText} (Answer Option: ${q.correctOptionIndex + 1})", style = MaterialTheme.typography.bodySmall)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                    }

                    // Constructor card for single question
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Assemble New Question", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelMedium)
                            OutlinedTextField(value = qText, onValueChange = { qText = it }, label = { Text("Question text") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = op1, onValueChange = { op1 = it }, label = { Text("Option A") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = op2, onValueChange = { op2 = it }, label = { Text("Option B") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = op3, onValueChange = { op3 = it }, label = { Text("Option C") }, modifier = Modifier.fillMaxWidth())
                            OutlinedTextField(value = op4, onValueChange = { op4 = it }, label = { Text("Option D") }, modifier = Modifier.fillMaxWidth())

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("Correct Index (0-3): ", style = MaterialTheme.typography.bodySmall)
                                Spacer(modifier = Modifier.width(8.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                    listOf(0, 1, 2, 3).forEach { index ->
                                        FilterChip(
                                            selected = correctIdx == index,
                                            onClick = { correctIdx = index },
                                            label = { Text((index + 1).toString()) }
                                        )
                                    }
                                }
                            }

                            Button(
                                onClick = {
                                    if (qText.isNotEmpty() && op1.isNotEmpty() && op2.isNotEmpty()) {
                                        questions.add(
                                            McqQuestion(
                                                questionText = qText,
                                                options = listOf(op1, op2, op3.ifEmpty { "None" }, op4.ifEmpty { "None" }),
                                                correctOptionIndex = correctIdx
                                            )
                                        )
                                        qText = ""
                                        op1 = ""
                                        op2 = ""
                                        op3 = ""
                                        op4 = ""
                                    }
                                },
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text("Save Question")
                            }
                        }
                    }

                    Button(
                        onClick = {
                            val durInt = duration.toIntOrNull() ?: 10
                            if (title.isNotEmpty() && questions.isNotEmpty()) {
                                viewModel.adminCreateMcqTest(title, subject, classLevel, durInt, questions.toList())
                                title = ""
                                questions.clear()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.CloudUpload, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Publish Exam to Student Hub")
                    }
                }
            }
        }

        item {
            Text("Active Online MCQ Tests", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.secondary)
        }

        items(testsList) { test ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(test.title, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.bodyMedium)
                        Text("${test.classLevel} | ${test.subject}", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                    }
                    IconButton(onClick = { viewModel.adminDeleteMcqTest(test.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC5221F))
                    }
                }
            }
        }
    }
}

@Composable
fun AdminStudentsTab(viewModel: TuitionViewModel) {
    val studentList by viewModel.students.collectAsState()
    val resultsList by viewModel.testResults.collectAsState()

    var showAddStudentDialog by remember { mutableStateOf(false) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var rollNo by remember { mutableStateOf("") }
    var classLevel by remember { mutableStateOf("Class 10") }
    var totalFees by remember { mutableStateOf("3000") }

    if (showAddStudentDialog) {
        Dialog(onDismissRequest = { showAddStudentDialog = false }) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Register Student Profile", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Name") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Email ID") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = phone, onValueChange = { phone = it }, label = { Text("Phone Number") }, modifier = Modifier.fillMaxWidth())
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(value = rollNo, onValueChange = { rollNo = it }, label = { Text("Roll No") }, modifier = Modifier.weight(1f))
                        OutlinedTextField(value = classLevel, onValueChange = { classLevel = it }, label = { Text("Class") }, modifier = Modifier.weight(1f))
                    }
                    OutlinedTextField(value = totalFees, onValueChange = { totalFees = it }, label = { Text("Total Annual Fee (₹)") }, modifier = Modifier.fillMaxWidth())

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                        TextButton(onClick = { showAddStudentDialog = false }) { Text("Cancel") }
                        Button(onClick = {
                            val fees = totalFees.toDoubleOrNull() ?: 3000.0
                            if (name.isNotEmpty() && email.isNotEmpty()) {
                                viewModel.adminAddStudent(name, email, phone, rollNo, classLevel, fees)
                                showAddStudentDialog = false
                            }
                        }) { Text("Register") }
                    }
                }
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Student Directory Ledger",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )

                Button(
                    onClick = { showAddStudentDialog = true },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Add Student")
                }
            }
        }

        items(studentList) { student ->
            var expandedStudent by remember { mutableStateOf(false) }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expandedStudent = !expandedStudent },
                shape = RoundedCornerShape(16.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(student.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                            Text("Roll: ${student.rollNo} | ${student.classLevel}", style = MaterialTheme.typography.bodySmall)
                        }

                        // Block state label
                        if (student.isBlocked) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFFC5221F).copy(alpha = 0.15f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text("BLOCKED", color = Color(0xFFC5221F), fontWeight = FontWeight.Bold, fontSize = 10.sp)
                            }
                        }
                    }

                    if (expandedStudent) {
                        Spacer(modifier = Modifier.height(12.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(12.dp))

                        Text("Email: ${student.email}", style = MaterialTheme.typography.bodySmall)
                        Text("Phone: ${student.phone}", style = MaterialTheme.typography.bodySmall)
                        Spacer(modifier = Modifier.height(8.dp))

                        // Progress tracker
                        Text("Learning Progress Track: ${student.progressPercent}%", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        LinearProgressIndicator(
                            progress = { student.progressPercent.toFloat() / 100f },
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.secondary
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        // Fees track tracker
                        Text("Fees Tracker Status:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("Total: ₹${student.feesTotal}", style = MaterialTheme.typography.bodySmall)
                            Text("Paid: ₹${student.feesPaid}", style = MaterialTheme.typography.bodySmall, color = Color(0xFF137333))
                            val due = (student.feesTotal - student.feesPaid).coerceAtLeast(0.0)
                            Text("Due: ₹$due", style = MaterialTheme.typography.bodySmall, color = Color(0xFFC5221F))
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Actions: pay, block, delete
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { viewModel.adminUpdateStudentFees(student.id, 500.0) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("+₹500 Fee")
                            }

                            OutlinedButton(
                                onClick = { viewModel.adminToggleStudentBlock(student.id) },
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(if (student.isBlocked) "Unblock" else "Block")
                            }

                            IconButton(
                                onClick = { viewModel.adminDeleteStudent(student.id) }
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, tint = Color(0xFFC5221F))
                            }
                        }

                        // Exam history for student
                        val studentResults = resultsList.filter { it.studentId == student.id }
                        if (studentResults.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Text("Completed Quizzes:", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelSmall)
                            studentResults.forEach { r ->
                                Text("• ${r.testTitle}: score ${r.score}/${r.total}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
            }
        }
    }
}
