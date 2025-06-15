package com.example.AWMI.entity;

/**
 * 异步处理任务数据类
 */
public class Task {
    public enum Status { PENDING, DONE, FAILED }

    private String id;
    private Status status;
    private String result;

    // 构造函数
    public Task(String id) {
        this.id = id;
        this.status = Status.PENDING;
    }

    // Getter和Setter方法
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id='" + id + '\'' +
                ", status=" + status +
                ", result='" + result + '\'' +
                '}';
    }
}
