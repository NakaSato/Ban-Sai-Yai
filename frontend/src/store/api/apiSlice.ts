import { createApi, fetchBaseQuery } from '@reduxjs/toolkit/query/react';
import { RootState } from '../index';
import { API_BASE_URL } from '@/constants';

// Base query with authorization
const baseQuery = fetchBaseQuery({
  baseUrl: API_BASE_URL,
  prepareHeaders: (headers, { getState }) => {
    const token = (getState() as RootState).auth.token;
    if (token) {
      headers.set('authorization', `Bearer ${token}`);
    }
    headers.set('Content-Type', 'application/json');
    return headers;
  },
});

// Base query with error handling
const baseQueryWithAuth = async (args: any, api: any, extraOptions: any) => {
  let result = await baseQuery(args, api, extraOptions);

  // Handle 401 Unauthorized
  if (result.error && result.error.status === 401) {
    // You could dispatch a logout action here
    console.error('Authentication failed');
  }

  return result;
};

export const apiSlice = createApi({
  reducerPath: 'api',
  baseQuery: baseQueryWithAuth,
  tagTypes: [
    'User',
    'Member',
    'Loan',
    'LoanBalance',
    'Collateral',
    'Guarantor',
    'SavingAccount',
    'SavingTransaction',
    'SavingBalance',
    'Payment',
    'Dashboard'
  ],
  endpoints: () => ({}),
});
