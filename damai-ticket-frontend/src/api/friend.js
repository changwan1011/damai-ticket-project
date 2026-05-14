import request from "./request";

/**
 * 发送好友申请
 */
export function sendFriendRequest(userId, friendId) {
  return request.post("/friend/request", { userId, friendId });
}

/**
 * 获取好友列表
 */
export function getFriendList(userId) {
  return request.get("/friend/list", { params: { userId } });
}

/**
 * 获取待处理的好友申请
 */
export function getPendingRequests(userId) {
  return request.get("/friend/requests", { params: { userId } });
}

/**
 * 确认好友申请
 */
export function acceptFriend(requestId, userId) {
  return request.post("/friend/accept", { requestId, userId });
}

/**
 * 拒绝好友申请
 */
export function rejectFriend(requestId, userId) {
  return request.post("/friend/reject", { requestId, userId });
}

/**
 * 删除好友
 */
export function deleteFriend(userId, friendId) {
  return request.post("/friend/delete", { userId, friendId });
}
