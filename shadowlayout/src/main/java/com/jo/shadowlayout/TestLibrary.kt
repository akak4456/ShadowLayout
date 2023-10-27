package com.jo.shadowlayout

import android.content.Context
import android.widget.Toast

object TestLibrary {
    fun showToast(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}