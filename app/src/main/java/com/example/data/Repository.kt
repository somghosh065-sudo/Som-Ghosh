package com.example.data

import android.content.Context
import android.util.Log
import androidx.room.Room
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.io.File
import com.google.firebase.firestore.DocumentChange

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
            if (FirebaseService.isFirebaseAvailable) {
                setupFirestoreRealtimeSync()
                uploadLocalDataToFirestore()
            }
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
                
                // Save to Firebase Firestore if available
                if (FirebaseService.isFirebaseAvailable) {
                    FirebaseService.firestore?.collection("students")?.document(student.id)?.set(student)
                }
            }
            _currentUser.value = student
        }
        return true
    }

    fun logout() {
        _isAdminLoggedIn.value = false
        _currentUser.value = null
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.auth?.signOut()
        }
    }

    suspend fun updateStudentProfile(profile: StudentProfile) {
        database.studentProfileDao().updateStudent(profile)
        if (_currentUser.value?.id == profile.id) {
            _currentUser.value = profile
        }
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("students")?.document(profile.id)?.set(profile)
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
    suspend fun addCourse(course: Course) {
        database.courseDao().insertCourse(course)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("courses")?.document(course.id)?.set(course)
        }
    }

    suspend fun deleteCourse(id: String) {
        database.courseDao().deleteCourse(id)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("courses")?.document(id)?.delete()
        }
    }

    // Notes
    suspend fun addNote(note: Note) {
        database.noteDao().insertNote(note)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("notes")?.document(note.id)?.set(note)
        }
    }

    suspend fun deleteNote(id: String) {
        database.noteDao().deleteNote(id)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("notes")?.document(id)?.delete()
        }
    }

    suspend fun toggleDownloadNote(id: String, localPath: String): Boolean {
        val allNotes = database.noteDao().getAllNotes().first()
        val note = allNotes.find { it.id == id } ?: return false
        val updatedNote = note.copy(isDownloaded = true, localPath = localPath)
        database.noteDao().updateNote(updatedNote)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("notes")?.document(id)?.set(updatedNote)
        }
        return true
    }

    // Videos
    suspend fun addVideo(video: Video) {
        database.videoDao().insertVideo(video)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("videos")?.document(video.id)?.set(video)
        }
    }

    suspend fun deleteVideo(id: String) {
        database.videoDao().deleteVideo(id)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("videos")?.document(id)?.delete()
        }
    }

    // Live Classes
    suspend fun addLiveClass(liveClass: LiveClass) {
        database.liveClassDao().insertLiveClass(liveClass)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("live_classes")?.document(liveClass.id)?.set(liveClass)
        }
    }

    suspend fun deleteLiveClass(id: String) {
        database.liveClassDao().deleteLiveClass(id)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("live_classes")?.document(id)?.delete()
        }
    }

    // Homeworks
    suspend fun addHomework(homework: Homework) {
        database.homeworkDao().insertHomework(homework)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("homework")?.document(homework.id)?.set(homework)
        }
    }

    suspend fun submitHomework(homeworkId: String, submissionText: String) {
        val allHomework = database.homeworkDao().getAllHomework().first()
        val hw = allHomework.find { it.id == homeworkId } ?: return
        val updatedHw = hw.copy(isSubmitted = true, submissionText = submissionText)
        database.homeworkDao().updateHomework(updatedHw)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("homework")?.document(homeworkId)?.set(updatedHw)
        }
    }

    suspend fun gradeHomework(homeworkId: String, grade: String) {
        val allHomework = database.homeworkDao().getAllHomework().first()
        val hw = allHomework.find { it.id == homeworkId } ?: return
        val updatedHw = hw.copy(grade = grade)
        database.homeworkDao().updateHomework(updatedHw)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("homework")?.document(homeworkId)?.set(updatedHw)
        }
    }

    suspend fun deleteHomework(id: String) {
        database.homeworkDao().deleteHomework(id)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("homework")?.document(id)?.delete()
        }
    }

    // MCQ Tests
    suspend fun addMcqTest(test: McqTest) {
        database.mcqTestDao().insertMcqTest(test)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("mcq_tests")?.document(test.id)?.set(test)
        }
    }

    suspend fun submitTestResult(result: TestResult) {
        database.testResultDao().insertResult(result)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("test_results")?.document(result.id)?.set(result)
        }
    }

    suspend fun deleteMcqTest(id: String) {
        database.mcqTestDao().deleteMcqTest(id)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("mcq_tests")?.document(id)?.delete()
        }
    }

    // Students Management
    suspend fun addStudent(student: StudentProfile) {
        database.studentProfileDao().insertStudent(student)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("students")?.document(student.id)?.set(student)
        }
    }

    suspend fun deleteStudent(id: String) {
        database.studentProfileDao().deleteStudent(id)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("students")?.document(id)?.delete()
        }
    }

    suspend fun recordFeesPayment(studentId: String, amount: Double) {
        val student = database.studentProfileDao().getStudentById(studentId) ?: return
        val newPaid = (student.feesPaid + amount).coerceAtMost(student.feesTotal)
        val updatedStudent = student.copy(feesPaid = newPaid)
        database.studentProfileDao().updateStudent(updatedStudent)
        if (_currentUser.value?.id == studentId) {
            _currentUser.value = updatedStudent
        }
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("students")?.document(studentId)?.set(updatedStudent)
        }
    }

    // Banners
    suspend fun addBanner(banner: Banner) {
        database.bannerDao().insertBanner(banner)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("banners")?.document(banner.id)?.set(banner)
        }
    }

    suspend fun deleteBanner(id: String) {
        database.bannerDao().deleteBanner(id)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("banners")?.document(id)?.delete()
        }
    }

    // Notices
    suspend fun addNotice(notice: Notice) {
        database.noticeDao().insertNotice(notice)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("notices")?.document(notice.id)?.set(notice)
        }
    }

    suspend fun deleteNotice(id: String) {
        database.noticeDao().deleteNotice(id)
        if (FirebaseService.isFirebaseAvailable) {
            FirebaseService.firestore?.collection("notices")?.document(id)?.delete()
        }
    }

    // Real-time Cloud Synchronization
    private fun setupFirestoreRealtimeSync() {
        val db = FirebaseService.firestore ?: return

        db.collection("courses").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in it.documentChanges) {
                        val course = Course(
                            id = doc.document.id,
                            name = doc.document.getString("name") ?: "",
                            description = doc.document.getString("description") ?: ""
                        )
                        when (doc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                database.courseDao().insertCourse(course)
                            }
                            DocumentChange.Type.REMOVED -> {
                                database.courseDao().deleteCourse(course.id)
                            }
                        }
                    }
                }
            }
        }

        db.collection("notices").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in it.documentChanges) {
                        val notice = Notice(
                            id = doc.document.id,
                            title = doc.document.getString("title") ?: "",
                            content = doc.document.getString("content") ?: "",
                            date = doc.document.getString("date") ?: "",
                            isImportant = doc.document.getBoolean("isImportant") ?: false
                        )
                        when (doc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                database.noticeDao().insertNotice(notice)
                            }
                            DocumentChange.Type.REMOVED -> {
                                database.noticeDao().deleteNotice(notice.id)
                            }
                        }
                    }
                }
            }
        }

        db.collection("banners").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in it.documentChanges) {
                        val banner = Banner(
                            id = doc.document.id,
                            imageUrl = doc.document.getString("imageUrl") ?: "",
                            title = doc.document.getString("title") ?: ""
                        )
                        when (doc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                database.bannerDao().insertBanner(banner)
                            }
                            DocumentChange.Type.REMOVED -> {
                                database.bannerDao().deleteBanner(banner.id)
                            }
                        }
                    }
                }
            }
        }

        db.collection("notes").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in it.documentChanges) {
                        val note = Note(
                            id = doc.document.id,
                            title = doc.document.getString("title") ?: "",
                            subject = doc.document.getString("subject") ?: "",
                            classLevel = doc.document.getString("classLevel") ?: "",
                            description = doc.document.getString("description") ?: "",
                            fileUri = doc.document.getString("fileUri") ?: "",
                            isDownloaded = doc.document.getBoolean("isDownloaded") ?: false,
                            localPath = doc.document.getString("localPath") ?: "",
                            timestamp = doc.document.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                        when (doc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                database.noteDao().insertNote(note)
                            }
                            DocumentChange.Type.REMOVED -> {
                                database.noteDao().deleteNote(note.id)
                            }
                        }
                    }
                }
            }
        }

        db.collection("videos").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in it.documentChanges) {
                        val video = Video(
                            id = doc.document.id,
                            title = doc.document.getString("title") ?: "",
                            subject = doc.document.getString("subject") ?: "",
                            classLevel = doc.document.getString("classLevel") ?: "",
                            videoUrl = doc.document.getString("videoUrl") ?: "",
                            isYoutube = doc.document.getBoolean("isYoutube") ?: true,
                            description = doc.document.getString("description") ?: "",
                            timestamp = doc.document.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                        when (doc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                database.videoDao().insertVideo(video)
                            }
                            DocumentChange.Type.REMOVED -> {
                                database.videoDao().deleteVideo(video.id)
                            }
                        }
                    }
                }
            }
        }

        db.collection("live_classes").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in it.documentChanges) {
                        val liveClass = LiveClass(
                            id = doc.document.id,
                            title = doc.document.getString("title") ?: "",
                            subject = doc.document.getString("subject") ?: "",
                            classLevel = doc.document.getString("classLevel") ?: "",
                            joinUrl = doc.document.getString("joinUrl") ?: "",
                            date = doc.document.getString("date") ?: "",
                            time = doc.document.getString("time") ?: "",
                            isCompleted = doc.document.getBoolean("isCompleted") ?: false
                        )
                        when (doc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                database.liveClassDao().insertLiveClass(liveClass)
                            }
                            DocumentChange.Type.REMOVED -> {
                                database.liveClassDao().deleteLiveClass(liveClass.id)
                            }
                        }
                    }
                }
            }
        }

        db.collection("homework").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in it.documentChanges) {
                        val homework = Homework(
                            id = doc.document.id,
                            title = doc.document.getString("title") ?: "",
                            subject = doc.document.getString("subject") ?: "",
                            classLevel = doc.document.getString("classLevel") ?: "",
                            description = doc.document.getString("description") ?: "",
                            dueDate = doc.document.getString("dueDate") ?: "",
                            submissionText = doc.document.getString("submissionText"),
                            isSubmitted = doc.document.getBoolean("isSubmitted") ?: false,
                            grade = doc.document.getString("grade")
                        )
                        when (doc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                database.homeworkDao().insertHomework(homework)
                            }
                            DocumentChange.Type.REMOVED -> {
                                database.homeworkDao().deleteHomework(homework.id)
                            }
                        }
                    }
                }
            }
        }

        db.collection("mcq_tests").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in it.documentChanges) {
                        val test = McqTest(
                            id = doc.document.id,
                            title = doc.document.getString("title") ?: "",
                            subject = doc.document.getString("subject") ?: "",
                            classLevel = doc.document.getString("classLevel") ?: "",
                            durationMinutes = doc.document.getLong("durationMinutes")?.toInt() ?: 15,
                            questionsJson = doc.document.getString("questionsJson") ?: ""
                        )
                        when (doc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                database.mcqTestDao().insertMcqTest(test)
                            }
                            DocumentChange.Type.REMOVED -> {
                                database.mcqTestDao().deleteMcqTest(test.id)
                            }
                        }
                    }
                }
            }
        }

        db.collection("test_results").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in it.documentChanges) {
                        val result = TestResult(
                            id = doc.document.id,
                            testId = doc.document.getString("testId") ?: "",
                            testTitle = doc.document.getString("testTitle") ?: "",
                            studentId = doc.document.getString("studentId") ?: "",
                            studentName = doc.document.getString("studentName") ?: "",
                            score = doc.document.getLong("score")?.toInt() ?: 0,
                            total = doc.document.getLong("total")?.toInt() ?: 0,
                            timestamp = doc.document.getLong("timestamp") ?: System.currentTimeMillis()
                        )
                        when (doc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                database.testResultDao().insertResult(result)
                            }
                            DocumentChange.Type.REMOVED -> {
                                // No action needed or can delete result
                            }
                        }
                    }
                }
            }
        }

        db.collection("students").addSnapshotListener { snapshots, e ->
            if (e != null) return@addSnapshotListener
            snapshots?.let {
                CoroutineScope(Dispatchers.IO).launch {
                    for (doc in it.documentChanges) {
                        val student = StudentProfile(
                            id = doc.document.id,
                            name = doc.document.getString("name") ?: "",
                            email = doc.document.getString("email") ?: "",
                            phone = doc.document.getString("phone") ?: "",
                            rollNo = doc.document.getString("rollNo") ?: "",
                            classLevel = doc.document.getString("classLevel") ?: "Class 10",
                            feesPaid = doc.document.getDouble("feesPaid") ?: 0.0,
                            feesTotal = doc.document.getDouble("feesTotal") ?: 3000.0,
                            progressPercent = doc.document.getLong("progressPercent")?.toInt() ?: 0,
                            isBlocked = doc.document.getBoolean("isBlocked") ?: false
                        )
                        when (doc.type) {
                            DocumentChange.Type.ADDED,
                            DocumentChange.Type.MODIFIED -> {
                                database.studentProfileDao().insertStudent(student)
                                if (_currentUser.value?.id == student.id) {
                                    _currentUser.value = student
                                }
                            }
                            DocumentChange.Type.REMOVED -> {
                                database.studentProfileDao().deleteStudent(student.id)
                            }
                        }
                    }
                }
            }
        }
    }

    private fun uploadLocalDataToFirestore() {
        val db = FirebaseService.firestore ?: return
        db.collection("courses").limit(1).get().addOnSuccessListener { querySnapshot ->
            if (querySnapshot.isEmpty) {
                Log.d("TuitionRepository", "Firestore is empty. Uploading seeded data...")
                CoroutineScope(Dispatchers.IO).launch {
                    val allCourses = database.courseDao().getAllCourses().first()
                    allCourses.forEach { db.collection("courses").document(it.id).set(it) }

                    val allNotices = database.noticeDao().getAllNotices().first()
                    allNotices.forEach { db.collection("notices").document(it.id).set(it) }

                    val allBanners = database.bannerDao().getAllBanners().first()
                    allBanners.forEach { db.collection("banners").document(it.id).set(it) }

                    val allNotes = database.noteDao().getAllNotes().first()
                    allNotes.forEach { db.collection("notes").document(it.id).set(it) }

                    val allVideos = database.videoDao().getAllVideos().first()
                    allVideos.forEach { db.collection("videos").document(it.id).set(it) }

                    val allLiveClasses = database.liveClassDao().getAllLiveClasses().first()
                    allLiveClasses.forEach { db.collection("live_classes").document(it.id).set(it) }

                    val allHomework = database.homeworkDao().getAllHomework().first()
                    allHomework.forEach { db.collection("homework").document(it.id).set(it) }

                    val allTests = database.mcqTestDao().getAllMcqTests().first()
                    allTests.forEach { db.collection("mcq_tests").document(it.id).set(it) }

                    val allStudents = database.studentProfileDao().getAllStudents().first()
                    allStudents.forEach { db.collection("students").document(it.id).set(it) }
                }
            }
        }
    }

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
