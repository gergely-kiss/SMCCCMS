import axios from 'axios';

const API_BASE_URL = `http://localhost:${import.meta.env.VITE_BE_PORT ?? 8080}/api`;

export interface User {
  id: number;
  govId: string;
  firstName: string;
  lastName: string;
  roles: string[];
}

export interface VerifyIdRequest {
  govId: string;
}

export interface ContactRequest {
  contact: string;
}

export interface CodeRequest {
  code: string;
}

export interface CodeResponse {
  code: string;
}

// Create axios instance with default config
const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  withCredentials: true, // Important for cookie handling
});

export const authApi = {
  async verifyId(govId: string): Promise<User> {
    const response = await api.post<User>('/auth/verify-id', { govId });
    return response.data;
  },

  async requestCode(contact: string): Promise<CodeResponse> {
    const response = await api.post<CodeResponse>('/auth/request-code', { contact });
    return response.data;
  },

  async verifyCode(code: string): Promise<User> {
    const response = await api.post<User>('/auth/verify-code', { code });
    return response.data;
  },
};

export default api;