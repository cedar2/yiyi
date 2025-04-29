package com.example.AWMI.utils;

import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Date;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Component
public class JwtUtils {
    private static final String SECRET_KEY = "YourSecretKey";
    private static final long EXPIRATION_TIME = 86400000;  // 1 天

    // 改为静态方法
    public static String generateToken(String email) {
        long now = System.currentTimeMillis();
        String header = Base64.getEncoder().encodeToString("{\"alg\":\"HS256\",\"typ\":\"JWT\"}".getBytes());
        String payload = Base64.getEncoder().encodeToString(("{\"sub\":\"" + email + "\",\"exp\":" + (now + EXPIRATION_TIME) + "}").getBytes());
        String signature = hmacSha256(header + "." + payload, SECRET_KEY);
        return header + "." + payload + "." + signature;
    }

    public static String parseToken(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) return null;

        String payloadJson = new String(Base64.getDecoder().decode(parts[1]));
        if (!payloadJson.contains("\"exp\"")) return null;

        long exp = Long.parseLong(payloadJson.split("\"exp\":")[1].split("}")[0]);
        if (exp < System.currentTimeMillis()) return null;  // 过期

        return payloadJson.split("\"sub\":\"")[1].split("\"")[0];
    }

    private static String hmacSha256(String data, String key) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(mac.doFinal(data.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new RuntimeException("JWT 签名失败", e);
        }
    }
}
