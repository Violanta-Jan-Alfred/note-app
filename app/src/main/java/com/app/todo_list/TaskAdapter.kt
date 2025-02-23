package com.app.todo_list

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class TaskAdapter(
    private var tasksList: MutableList<Task>,
    private val itemClickListener: (Task) -> Unit
) : RecyclerView.Adapter<TaskAdapter.TaskViewHolder>() {

    private var tasksListFiltered: MutableList<Task> = tasksList

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasksListFiltered[position]
        holder.bind(task)
    }

    override fun getItemCount(): Int {
        return tasksListFiltered.size
    }

    @SuppressLint("NotifyDataSetChanged")
    fun filter(query: String) {
        tasksListFiltered = if (query.isEmpty()) {
            tasksList
        } else {
            tasksList.filter { task ->
                task.title.contains(query, ignoreCase = true) ||
                        task.startDate.contains(query, ignoreCase = true) ||
                        task.endDate.contains(query, ignoreCase = true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTitle: TextView = itemView.findViewById(R.id.txtTitle)
        private val txtStartDate: TextView = itemView.findViewById(R.id.txtStartDate)
        private val txtEndDate: TextView = itemView.findViewById(R.id.txtEndDate)
        private val checkBox: CheckBox = itemView.findViewById(R.id.taskCheckBox)
        private val imgTaskDone: ImageView = itemView.findViewById(R.id.imgDone)

        fun bind(task: Task) {
            txtTitle.text = task.title
            txtStartDate.text = task.startDate
            txtEndDate.text = task.endDate
            checkBox.isChecked = task.isChecked

            updateCardViewAppearance(task.isChecked)

            checkBox.setOnCheckedChangeListener { _, isChecked ->
                task.isChecked = isChecked
                updateCardViewAppearance(isChecked)

                val database = Database(itemView.context)
                database.updateTask(task.id, task.startDate, task.endDate, task.title, isChecked)
            }

            itemView.setOnClickListener { itemClickListener(task) }
        }

        private fun updateCardViewAppearance(isChecked: Boolean) {
            if (isChecked) {
                imgTaskDone.visibility = View.VISIBLE
                val gifResource = if (Math.random() < 0.5) {
                    R.drawable.done_task
                } else {
                    R.drawable.done_task_two
                }
                Glide.with(itemView.context)
                    .asGif()
                    .load(gifResource)
                    .into(imgTaskDone)
            } else {
                imgTaskDone.visibility = View.GONE
            }
            setTextStrikeThrough(isChecked)
        }

        private fun setTextStrikeThrough(isChecked: Boolean) {
            txtTitle.paintFlags = if (isChecked) {
                txtTitle.paintFlags or android.graphics.Paint.STRIKE_THRU_TEXT_FLAG
            } else {
                txtTitle.paintFlags and android.graphics.Paint.STRIKE_THRU_TEXT_FLAG.inv()
            }
        }
    }

    fun removeTask(task: Task) {
        val position = tasksList.indexOf(task)
        if (position != -1) {
            tasksList.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}
