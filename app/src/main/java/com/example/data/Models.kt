package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey val id: String,
    val name: String,
    val description: String
)

@Entity(tableName = "notes")
data class Note(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val classLevel: String,
    val description: String,
    val fileUri: String,
    val isDownloaded: Boolean = false,
    val localPath: String = "",
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "videos")
data class Video(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val classLevel: String,
    val videoUrl: String,
    val isYoutube: Boolean = true,
    val description: String,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "live_classes")
data class LiveClass(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val classLevel: String,
    val joinUrl: String,
    val date: String,
    val time: String,
    val isCompleted: Boolean = false
)

@Entity(tableName = "homework")
data class Homework(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val classLevel: String,
    val description: String,
    val dueDate: String,
    val submissionText: String? = null,
    val isSubmitted: Boolean = false,
    val grade: String? = null
)

@Entity(tableName = "mcq_tests")
data class McqTest(
    @PrimaryKey val id: String,
    val title: String,
    val subject: String,
    val classLevel: String,
    val durationMinutes: Int,
    val questionsJson: String // Serialized list of McqQuestion
)

data class McqQuestion(
    val questionText: String,
    val options: List<String>,
    val correctOptionIndex: Int
)

@Entity(tableName = "test_results")
data class TestResult(
    @PrimaryKey val id: String,
    val testId: String,
    val testTitle: String,
    val studentId: String,
    val studentName: String,
    val score: Int,
    val total: Int,
    val timestamp: Long = System.currentTimeMillis()
)

@Entity(tableName = "students")
data class StudentProfile(
    @PrimaryKey val id: String,
    val name: String,
    val email: String,
    val phone: String,
    val rollNo: String,
    val classLevel: String,
    val feesPaid: Double,
    val feesTotal: Double,
    val progressPercent: Int = 0,
    val isBlocked: Boolean = false
)

@Entity(tableName = "banners")
data class Banner(
    @PrimaryKey val id: String,
    val imageUrl: String,
    val title: String
)

@Entity(tableName = "notices")
data class Notice(
    @PrimaryKey val id: String,
    val title: String,
    val content: String,
    val date: String,
    val isImportant: Boolean = false
)

@Entity(tableName = "app_config")
data class AppConfig(
    @PrimaryKey val id: String = "current",
    val language: String = "en", // "en" or "bn"
    val isDarkMode: Boolean = false
)
