# Authentication API Test Results

## Prerequisites

The backend server must be running on port 9090.

### Start the server:

```bash
cd /Users/chanthawat/Developments/FINAL/backend
mvn spring-boot:run
```

Wait for the server to start (you should see "Started BansaiyaiApplication" in the logs).

## Running the Tests

### Option 1: Run the automated test script

```bash
./test-auth-apis.sh
```

### Option 2: Manual testing with curl

#### 1. Check Username Availability

```bash
curl -s http://localhost:9090/api/auth/check-username/testuser | jq .
```

#### 2. Check Email Availability

```bash
curl -s http://localhost:9090/api/auth/check-email/test@example.com | jq .
```

#### 3. Login (Get Token)

```bash
curl -X POST http://localhost:9090/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123","rememberMe":true}' | jq .
```

Save the token from the response:

```bash
export TOKEN="your_token_here"
```

#### 4. Get Current User

```bash
curl http://localhost:9090/api/auth/me \
  -H "Authorization: Bearer $TOKEN" | jq .
```

#### 5. Refresh Token

First login with rememberMe=true to get a refresh token, then:

```bash
curl -X POST http://localhost:9090/api/auth/refresh \
  -H "Content-Type: application/json" \
  -d '{"token":"your_refresh_token_here"}' | jq .
```

#### 6. Logout

```bash
curl -X POST http://localhost:9090/api/auth/logout \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" | jq .
```

## Expected Test Results

### Test 1: Check Username Availability (nonexistent)

- **Status**: 200 OK
- **Response**: `{"available": true}`

### Test 2: Check Username Availability (existing - admin)

- **Status**: 200 OK
- **Response**: `{"available": false}`

### Test 3: Check Email Availability

- **Status**: 200 OK
- **Response**: `{"available": true}` or `{"available": false}`

### Test 4: Login with Invalid Credentials

- **Status**: 401 Unauthorized
- **Response**: `{"message": "Invalid username or password"}`

### Test 5: Login with Valid Credentials

- **Status**: 200 OK
- **Response**:

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiJ9..." (if rememberMe=true),
  "expiresIn": 86400000,
  "type": "Bearer",
  "id": 1,
  "username": "admin",
  "email": "admin@bansaiyai.com",
  "role": "ADMIN",
  "permissions": ["ROLE_ADMIN", ...]
}
```

### Test 6: Get Current User (Authenticated)

- **Status**: 200 OK
- **Response**:

```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@bansaiyai.com",
  "role": "ADMIN",
  "permissions": ["ROLE_ADMIN", ...]
}
```

### Test 7: Get Current User (Unauthenticated)

- **Status**: 403 Forbidden

### Test 8: Refresh Token (Valid)

- **Status**: 200 OK
- **Response**:

```json
{
  "token": "new_access_token",
  "refreshToken": "new_refresh_token",
  "expiresIn": 86400000,
  "type": "Bearer"
}
```

### Test 9: Refresh Token (Invalid)

- **Status**: 401 Unauthorized
- **Response**: `{"message": "Invalid refresh token", "code": "INVALID_TOKEN"}`

### Test 10: Logout (Authenticated)

- **Status**: 200 OK
- **Response**: `{"message": "User logged out successfully"}`

### Test 11: Logout (Unauthenticated)

- **Status**: 403 Forbidden

## Notes

- The base URL is: `http://localhost:9090/api`
- All authenticated endpoints require the `Authorization: Bearer <token>` header
- Tokens expire after 24 hours (86400000 ms) by default
- Refresh tokens expire after 7 days (604800000 ms) when rememberMe=true
- The admin credentials are: `admin` / `admin123`

## Troubleshooting

### Server not responding

```bash
# Check if server is running
lsof -i:9090

# Check server logs
tail -f backend.log
```

### Port already in use

```bash
# Find process using port 9090
lsof -ti:9090

# Kill the process (if needed)
kill -9 $(lsof -ti:9090)
```

### Database connection errors

Check that MariaDB/MySQL is running and the database `ban_sai_yai` exists.
