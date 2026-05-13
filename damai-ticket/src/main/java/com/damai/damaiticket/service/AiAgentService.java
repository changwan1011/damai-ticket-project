package com.damai.damaiticket.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.damai.damaiticket.config.AiAgentConfig;
import com.damai.damaiticket.entity.ShowEvent;
import com.damai.damaiticket.entity.Seat;
import com.damai.damaiticket.mapper.ShowEventMapper;
import com.damai.damaiticket.mapper.SeatMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.*;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI Agent 服务
 */
@Service
public class AiAgentService {

    private static final Logger log = LoggerFactory.getLogger(AiAgentService.class);

    @Autowired
    private AiAgentConfig aiConfig;

    @Autowired
    private ShowEventMapper showEventMapper;

    @Autowired
    private SeatMapper seatMapper;

    @Autowired
    private ObjectMapper objectMapper;

    private final RestTemplate restTemplate;

    // 存储对话历史
    private final Map<String, List<Map<String, String>>> conversationHistory = new ConcurrentHashMap<>();

    // 工具定义
    private final List<Map<String, Object>> tools;

    // 简单提示词（用于不支持 tools 的模型）
    private final String simplePrompt = "你是一个票务系统的智能客服助手，名叫\"小票\"。\n" +
        "请用友好、简洁的中文回答用户关于演出查询、购票咨询的问题。\n" +
        "如果用户询问演出信息，你可以直接查询数据库回答。\n" +
        "当前演出数据可以从 show_event 表查询。";

    public AiAgentService() {
        // 配置 RestTemplate 超时
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(30000);  // 连接超时 30 秒
        factory.setReadTimeout(120000);    // 读取超时 120 秒（AI 生成可能较慢）
        this.restTemplate = new RestTemplate(factory);
        this.tools = initTools();
    }

    /**
     * 初始化工具定义
     */
    private List<Map<String, Object>> initTools() {
        List<Map<String, Object>> toolsList = new ArrayList<>();

        // 搜索演出工具
        Map<String, Object> searchShows = new HashMap<>();
        searchShows.put("type", "function");
        Map<String, Object> searchShowsFunction = new HashMap<>();
        searchShowsFunction.put("name", "search_shows");
        searchShowsFunction.put("description", "搜索演出列表，支持按关键词和分类筛选");
        Map<String, Object> searchShowsParams = new HashMap<>();
        searchShowsParams.put("type", "object");
        Map<String, Object> searchShowsProperties = new HashMap<>();
        searchShowsProperties.put("keyword", createProperty("string", "搜索关键词"));
        searchShowsProperties.put("category", createProperty("string", "演出分类"));
        searchShowsParams.put("properties", searchShowsProperties);
        searchShowsFunction.put("parameters", searchShowsParams);
        searchShows.put("function", searchShowsFunction);
        toolsList.add(searchShows);

        // 演出详情工具
        Map<String, Object> showDetail = new HashMap<>();
        showDetail.put("type", "function");
        Map<String, Object> showDetailFunction = new HashMap<>();
        showDetailFunction.put("name", "get_show_detail");
        showDetailFunction.put("description", "获取演出详情");
        Map<String, Object> showDetailParams = new HashMap<>();
        showDetailParams.put("type", "object");
        Map<String, Object> showDetailProperties = new HashMap<>();
        showDetailProperties.put("showId", createProperty("integer", "演出ID"));
        showDetailParams.put("properties", showDetailProperties);
        showDetailParams.put("required", createRequiredList("showId"));
        showDetailFunction.put("parameters", showDetailParams);
        showDetail.put("function", showDetailFunction);
        toolsList.add(showDetail);

        // 可用座位工具
        Map<String, Object> availableSeats = new HashMap<>();
        availableSeats.put("type", "function");
        Map<String, Object> availableSeatsFunction = new HashMap<>();
        availableSeatsFunction.put("name", "get_available_seats");
        availableSeatsFunction.put("description", "获取演出可用座位");
        Map<String, Object> availableSeatsParams = new HashMap<>();
        availableSeatsParams.put("type", "object");
        Map<String, Object> availableSeatsProperties = new HashMap<>();
        availableSeatsProperties.put("showId", createProperty("integer", "演出ID"));
        availableSeatsParams.put("properties", availableSeatsProperties);
        availableSeatsParams.put("required", createRequiredList("showId"));
        availableSeatsFunction.put("parameters", availableSeatsParams);
        availableSeats.put("function", availableSeatsFunction);
        toolsList.add(availableSeats);

        return toolsList;
    }

