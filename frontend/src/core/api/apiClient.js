import axios from 'axios';

/**
 * State Smart Life Central API Client
 * Configures base URL, timeout, request correlation IDs (X-Request-ID), and error interception.
 */
const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api/v1',
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

// Request Interceptor: Attach JWT Bearer Token and Request Correlation ID
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('ssl_token');
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`;
    }

    // Attach unique client request correlation ID
    const requestId = 'req_' + Math.random().toString(36).substring(2, 11) + Date.now().toString(36);
    config.headers['X-Request-ID'] = requestId;

    return config;
  },
  (error) => Promise.reject(error)
);

// Response Interceptor: Uniform error handling for API responses
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const errorEnvelope = error.response?.data || {
      status: error.response?.status || 500,
      error: 'NETWORK_ERROR',
      message: error.message || 'An unexpected connection error occurred.',
      timestamp: new Date().toISOString()
    };
    return Promise.reject(errorEnvelope);
  }
);

export default apiClient;
