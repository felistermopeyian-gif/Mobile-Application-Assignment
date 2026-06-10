package com.example.studentmanagementapp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class StorageManager(private val context: Context) {

    private val studentsFileName = "students.json"
    private val staffFileName = "staff.json"

    // --- Student Methods ---
    fun saveStudents(students: List<Student>) {
        val jsonArray = JSONArray()
        students.forEach { student ->
            val obj = JSONObject()
            obj.put("id", student.id)
            obj.put("name", student.name)
            obj.put("grade", student.grade)
            obj.put("gender", student.gender)
            obj.put("guardianName", student.guardianName)
            obj.put("guardianPhone", student.guardianPhone)
            obj.put("feesPaid", student.feesPaid)
            obj.put("feesTotal", student.feesTotal)
            obj.put("email", student.email)
            obj.put("admissionDate", student.admissionDate)
            obj.put("status", student.status)
            obj.put("attendanceToday", student.attendanceToday)
            
            val scoresArray = JSONArray()
            student.scores.forEach { score ->
                val sObj = JSONObject()
                sObj.put("subject", score.subject)
                sObj.put("score", score.score)
                scoresArray.put(sObj)
            }
            obj.put("scores", scoresArray)
            
            jsonArray.put(obj)
        }
        context.openFileOutput(studentsFileName, Context.MODE_PRIVATE).use {
            it.write(jsonArray.toString().toByteArray())
        }
    }

    fun addStudent(student: Student) {
        val students = loadStudents().toMutableList()
        students.add(student)
        saveStudents(students)
    }

    fun updateStudent(updatedStudent: Student) {
        val students = loadStudents().toMutableList()
        val index = students.indexOfFirst { it.id == updatedStudent.id }
        if (index != -1) {
            students[index] = updatedStudent
            saveStudents(students)
        }
    }

    fun deleteStudent(studentId: String) {
        val students = loadStudents().toMutableList()
        if (students.removeAll { it.id == studentId }) {
            saveStudents(students)
        }
    }

    fun loadStudents(): List<Student> {
        val file = context.getFileStreamPath(studentsFileName)
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readBytes().toString(Charsets.UTF_8)
            val jsonArray = JSONArray(jsonString)
            val students = mutableListOf<Student>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                val scores = mutableListOf<SubjectScore>()
                val scoresArray = obj.optJSONArray("scores")
                if (scoresArray != null) {
                    for (j in 0 until scoresArray.length()) {
                        val sObj = scoresArray.getJSONObject(j)
                        scores.add(SubjectScore(sObj.getString("subject"), sObj.getInt("score")))
                    }
                }
                
                students.add(Student(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    grade = obj.optString("grade", ""),
                    gender = obj.optString("gender", "Male"),
                    guardianName = obj.optString("guardianName", ""),
                    guardianPhone = obj.optString("guardianPhone", ""),
                    feesPaid = obj.optDouble("feesPaid", 0.0),
                    feesTotal = obj.optDouble("feesTotal", 0.0),
                    email = obj.optString("email", ""),
                    admissionDate = obj.optLong("admissionDate", System.currentTimeMillis()),
                    status = obj.optString("status", "Active"),
                    attendanceToday = obj.optBoolean("attendanceToday", false),
                    scores = scores
                ))
            }
            students
        } catch (e: Exception) {
            emptyList()
        }
    }

    // --- Staff Methods ---
    fun saveStaff(staffList: List<Staff>) {
        val jsonArray = JSONArray()
        staffList.forEach { staff ->
            val obj = JSONObject()
            obj.put("id", staff.id)
            obj.put("name", staff.name)
            obj.put("role", staff.role)
            obj.put("email", staff.email)
            obj.put("phone", staff.phone)
            obj.put("salary", staff.salary)
            obj.put("bonus", staff.bonus)
            obj.put("deductions", staff.deductions)
            obj.put("paymentStatus", staff.paymentStatus)
            obj.put("bankName", staff.bankName)
            obj.put("accountNumber", staff.accountNumber)
            obj.put("joinDate", staff.joinDate)
            obj.put("attendanceToday", staff.attendanceToday)
            obj.put("bio", staff.bio)
            jsonArray.put(obj)
        }
        context.openFileOutput(staffFileName, Context.MODE_PRIVATE).use {
            it.write(jsonArray.toString().toByteArray())
        }
    }

    fun addStaff(staff: Staff) {
        val staffList = loadStaff().toMutableList()
        staffList.add(staff)
        saveStaff(staffList)
    }

    fun updateStaff(updatedStaff: Staff) {
        val staffList = loadStaff().toMutableList()
        val index = staffList.indexOfFirst { it.id == updatedStaff.id }
        if (index != -1) {
            staffList[index] = updatedStaff
            saveStaff(staffList)
        }
    }

    fun deleteStaff(staffId: String) {
        val staffList = loadStaff().toMutableList()
        if (staffList.removeAll { it.id == staffId }) {
            saveStaff(staffList)
        }
    }

    fun loadStaff(): List<Staff> {
        val file = context.getFileStreamPath(staffFileName)
        if (!file.exists()) return emptyList()
        return try {
            val jsonString = file.readBytes().toString(Charsets.UTF_8)
            val jsonArray = JSONArray(jsonString)
            val staffList = mutableListOf<Staff>()
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                staffList.add(Staff(
                    id = obj.getString("id"),
                    name = obj.getString("name"),
                    role = obj.getString("role"),
                    email = obj.optString("email", ""),
                    phone = obj.optString("phone", ""),
                    salary = obj.optDouble("salary", 0.0),
                    bonus = obj.optDouble("bonus", 0.0),
                    deductions = obj.optDouble("deductions", 0.0),
                    paymentStatus = obj.optString("paymentStatus", "Unpaid"),
                    bankName = obj.optString("bankName", ""),
                    accountNumber = obj.optString("accountNumber", ""),
                    joinDate = obj.optLong("joinDate", System.currentTimeMillis()),
                    attendanceToday = obj.optBoolean("attendanceToday", false),
                    bio = obj.optString("bio", "Dedicated educator at Osuyai Academy.")
                ))
            }
            staffList
        } catch (e: Exception) {
            emptyList()
        }
    }
}
