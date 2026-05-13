package com.damai.damaiticket.controller;

import com.damai.damaiticket.service.AiAgentService;
import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

/**
 * AI Agent 智能助手控制器
 */
@RestController
@RequestMapping("/api/ai")
@CrossOrigin
public class AiAgentController {

    @Autowired
    private AiAgentService aiAgentService;

    /**
     * 发送消息给 AI 助手
     */
    @PostMapping("/chat")
    public Map<String, Object> chat(@RequestBody Map<String, Object> body) {
        String message = (String) body.get("message");
        String sessionId = (String) body.getOrDefault("sessionId", "default");

        if (message == null || message.trim().isEmpty()) {
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("message", "消息不能为空");
            return result;
        }

        return aiAgentService.chat(sessionId, message.trim());
    }

    /**
     * 清空对话历史
     */
    @PostMapping("/clear")
    public Map<String, Object> clear(@RequestBody Map<String, Object> body) {
        String sessionId = (String) body.getOrDefault("sessionId", "default");
        aiAgentService.clearHistory(sessionId);
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "对话已清空");
        return result;
    }

    /**
     * 健康检查
     */
    @GetMapping("/health")
    public Map<String, Object> health() {
        return aiAgentService.healthCheck();
    }
}
