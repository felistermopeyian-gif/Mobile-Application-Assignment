package com.example.studentmanagementapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.studentmanagementapp.ui.theme.StudentManagementAppTheme
import java.util.*

sealed class Screen {
    object Login : Screen()
    object SignUp : Screen()
    object ForgotPassword : Screen()
    object Dashboard : Screen()
    data class StudentForm(val student: Student? = null) : Screen()
    data class StaffForm(val staff: Staff? = null) : Screen()
}

class MainActivity : ComponentActivity() {
    private lateinit var storageManager: StorageManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storageManager = StorageManager(this)
        setContent {
            StudentManagementAppTheme {
                AppNavigation(storageManager)
            }
        }
    }
}

@Composable
fun AppNavigation(storageManager: StorageManager) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Login) }
    var students by remember { mutableStateOf(storageManager.loadStudents()) }
    var staffList by remember { mutableStateOf(storageManager.loadStaff()) }

    // System Back Button Support
    BackHandler(enabled = currentScreen != Screen.Login) {
        currentScreen = when (currentScreen) {
            is Screen.Dashboard -> Screen.Login
            is Screen.StudentForm, is Screen.StaffForm -> Screen.Dashboard
            is Screen.SignUp, is Screen.ForgotPassword -> Screen.Login
            else -> Screen.Login
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                slideInHorizontally { it } + fadeIn() togetherWith slideOutHorizontally { -it } + fadeOut()
            }, label = "ScreenTransition"
        ) { screen ->
            when (screen) {
                is Screen.Login -> LoginScreen(
                    onLoginSuccess = { currentScreen = Screen.Dashboard },
                    onSignUp = { currentScreen = Screen.SignUp },
                    onForgotPassword = { currentScreen = Screen.ForgotPassword }
                )
                is Screen.SignUp -> SignUpScreen(
                    onBack = { currentScreen = Screen.Login },
                    onSignUpSuccess = { currentScreen = Screen.Login }
                )
                is Screen.ForgotPassword -> ForgotPasswordScreen(
                    onBack = { currentScreen = Screen.Login }
                )
                is Screen.Dashboard -> DashboardScreen(
                    students = students,
                    staffList = staffList,
                    onUpdateStudent = { updated ->
                        storageManager.updateStudent(updated)
                        students = storageManager.loadStudents()
                    },
                    onUpdateStaff = { updated ->
                        storageManager.updateStaff(updated)
                        staffList = storageManager.loadStaff()
                    },
                    onAddStudent = { currentScreen = Screen.StudentForm() },
                    onEditStudent = { student -> currentScreen = Screen.StudentForm(student) },
                    onDeleteStudent = { studentId ->
                        storageManager.deleteStudent(studentId)
                        students = storageManager.loadStudents()
                    },
                    onAddStaff = { currentScreen = Screen.StaffForm() },
                    onEditStaff = { staff -> currentScreen = Screen.StaffForm(staff) },
                    onDeleteStaff = { staffId ->
                        storageManager.deleteStaff(staffId)
                        staffList = storageManager.loadStaff()
                    },
                    onLogout = { currentScreen = Screen.Login }
                )
                is Screen.StudentForm -> StudentFormScreen(
                    student = screen.student,
                    onSave = { updatedStudent ->
                        if (screen.student == null) {
                            storageManager.addStudent(updatedStudent)
                        } else {
                            storageManager.updateStudent(updatedStudent)
                        }
                        students = storageManager.loadStudents()
                        currentScreen = Screen.Dashboard
                    },
                    onBack = { currentScreen = Screen.Dashboard }
                )
                is Screen.StaffForm -> StaffFormScreen(
                    staff = screen.staff,
                    onSave = { updatedStaff ->
                        if (screen.staff == null) {
                            storageManager.addStaff(updatedStaff)
                        } else {
                            storageManager.updateStaff(updatedStaff)
                        }
                        staffList = storageManager.loadStaff()
                        currentScreen = Screen.Dashboard
                    },
                    onBack = { currentScreen = Screen.Dashboard }
                )
            }
        }
    }
}

