package com.example.data

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses")
    fun getAllCourses(): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: Course)

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourse(id: String)
}

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes ORDER BY timestamp DESC")
    fun getAllNotes(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE classLevel = :classLevel ORDER BY timestamp DESC")
    fun getNotesByClass(classLevel: String): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: Note)

    @Update
    suspend fun updateNote(note: Note)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNote(id: String)
}

@Dao
interface VideoDao {
    @Query("SELECT * FROM videos ORDER BY timestamp DESC")
    fun getAllVideos(): Flow<List<Video>>

    @Query("SELECT * FROM videos WHERE classLevel = :classLevel ORDER BY timestamp DESC")
    fun getVideosByClass(classLevel: String): Flow<List<Video>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideo(video: Video)

    @Query("DELETE FROM videos WHERE id = :id")
    suspend fun deleteVideo(id: String)
}

@Dao
interface LiveClassDao {
    @Query("SELECT * FROM live_classes ORDER BY id DESC")
    fun getAllLiveClasses(): Flow<List<LiveClass>>

    @Query("SELECT * FROM live_classes WHERE classLevel = :classLevel AND isCompleted = 0")
    fun getLiveClassesByClass(classLevel: String): Flow<List<LiveClass>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLiveClass(liveClass: LiveClass)

    @Query("DELETE FROM live_classes WHERE id = :id")
    suspend fun deleteLiveClass(id: String)
}

@Dao
interface HomeworkDao {
    @Query("SELECT * FROM homework ORDER BY id DESC")
    fun getAllHomework(): Flow<List<Homework>>

    @Query("SELECT * FROM homework WHERE classLevel = :classLevel ORDER BY id DESC")
    fun getHomeworkByClass(classLevel: String): Flow<List<Homework>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHomework(homework: Homework)

    @Update
    suspend fun updateHomework(homework: Homework)

    @Query("DELETE FROM homework WHERE id = :id")
    suspend fun deleteHomework(id: String)
}

@Dao
interface McqTestDao {
    @Query("SELECT * FROM mcq_tests ORDER BY id DESC")
    fun getAllMcqTests(): Flow<List<McqTest>>

    @Query("SELECT * FROM mcq_tests WHERE classLevel = :classLevel")
    fun getMcqTestsByClass(classLevel: String): Flow<List<McqTest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMcqTest(test: McqTest)

    @Query("DELETE FROM mcq_tests WHERE id = :id")
    suspend fun deleteMcqTest(id: String)
}

@Dao
interface TestResultDao {
    @Query("SELECT * FROM test_results ORDER BY timestamp DESC")
    fun getAllResults(): Flow<List<TestResult>>

    @Query("SELECT * FROM test_results WHERE studentId = :studentId ORDER BY timestamp DESC")
    fun getResultsByStudent(studentId: String): Flow<List<TestResult>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResult(result: TestResult)
}

@Dao
interface StudentProfileDao {
    @Query("SELECT * FROM students")
    fun getAllStudents(): Flow<List<StudentProfile>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getStudentById(id: String): StudentProfile?

    @Query("SELECT * FROM students WHERE id = :id")
    fun getStudentFlow(id: String): Flow<StudentProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudent(student: StudentProfile)

    @Update
    suspend fun updateStudent(student: StudentProfile)

    @Query("DELETE FROM students WHERE id = :id")
    suspend fun deleteStudent(id: String)
}

@Dao
interface BannerDao {
    @Query("SELECT * FROM banners")
    fun getAllBanners(): Flow<List<Banner>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBanner(banner: Banner)

    @Query("DELETE FROM banners WHERE id = :id")
    suspend fun deleteBanner(id: String)
}

@Dao
interface NoticeDao {
    @Query("SELECT * FROM notices ORDER BY date DESC")
    fun getAllNotices(): Flow<List<Notice>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotice(notice: Notice)

    @Query("DELETE FROM notices WHERE id = :id")
    suspend fun deleteNotice(id: String)
}

@Dao
interface AppConfigDao {
    @Query("SELECT * FROM app_config WHERE id = 'current'")
    fun getConfig(): Flow<AppConfig?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertConfig(config: AppConfig)
}

@Database(
    entities = [
        Course::class,
        Note::class,
        Video::class,
        LiveClass::class,
        Homework::class,
        McqTest::class,
        TestResult::class,
        StudentProfile::class,
        Banner::class,
        Notice::class,
        AppConfig::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun courseDao(): CourseDao
    abstract fun noteDao(): NoteDao
    abstract fun videoDao(): VideoDao
    abstract fun liveClassDao(): LiveClassDao
    abstract fun homeworkDao(): HomeworkDao
    abstract fun mcqTestDao(): McqTestDao
    abstract fun testResultDao(): TestResultDao
    abstract fun studentProfileDao(): StudentProfileDao
    abstract fun bannerDao(): BannerDao
    abstract fun noticeDao(): NoticeDao
    abstract fun appConfigDao(): AppConfigDao
}
