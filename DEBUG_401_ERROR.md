# 🔍 HƯỚNG DẪN DEBUG LỖI 401 UNAUTHORIZED

## 📋 TÓM TẮT CÁCH HOẠT ĐỘNG HỆ THỐNG

### 1️⃣ **Luồng đăng nhập:**
```
User nhập email/password 
  ↓
Gọi API: POST /api/v1/user-service/auth/login
  ↓
Backend trả về LoginResponse {token, userId, username}
  ↓
Lưu token vào SharedPreferences (key: "access_token")
  ↓
Lưu thông tin user (userId, email, username)
  ↓
Chuyển sang HomeActivity
```

### 2️⃣ **Luồng lấy danh sách xe:**
```
VehicleActivity.onCreate()
  ↓
Gọi API: GET /api/v1/user-service/vehicle?ownedByMe=true
  ↓
AuthInterceptor tự động lấy token từ TokenManager
  ↓
Gắn header: Authorization: Bearer <token>
  ↓
Backend kiểm tra token → Trả về danh sách xe hoặc 401
```

---

## ❌ VẤN ĐỀ PHÁT HIỆN

### **Lỗi 401 Unauthorized khi lấy danh sách xe**

**Nguyên nhân có thể:**

1. ❌ **Token không được lưu đúng** khi đăng nhập
2. ❌ **Token đã hết hạn** (backend thường set expire 24h-7 ngày)
3. ❌ **Backend không trả về token** trong response
4. ❌ **Token bị xóa** do clear app data hoặc logout
5. ❌ **AuthInterceptor không gắn token** vào request

---

## 🛠️ CÁCH DEBUG (THEO BƯỚC)

### **Bước 1: Xem log khi đăng nhập**

1. Mở **Android Studio** → Tab **Logcat** (góc dưới)
2. Filter: Chọn device/emulator đang chạy
3. Tìm kiếm: Gõ `LoginActivity` vào ô filter
4. Đăng nhập lại vào app
5. **Kiểm tra các log sau:**

```
✅ LoginActivity: ========== LOGIN SUCCESS ==========
✅ LoginActivity: Raw token from backend: YES (length=XXX)
✅ LoginActivity: Token after cleaning: eyJhbGciOiJIUzI1NiIsInR5cCI...
✅ LoginActivity: ✅ Token saved and verified successfully!
✅ LoginActivity: User info saved - ID: xxx, Email: xxx@xxx.com
```

**❗ Nếu thấy:**
- `❌ Backend returned NULL or EMPTY token!` → **Backend không trả token**
- `❌ Token save FAILED` → **Lỗi lưu token**
- `❌ EXCEPTION when saving token` → **Exception khi save**

### **Bước 2: Xem log khi lấy danh sách xe**

1. Logcat filter: Gõ `VehicleActivity`
2. Vào màn hình quản lý xe
3. **Kiểm tra log:**

```
✅ VehicleActivity: Token exists: true
✅ VehicleActivity: Token preview: eyJhbGciOiJIUzI1NiIs...
✅ VehicleActivity: API Response received - Success: true
```

**❗ Nếu thấy:**
- `Token exists: false` → **Token bị mất**
- `API Error occurred` → **Lỗi gọi API**
- `401` hoặc `unauthorized` → **Token hết hạn hoặc không hợp lệ**

### **Bước 3: Kiểm tra HTTP request**

1. Logcat filter: Gõ `OkHttp` hoặc xóa filter để xem tất cả
2. Tìm request đến `/vehicle`
3. **Kiểm tra header:**

```
✅ Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**❗ Nếu thiếu header `Authorization`** → AuthInterceptor không hoạt động

---

## 🔧 GIẢI PHÁP

### **Giải pháp 1: Đăng nhập lại (Đơn giản nhất)**

1. Mở app
2. Nếu thấy lỗi 401, app sẽ **tự động chuyển về màn hình Login**
3. Đăng nhập lại → Token mới sẽ được lưu
4. Thử vào màn hình quản lý xe lại

### **Giải pháp 2: Clear app data và đăng nhập lại**

**Trên thiết bị Android:**
```
Settings → Apps → ParkMate → Storage → Clear Data
```

**Hoặc qua ADB:**
```bash
adb shell pm clear com.parkmate.android
```

Sau đó đăng nhập lại từ đầu.

### **Giải pháp 3: Kiểm tra backend**

Nếu vẫn lỗi sau khi đăng nhập lại:

1. **Test API bằng Postman:**
   ```
   POST http://your-backend-url/api/v1/user-service/auth/login
   Body: {"email": "test@test.com", "password": "123456"}
   
   → Kiểm tra response có trả về token không
   ```

2. **Test API vehicle với token:**
   ```
   GET http://your-backend-url/api/v1/user-service/vehicle?ownedByMe=true
   Headers: 
     Authorization: Bearer <token-từ-login>
   
   → Kiểm tra có trả về 200 OK hay 401
   ```

---

## 📱 KIỂM TRA NHANH

### **Cách 1: Xem SharedPreferences trong Android Studio**

1. Chạy app trên emulator/device
2. Android Studio → **View** → **Tool Windows** → **App Inspection**
3. Tab **Database Inspector** → Chọn app → **Shared Preferences**
4. Tìm file `auth_prefs.xml`
5. Xem key `access_token` có giá trị không

### **Cách 2: Xem bằng ADB**

```bash
adb shell "run-as com.parkmate.android cat /data/data/com.parkmate.android/shared_prefs/auth_prefs.xml"
```

Kết quả mong đợi:
```xml
<map>
  <string name="access_token">eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...</string>
</map>
```

---

## 📞 HƯỚNG DẪN CHO BẠN

**NGAY BÂY GIỜ hãy làm theo:**

1. ✅ **Cài đặt app mới** (đã build thành công)
2. ✅ **Mở Logcat** trong Android Studio
3. ✅ **Đăng nhập lại** và xem log
4. ✅ **Vào màn hình quản lý xe** và xem log
5. ✅ **Gửi cho tôi các log bạn thấy**

**Log quan trọng cần xem:**
- `LoginActivity` → Có token từ backend không?
- `VehicleActivity` → Token có tồn tại không?
- `AuthInterceptor` → Token có được gắn vào request không?
- HTTP response → Backend trả về lỗi gì?

---

## 🎯 KẾT LUẬN

**Hệ thống hoạt động đúng**, nhưng có 3 điểm cần kiểm tra:

1. ✅ **Backend có trả token không** → Check log LoginActivity
2. ✅ **Token có được lưu đúng không** → Check SharedPreferences
3. ✅ **Token có hết hạn không** → Đăng nhập lại để lấy token mới

**99% khả năng:** Token đã hết hạn hoặc bị xóa. Chỉ cần **đăng nhập lại** là sẽ hoạt động.

Nếu vẫn lỗi sau khi đăng nhập lại → Có vấn đề ở backend hoặc cấu hình API.