@Composable
fun LoginScreen(onLoginSuccess: () -> Unit, onSignUp: () -> Unit, onForgotPassword: () -> Unit) {
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFF0D47A1), Color(0xFF1976D2), Color(0xFF42A5F5))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Box(modifier = Modifier.offset(x = (-100).dp, y = (-200).dp).size(250.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.1f)))
        Box(modifier = Modifier.offset(x = 150.dp, y = 300.dp).size(200.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.05f)))

        Card(
            modifier = Modifier.fillMaxWidth(0.9f).padding(16.dp),
            shape = RoundedCornerShape(40.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.9f))
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Surface(
                    shape = CircleShape,
                    color = Color(0xFF0D47A1),
                    modifier = Modifier.size(100.dp),
                    shadowElevation = 8.dp
                ) {
                    Icon(imageVector = Icons.Default.AutoStories, contentDescription = null, modifier = Modifier.padding(24.dp), tint = Color.White)
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text("OSUYAI ACADEMY", fontSize = 26.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D47A1), letterSpacing = 2.sp)
                Text("INTELLIGENT CAMPUS HUB", fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                
                Spacer(modifier = Modifier.height(40.dp))

                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("Staff Identity") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = { Icon(Icons.Default.AccountCircle, null, tint = Color(0xFF0D47A1)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                )
                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Access Key") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(20.dp),
                    leadingIcon = { Icon(Icons.Default.Fingerprint, null, tint = Color(0xFF0D47A1)) },
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (username.isNotBlank() && password.isNotBlank()) onLoginSuccess() }),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                )
                
                TextButton(onClick = onForgotPassword, modifier = Modifier.align(Alignment.End)) {
                    Text("Lost access key?", color = Color(0xFF0D47A1), fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = { if (username.isNotBlank() && password.isNotBlank()) onLoginSuccess() },
                    modifier = Modifier.fillMaxWidth().height(64.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))
                ) {
                    Text("UNLOCK SYSTEM", fontSize = 18.sp, fontWeight = FontWeight.ExtraBold)
                }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                TextButton(onClick = onSignUp) {
                    Text("No account? Request Digital ID", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    students: List<Student>,
    staffList: List<Staff>,
    onUpdateStudent: (Student) -> Unit,
    onUpdateStaff: (Staff) -> Unit,
    onAddStudent: () -> Unit,
    onEditStudent: (Student) -> Unit,
    onDeleteStudent: (String) -> Unit,
    onAddStaff: () -> Unit,
    onEditStaff: (Staff) -> Unit,
    onDeleteStaff: (String) -> Unit,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredStudents = students.filter { it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true) }
    val filteredStaff = staffList.filter { it.name.contains(searchQuery, ignoreCase = true) || it.id.contains(searchQuery, ignoreCase = true) }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.background(Color(0xFF0D47A1))) {
                CenterAlignedTopAppBar(
                    title = { Text("OSUYAI CAMPUS HUB", fontWeight = FontWeight.Black, fontSize = 18.sp) },
                    actions = { IconButton(onClick = onLogout) { Icon(Icons.AutoMirrored.Filled.Logout, null) } },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent, titleContentColor = Color.White, actionIconContentColor = Color.White)
                )
                
                NoticeBoard()

                LazyRow(modifier = Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    item { SummaryChip("Total Pupils", students.size.toString(), Icons.Default.Face) }
                    item { SummaryChip("Total Staff", staffList.size.toString(), Icons.Default.Groups) }
                    item { SummaryChip("Attendance", "${(students.count { it.attendanceToday } + staffList.count { it.attendanceToday })} Present", Icons.Default.EventAvailable) }
                    item { SummaryChip("Fee Revenue", "₦${students.sumOf { it.feesPaid }.toInt()}", Icons.Default.Payments) }
                }

                TabRow(selectedTabIndex = selectedTab, containerColor = Color.Transparent, contentColor = Color.White, indicator = { tabPositions ->
                    TabRowDefaults.SecondaryIndicator(Modifier.tabIndicatorOffset(tabPositions[selectedTab]), color = Color.White, height = 4.dp)
                }) {
                    Tab(selected = selectedTab == 0, onClick = { selectedTab = 0 }, text = { Text("ACADEMY", fontWeight = FontWeight.Bold) })
                    Tab(selected = selectedTab == 1, onClick = { selectedTab = 1 }, text = { Text("OPERATIONS", fontWeight = FontWeight.Bold) })
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { if (selectedTab == 0) onAddStudent() else onAddStaff() }, containerColor = Color(0xFF0D47A1), contentColor = Color.White, shape = RoundedCornerShape(20.dp)) {
                Icon(if (selectedTab == 0) Icons.Default.PersonAddAlt1 else Icons.Default.GroupAdd, null)
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues).background(Color(0xFFF8FAFC))) {
            Column {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    placeholder = { Text("Global Search...") },
                    leadingIcon = { Icon(Icons.Default.Search, null, tint = Color(0xFF0D47A1)) },
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedContainerColor = Color.White, unfocusedContainerColor = Color.White, focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                )

                if (selectedTab == 0) {
                    if (filteredStudents.isEmpty()) EmptyStateView("Academy is Quiet") else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(filteredStudents) { student ->
                                SmartStudentCard(
                                    student = student, 
                                    onEdit = { onEditStudent(student) }, 
                                    onDelete = { onDeleteStudent(student.id) },
                                    onAttendanceToggle = { onUpdateStudent(student.copy(attendanceToday = !student.attendanceToday)) }
                                )
                            }
                        }
                    }
                } else {
                    if (filteredStaff.isEmpty()) EmptyStateView("Operations Halted") else {
                        LazyColumn(modifier = Modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(filteredStaff) { staff ->
                                SmartStaffCard(
                                    staff = staff, 
                                    onEdit = { onEditStaff(staff) }, 
                                    onDelete = { onDeleteStaff(staff.id) },
                                    onAttendanceToggle = { onUpdateStaff(staff.copy(attendanceToday = !staff.attendanceToday)) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NoticeBoard() {
    val messages = listOf(
        "📢 Inter-house sports coming up next Friday!",
        "🗓️ Mid-term assessment starts tomorrow.",
        "💡 Don't forget to commit payroll by 25th.",
        "🌟 Congratulations to Primary 5 for best attendance!"
    )
    var currentIndex by remember { mutableIntStateOf(0) }
    
    LaunchedEffect(Unit) {
        while(true) {
            kotlinx.coroutines.delay(4000)
            currentIndex = (currentIndex + 1) % messages.size
        }
    }

    Box(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.1f)).padding(8.dp), contentAlignment = Alignment.Center) {
        AnimatedContent(targetState = messages[currentIndex], transitionSpec = { fadeIn() togetherWith fadeOut() }, label = "Notice") { text ->
            Text(text, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold, maxLines = 1)
        }
    }
}

@Composable
fun SummaryChip(label: String, value: String, icon: ImageVector) {
    Surface(color = Color.White.copy(alpha = 0.15f), shape = RoundedCornerShape(16.dp)) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, null, modifier = Modifier.size(20.dp), tint = Color.White)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(value, color = Color.White, fontWeight = FontWeight.Black, fontSize = 16.sp)
                Text(label, color = Color.White.copy(alpha = 0.7f), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SmartStudentCard(student: Student, onEdit: () -> Unit, onDelete: () -> Unit, onAttendanceToggle: () -> Unit) {
    var showIDCard by remember { mutableStateOf(false) }
    
    if (showIDCard) {
        DigitalIDDialog(name = student.name, id = student.id, sub = student.grade, isStudent = true) { showIDCard = false }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showIDCard = true },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).clip(RoundedCornerShape(20.dp)).background(Color(0xFFE3F2FD)), contentAlignment = Alignment.Center) {
                    Text(student.name.take(1).uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D47A1))
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(student.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = Color(0xFF0D47A1).copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
                            Text(student.grade, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = Color(0xFF0D47A1), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        PerformanceBadge(student.performanceLevel)
                    }
                }
                Checkbox(checked = student.attendanceToday, onCheckedChange = { onAttendanceToggle() }, colors = CheckboxDefaults.colors(checkedColor = Color(0xFF0D47A1)))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                InfoItem(Icons.Default.PhoneInTalk, student.guardianPhone.ifBlank { "No Contact" })
                InfoItem(Icons.Default.QueryStats, "Avg: ${String.format("%.1f", student.averageScore)}%")
            }

            Spacer(modifier = Modifier.height(16.dp))
            
            val feeProgress = if (student.feesTotal > 0) (student.feesPaid / student.feesTotal).toFloat() else 0f
            LinearProgressIndicator(progress = { feeProgress }, modifier = Modifier.fillMaxWidth().height(10.dp).clip(CircleShape), color = if (feeProgress >= 1f) Color(0xFF10B981) else Color(0xFFF59E0B), trackColor = Color(0xFFF1F5F9))
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End) {
                IconButton(onClick = onDelete) { Icon(Icons.Default.DeleteOutline, null, tint = Color.LightGray) }
                Button(onClick = onEdit, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))) {
                    Text("OPEN PROFILE", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun SmartStaffCard(staff: Staff, onEdit: () -> Unit, onDelete: () -> Unit, onAttendanceToggle: () -> Unit) {
    var showIDCard by remember { mutableStateOf(false) }
    
    if (showIDCard) {
        DigitalIDDialog(name = staff.name, id = staff.id, sub = staff.role, isStudent = false) { showIDCard = false }
    }

    Card(
        modifier = Modifier.fillMaxWidth().clickable { showIDCard = true },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(64.dp).clip(CircleShape).background(Color(0xFFF5F3FF)), contentAlignment = Alignment.Center) {
                    Text(staff.name.take(1).uppercase(), fontSize = 28.sp, fontWeight = FontWeight.Black, color = Color(0xFF7C3AED))
                }
                Spacer(modifier = Modifier.width(20.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(staff.name, fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF1E293B))
                    Text(staff.role, color = Color(0xFF7C3AED), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
                Switch(checked = staff.attendanceToday, onCheckedChange = { onAttendanceToggle() }, colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF7C3AED)))
            }
            
            Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color(0xFFF1F5F9))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text("EXPECTED PAY", fontSize = 10.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                    Text("₦${staff.netPay.toInt()}", fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D47A1))
                }
                Surface(color = if(staff.paymentStatus == "Paid") Color(0xFFD1FAE5) else Color(0xFFFEE2E2), shape = RoundedCornerShape(12.dp)) {
                    Text(staff.paymentStatus, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), color = if(staff.paymentStatus == "Paid") Color(0xFF065F46) else Color(0xFF991B1B), fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
            }
            
            Row(modifier = Modifier.fillMaxWidth().padding(top = 16.dp), horizontalArrangement = Arrangement.End, verticalAlignment = Alignment.CenterVertically) {
                TextButton(onClick = onDelete) { Text("DISMISS", color = Color.LightGray, fontSize = 12.sp) }
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onEdit, shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) {
                    Icon(Icons.Default.VerifiedUser, null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("PAYROLL", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DigitalIDDialog(name: String, id: String, sub: String, isStudent: Boolean, onDismiss: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Card(modifier = Modifier.fillMaxWidth().height(450.dp), shape = RoundedCornerShape(40.dp)) {
            Box(modifier = Modifier.fillMaxSize()) {
                Box(modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(listOf(if(isStudent) Color(0xFF0D47A1) else Color(0xFF7C3AED), Color.Black))))
                
                Column(modifier = Modifier.fillMaxSize().padding(32.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("OSUYAI ACADEMY", color = Color.White, fontWeight = FontWeight.Black, fontSize = 18.sp, letterSpacing = 4.sp)
                    Text("OFFICIAL DIGITAL ID", color = Color.White.copy(alpha = 0.6f), fontSize = 10.sp)
                    
                    Spacer(modifier = Modifier.height(40.dp))
                    
                    Surface(modifier = Modifier.size(140.dp), shape = CircleShape, color = Color.White, border = BorderStroke(4.dp, Color.White.copy(alpha = 0.2f))) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(name.take(1), fontSize = 72.sp, fontWeight = FontWeight.Black, color = if(isStudent) Color(0xFF0D47A1) else Color(0xFF7C3AED))
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(name.uppercase(), color = Color.White, fontWeight = FontWeight.Black, fontSize = 24.sp, textAlign = TextAlign.Center)
                    Text(sub, color = if(isStudent) Color(0xFF60A5FA) else Color(0xFFC084FC), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    
                    Spacer(modifier = Modifier.height(32.dp))
                    
                    Row(modifier = Modifier.fillMaxWidth().background(Color.White.copy(alpha = 0.1f), RoundedCornerShape(16.dp)).padding(16.dp), horizontalArrangement = Arrangement.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("IDENTITY CODE", color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                            Text(id, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    
                    Spacer(modifier = Modifier.weight(1f))
                    Text("SCAN FOR VERIFICATION", color = Color.White.copy(alpha = 0.3f), fontSize = 10.sp)
                }
            }
        }
    }
}

@Composable
fun PerformanceBadge(level: String) {
    val color = when(level) {
        "Excellent" -> Color(0xFF10B981)
        "Good" -> Color(0xFF3B82F6)
        "Fair" -> Color(0xFFF59E0B)
        else -> Color(0xFFEF4444)
    }
    Surface(color = color.copy(alpha = 0.1f), shape = RoundedCornerShape(8.dp)) {
        Text(level, modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InfoItem(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(6.dp))
        Text(text, fontSize = 12.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
    }
}

@Composable
fun EmptyStateView(message: String) {
    Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Icon(Icons.Default.CloudQueue, null, modifier = Modifier.size(100.dp), tint = Color.LightGray)
        Spacer(modifier = Modifier.height(16.dp))
        Text(message, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray)
    }
}

@Composable
fun FormSectionTitle(title: String) {
    Text(title, modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp), fontSize = 12.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D47A1), letterSpacing = 2.sp)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StudentFormScreen(student: Student?, onSave: (Student) -> Unit, onBack: () -> Unit) {
    var id by remember { mutableStateOf(student?.id ?: "") }
    var name by remember { mutableStateOf(student?.name ?: "") }
    var grade by remember { mutableStateOf(student?.grade ?: "Primary 1") }
    var gender by remember { mutableStateOf(student?.gender ?: "Male") }
    var guardianName by remember { mutableStateOf(student?.guardianName ?: "") }
    var guardianPhone by remember { mutableStateOf(student?.guardianPhone ?: "") }
    var feesPaid by remember { mutableStateOf(student?.feesPaid?.toString() ?: "0.0") }
    var feesTotal by remember { mutableStateOf(student?.feesTotal?.toString() ?: "50000.0") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("PUPIL IDENTITY FORM", fontWeight = FontWeight.Black) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp).verticalScroll(rememberScrollState())) {
            FormSectionTitle("CORE DATA")
            OutlinedTextField(
                value = id, 
                onValueChange = { id = it }, 
                label = { Text("Registration ID") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp), 
                enabled = student == null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("Legal Name") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            FormSectionTitle("ACADEMICS")
            Row {
                Box(modifier = Modifier.weight(1f)) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedTextField(value = grade, onValueChange = {}, label = { Text("Class") }, readOnly = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Nursery 1", "Nursery 2", "Primary 1", "Primary 2", "Primary 3", "Primary 4", "Primary 5", "Primary 6").forEach { g ->
                            DropdownMenuItem(text = { Text(g) }, onClick = { grade = g; expanded = false })
                        }
                    }
                }
                Spacer(modifier = Modifier.width(16.dp))
                Box(modifier = Modifier.weight(1f)) {
                    var expanded by remember { mutableStateOf(false) }
                    OutlinedTextField(value = gender, onValueChange = {}, label = { Text("Gender") }, readOnly = true, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), trailingIcon = { IconButton(onClick = { expanded = true }) { Icon(Icons.Default.ArrowDropDown, null) } }, colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        listOf("Male", "Female").forEach { g -> DropdownMenuItem(text = { Text(g) }, onClick = { gender = g; expanded = false }) }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            FormSectionTitle("GUARDIAN CONTACT")
            OutlinedTextField(
                value = guardianName, 
                onValueChange = { guardianName = it }, 
                label = { Text("Guardian Name") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = guardianPhone, 
                onValueChange = { guardianPhone = it }, 
                label = { Text("Phone Number") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )

            Spacer(modifier = Modifier.height(24.dp))
            FormSectionTitle("FINANCE")
            Row {
                OutlinedTextField(
                    value = feesTotal, 
                    onValueChange = { feesTotal = it }, 
                    label = { Text("Fees") }, 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                    keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Right) }),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                )
                Spacer(modifier = Modifier.width(16.dp))
                OutlinedTextField(
                    value = feesPaid, 
                    onValueChange = { feesPaid = it }, 
                    label = { Text("Paid") }, 
                    modifier = Modifier.weight(1f), 
                    shape = RoundedCornerShape(16.dp),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { if (id.isNotBlank() && name.isNotBlank()) onSave(Student(id = id, name = name, grade = grade, gender = gender, guardianName = guardianName, guardianPhone = guardianPhone, feesPaid = feesPaid.toDoubleOrNull() ?: 0.0, feesTotal = feesTotal.toDoubleOrNull() ?: 0.0)) }),
                    colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
                )
            }

            Spacer(modifier = Modifier.height(48.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(20.dp)) {
                    Text("BACK", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { if (id.isNotBlank() && name.isNotBlank()) onSave(Student(id = id, name = name, grade = grade, gender = gender, guardianName = guardianName, guardianPhone = guardianPhone, feesPaid = feesPaid.toDoubleOrNull() ?: 0.0, feesTotal = feesTotal.toDoubleOrNull() ?: 0.0)) }, modifier = Modifier.weight(1.5f).height(64.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))) {
                    Text("COMMIT & NEXT", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StaffFormScreen(staff: Staff?, onSave: (Staff) -> Unit, onBack: () -> Unit) {
    var id by remember { mutableStateOf(staff?.id ?: "") }
    var name by remember { mutableStateOf(staff?.name ?: "") }
    var role by remember { mutableStateOf(staff?.role ?: "Teacher") }
    var salary by remember { mutableStateOf(staff?.salary?.toString() ?: "0.0") }
    var bankName by remember { mutableStateOf(staff?.bankName ?: "") }
    var accountNumber by remember { mutableStateOf(staff?.accountNumber ?: "") }
    val focusManager = LocalFocusManager.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("STAFF IDENTITY FORM", fontWeight = FontWeight.Black) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, null) } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp).verticalScroll(rememberScrollState())) {
            FormSectionTitle("IDENTITY")
            OutlinedTextField(
                value = id, 
                onValueChange = { id = it }, 
                label = { Text("Staff ID") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp), 
                enabled = staff == null,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = name, 
                onValueChange = { name = it }, 
                label = { Text("Full Name") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            FormSectionTitle("PAYROLL")
            OutlinedTextField(
                value = salary, 
                onValueChange = { salary = it }, 
                label = { Text("Base Salary (₦)") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = bankName, 
                onValueChange = { bankName = it }, 
                label = { Text("Bank Name") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = accountNumber, 
                onValueChange = { accountNumber = it }, 
                label = { Text("Account Number") }, 
                modifier = Modifier.fillMaxWidth(), 
                shape = RoundedCornerShape(16.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                keyboardActions = KeyboardActions(onDone = { if (id.isNotBlank() && name.isNotBlank()) onSave(Staff(id = id, name = name, role = role, salary = salary.toDoubleOrNull() ?: 0.0, bankName = bankName, accountNumber = accountNumber)) }),
                colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black)
            )

            Spacer(modifier = Modifier.height(48.dp))
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(64.dp), shape = RoundedCornerShape(20.dp)) {
                    Text("BACK", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { if (id.isNotBlank() && name.isNotBlank()) onSave(Staff(id = id, name = name, role = role, salary = salary.toDoubleOrNull() ?: 0.0, bankName = bankName, accountNumber = accountNumber)) }, modifier = Modifier.weight(1.5f).height(64.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF7C3AED))) {
                    Text("COMMIT & NEXT", fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun SignUpScreen(onBack: () -> Unit, onSignUpSuccess: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var staffId by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    val focusManager = LocalFocusManager.current

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp).verticalScroll(rememberScrollState())) {
            IconButton(onClick = onBack) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null) }
            Text("Request Digital ID", fontSize = 32.sp, fontWeight = FontWeight.Black, color = Color(0xFF0D47A1))
            Text("Enter your official details to join the hub", fontSize = 14.sp, color = Color.Gray)
            
            Spacer(modifier = Modifier.height(40.dp))
            
            OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Full Name") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Official Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = staffId, onValueChange = { staffId = it }, label = { Text("Staff ID") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next), keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(value = password, onValueChange = { password = it }, label = { Text("System Access Key") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), visualTransformation = PasswordVisualTransformation(), keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { onSignUpSuccess() }), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
            
            Spacer(modifier = Modifier.height(40.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("BACK")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = onSignUpSuccess, modifier = Modifier.weight(2f).height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0D47A1))) {
                    Text("REQUEST ACCESS", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun ForgotPasswordScreen(onBack: () -> Unit) {
    var email by remember { mutableStateOf("") }
    val context = LocalContext.current

    Box(modifier = Modifier.fillMaxSize().background(Color.White), contentAlignment = Alignment.Center) {
        Column(modifier = Modifier.fillMaxSize().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Row(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = onBack) { Icon(Icons.Default.Close, null) }
            }
            Spacer(modifier = Modifier.height(40.dp))
            Icon(Icons.Default.LockReset, null, modifier = Modifier.size(100.dp), tint = Color(0xFFD32F2F))
            Spacer(modifier = Modifier.height(24.dp))
            Text("Reset Access Key", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Request an OTA reset from school IT", textAlign = TextAlign.Center, color = Color.Gray, modifier = Modifier.padding(horizontal = 32.dp))
            
            Spacer(modifier = Modifier.height(40.dp))
            
            OutlinedTextField(value = email, onValueChange = { email = it }, label = { Text("Official Email") }, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(12.dp), keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Done), keyboardActions = KeyboardActions(onDone = { 
                Toast.makeText(context, "OTA link sent to $email", Toast.LENGTH_LONG).show()
                onBack()
            }), colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.Black, unfocusedTextColor = Color.Black))
            
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(modifier = Modifier.fillMaxWidth()) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f).height(56.dp), shape = RoundedCornerShape(12.dp)) {
                    Text("BACK")
                }
                Spacer(modifier = Modifier.width(16.dp))
                Button(onClick = { 
                    Toast.makeText(context, "OTA link sent to $email", Toast.LENGTH_LONG).show()
                    onBack()
                }, modifier = Modifier.weight(2f).height(56.dp), shape = RoundedCornerShape(12.dp), colors = ButtonDefaults.buttonColors(containerColor = Color.Black)) {
                    Text("SEND RESET LINK")
                }
            }
        }
    }
}