    private Map<String, Object> createProperty(String type, String description) {
        Map<String, Object> prop = new HashMap<>();
        prop.put("type", type);
        prop.put("description", description);
        return prop;
    }

    private List<String> createRequiredList(String... items) {
        return Arrays.asList(items);
    }

    /**
     * 处理用户消息
     */
    public Map<String, Object> chat(String sessionId, String message) {
        try {
            log.info("[AI] 收到消息: {}", message);

            // 检查 AI 是否启用
            if (!aiConfig.isEnabled()) {
                return createResult(true, "AI 助手当前未启用，请稍后再试。", "text");
            }

            // 获取对话历史
            List<Map<String, String>> history = conversationHistory
                .computeIfAbsent(sessionId, k -> new ArrayList<>());

            // 限制历史长度
            if (history.size() > aiConfig.getMaxConversations()) {
                history = new ArrayList<>(history.subList(history.size() - aiConfig.getMaxConversations(), history.size()));
                conversationHistory.put(sessionId, history);
            }

            // 构建消息列表
            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(createMessage("system", aiConfig.getSystemPrompt()));
            messages.addAll(history);
            messages.add(createMessage("user", message));

            // 调用 AI（带重试逻辑）
            Map<String, Object> response = callAiWithFallback(messages);

            Boolean success = (Boolean) response.getOrDefault("success", false);
            if (success) {
                String aiMessage = (String) response.get("message");

                // 保存对话历史
                history.add(createMessage("user", message));
                history.add(createMessage("assistant", aiMessage));

                log.info("[AI] 回复成功: {}", aiMessage.substring(0, Math.min(100, aiMessage.length())));
                return createResult(true, aiMessage, "text");
            } else {
                String error = (String) response.get("error");
                log.error("[AI] 调用失败: {}", error);
                return createResult(false, "AI 服务暂时不可用：" + error, "text");
            }
        } catch (Exception e) {
            log.error("[AI] 异常: ", e);
            return createResult(false, "抱歉，服务出了点问题：" + e.getMessage(), "text");
        }
    }

    /**
     * 带降级策略的 AI 调用
     */
    private Map<String, Object> callAiWithFallback(List<Map<String, String>> messages) {
        try {
            // 第一次尝试：带 tools 调用
            return callAi(messages, true);
        } catch (Exception e) {
            log.warn("[AI] 带 tools 调用失败，尝试不带 tools: {}", e.getMessage());
            try {
                // 第二次尝试：不带 tools
                return callAi(messages, false);
            } catch (Exception e2) {
                log.error("[AI] 不带 tools 调用也失败: {}", e2.getMessage());
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", e2.getMessage());
                return result;
            }
        }
    }

    private Map<String, String> createMessage(String role, String content) {
        Map<String, String> msg = new HashMap<>();
        msg.put("role", role);
        msg.put("content", content);
        return msg;
    }

