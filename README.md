# ParkMate Android App

## Giới thiệu
ParkMate là ứng dụng Android Java giao tiếp với backend qua API.

## Cấu trúc thư mục
```text
app/src/main/java/com/parkmate/android/
├── activity/      # Màn hình chính (MainActivity...)
├── fragment/      # Màn hình phụ (SampleFragment...)
├── model/         # Đối tượng dữ liệu từ API (SampleModel...)
├── adapter/       # Adapter cho danh sách (SampleAdapter...)
├── network/       # Giao tiếp API (ApiService...)
├── utils/         # Tiện ích chung (Utils...)
```
```text
app/src/main/res/layout/
├── activity_main.xml
├── fragment_sample.xml
├── item_sample.xml
```

## Hướng dẫn setup
1. Mở project bằng Android Studio.
2. Đảm bảo đã cài đặt JDK phù hợp (Java 8+).
3. Sync Gradle và build project.
4. Thêm các API thực tế vào network/ và model/.
5. Thay thế các file mẫu bằng code của bạn.

## Lưu ý
- Không sử dụng ViewModel, App.java, hoặc database cục bộ.
- Giao tiếp backend qua API (Retrofit, OkHttp...)
- Có thể mở rộng thêm các package nếu cần.
