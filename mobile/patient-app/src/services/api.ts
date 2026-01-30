import axios, { AxiosInstance, InternalAxiosRequestConfig } from 'axios';
import Constants from 'expo-constants';

const API_URL = Constants.expoConfig?.extra?.apiUrl || 'http://localhost:8080/api/v1';

class ApiService {
  private client: AxiosInstance;
  private authToken: string | null = null;

  constructor() {
    this.client = axios.create({
      baseURL: API_URL,
      timeout: 30000,
      headers: {
        'Content-Type': 'application/json',
      },
    });

    this.client.interceptors.request.use(
      (config: InternalAxiosRequestConfig) => {
        if (this.authToken) {
          config.headers.Authorization = `Bearer ${this.authToken}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    this.client.interceptors.response.use(
      (response) => response,
      async (error) => {
        if (error.response?.status === 401) {
          // Token expired, handle refresh or logout
          this.authToken = null;
        }
        return Promise.reject(error);
      }
    );
  }

  setAuthToken(token: string | null) {
    this.authToken = token;
  }

  get<T>(url: string, config?: object) {
    return this.client.get<T>(url, config);
  }

  post<T>(url: string, data?: object, config?: object) {
    return this.client.post<T>(url, data, config);
  }

  put<T>(url: string, data?: object, config?: object) {
    return this.client.put<T>(url, data, config);
  }

  patch<T>(url: string, data?: object, config?: object) {
    return this.client.patch<T>(url, data, config);
  }

  delete<T>(url: string, config?: object) {
    return this.client.delete<T>(url, config);
  }
}

export const api = new ApiService();

// Type-safe API endpoints
export const endpoints = {
  // Auth
  auth: {
    login: '/auth/login',
    register: '/auth/register',
    logout: '/auth/logout',
    refreshToken: '/auth/refresh',
    sendOTP: '/auth/send-otp',
    verifyOTP: '/auth/verify-otp',
  },

  // Users
  users: {
    profile: '/users/profile',
    updateProfile: '/users/profile',
    uploadAvatar: '/users/avatar',
  },

  // Doctors
  doctors: {
    search: '/doctors/search',
    getById: (id: string) => `/doctors/${id}`,
    getSlots: (id: string) => `/doctors/${id}/slots`,
    getReviews: (id: string) => `/doctors/${id}/reviews`,
  },

  // Appointments
  appointments: {
    list: '/appointments',
    create: '/appointments',
    getById: (id: string) => `/appointments/${id}`,
    cancel: (id: string) => `/appointments/${id}/cancel`,
    reschedule: (id: string) => `/appointments/${id}/reschedule`,
  },

  // Consultations
  consultations: {
    getById: (id: string) => `/consultations/${id}`,
    getToken: (id: string) => `/consultations/${id}/token`,
    sendMessage: (id: string) => `/consultations/${id}/messages`,
    getMessages: (id: string) => `/consultations/${id}/messages`,
  },

  // Reviews
  reviews: {
    submit: '/reviews',
    vote: (id: string) => `/reviews/${id}/vote`,
    report: (id: string) => `/reviews/${id}/report`,
  },

  // Health Records
  health: {
    records: '/health-records',
    uploadDocument: '/health-records/documents',
    getVitals: '/health-records/vitals',
  },

  // Prescriptions
  prescriptions: {
    list: '/prescriptions',
    getById: (id: string) => `/prescriptions/${id}`,
    download: (id: string) => `/prescriptions/${id}/download`,
  },

  // Pharmacy
  pharmacy: {
    search: '/pharmacy/search',
    order: '/pharmacy/orders',
    getOrder: (id: string) => `/pharmacy/orders/${id}`,
  },

  // Articles
  articles: {
    list: '/articles',
    getBySlug: (slug: string) => `/articles/${slug}`,
    like: (id: string) => `/articles/${id}/like`,
    bookmark: (id: string) => `/articles/${id}/bookmark`,
    bookmarks: '/articles/bookmarks',
  },

  // Notifications
  notifications: {
    list: '/notifications',
    markRead: (id: string) => `/notifications/${id}/read`,
    markAllRead: '/notifications/read-all',
    unreadCount: '/notifications/unread-count',
    registerDevice: '/notifications/register-device',
  },
};