    private Map<String, Object> createResult(boolean success, String message, String type) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", success);
        result.put("message", message);
        result.put("type", type);
        return result;
    }

    /**
     * 调用 AI 接口
     */
    private Map<String, Object> callAi(List<Map<String, String>> messages, boolean withTools) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + aiConfig.getApiKey());

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiConfig.getModel());
        requestBody.put("messages", messages);
        
        if (withTools) {
            requestBody.put("tools", tools);
        }
        
        requestBody.put("temperature", 0.7);

        log.info("[AI] 请求体: model={}, withTools={}", aiConfig.getModel(), withTools);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        String url = aiConfig.getBaseUrl() + "/chat/completions";
        log.info("[AI] 开始请求 AI 接口...");

        try {
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                entity,
                String.class
            );

            log.info("[AI] 收到响应，状态码: {}", response.getStatusCode());

            JsonNode root = objectMapper.readTree(response.getBody());
            
            // 检查错误
            if (root.has("error")) {
                Map<String, Object> result = new HashMap<>();
                result.put("success", false);
                result.put("error", root.get("error").asText());
                return result;
            }

            JsonNode choices = root.get("choices");

            if (choices != null && choices.isArray() && choices.size() > 0) {
                JsonNode choice = choices.get(0);
                JsonNode message = choice.get("message");

                String finishReason = choice.has("finish_reason") ? 
                    choice.get("finish_reason").asText() : "stop";

                // 检查是否有工具调用
                if (withTools && message.has("tool_calls") && message.get("tool_calls").isArray()) {
                    List<Map<String, Object>> toolCalls = new ArrayList<>();
                    for (JsonNode tc : message.get("tool_calls")) {
                        Map<String, Object> toolCall = new HashMap<>();
                        toolCall.put("id", tc.get("id").asText());
                        toolCall.put("name", tc.get("function").get("name").asText());
                        toolCall.put("arguments", tc.get("function").get("arguments").asText());
                        toolCalls.add(toolCall);
                    }
                    
                    // 处理工具调用
                    try {
                        String toolResult = processToolCalls(messages, toolCalls);
                        
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", true);
                        result.put("message", toolResult);
                        return result;
                    } catch (Exception e) {
                        log.error("[AI] 工具调用处理失败: {}", e.getMessage(), e);
                        Map<String, Object> result = new HashMap<>();
                        result.put("success", false);
                        result.put("error", "工具调用失败: " + e.getMessage());
                        return result;
                    }
                }

                Map<String, Object> result = new HashMap<>();
                result.put("success", true);
                result.put("message", message.get("content").asText());
                return result;
            }

            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "AI 返回格式错误");
            return result;
            
        } catch (JsonProcessingException e) {
            log.error("[AI] JSON 解析失败: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "AI 响应解析失败: " + e.getMessage());
            return result;
        } catch (Exception e) {
            log.error("[AI] AI 接口调用失败: {}", e.getMessage(), e);
            Map<String, Object> result = new HashMap<>();
            result.put("success", false);
            result.put("error", "AI 接口调用失败: " + e.getMessage());
            return result;
        }
    }

    /**
     * 处理工具调用
     */
    private String processToolCalls(List<Map<String, String>> messages, List<Map<String, Object>> toolCalls) throws Exception {
        List<Map<String, String>> toolResults = new ArrayList<>();
        StringBuilder summaryResult = new StringBuilder();

        for (Map<String, Object> toolCall : toolCalls) {
            String toolName = (String) toolCall.get("name");
            String arguments = (String) toolCall.get("arguments");
            String toolCallId = (String) toolCall.get("id");

            log.info("[AI] 执行工具: {} 参数: {}", toolName, arguments);

            // 执行工具
            String result = executeTool(toolName, arguments);
            
            // 收集工具结果用于降级
            if (summaryResult.length() > 0) {
                summaryResult.append("\n\n");
            }
            summaryResult.append(result);

            // 添加工具结果
            Map<String, String> toolMsg = new HashMap<>();
            toolMsg.put("tool_call_id", toolCallId);
            toolMsg.put("role", "tool");
            toolMsg.put("name", toolName);
            toolMsg.put("content", result);
            toolResults.add(toolMsg);
        }

        // 尝试调用 AI 生成最终回复
        try {
            messages.add(createMessage("assistant", "我会帮您查询..."));
            messages.addAll(toolResults);

            Map<String, Object> finalResponse = callAi(messages, false);
            if ((Boolean) finalResponse.getOrDefault("success", false)) {
                return (String) finalResponse.get("message");
            }
        } catch (Exception e) {
            log.warn("[AI] 生成回复失败，使用工具结果降级: {}", e.getMessage());
            // 降级：直接返回工具查询结果
        }

        // 降级返回：整理工具查询结果
        return summaryResult.toString();
    }

    /**
     * 执行工具
     */
    private String executeTool(String toolName, String arguments) throws Exception {
        JsonNode args = objectMapper.readTree(arguments);

        switch (toolName) {
            case "search_shows":
                return searchShows(args);
            case "get_show_detail":
                return getShowDetail(args);
            case "get_available_seats":
                return getAvailableSeats(args);
            default:
                return "未知工具：" + toolName;
        }
    }

    /**
     * 搜索演出
     */
    private String searchShows(JsonNode args) {
        String keyword = null;
        String category = null;
        
        if (args.has("keyword") && !args.get("keyword").isNull()) {
            keyword = args.get("keyword").asText();
        }
        if (args.has("category") && !args.get("category").isNull()) {
            category = args.get("category").asText();
        }

        QueryWrapper<ShowEvent> qw = new QueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            final String searchKeyword = keyword;
            qw.and(w -> w.like("title", searchKeyword).or().like("location", searchKeyword));
        }
        if (StringUtils.hasText(category) && !"ALL".equalsIgnoreCase(category)) {
            qw.eq("category", category);
        }
        qw.orderByDesc("create_time");
        qw.last("LIMIT 10");

        List<ShowEvent> shows = showEventMapper.selectList(qw);

        if (shows.isEmpty()) {
            return "没有找到符合条件的演出。请尝试其他关键词或分类。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append("为您找到以下演出：\n\n");
        for (int i = 0; i < shows.size(); i++) {
            ShowEvent show = shows.get(i);
            sb.append(String.format("%d. 【%s】\n", i + 1, show.getTitle()));
            sb.append(String.format("   地点：%s\n", show.getLocation()));
            sb.append(String.format("   时间：%s\n", show.getShowTime()));
            sb.append(String.format("   价格：%s元起\n", show.getPrice()));
            sb.append(String.format("   分类：%s\n\n", show.getCategory()));
        }
        sb.append("输入演出名称或编号可获取详细信息~");

        return sb.toString();
    }

    /**
     * 获取演出详情
     */
    private String getShowDetail(JsonNode args) {
        if (!args.has("showId")) {
            return "请提供演出ID";
        }

        Long showId = args.get("showId").asLong();
        ShowEvent show = showEventMapper.selectById(showId);

        if (show == null) {
            return "未找到该演出，可能已下架或ID错误。";
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【%s】\n\n", show.getTitle()));
        sb.append(String.format("地点：%s\n", show.getLocation()));
        sb.append(String.format("时间：%s\n", show.getShowTime()));
        sb.append(String.format("价格：%s元起\n", show.getPrice()));
        sb.append(String.format("分类：%s\n", show.getCategory()));
        if (show.getDescription() != null) {
            sb.append(String.format("\n介绍：\n%s\n", show.getDescription()));
        }

        // 查询可用座位数量
        QueryWrapper<Seat> seatQw = new QueryWrapper<>();
        seatQw.eq("show_id", showId).eq("status", 0);
        Long availableCount = seatMapper.selectCount(seatQw);
        sb.append(String.format("\n余票：%d 张可选\n", availableCount));

        return sb.toString();
    }

    /**
     * 获取可用座位
     */
    private String getAvailableSeats(JsonNode args) {
        if (!args.has("showId")) {
            return "请提供演出ID";
        }

        Long showId = args.get("showId").asLong();
        ShowEvent show = showEventMapper.selectById(showId);

        if (show == null) {
            return "未找到该演出。";
        }

        QueryWrapper<Seat> qw = new QueryWrapper<>();
        qw.eq("show_id", showId).eq("status", 0).last("LIMIT 20");

        List<Seat> seats = seatMapper.selectList(qw);

        if (seats.isEmpty()) {
            return String.format("【%s】目前暂无余票，建议关注后续场次。", show.getTitle());
        }

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("【%s】可选座位：\n\n", show.getTitle()));

        // 按价格分组
        Map<Double, List<String>> priceSeats = new TreeMap<>();
        for (Seat seat : seats) {
            Double price = seat.getPrice().doubleValue();
            priceSeats.computeIfAbsent(price, k -> new ArrayList<>())
                .add(seat.getSeatNumber());
        }

        for (Map.Entry<Double, List<String>> entry : priceSeats.entrySet()) {
            List<String> seatList = entry.getValue();
            String shown = String.join(", ", seatList.subList(0, Math.min(5, seatList.size())));
            sb.append(String.format("%s元区：%s 等%d个座位\n", 
                entry.getKey(), shown, entry.getValue().size()));
        }

        sb.append("\n如需购票，请前往演出详情页选座购买~");
        return sb.toString();
    }

    /**
     * 清空对话历史
     */
    public void clearHistory(String sessionId) {
        conversationHistory.remove(sessionId);
    }

    /**
     * 健康检查
     */
    public Map<String, Object> healthCheck() {
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ok");
        result.put("aiEnabled", aiConfig.isEnabled());
        result.put("provider", aiConfig.getProvider());
        result.put("model", aiConfig.getModel());
        return result;
    }
}
