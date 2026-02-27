import axios from "axios";

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL;

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    "Content-Type": "application/json",
  },
});

export interface CreateShortUrlRequest {
  originalUrl: string;
  expiryDate?: string | null;
}

export interface ShortUrlResponse {
  shortUrl: string;
  originalUrl: string;
  expiryDate?: string;
  createdAt: string;
}

export interface ShortUrlStatsResponse {
  originalUrl: string;
  shortCode: string;
  clickCount: number;
  createdAt: string;
  expiryDate?: string;
  updatedAt?: string;
}

export const createShortUrl = async (
  payload: CreateShortUrlRequest
): Promise<ShortUrlResponse> => {
  const response = await api.post(
    "/api/v1/short-urls",
    payload
  );
  return response.data;
};

export const getStats = async (
  shortCode: string
): Promise<ShortUrlStatsResponse> => {
  const response = await api.get(
    `/api/v1/short-urls/${shortCode}/stats`
  );
  return response.data;
};