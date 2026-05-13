package com.damai.damaiticket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AI Agent 配置类
 * 支持多种 AI 模型接入
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "ai")
public class AiAgentConfig {

    /**
     * 是否启用 AI 功能
     */
    private boolean enabled = true;

    /**
     * AI 模型类型：openai / zhipu / doubao / siliconflow 等
     */
    private String provider = "openai";

    /**
     * API Key
     */
    private String apiKey;

    /**
     * API 地址（可自定义代理地址）
     */
    private String baseUrl = "https://api.openai.com/v1";

    /**
     * 模型名称
     */
    private String model = "gpt-3.5-turbo";

    /**
     * 系统提示词
     */
    private String systemPrompt = "你是一个票务系统的智能客服助手，名叫\"小票\"。\n" +
        "你的职责是帮助用户：\n" +
        "1. 查询演出信息（演出名称、地点、时间、价格、座位状态）\n" +
        "2. 推荐适合的演出\n" +
        "3. 解答购票相关问题（如何购票、如何退票、如何选座等）\n\n" +
        "请用友好、简洁的中文回答。\n" +
        "如果用户询问你不知道的信息，请引导用户查看演出列表或联系客服。\n\n" +
        "可用的工具（Function Calling）：\n" +
        "- search_shows: 搜索演出列表（支持关键词和分类筛选）\n" +
        "- get_show_detail: 获取演出详情\n" +
        "- get_available_seats: 获取演出可用座位";

    /**
     * 对话超时时间（毫秒）
     */
    private int timeout = 30000;

    /**
     * 最大对话轮次
     */
    private int maxConversations = 20;
}
