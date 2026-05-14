import request from "./request";

/**
 * 发送消息
 */
export function sendMessage(fromUserId, toUserId, content) {
  return request.post("/chat/send", { fromUserId, toUserId, content });
}

/**
 * 获取聊天历史
 */
export function getChatHistory(userId, friendId) {
  return request.get("/chat/history", { params: { userId, friendId } });
}

/**
 * 获取聊天会话列表
 */
export function getChatSessions(userId) {
  return request.get("/chat/sessions", { params: { userId } });
}

/**
 * 获取未读消息数
 */
export function getUnreadCount(userId) {
  return request.get("/chat/unread-count", { params: { userId } });
}

/**
 * 标记消息为已读
 */
export function markAsRead(userId, fromUserId) {
  return request.post("/chat/mark-read", { userId, fromUserId });
}
