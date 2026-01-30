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

// Doctor-specific API endpoints
export const endpoints = {
  // Doctor Auth
  auth: {
    login: '/doctors/auth/login',
    logout: '/doctors/auth/logout',
    refreshToken: '/doctors/auth/refresh',
  },

  // Profile
  profile: {
    get: '/doctors/profile',
    update: '/doctors/profile',
    updateAvatar: '/doctors/profile/avatar',
    updateSchedule: '/doctors/profile/schedule',
    getStats: '/doctors/profile/stats',
  },

  // Appointments
  appointments: {
    list: '/doctors/appointments',
    getById: (id: string) => `/doctors/appointments/${id}`,
    updateStatus: (id: string) => `/doctors/appointments/${id}/status`,
    getSlots: '/doctors/slots',
    updateSlots: '/doctors/slots',
  },

  // Consultations
  consultations: {
    list: '/doctors/consultations',
    getById: (id: string) => `/doctors/consultations/${id}`,
    getToken: (id: string) => `/doctors/consultations/${id}/token`,
    complete: (id: string) => `/doctors/consultations/${id}/complete`,
    sendMessage: (id: string) => `/doctors/consultations/${id}/messages`,
  },

  // Patients
  patients: {
    list: '/doctors/patients',
    getById: (id: string) => `/doctors/patients/${id}`,
    getHistory: (id: string) => `/doctors/patients/${id}/history`,
    getNotes: (id: string) => `/doctors/patients/${id}/notes`,
    addNote: (id: string) => `/doctors/patients/${id}/notes`,
  },

  // Prescriptions
  prescriptions: {
    create: '/doctors/prescriptions',
    getById: (id: string) => `/doctors/prescriptions/${id}`,
    getTemplates: '/doctors/prescriptions/templates',
    saveTemplate: '/doctors/prescriptions/templates',
  },

  // Reviews
  reviews: {
    list: '/doctors/reviews',
    respond: (id: string) => `/doctors/reviews/${id}/respond`,
    getStats: '/doctors/reviews/stats',
  },

  // Earnings
  earnings: {
    summary: '/doctors/earnings/summary',
    transactions: '/doctors/earnings/transactions',
    withdraw: '/doctors/earnings/withdraw',
    getBankDetails: '/doctors/earnings/bank',
    updateBankDetails: '/doctors/earnings/bank',
  },

  // Notifications
  notifications: {
    list: '/doctors/notifications',
    markRead: (id: string) => `/doctors/notifications/${id}/read`,
    markAllRead: '/doctors/notifications/read-all',
    settings: '/doctors/notifications/settings',
    registerDevice: '/doctors/notifications/register-device',
  },
};
