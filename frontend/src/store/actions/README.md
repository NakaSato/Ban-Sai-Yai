# Authentication Actions

This directory contains enhanced authentication actions that integrate Redux state management with API calls.

## Available Actions

### `loginUser(credentials: LoginRequest)`

Enhanced login action that handles the complete login flow including:
- Sending credentials to the backend
- Handling the `rememberMe` parameter
- Storing access token and optional refresh token
- Setting user data in Redux state

**Usage:**
```typescript
import { useAppDispatch } from '@/hooks/redux';
import { loginUser } from '@/store';

const dispatch = useAppDispatch();

// Login with Remember Me
await dispatch(loginUser({
  username: 'user@example.com',
  password: 'password123',
  rememberMe: true, // Will store refresh token
}));

// Login without Remember Me (session only)
await dispatch(loginUser({
  username: 'user@example.com',
  password: 'password123',
  rememberMe: false, // No refresh token stored
}));
```

### `logoutUser()`

Enhanced logout action that:
- Calls the backend API to invalidate tokens server-side
- Clears all tokens from Redux state
- Removes tokens from localStorage
- Handles API failures gracefully (still clears local state)

**Usage:**
```typescript
import { useAppDispatch } from '@/hooks/redux';
import { logoutUser } from '@/store';

const dispatch = useAppDispatch();

// Logout
await dispatch(logoutUser());
// User will be logged out and redirected to login page
```

### `refreshAuthToken(refreshToken: string)`

Refreshes the access token using a refresh token:
- Calls the backend refresh endpoint
- Updates both access and refresh tokens (token rotation)
- Clears auth state if refresh fails

**Usage:**
```typescript
import { useAppDispatch } from '@/hooks/redux';
import { refreshAuthToken } from '@/store';

const dispatch = useAppDispatch();

try {
  const response = await dispatch(refreshAuthToken(currentRefreshToken));
  // Tokens updated successfully
} catch (error) {
  // Refresh failed, user will be logged out
}
```

## Redux Slice Updates

### New Actions

#### `updateTokens({ token, refreshToken? })`

Updates both access and refresh tokens in state and localStorage. This is used during token refresh to implement token rotation.

**Usage:**
```typescript
import { updateTokens } from '@/store/slices/authSlice';

dispatch(updateTokens({
  token: 'new-access-token',
  refreshToken: 'new-refresh-token', // Optional
}));
```

### Updated Actions

#### `setAuth({ user, token, refreshToken? })`

Now properly handles the optional `refreshToken` parameter:
- If `refreshToken` is provided, it's stored in state and localStorage
- If `refreshToken` is not provided, any existing refresh token is removed

## API Updates

### Login Endpoint

The login mutation now properly sends the `rememberMe` parameter and handles the refresh token in the response:

```typescript
const [login] = useLoginMutation();

const response = await login({
  username: 'user@example.com',
  password: 'password123',
  rememberMe: true,
}).unwrap();

// response.token - Access token
// response.refreshToken - Refresh token (only if rememberMe was true)
// response.user - User data
```

### Refresh Token Endpoint

The refresh token mutation now returns both the new access token and new refresh token (for token rotation):

```typescript
const [refreshToken] = useRefreshTokenMutation();

const response = await refreshToken({
  token: currentRefreshToken,
}).unwrap();

// response.token - New access token
// response.refreshToken - New refresh token (optional, for rotation)
```

### Logout Endpoint

The logout mutation is now properly integrated with the `logoutUser` action to ensure server-side token invalidation.

## Token Storage

Tokens are stored in localStorage with the following keys:
- `auth_token` - Access token
- `refresh_token` - Refresh token (only when Remember Me is enabled)
- `user_data` - User information

## Security Considerations

1. **Token Rotation**: When refreshing tokens, the backend should issue a new refresh token to prevent token reuse attacks.

2. **Server-Side Invalidation**: The logout action calls the backend to invalidate tokens server-side before clearing local state.

3. **Graceful Degradation**: If the logout API call fails, local state is still cleared to ensure the user is logged out on the client side.

4. **Automatic Cleanup**: Failed refresh attempts automatically clear auth state and redirect to login.

## Integration with useTokenRefresh Hook

The `useTokenRefresh` hook automatically uses the `updateTokens` action to handle token refresh:

```typescript
import { useTokenRefresh } from '@/hooks/useTokenRefresh';

// In your App component
function App() {
  useTokenRefresh({
    refreshThresholdMinutes: 5, // Refresh when token has 5 minutes left
    maxRetries: 3, // Retry up to 3 times on failure
    enabled: true, // Enable automatic refresh
  });

  return <YourApp />;
}
```

## Example: Complete Login Flow

```typescript
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAppDispatch } from '@/hooks/redux';
import { loginUser } from '@/store';

function LoginPage() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();
  const [rememberMe, setRememberMe] = useState(false);

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    try {
      await dispatch(loginUser({
        username: formData.username,
        password: formData.password,
        rememberMe, // Pass the Remember Me preference
      }));
      
      // Login successful, redirect to dashboard
      navigate('/dashboard');
    } catch (error) {
      // Handle login error
      console.error('Login failed:', error);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      {/* Form fields */}
      <input type="checkbox" checked={rememberMe} onChange={(e) => setRememberMe(e.target.checked)} />
      <label>Remember Me</label>
      <button type="submit">Login</button>
    </form>
  );
}
```

## Example: Logout Flow

```typescript
import { useNavigate } from 'react-router-dom';
import { useAppDispatch } from '@/hooks/redux';
import { logoutUser } from '@/store';

function LogoutButton() {
  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const handleLogout = async () => {
    try {
      await dispatch(logoutUser());
      // Logout successful, redirect to login
      navigate('/login');
    } catch (error) {
      // Even if API call fails, user is logged out locally
      navigate('/login');
    }
  };

  return <button onClick={handleLogout}>Logout</button>;
}
```
