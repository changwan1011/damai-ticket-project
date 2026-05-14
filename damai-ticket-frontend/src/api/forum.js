import request from "./request";

/**
 * 发布帖子
 */
export function publishPost(userId, title, content) {
  return request.post("/forum/post", { userId, title, content });
}

/**
 * 获取帖子列表
 */
export function getPostList() {
  return request.get("/forum/posts");
}

/**
 * 获取我的帖子
 */
export function getMyPosts(userId) {
  return request.get("/forum/my-posts", { params: { userId } });
}

/**
 * 获取帖子详情
 */
export function getPostDetail(postId) {
  return request.get(`/forum/post/${postId}`);
}

/**
 * 点赞帖子
 */
export function likePost(postId) {
  return request.post("/forum/like", { postId });
}

/**
 * 删除帖子
 */
export function deletePost(postId, userId) {
  return request.post("/forum/delete", { postId, userId });
}

/**
 * 发布回复
 */
export function publishReply(postId, userId, content) {
  return request.post("/forum/reply", { postId, userId, content });
}

/**
 * 获取回复列表
 */
export function getReplies(postId) {
  return request.get(`/forum/replies/${postId}`);
}

/**
 * 删除回复
 */
export function deleteReply(replyId, userId) {
  return request.post("/forum/reply/delete", { replyId, userId });
}
