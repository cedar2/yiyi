package com.example.AWMI.utils;

import java.sql.*;
import java.util.Map;
import java.util.TreeMap;

public class BookWordCountCalculator {

    // 数据库连接信息
    private static final String URL = "jdbc:mysql://localhost:3306/zhouyi?serverTimezone=GMT%2b8";
    private static final String USER = "root";
    private static final String PASSWORD = "896896";

    public static Map<Integer, String> getBookWordCount() {
        // 使用 TreeMap，自动按 book_id 升序排序
        Map<Integer, String> result = new TreeMap<>();
        int totalWords = 0; // 统计所有书籍的总字数

        // SQL 查询：计算每个 book_id 下的 content 字数总和，并关联 book_name
        String sql = "SELECT book_id, book_name, SUM(CHAR_LENGTH(content)) AS total_word_count " +
                "FROM fulltextbaseonchapter GROUP BY book_id, book_name";

        try (Connection conn = DriverManager.getConnection(URL, USER, PASSWORD);
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int bookId = rs.getInt("book_id");
                String bookName = rs.getString("book_name");
                int totalWordCount = rs.getInt("total_word_count");

                totalWords += totalWordCount; // 统计所有书的总字数

                // 将结果存入 Map，TreeMap 会自动按 key（book_id）升序排序
                result.put(bookId, bookName + " - 总字数: " + totalWordCount);
            }

            // 额外存入总字数信息（使用 `Integer.MAX_VALUE` 作为 key，确保它始终排在最后）
            result.put(Integer.MAX_VALUE, "所有书籍总字数: " + totalWords);

        } catch (SQLException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static void main(String[] args) {
        Map<Integer, String> bookWordCounts = getBookWordCount();
        bookWordCounts.forEach((id, info) -> {
            if (id == Integer.MAX_VALUE) {
                System.out.println(info); // 直接打印总字数
            } else {
                System.out.println("Book ID: " + id + ", " + info);
            }
        });
    }
}
