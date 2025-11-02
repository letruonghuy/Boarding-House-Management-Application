package com.example.qunlphngtr.dao

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteException
import com.example.qunlphngtr.database.DatabaseHelper
import com.example.qunlphngtr.model.User

class UserDao(context: Context) {
    private val dbHelper = DatabaseHelper(context)

    /**
     * Dùng để đăng ký tài khoản mới
     * Trả về ID của user mới, hoặc -1 nếu thất bại (ví dụ: username đã tồn tại)
     */
    fun insertUser(user: User): Long {
        val db = dbHelper.writableDatabase
        val values = ContentValues().apply {
            put("username", user.username)
            put("password", user.password) // LƯU Ý: Trong app thực tế, bạn PHẢI mã hóa (hash) mật khẩu này
            put("role", user.role)
        }

        var id: Long = -1
        try {
            id = db.insertOrThrow("User", null, values)
        } catch (e: SQLiteException) {
            // Lỗi có thể xảy ra do vi phạm ràng buộc UNIQUE (username đã tồn tại)
            e.printStackTrace()
        } finally {
            db.close()
        }
        return id
    }

    /**
     * Dùng để kiểm tra đăng nhập
     * Trả về object User nếu thành công, hoặc null nếu sai thông tin
     */
    fun checkLogin(username: String, password: String): User? {
        val db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM User WHERE username = ? AND password = ?",
            arrayOf(username, password)
        )

        var user: User? = null
        if (cursor.moveToFirst()) {
            user = User(
                id = cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                username = cursor.getString(cursor.getColumnIndexOrThrow("username")),
                password = cursor.getString(cursor.getColumnIndexOrThrow("password")),
                role = cursor.getString(cursor.getColumnIndexOrThrow("role"))
            )
        }

        cursor.close()
        db.close()
        return user
    }
}