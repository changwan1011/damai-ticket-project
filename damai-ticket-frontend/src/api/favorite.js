import request from './request'

/**
 * 添加收藏
 */
export function addFavorite(showId) {
  return request.post('/favorite/add', null, {
    params: { showId }
  })
}

/**
 * 取消收藏
 */
export function removeFavorite(showId) {
  return request.post('/favorite/remove', null, {
    params: { showId }
  })
}

/**
 * 获取我的收藏列表
 */
export function getMyFavorites() {
  return request.get('/favorite/list')
}

/**
 * 批量检查收藏状态
 */
export function checkFavorite(showIds) {
  return request.get('/favorite/check', {
    params: { showIds: showIds.join(',') }
  })
}
