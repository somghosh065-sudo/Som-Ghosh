package com.example.data

import android.content.Context
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File

class TuitionRepository private constructor(private val context: Context) {

    private val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "tuition_classes_db"
    ).fallbackToDestructiveMigration().build()

    // Exposed Flows
    val courses: Flow<List<Course>> = database.courseDao().getAllCourses()
    val notes: Flow<List<Note>> = database.noteDao().getAllNotes()
    val videos: Flow<List<Video>> = database.videoDao().getAllVideos()
    val liveClasses: Flow<List<LiveClass>> = database.liveClassDao().getAllLiveClasses()
    val homeworks: Flow<List<Homework>> = database.homeworkDao().getAllHomework()
    val mcqTests: Flow<List<McqTest>> = database.mcqTestDao().getAllMcqTests()
    val testResults: Flow<List<TestResult>> = database.testResultDao().getAllResults()
    val students: Flow<List<StudentProfile>> = database.studentProfileDao().getAllStudents()
    val banners: Flow<List<Banner>> = database.bannerDao().getAllBanners()
    val notices: Flow<List<Notice>> = database.noticeDao().getAllNotices()
    val appConfig: Flow<AppConfig?> = database.appConfigDao().getConfig()

    // Authentication States
    private val _currentUser = MutableStateFlow<StudentProfile?>(null)
    val currentUser: StateFlow<StudentProfile?> = _currentUser

    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn

    init {
        CoroutineScope(Dispatchers.IO).launch {
            seedInitialData()
        }
    }

    // Authentication & User State
    fun loginAsAdmin(): Boolean {
        _isAdminLoggedIn.value = true
        _currentUser.value = null
        return true
    }

    fun loginAsStudent(email: String, name: String = "Test Student"): Boolean {
        _isAdminLoggedIn.value = false
        CoroutineScope(Dispatchers.IO).launch {
            // Find student by email or create a dummy one
            val allStudents = database.studentProfileDao().getAllStudents().first()
            var student = allStudents.find { it.email.equals(email, ignoreCase = true) }
            if (student == null) {
                student = StudentProfile(
                    id = "student_${System.currentTimeMillis()}",
                    name = name,
                    email = email,
                    phone = "+91 98765 43210",
                    rollNo = "R-${100 + allStudents.size}",
                    classLevel = "Class 10",
                    feesPaid = 1200.0,
                    feesTotal = 3000.0,
                    progressPercent = 75
                )
                database.studentProfileDao().insertStudent(student)
            }
            _currentUser.value = student
        }
        return true
    }

    fun logout() {
        _isAdminLoggedIn.value = false
        _currentUser.value = null
    }

    suspend fun updateStudentProfile(profile: StudentProfile) {
        database.studentProfileDao().updateStudent(profile)
        if (_currentUser.value?.id == profile.id) {
            _currentUser.value = profile
        }
    }

    // App Preferences / Settings
    suspend fun updateLanguage(lang: String) {
        val current = appConfig.first() ?: AppConfig()
        database.appConfigDao().insertConfig(current.copy(language = lang))
    }

    suspend fun updateTheme(isDark: Boolean) {
        val current = appConfig.first() ?: AppConfig()
        database.appConfigDao().insertConfig(current.copy(isDarkMode = isDark))
    }

    // Courses
    suspend fun addCourse(course: Course) = database.courseDao().insertCourse(course)
    suspend fun deleteCourse(id: String) = database.courseDao().deleteCourse(id)

    // Notes
    suspend fun addNote(note: Note) = database.noteDao().insertNote(note)
    suspend fun deleteNote(id: String) = database.noteDao().deleteNote(id)
    suspend fun toggleDownloadNote(id: String, localPath: String): Boolean {
        val allNotes = database.noteDao().getAllNotes().first()
        val note = allNotes.find { it.id == id } ?: return false
        database.noteDao().updateNote(note.copy(isDownloaded = true, localPath = localPath))
        return true
    }

    // Videos
    suspend fun addVideo(video: Video) = database.videoDao().insertVideo(video)
    suspend fun deleteVideo(id: String) = database.videoDao().deleteVideo(id)

    // Live Classes
    suspend fun addLiveClass(liveClass: LiveClass) = database.liveClassDao().insertLiveClass(liveClass)
    suspend fun deleteLiveClass(id: String) = database.liveClassDao().deleteLiveClass(id)

    // Homeworks
    suspend fun addHomework(homework: Homework) = database.homeworkDao().insertHomework(homework)
    suspend fun submitHomework(homeworkId: String, submissionText: String) {
        val allHomework = database.homeworkDao().getAllHomework().first()
        val hw = allHomework.find { it.id == homeworkId } ?: return
        database.homeworkDao().updateHomework(
            hw.copy(
                isSubmitted = true,
                submissionText = submissionText
            )
        )
    }
    suspend fun gradeHomework(homeworkId: String, grade: String) {
        val allHomework = database.homeworkDao().getAllHomework().first()
        val hw = allHomework.find { it.id == homeworkId } ?: return
        database.homeworkDao().updateHomework(hw.copy(grade = grade))
    }
    suspend fun deleteHomework(id: String) = database.homeworkDao().deleteHomework(id)

    // MCQ Tests
    suspend fun addMcqTest(test: McqTest) = database.mcqTestDao().insertMcqTest(test)
    suspend fun submitTestResult(result: TestResult) = database.testResultDao().insertResult(result)
    suspend fun deleteMcqTest(id: String) = database.mcqTestDao().deleteMcqTest(id)

    // Students Management
    suspend fun addStudent(student: StudentProfile) = database.studentProfileDao().insertStudent(student)
    suspend fun deleteStudent(id: String) = database.studentProfileDao().deleteStudent(id)
    suspend fun recordFeesPayment(studentId: String, amount: Double) {
        val student = database.studentProfileDao().getStudentById(studentId) ?: return
        val newPaid = (student.feesPaid + amount).coerceAtMost(student.feesTotal)
        database.studentProfileDao().updateStudent(student.copy(feesPaid = newPaid))
        if (_currentUser.value?.id == studentId) {
            _currentUser.value = student.copy(feesPaid = newPaid)
        }
    }

    // Banners
    suspend fun addBanner(banner: Banner) = database.bannerDao().insertBanner(banner)
    suspend fun deleteBanner(id: String) = database.bannerDao().deleteBanner(id)

    // Notices
    suspend fun addNotice(notice: Notice) = database.noticeDao().insertNotice(notice)
    suspend fun deleteNotice(id: String) = database.noticeDao().deleteNotice(id)

    // Private helpers
    private suspend fun seedInitialData() {
        // App Preferences
        val currentConfig = database.appConfigDao().getConfig().first()
        if (currentConfig == null) {
            database.appConfigDao().insertConfig(AppConfig(language = "en", isDarkMode = false))
        }

        // Only seed if empty
        val existingCourses = database.courseDao().getAllCourses().first()
        if (existingCourses.isNotEmpty()) return

        // 1. Seed Courses
        val sampleCourses = listOf(
            Course("c6", "Class 6", "General Tuition Batch"),
            Course("c7", "Class 7", "General Tuition Batch"),
            Course("c8", "Class 8", "High School Foundation Batch"),
            Course("c9", "Class 9", "Secondary Pre-Board Preparation"),
            Course("c10", "Class 10", "Secondary Board Exam Batch")
        )
        sampleCourses.forEach { database.courseDao().insertCourse(it) }

        // 2. Seed Notices
        val sampleNotices = listOf(
            Notice("n1", "Summer Vacation Extra Class Schedule", "Special mathematics classes will be conducted during morning hours 8 AM to 10 AM starting Monday.", "2026-07-16", true),
            Notice("n2", "Offline Mock Exam Announcement", "Offline mock tests for Mathematics and Physical Science will take place on Sunday at 10:00 AM.", "2026-07-15", false)
        )
        sampleNotices.forEach { database.noticeDao().insertNotice(it) }

        // 3. Seed Banners
        val sampleBanners = listOf(
            Banner("b1", "img_tuition_hero", "Join Board Preparation Batch 2026!"),
            Banner("b2", "img_tuition_hero", "Weekly MCQ Practice Sessions Active")
        )
        sampleBanners.forEach { database.bannerDao().insertBanner(it) }

        // 4. Seed Notes
        val sampleNotes = listOf(
            Note("nt1", "Class 10 Trigonometry Formula Sheet", "Mathematics", "Class 10", "Comprehensive PDF containing all important trigonometric formulas, equations and identity proofs.", "pdf_trig_formulas", false),
            Note("nt2", "Class 10 Newton's Laws of Motion", "Physical Science", "Class 10", "Complete lecture notes on force, inertia, and practical numerical exercises.", "pdf_newton_laws", false),
            Note("nt3", "Class 9 English Poem Summary - The Brook", "English", "Class 9", "Stanza by stanza analysis, literary devices, and important questions and answers.", "pdf_brook_summary", false),
            Note("nt4", "Class 10 Respiration Study Guide", "Life Science", "Class 10", "Important diagrams of human respiration, anaerobic processes, and previous year board questions.", "pdf_respiration", false)
        )
        sampleNotes.forEach { database.noteDao().insertNote(it) }

        // 5. Seed Videos
        val sampleVideos = listOf(
            Video("v1", "Trigonometric Identities Made Easy", "Mathematics", "Class 10", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", true, "Step-by-step video solving critical board exam questions on identities."),
            Video("v2", "Understanding Chemical Equations", "Physical Science", "Class 10", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", true, "Simple tricks to balance organic and inorganic chemical reactions."),
            Video("v3", "Indian Rivers & Drainage Systems", "Geography", "Class 9", "https://www.youtube.com/watch?v=dQw4w9WgXcQ", true, "High definition animated map tracing major rivers of India.")
        )
        sampleVideos.forEach { database.videoDao().insertVideo(it) }

        // 6. Seed Live Classes
        val sampleLiveClasses = listOf(
            LiveClass("lc1", "Quadratic Equations Live Doubt Session", "Mathematics", "Class 10", "https://meet.google.com/abc-defg-hij", "2026-07-18", "11:00 AM"),
            LiveClass("lc2", "Structure of Atom Interactive Session", "Physical Science", "Class 9", "https://meet.google.com/abc-defg-hij", "2026-07-19", "04:30 PM")
        )
        sampleLiveClasses.forEach { database.liveClassDao().insertLiveClass(it) }

        // 7. Seed Homework
        val sampleHomeworks = listOf(
            Homework("h1", "Trigonometry Ex 8.4 Solved Submissions", "Mathematics", "Class 10", "Solve Questions 1 to 5 from NCERT exercise 8.4 on neat pages and upload submit here.", "2026-07-20"),
            Homework("h2", "Balance the Chemical Equations", "Physical Science", "Class 10", "Balance the 10 chemical equations written in chapter 2 exercise questions and write their reaction types.", "2026-07-22")
        )
        sampleHomeworks.forEach { database.homeworkDao().insertHomework(it) }

        // 8. Seed McqTests
        val sampleTests = listOf(
            McqTest(
                id = "t1",
                title = "Class 10 Chapter 1 Mathematics Quiz",
                subject = "Mathematics",
                classLevel = "Class 10",
                durationMinutes = 15,
                questionsJson = """
                    [
                        {"questionText": "What is the HCF of two consecutive even integers?", "options": ["1", "2", "4", "None"], "correctOptionIndex": 1},
                        {"questionText": "The product of a non-zero rational and an irrational number is:", "options": ["Always irrational", "Always rational", "Rational or irrational", "One"], "correctOptionIndex": 0},
                        {"questionText": "If one zero of the quadratic polynomial x² + 3x + k is 2, then the value of k is:", "options": ["10", "-10", "-7", "-2"], "correctOptionIndex": 1}
                    ]
                """.trimIndent()
            ),
            McqTest(
                id = "t2",
                title = "Class 10 Metals and Non-Metals Quiz",
                subject = "Physical Science",
                classLevel = "Class 10",
                durationMinutes = 10,
                questionsJson = """
                    [
                        {"questionText": "Which metal is liquid at room temperature?", "options": ["Sodium", "Mercury", "Gallium", "Cesium"], "correctOptionIndex": 1},
                        {"questionText": "Which non-metal is a good conductor of electricity?", "options": ["Graphite", "Diamond", "Sulfur", "Phosphorus"], "correctOptionIndex": 0}
                    ]
                """.trimIndent()
            )
        )
        sampleTests.forEach { database.mcqTestDao().insertMcqTest(it) }

        // 9. Seed Students
        val sampleStudents = listOf(
            StudentProfile("s1", "Rahul Sharma", "rahul@gmail.com", "+91 99112 23344", "R-101", "Class 10", 1500.0, 3000.0, 82),
            StudentProfile("s2", "Priya Das", "priya@gmail.com", "+91 99334 45566", "R-102", "Class 10", 3000.0, 3000.0, 95),
            StudentProfile("s3", "Sourav Ghosh", "somghosh065@gmail.com", "+91 98300 12345", "R-103", "Class 10", 1200.0, 3000.0, 88),
            StudentProfile("s4", "Ananya Roy", "ananya@gmail.com", "+91 98311 22334", "R-104", "Class 9", 800.0, 2500.0, 64)
        )
        sampleStudents.forEach { database.studentProfileDao().insertStudent(it) }
    }

    companion object {
        @Volatile
        private var instance: TuitionRepository? = null

        fun getInstance(context: Context): TuitionRepository {
            return instance ?: synchronized(this) {
                instance ?: TuitionRepository(context).also { instance = it }
            }
        }
    }
}
