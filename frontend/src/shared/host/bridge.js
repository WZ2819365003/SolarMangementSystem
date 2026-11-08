import axios from 'axios'
import { mockHandlers } from '@/shared/mock/data'

const REQUEST_MODE_BACKEND = 'backend'
const REQUEST_MODE_MOCK = 'mock'
const backendBaseUrl = process.env.VUE_APP_BACKEND_BASE_URL || '/api'
const requestMode = process.env.VUE_APP_REQUEST_MODE || REQUEST_MODE_BACKEND
const AUTH_TOKEN_KEY = 'pvms.auth.token'
const AUTH_USER_KEY = 'pvms.auth.username'

let unauthorizedHandler = null

export function getAuthToken() {
  return sessionStorage.getItem(AUTH_TOKEN_KEY)
}

function getAuthUser() {
  return sessionStorage.getItem(AUTH_USER_KEY)
}

function clearAuthSession() {
  sessionStorage.removeItem(AUTH_TOKEN_KEY)
  sessionStorage.removeItem(AUTH_USER_KEY)
}

function authHeaders() {
  const token = getAuthToken()
  return token ? { Authorization: `Bearer ${token}` } : {}
}

export function setUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

export async function login(username, password) {
  const response = await axios.post(`${backendBaseUrl}/auth/login`, {
    username,
    password
  })
  const { token, username: authenticatedUser } = response.data.data
  sessionStorage.setItem(AUTH_TOKEN_KEY, token)
  sessionStorage.setItem(AUTH_USER_KEY, authenticatedUser)
}

export async function logout() {
  try {
    await axios.post(`${backendBaseUrl}/auth/logout`, null, {
      headers: authHeaders()
    })
  } catch (error) {
    // 令牌可能已失效，无论服务端结果如何都要清掉本地登录态
  } finally {
    clearAuthSession()
  }
}

export function getRequestErrorMessage(error, fallback) {
  const data = error && error.response && error.response.data
  return (data && data.message) || fallback
}

function getTsxRuntime() {
  return window._tsx || {}
}

function normalizeResponse(payload) {
  if (payload && typeof payload.code !== 'undefined') {
    return payload
  }
  return {
    code: 0,
    message: 'success',
    data: payload
  }
}

export function isHostMode() {
  return Boolean(window.__POWERED_BY_QIANKUN__)
}

export function initializeHostBridge() {
  if (!document.body.classList.contains('pvms-body')) {
    document.body.classList.add('pvms-body')
  }
}

export function buildRuntimeContext(props = {}) {
  const tsx = getTsxRuntime()
  const userInfo = tsx.userInfo || {}

  return {
    hostMode: isHostMode(),
    tenantName: props.tenantName || tsx.tenantName || '示范园区光伏集群',
    userName: props.userName || userInfo.name || getAuthUser() || '开发联调用户',
    themeName: props.themeName || tsx.themeName || 'light',
    hasHostRequest: typeof tsx.apirequest === 'function'
  }
}

export function syncDocumentTitle(title) {
  document.title = title
}

function shouldUseHostRequest(apirequest) {
  return typeof apirequest === 'function'
}

function shouldUseMockRequest() {
  return requestMode === REQUEST_MODE_MOCK
}

// 宿主模式由宿主负责鉴权，mock 模式不访问后端，只有直连后端时才需要本应用登录
export function isAuthRequired() {
  return (
    !shouldUseHostRequest(getTsxRuntime().apirequest) && !shouldUseMockRequest()
  )
}

async function requestViaBackend(url, options = {}) {
  try {
    const response = await axios({
      baseURL: backendBaseUrl,
      url,
      method: options.method || 'get',
      data: options.data,
      params: options.params,
      headers: authHeaders()
    })

    return normalizeResponse(response.data)
  } catch (error) {
    if (error.response && error.response.status === 401) {
      clearAuthSession()
      if (unauthorizedHandler) {
        unauthorizedHandler()
      }
    }
    throw error
  }
}

export async function request(url, options = {}) {
  const tsx = getTsxRuntime()
  const apirequest = tsx.apirequest

  if (shouldUseHostRequest(apirequest)) {
    const response = await apirequest({
      url,
      method: options.method || 'get',
      data: options.data,
      params: options.params
    })
    return normalizeResponse(response)
  }

  if (!shouldUseMockRequest()) {
    return requestViaBackend(url, options)
  }

  const handler = mockHandlers[url]
  if (!handler) {
    return normalizeResponse(null)
  }

  return Promise.resolve(normalizeResponse(handler(options)))
}
