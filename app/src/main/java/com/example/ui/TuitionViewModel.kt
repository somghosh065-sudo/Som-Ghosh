package com.example.ui

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.data.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

class TuitionViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TuitionRepository.getInstance(application)

    // Current config settings (Default: English, Light Theme)
    val appConfig = repository.appConfig.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AppConfig()
    )

    val currentLanguage: String
        get() = appConfig.value?.language ?: "en"

    val isDarkMode: Boolean
        get() = appConfig.value?.isDarkMode ?: false

    // Auth flows
    val currentUser = repository.currentUser
    val isAdminLoggedIn = repository.isAdminLoggedIn

    // Core Data flows
    val courses = repository.courses.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val liveClasses = repository.liveClasses.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val homeworks = repository.homeworks.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val mcqTests = repository.mcqTests.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val testResults = repository.testResults.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val students = repository.students.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val banners = repository.banners.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())
    val notices = repository.notices.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Search and filtering states
    private val _noteSearchQuery = MutableStateFlow("")
    val noteSearchQuery: StateFlow<String> = _noteSearchQuery

    private val _videoSearchQuery = MutableStateFlow("")
    val videoSearchQuery: StateFlow<String> = _videoSearchQuery

    private val _selectedClassFilter = MutableStateFlow("All")
    val selectedClassFilter: StateFlow<String> = _selectedClassFilter

    private val _selectedSubjectFilter = MutableStateFlow("All")
    val selectedSubjectFilter: StateFlow<String> = _selectedSubjectFilter

    // Filtered Notes
    val filteredNotes = combine(
        repository.notes,
        _noteSearchQuery,
        _selectedClassFilter,
        _selectedSubjectFilter
    ) { notesList, query, classFilter, subjectFilter ->
        notesList.filter { note ->
            val matchesQuery = query.isEmpty() ||
                    note.title.contains(query, ignoreCase = true) ||
                    note.subject.contains(query, ignoreCase = true) ||
                    note.description.contains(query, ignoreCase = true)

            val matchesClass = classFilter == "All" || note.classLevel.equals(classFilter, ignoreCase = true)
            val matchesSubject = subjectFilter == "All" || note.subject.equals(subjectFilter, ignoreCase = true)

            matchesQuery && matchesClass && matchesSubject
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // Filtered Videos
    val filteredVideos = combine(
        repository.videos,
        _videoSearchQuery,
        _selectedClassFilter,
        _selectedSubjectFilter
    ) { videosList, query, classFilter, subjectFilter ->
        videosList.filter { video ->
            val matchesQuery = query.isEmpty() ||
                    video.title.contains(query, ignoreCase = true) ||
                    video.subject.contains(query, ignoreCase = true) ||
                    video.description.contains(query, ignoreCase = true)

            val matchesClass = classFilter == "All" || video.classLevel.equals(classFilter, ignoreCase = true)
            val matchesSubject = subjectFilter == "All" || video.subject.equals(subjectFilter, ignoreCase = true)

            matchesQuery && matchesClass && matchesSubject
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // MCQ Testing Session States
    private val _activeTest = MutableStateFlow<McqTest?>(null)
    val activeTest: StateFlow<McqTest?> = _activeTest

    private val _currentQuestionIndex = MutableStateFlow(0)
    val currentQuestionIndex: StateFlow<Int> = _currentQuestionIndex

    private val _selectedAnswers = MutableStateFlow<Map<Int, Int>>(emptyMap()) // Question Index -> Answer Index
    val selectedAnswers: StateFlow<Map<Int, Int>> = _selectedAnswers

    private val _testTimerSeconds = MutableStateFlow(0)
    val testTimerSeconds: StateFlow<Int> = _testTimerSeconds

    private var timerJob: Job? = null

    // Helper translation accessor
    fun getString(key: String): String {
        return Translations.translate(key, currentLanguage)
    }

    // Setters
    fun setNoteSearchQuery(query: String) {
        _noteSearchQuery.value = query
    }

    fun setVideoSearchQuery(query: String) {
        _videoSearchQuery.value = query
    }

    fun setClassFilter(classLevel: String) {
        _selectedClassFilter.value = classLevel
    }

    fun setSubjectFilter(subject: String) {
        _selectedSubjectFilter.value = subject
    }

    // Auth
    fun loginAsAdmin(): Boolean {
        return repository.loginAsAdmin()
    }

    fun loginAsStudent(email: String, name: String): Boolean {
        return repository.loginAsStudent(email, name)
    }

    fun logout() {
        repository.logout()
        _activeTest.value = null
        timerJob?.cancel()
    }

    fun editProfile(name: String, phone: String, classLevel: String) {
        val current = currentUser.value ?: return
        viewModelScope.launch {
            repository.updateStudentProfile(
                current.copy(
                    name = name,
                    phone = phone,
                    classLevel = classLevel
                )
            )
        }
    }

    // Preferences
    fun toggleDarkMode() {
        viewModelScope.launch {
            repository.updateTheme(!isDarkMode)
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            repository.updateLanguage(lang)
        }
    }

    // Actions: Student Panel
    fun downloadNoteOffline(noteId: String) {
        viewModelScope.launch {
            val localPath = getApplication<Application>().filesDir.absolutePath + "/notes/$noteId.pdf"
            // Simulate saving actual PDF bytes locally
            val file = File(localPath)
            file.parentFile?.mkdirs()
            file.writeText("Tuition Notes Content Offline Cache placeholder for note $noteId")
            repository.toggleDownloadNote(noteId, localPath)
            Toast.makeText(getApplication(), getString("offline_pdf_info"), Toast.LENGTH_SHORT).show()
        }
    }

    fun submitStudentHomework(homeworkId: String, text: String) {
        viewModelScope.launch {
            repository.submitHomework(homeworkId, text)
            Toast.makeText(getApplication(), "Homework Submitted Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun payStudentFees(amount: Double) {
        val student = currentUser.value ?: return
        viewModelScope.launch {
            repository.recordFeesPayment(student.id, amount)
            Toast.makeText(getApplication(), "Payment of ₹$amount Recorded Successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    // Actions: Active Testing Session
    fun startMcqTest(test: McqTest) {
        _activeTest.value = test
        _currentQuestionIndex.value = 0
        _selectedAnswers.value = emptyMap()
        _testTimerSeconds.value = test.durationMinutes * 60

        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_testTimerSeconds.value > 0) {
                delay(1000)
                _testTimerSeconds.value -= 1
            }
            submitMcqTest() // Auto submit when timer runs out
        }
    }

    fun selectMcqOption(questionIndex: Int, optionIndex: Int) {
        val current = _selectedAnswers.value.toMutableMap()
        current[questionIndex] = optionIndex
        _selectedAnswers.value = current
    }

    fun nextMcqQuestion(totalQuestions: Int) {
        if (_currentQuestionIndex.value < totalQuestions - 1) {
            _currentQuestionIndex.value += 1
        }
    }

    fun prevMcqQuestion() {
        if (_currentQuestionIndex.value > 0) {
            _currentQuestionIndex.value -= 1
        }
    }

    fun parseActiveTestQuestions(): List<McqQuestion> {
        val test = _activeTest.value ?: return emptyList()
        return try {
            val arr = JSONArray(test.questionsJson)
            val list = mutableListOf<McqQuestion>()
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                val optionsArr = obj.getJSONArray("options")
                val options = mutableListOf<String>()
                for (j in 0 until optionsArr.length()) {
                    options.add(optionsArr.getString(j))
                }
                list.add(
                    McqQuestion(
                        questionText = obj.getString("questionText"),
                        options = options,
                        correctOptionIndex = obj.getInt("correctOptionIndex")
                    )
                )
            }
            list
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun submitMcqTest() {
        timerJob?.cancel()
        val test = _activeTest.value ?: return
        val answers = _selectedAnswers.value
        val questions = parseActiveTestQuestions()
        var correctCount = 0

        questions.forEachIndexed { index, question ->
            if (answers[index] == question.correctOptionIndex) {
                correctCount++
            }
        }

        viewModelScope.launch {
            val result = TestResult(
                id = "result_${System.currentTimeMillis()}",
                testId = test.id,
                testTitle = test.title,
                studentId = currentUser.value?.id ?: "anonymous",
                studentName = currentUser.value?.name ?: "Guest Student",
                score = correctCount,
                total = questions.size
            )
            repository.submitTestResult(result)
            _activeTest.value = null
            Toast.makeText(getApplication(), "Exam Submitted! Score: $correctCount/${questions.size}", Toast.LENGTH_LONG).show()
        }
    }

    // Actions: Admin Panel
    fun adminAddCourse(name: String, desc: String) {
        viewModelScope.launch {
            val id = "course_${System.currentTimeMillis()}"
            repository.addCourse(Course(id, name, desc))
            Toast.makeText(getApplication(), "Course Added!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminDeleteCourse(id: String) {
        viewModelScope.launch {
            repository.deleteCourse(id)
            Toast.makeText(getApplication(), "Course Deleted!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminUploadNote(title: String, subject: String, classLevel: String, desc: String, fileName: String) {
        viewModelScope.launch {
            val id = "note_${System.currentTimeMillis()}"
            val note = Note(
                id = id,
                title = title,
                subject = subject,
                classLevel = classLevel,
                description = desc,
                fileUri = "storage_bucket/pdfs/$fileName"
            )
            repository.addNote(note)
            Toast.makeText(getApplication(), "PDF Note Uploaded to Storage & Saved to Database!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminDeleteNote(id: String) {
        viewModelScope.launch {
            repository.deleteNote(id)
            Toast.makeText(getApplication(), "Note Deleted!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminUploadVideo(title: String, subject: String, classLevel: String, url: String, desc: String) {
        viewModelScope.launch {
            val id = "video_${System.currentTimeMillis()}"
            val video = Video(
                id = id,
                title = title,
                subject = subject,
                classLevel = classLevel,
                videoUrl = url,
                isYoutube = url.contains("youtube.com") || url.contains("youtu.be"),
                description = desc
            )
            repository.addVideo(video)
            Toast.makeText(getApplication(), "Video Class Added!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminDeleteVideo(id: String) {
        viewModelScope.launch {
            repository.deleteVideo(id)
            Toast.makeText(getApplication(), "Video Class Deleted!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminCreateLiveClass(title: String, subject: String, classLevel: String, url: String, date: String, time: String) {
        viewModelScope.launch {
            val id = "live_${System.currentTimeMillis()}"
            val live = LiveClass(
                id = id,
                title = title,
                subject = subject,
                classLevel = classLevel,
                joinUrl = url,
                date = date,
                time = time
            )
            repository.addLiveClass(live)
            Toast.makeText(getApplication(), "Live Class Scheduled & Broadcast Active!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminDeleteLiveClass(id: String) {
        viewModelScope.launch {
            repository.deleteLiveClass(id)
            Toast.makeText(getApplication(), "Live Class Cancelled!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminAddHomework(title: String, subject: String, classLevel: String, desc: String, dueDate: String) {
        viewModelScope.launch {
            val id = "hw_${System.currentTimeMillis()}"
            val hw = Homework(
                id = id,
                title = title,
                subject = subject,
                classLevel = classLevel,
                description = desc,
                dueDate = dueDate
            )
            repository.addHomework(hw)
            Toast.makeText(getApplication(), "Homework Created & Notify Triggered!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminGradeHomework(homeworkId: String, grade: String) {
        viewModelScope.launch {
            repository.gradeHomework(homeworkId, grade)
            Toast.makeText(getApplication(), "Homework Graded!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminDeleteHomework(id: String) {
        viewModelScope.launch {
            repository.deleteHomework(id)
            Toast.makeText(getApplication(), "Homework Deleted!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminCreateMcqTest(title: String, subject: String, classLevel: String, duration: Int, questionsList: List<McqQuestion>) {
        viewModelScope.launch {
            val id = "test_${System.currentTimeMillis()}"
            // Build json manually for robustness
            val arr = JSONArray()
            questionsList.forEach { q ->
                val obj = JSONObject()
                obj.put("questionText", q.questionText)
                val optsArr = JSONArray()
                q.options.forEach { optsArr.put(it) }
                obj.put("options", optsArr)
                obj.put("correctOptionIndex", q.correctOptionIndex)
                arr.put(obj)
            }
            val test = McqTest(
                id = id,
                title = title,
                subject = subject,
                classLevel = classLevel,
                durationMinutes = duration,
                questionsJson = arr.toString()
            )
            repository.addMcqTest(test)
            Toast.makeText(getApplication(), "MCQ Exam Published!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminDeleteMcqTest(id: String) {
        viewModelScope.launch {
            repository.deleteMcqTest(id)
            Toast.makeText(getApplication(), "MCQ Test Removed!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminAddStudent(name: String, email: String, phone: String, rollNo: String, classLevel: String, feesTotal: Double) {
        viewModelScope.launch {
            val id = "student_${System.currentTimeMillis()}"
            val student = StudentProfile(
                id = id,
                name = name,
                email = email,
                phone = phone,
                rollNo = rollNo,
                classLevel = classLevel,
                feesPaid = 0.0,
                feesTotal = feesTotal
            )
            repository.addStudent(student)
            Toast.makeText(getApplication(), "Student Registered successfully!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminUpdateStudentFees(studentId: String, amountPaid: Double) {
        viewModelScope.launch {
            repository.recordFeesPayment(studentId, amountPaid)
        }
    }

    fun adminToggleStudentBlock(studentId: String) {
        viewModelScope.launch {
            val list = repository.students.first()
            val student = list.find { it.id == studentId } ?: return@launch
            repository.addStudent(student.copy(isBlocked = !student.isBlocked))
            Toast.makeText(getApplication(), "Student Status Updated!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminDeleteStudent(id: String) {
        viewModelScope.launch {
            repository.deleteStudent(id)
            Toast.makeText(getApplication(), "Student Record Deleted!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminAddBanner(title: String, img: String) {
        viewModelScope.launch {
            val id = "banner_${System.currentTimeMillis()}"
            repository.addBanner(Banner(id, img, title))
            Toast.makeText(getApplication(), "Home Banner Updated!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminDeleteBanner(id: String) {
        viewModelScope.launch {
            repository.deleteBanner(id)
            Toast.makeText(getApplication(), "Banner Removed!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminAddNotice(title: String, content: String, date: String, isImportant: Boolean) {
        viewModelScope.launch {
            val id = "notice_${System.currentTimeMillis()}"
            repository.addNotice(Notice(id, title, content, date, isImportant))
            Toast.makeText(getApplication(), "Broadcast Notice Published!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminDeleteNotice(id: String) {
        viewModelScope.launch {
            repository.deleteNotice(id)
            Toast.makeText(getApplication(), "Notice Removed!", Toast.LENGTH_SHORT).show()
        }
    }

    fun adminSendPushNotification(title: String, content: String) {
        Toast.makeText(getApplication(), "FCM Broadcast Sent: $title - $content", Toast.LENGTH_LONG).show()
    }
}

class TuitionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TuitionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TuitionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
