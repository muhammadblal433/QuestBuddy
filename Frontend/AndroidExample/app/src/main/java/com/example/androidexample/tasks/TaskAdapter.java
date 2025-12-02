package com.example.androidexample.tasks;
import com.example.androidexample.R;

import android.content.Context;
import android.widget.Toast;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.example.androidexample.R;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

//adapter class that connects task data to recycler view
public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList; // display the tasks
    private Context context;
    private TaskManagerActivity activity; // to access update/delete methods

    public TaskAdapter(Context context, List<Task> taskList, TaskManagerActivity activity) {
        this.context = context;
        this.taskList = taskList;
        this.activity = activity;
    }

    // creates the layout for each task card
    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    // binds the task data to the layout elemetns
    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);

        holder.tvTitle.setText(task.getTitle());
        holder.tvDescription.setText(task.getDescription());
        holder.tvStatus.setText("Status: " + task.getStatus());
        holder.tvDueDate.setText("Due: " + task.getDueDate());

        holder.btnEdit.setOnClickListener(v -> {
            activity.updateTask(task.getTaskId(), task.getTitle(), "In Progress", task.getDueDate());
            Toast.makeText(context, "Updating task " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });


        holder.btnDelete.setOnClickListener(v -> {
            activity.deleteTask(task.getTaskId());
            Toast.makeText(context, "Deleting task " + task.getTitle(), Toast.LENGTH_SHORT).show();
        });

        holder.btnEdit.setOnClickListener(v -> {
            activity.showEditDialog(task);
        });
    }

    //returns the number of tasks created
    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvDescription, tvStatus, tvDueDate;
        Button btnEdit, btnDelete;

        //connect the xml files to the java objects
        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            tvDescription = itemView.findViewById(R.id.tvDescription);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvDueDate = itemView.findViewById(R.id.tvDueDate);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}

