package vn.cineshow.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatTestController {

    // Client gửi tới /app/chat-test
    @MessageMapping("/chat-test")
    @SendTo("/topic/chat-test")
    public String handleChat(String message) {
        System.out.println("📥 Client gửi: " + message);
        return "Server phản hồi: " + message.toUpperCase();
    }
}
