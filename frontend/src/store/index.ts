import { configureStore } from '@reduxjs/toolkit';
import { setupListeners } from '@reduxjs/toolkit/query';
import authSlice from './slices/authSlice';
import uiSlice from './slices/uiSlice';
import { apiSlice } from './api/apiSlice';
import { membersApi } from './api/membersApi';
import { loansApi } from './api/loansApi';
import { savingsApi } from './api/savingsApi';
import { paymentsApi } from './api/paymentsApi';
import { dashboardApi } from './api/dashboardApi';
import { reportsApi } from './api/reportsApi';

// Export auth actions for use in components
export { loginUser, logoutUser, refreshAuthToken } from './actions/authActions';

export const store = configureStore({
  reducer: {
    auth: authSlice,
    ui: uiSlice,
    api: apiSlice.reducer,
  },
  middleware: (getDefaultMiddleware) =>
    getDefaultMiddleware({
      serializableCheck: {
        ignoredActions: ['persist/PERSIST', 'persist/REHYDRATE'],
      },
    })
    .concat(apiSlice.middleware),
});

setupListeners(store.dispatch);

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
