# Dự án QLPhongTro - Ứng dụng Quản lý Phòng trọ trên Android

## 1. Giới thiệu

**QLPhongTro** là một ứng dụng di động Android được xây dựng bằng Kotlin, nhằm mục đích số hóa và đơn giản hóa việc quản lý phòng trọ. Ứng dụng cung cấp hai giao diện riêng biệt, đáp ứng nhu cầu của cả **Chủ trọ (Landlord)** và **Người thuê (Tenant)**.

*   **Đối với Chủ trọ:** Ứng dụng là một công cụ mạnh mẽ để quản lý phòng, theo dõi hợp đồng, ghi nhận hóa đơn tiền nhà, và giao tiếp với người thuê.
*   **Đối với Người thuê:** Ứng dụng mang lại sự tiện lợi trong việc theo dõi hóa đơn, thanh toán, xem thông tin hợp đồng và cập nhật thông tin cá nhân.

Dự án này thể hiện khả năng xây dựng một ứng dụng Android hoàn chỉnh với cơ sở dữ liệu cục bộ, quản lý trạng thái người dùng, và xử lý các tác vụ bất đồng bộ một cách hiệu quả.

## 2. Các chức năng chính

###  Chức năng cho Chủ trọ (Landlord)
*   **Đăng nhập:** Phân quyền và truy cập vào giao diện quản lý.
*   **Quản lý Phòng:** Thêm, xem chi tiết, cập nhật thông tin và trạng thái của từng phòng (tên, giá, diện tích, hình ảnh, mô tả).
*   **Quản lý Người thuê:** Thêm người thuê mới, gán họ vào các phòng cụ thể.
*   **Quản lý Hóa đơn:** Tạo hóa đơn hàng tháng cho từng phòng, ghi nhận các khoản đã thanh toán.
*   **Quản lý Hợp đồng:** Lưu trữ và quản lý thông tin hợp đồng của người thuê.
*   **Gửi thông báo:** Gửi thông báo chung hoặc riêng cho người thuê.

###  Chức năng cho Người thuê (Tenant)
*   **Đăng nhập:** Truy cập vào không gian cá nhân của mình.
*   **Trang chủ:**
    *   Hiển thị thông tin hóa đơn chưa thanh toán gần nhất (số tiền, hạn chót).
    *   Các lối tắt nhanh đến: Lịch sử hóa đơn, Thông báo, Dịch vụ...
*   **Chi tiết Hóa đơn:** Xem chi tiết các khoản phí (tiền phòng, điện, nước, dịch vụ...) và thực hiện thanh toán.
*   **Trang cá nhân:**
    *   Xem và cập nhật thông tin cá nhân (tên, phòng đang ở).
    *   Thay đổi ảnh đại diện (avatar).
    *   Xem lại hợp đồng thuê nhà.
    *   Đăng xuất an toàn.
*   **Nhận thông báo:** Xem các thông báo từ chủ trọ.

## 3. Công nghệ và Thư viện sử dụng

*   **Ngôn ngữ:** **Kotlin** - Ngôn ngữ chính thức được Google khuyến nghị cho phát triển Android.
*   **Kiến trúc & UI:**
    *   **Android SDK:** Sử dụng các thành phần cơ bản như `Activity`, `Fragment`.
    *   **XML Layouts:** Thiết kế giao diện người dùng.
    *   **Material Design Components:** Sử dụng các `CardView`, `MaterialButton` để tạo giao diện hiện đại.
    *   **RecyclerView:** Hiển thị các danh sách (phòng, thông báo...).
*   **Cơ sở dữ liệu:**
    *   **SQLite:** Lưu trữ dữ liệu cục bộ trên thiết bị (thông tin người dùng, phòng, hóa đơn...).
    *   **DAO (Data Access Object) Pattern:** Tổ chức mã nguồn truy cập cơ sở dữ liệu một cách rõ ràng.
*   **Quản lý phiên đăng nhập:**
    *   **SharedPreferences:** Lưu trạng thái đăng nhập và thông tin cơ bản của người dùng.
