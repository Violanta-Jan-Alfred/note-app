package com.app.todo_list

data class Task(
    val id: Int,
    val startDate: String,
    var endDate: String,
    var title: String,
    var isChecked: Boolean
)
