import axios from 'axios';

// Create Axios instance
const axiosClient = axios.create({
  baseURL: '/api', // Relative path to use Vite proxy
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10s timeout
});

// Request Interceptor: Attach Token
axiosClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// Response Interceptor: Handle Errors
axiosClient.interceptors.response.use(
  (response) => {
    return response;
  },
  (error) => {
    if (error.response) {
      // Handle 401 Unauthorized
      if (error.response.status === 401) {
        // Clear token and redirect to login if needed
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        // window.location.href = '/login'; // Optional: Force redirect
      }
      // Return server error message if available
      return Promise.reject(new Error(error.response.data?.message || 'Something went wrong'));
    }
    return Promise.reject(error);
  }
);

export default axiosClient;
