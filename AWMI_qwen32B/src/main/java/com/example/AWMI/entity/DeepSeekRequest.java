package com.example.AWMI.entity;

import java.util.List;

public class DeepSeekRequest {
    private String model;
    private boolean stream;
    private List<Message> messages;

    // 静态内部类
    public static class Message {
        private String role;
        private String content;

        // 全参构造器
        public Message(String role, String content) {
            this.role = role;
            this.content = content;
        }

        // Getter & Setter
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }

    // Getter & Setter
    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }
    public boolean isStream() { return stream; }
    public void setStream(boolean stream) { this.stream = stream; }
    public List<Message> getMessages() { return messages; }
    public void setMessages(List<Message> messages) { this.messages = messages; }
}