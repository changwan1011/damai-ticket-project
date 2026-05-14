import request from "./request";

/**
 * 获取我的所有电子票
 */
export function getMyTickets(userId) {
  return request.get("/eticket/list", { params: { userId } });
}

/**
 * 根据订单ID获取电子票
 */
export function getTicketsByOrder(orderId, userId) {
  return request.get(`/eticket/order/${orderId}`, { params: { userId } });
}

/**
 * 获取电子票详情
 */
export function getTicketDetail(ticketId, userId) {
  return request.get(`/eticket/${ticketId}`, { params: { userId } });
}

/**
 * 根据票码获取电子票信息
 */
export function getTicketByCode(ticketCode) {
  return request.get(`/eticket/code/${ticketCode}`);
}

/**
 * 验票
 */
export function verifyTicket(ticketCode) {
  return request.post("/eticket/verify", { ticketCode });
}