*   **Xử lý hình ảnh:**
    *   **Glide:** Thư viện mạnh mẽ để tải, cache và hiển thị hình ảnh một cách hiệu quả, đặc biệt là ảnh đại diện.
*   **Tác vụ nền & Quyền:**
    *   **NotificationManager:** Tạo và hiển thị thông báo hệ thống.
    *   **ActivityResultContracts:** API hiện đại để xử lý kết quả từ các Activity khác (ví dụ: chọn ảnh từ bộ sưu tập).

## 4. Hình ảnh minh họa dự án
### Màn hình trang chủ (Chủ trọ)
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/249173c2-7fe2-4427-9985-655d14298807" />

### Màn hình quản lý hóa đơn
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/29d1493c-c32c-476e-bc32-93aa8b986a08" />

### Màn  hình trang cá nhân chủ trọ
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/d8b68cdd-2423-4490-932c-054036e048cb" />

### Màn hình thông tin ngân hàng tạo mã QR thanh toán
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/78db6163-1e40-48c7-b93d-25380518b8d7" />

### Màn hình tạo hóa đơn
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/2749d17a-5d64-4cd4-8250-37abc247e314" />
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/600d8779-4068-496e-8102-b64f29ff36d4" />


### Màn hình quản lý người thuê trọ
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/9abc5847-c70f-4f90-9326-b1d0c5395b87" />


### Màn hình Welcome
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/d3071afe-e398-4e34-bc7a-111cedca4168" />


### Màn hình Đăng nhập & Đăng ký
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/1b63b687-8646-4c28-8854-211d395f5f2b" />

### Màn hình chính (Giao diện Người thuê)
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/bbabe534-bc4d-4510-ad29-17b8c1bbcc15" />

### Màn hình Trang cá nhân (Người thuê)
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/e6bfd672-f9ea-414c-bf9d-0c4a120b6791" />

### Màn hình Quản lý Phòng (Giao diện Chủ trọ)
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/7b3de365-e269-4441-b84b-4381b4a7220c" />
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/d8d0e2e7-9d93-4327-b260-711e52d490ac" />
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/9c53fb5a-89dc-4ae0-a671-598f9245fa08" />


### Màn hình Chi tiết Phòng
<img width="400" height="840" alt="image" src="https://github.com/user-attachments/assets/a8c0e440-11fc-4f59-8be0-f50f5d34fe12" />

## 5. Hướng dẫn cài đặt và chạy dự án

### Yêu cầu:
*   Android Studio (phiên bản mới nhất được khuyến nghị).
*   Một máy ảo Android (Emulator) hoặc một thiết bị Android thật.

### Các bước cài đặt:

1.  **Clone repository về máy:**
    ```bash
    git clone https://github.com/letruonghuy/Boarding-House-Management-Application
    ```

2.  **Mở dự án trong Android Studio:**
    *   Chọn `File` -> `Open...` và trỏ đến thư mục `QLPhongTro` bạn vừa clone về.

3.  **Đồng bộ Gradle:**
    *   Android Studio sẽ tự động đồng bộ và tải về các thư viện cần thiết (như Glide). Quá trình này có thể mất vài phút.

4.  **Chạy ứng dụng:**
    *   Chọn một máy ảo hoặc kết nối thiết bị thật.
    *   Nhấn nút `Run 'app'` (biểu tượng tam giác màu xanh) hoặc dùng phím tắt `Shift + F10`.

5.  **Sử dụng ứng dụng:**
    *   Ứng dụng sẽ khởi chạy và hiển thị màn hình đăng nhập.
    *   Bạn có thể tạo tài khoản mới với vai trò "Chủ trọ" hoặc "Người thuê" để trải nghiệm các luồng chức năng khác nhau.
    *   Dữ liệu được lưu trữ trong cơ sở dữ liệu SQLite trên chính thiết bị/máy ảo đó.

## 6. Thông tin tác giả

*   **Họ và tên:** Lê Trương Trường Huy

---
