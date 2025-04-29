package com.example.AWMI.service.impl;

import com.example.AWMI.entity.Task;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TaskService {
    private final Map<String, Task> tasks = new ConcurrentHashMap<>();

    public String createTask() {
        String taskId = "TASK-" + System.currentTimeMillis();
        tasks.put(taskId, new Task(taskId));
        return taskId;
    }

    public Task getTask(String id) {
        return tasks.get(id);
    }

    public void updateResult(String id, String result) {
        Task task = tasks.get(id);
        if(task != null) {
            task.setResult(result);
            System.out.println("正在更新， task != null");
            task.setStatus(Task.Status.DONE);
        }
    }

    // 每5分钟清理一次已完成的任务
    @Scheduled(fixedRate =5 * 60 * 1000)
    public void autoCleanTasks() {
        int initialSize = tasks.size();
        tasks.entrySet().removeIf(entry ->
                Task.Status.DONE.equals(entry.getValue().getStatus())
        );
        System.out.printf("已清理任务：%d → %d（删除 %d 个）%n",
                initialSize, tasks.size(), initialSize - tasks.size());
    }
}