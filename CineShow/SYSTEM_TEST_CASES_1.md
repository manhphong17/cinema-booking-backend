# SYSTEM TEST CASES - LUỒNG ĐẶT VÉ CHÍNH

## 📋 TỔNG QUAN LUỒNG CHÍNH

**Luồng đặt vé hoàn chỉnh:**

1. **Authentication** → Đăng nhập lấy JWT token
2. **Browse** → Xem danh sách phim, suất chiếu
3. **Select Seats** → Chọn ghế (WebSocket + Redis SeatHold)
4. **Add Concessions** → Thêm combo/đồ ăn (tùy chọn)
5. **Checkout** → Tạo Order + Payment URL
6. **Payment** → Thanh toán qua VNPay
7. **Payment Callback** → Xử lý IPN và Return URL
8. **Order Confirmation** → Xác nhận đơn hàng thành công

---

## 🧪 CHI TIẾT CÁC TEST CASE

### **PHẦN 1: AUTHENTICATION & AUTHORIZATION**

#### TC-AUTH-001: Đăng nhập thành công

- **Mục đích:** Verify user có thể đăng nhập và nhận JWT token
- **Precondition:** User đã tồn tại trong DB
- **Steps:**
  1. POST `/auth/sign-in` với email + password hợp lệ
- **Expected:**
  - Status 200
  - Response có `accessToken`, `refreshToken`, `userId`
  - Token có thể dùng để gọi các API protected

#### TC-AUTH-002: Đăng nhập với thông tin sai

- **Mục đích:** Verify hệ thống từ chối đăng nhập sai
- **Steps:**
  1. POST `/auth/sign-in` với email/password sai
- **Expected:**
  - Status 401
  - Không có token trong response

#### TC-AUTH-003: Truy cập API booking không có token

- **Mục đích:** Verify security filter hoạt động
- **Steps:**
  1. GET `/bookings/movies/{id}/show-times/{date}` không có header Authorization
- **Expected:**
  - Status 401 Unauthorized

#### TC-AUTH-004: Truy cập API booking với token hết hạn

- **Mục đích:** Verify JWT expiry check
- **Steps:**
  1. Dùng token đã hết hạn
  2. Gọi bất kỳ API booking nào
- **Expected:**
  - Status 401

---

### **PHẦN 2: BROWSE MOVIES & SHOWTIMES**

#### TC-BROWSE-001: Lấy danh sách suất chiếu theo phim và ngày

- **Mục đích:** Verify API trả về đúng showtimes
- **Precondition:**
  - Movie tồn tại
  - Có showtimes trong ngày đó
- **Steps:**
  1. GET `/bookings/movies/{movieId}/show-times/{date}` với token hợp lệ
- **Expected:**
  - Status 200
  - Response là array các ShowTimeResponse
  - Mỗi showtime có: `id`, `startTime`, `endTime`, `roomName`, `roomId`
  - Chỉ trả về showtimes chưa bắt đầu

#### TC-BROWSE-002: Lấy showtimes với movieId không tồn tại

- **Mục đích:** Verify error handling
- **Steps:**
  1. GET `/bookings/movies/99999/show-times/2024-12-25`
- **Expected:**
  - Status 404 hoặc 200 với array rỗng

#### TC-BROWSE-003: Lấy showtimes với ngày không có suất chiếu

- **Mục đích:** Verify trả về array rỗng khi không có data
- **Steps:**
  1. GET `/bookings/movies/{movieId}/show-times/2025-12-31`
- **Expected:**
  - Status 200
  - `data` là array rỗng `[]`

#### TC-BROWSE-004: Lấy showtimes theo startTime

- **Mục đích:** Verify API alternative endpoint
- **Steps:**
  1. GET `/bookings/movies/{movieId}/show-times/start-time/{startTime}`
- **Expected:**
  - Status 200
  - Trả về showtimes match với startTime

#### TC-BROWSE-005: Lấy danh sách ghế cho showtime

- **Mục đích:** Verify API trả về layout ghế và trạng thái
- **Precondition:** Showtime tồn tại, có seats
- **Steps:**
  1. GET `/bookings/show-times/{showTimeId}/seats` với token
- **Expected:**
  - Status 200
  - Response là array BookingSeatsResponse
  - Mỗi seat có: `seatId`, `row`, `column`, `status` (AVAILABLE, HELD, BOOKED)
  - Có thông tin `seatType`, `price`

---

### **PHẦN 3: SEAT SELECTION (WebSocket + Redis)**

#### TC-SEAT-001: Chọn 1 ghế thành công

- **Mục đích:** Verify seat hold mechanism hoạt động
- **Precondition:**
  - User đã login
  - Showtime có ghế AVAILABLE
- **Steps:**
  1. Gửi WebSocket message `/seat/select` với SeatSelectRequest
     - `action: SELECT_SEAT`
     - `showtimeId`, `userId`, `ticketIds: [ticketId1]`
- **Expected:**
  - Seat được hold trong Redis với TTL
  - OrderSession được tạo trong Redis
  - WebSocket broadcast status `HELD` cho tất cả clients
  - Seat status chuyển từ AVAILABLE → HELD

#### TC-SEAT-002: Chọn nhiều ghế cùng lúc

- **Mục đích:** Verify có thể chọn nhiều ghế
- **Steps:**
  1. SELECT_SEAT với `ticketIds: [id1, id2, id3]`
- **Expected:**
  - Tất cả ghế được hold
  - OrderSession chứa tất cả ticketIds
  - Tất cả ghế broadcast status HELD

#### TC-SEAT-003: Chọn ghế đã được người khác hold

- **Mục đích:** Verify race condition handling
- **Precondition:**
  - User A đã hold seat X
  - TTL chưa hết
- **Steps:**
  1. User B cố chọn cùng seat X
- **Expected:**
  - User B nhận status `FAILED`
  - Seat vẫn thuộc User A
  - Không có conflict

#### TC-SEAT-004: Chọn ghế đã BOOKED

- **Mục đích:** Verify không thể chọn ghế đã bán
- **Precondition:** Seat đã có status BOOKED trong DB
- **Steps:**
  1. SELECT_SEAT cho ghế đã BOOKED
- **Expected:**
  - Status `FAILED`
  - Seat không được hold

#### TC-SEAT-005: Bỏ chọn ghế (DESELECT)

- **Mục đích:** Verify release seat mechanism
- **Precondition:** User đã hold ghế
- **Steps:**
  1. DESELECT_SEAT với ticketId đã hold
- **Expected:**
  - Seat được release khỏi Redis
  - OrderSession được update (remove ticketId)
  - Broadcast status `RELEASED`
  - Seat status chuyển về AVAILABLE

#### TC-SEAT-006: Bỏ chọn ghế cuối cùng

- **Mục đích:** Verify khi bỏ hết ghế thì xóa OrderSession
- **Precondition:** User chỉ hold 1 ghế
- **Steps:**
  1. DESELECT_SEAT ghế duy nhất
- **Expected:**
  - OrderSession bị xóa khỏi Redis
  - Broadcast RELEASED

#### TC-SEAT-007: Seat hold TTL hết hạn

- **Mục đích:** Verify Redis TTL tự động release
- **Precondition:** User hold ghế, chờ TTL hết (default 600s)
- **Steps:**
  1. Hold ghế
  2. Chờ TTL hết (hoặc mock Redis expire)
- **Expected:**
  - Seat tự động release
  - OrderSession bị xóa
  - Seat status về AVAILABLE

#### TC-SEAT-008: Lấy TTL còn lại của seat hold

- **Mục đích:** Verify frontend có thể check countdown
- **Precondition:** User đang hold ghế
- **Steps:**
  1. GET `/bookings/show-times/{showtimeId}/users/{userId}/seat-hold/ttl`
- **Expected:**
  - Status 200
  - Response là số giây còn lại (0-600)

#### TC-SEAT-009: Lấy thông tin seat hold hiện tại

- **Mục đích:** Verify restore seat hold khi reload page
- **Precondition:** User đang hold ghế
- **Steps:**
  1. GET `/bookings/show-times/{showtimeId}/users/{userId}/seat-hold`
- **Expected:**
  - Status 200
  - Response có SeatHold với danh sách ticketIds đang hold

#### TC-SEAT-010: Concurrent booking - 2 users chọn cùng ghế

- **Mục đích:** Verify race condition được xử lý đúng
- **Steps:**
  1. User A và User B cùng lúc SELECT_SEAT cho cùng ticketId
- **Expected:**
  - Chỉ 1 user thành công (first come first served)
  - User còn lại nhận FAILED
  - Không có data corruption

#### TC-SEAT-011: Chọn ghế với showtimeId không tồn tại

- **Mục đích:** Verify error handling
- **Steps:**
  1. SELECT_SEAT với showtimeId = 99999
- **Expected:**
  - Status FAILED hoặc exception được handle

---

### **PHẦN 4: ORDER SESSION & CONCESSIONS**

#### TC-SESSION-001: Tạo OrderSession khi chọn ghế

- **Mục đích:** Verify OrderSession được tạo tự động
- **Precondition:** User chọn ghế thành công
- **Steps:**
  1. SELECT_SEAT
  2. GET `/bookings/order-session?showtimeId={id}&userId={id}`
- **Expected:**
  - Status 200
  - OrderSessionDTO có: `ticketIds`, `totalPrice`, `status: PENDING`
  - `createdAt`, `expiredAt` được set
  - TTL = default (600s)

#### TC-SESSION-002: Update OrderSession khi chọn thêm ghế

- **Mục đích:** Verify OrderSession được update, không tạo mới
- **Precondition:** User đã có OrderSession với 1 ghế
- **Steps:**
  1. SELECT_SEAT thêm ghế thứ 2
  2. GET order-session
- **Expected:**
  - OrderSession có 2 ticketIds
  - `totalPrice` được tính lại
  - `createdAt` giữ nguyên, `expiredAt` được extend

#### TC-SESSION-003: Thêm concessions vào OrderSession

- **Mục đích:** Verify có thể thêm combo
- **Precondition:** User đã có OrderSession
- **Steps:**
  1. POST `/bookings/order-session/concessions` với ConcessionListRequest
     - `showtimeId`, `userId`, `concessions: [{concessionId, quantity}]`
- **Expected:**
  - Status 200
  - OrderSession được update với concessions
  - `totalPrice` được tính lại (tickets + concessions)

#### TC-SESSION-004: Update quantity của concession

- **Mục đích:** Verify có thể sửa số lượng combo
- **Precondition:** OrderSession đã có concession
- **Steps:**
  1. POST `/bookings/order-session/concessions` với quantity mới
- **Expected:**
  - Quantity được update
  - Total price được tính lại

#### TC-SESSION-005: Thêm concession với quantity = 0 (xóa)

- **Mục đích:** Verify có thể xóa concession
- **Steps:**
  1. POST với quantity = 0
- **Expected:**
  - Concession bị remove khỏi OrderSession

#### TC-SESSION-006: Thêm concession với concessionId không tồn tại

- **Mục đích:** Verify error handling
- **Steps:**
  1. POST với concessionId = 99999
- **Expected:**
  - Status 404 hoặc error message

#### TC-SESSION-007: Thêm concession vượt quá stock

- **Mục đích:** Verify stock validation
- **Precondition:** Concession có `unitInStock = 5`
- **Steps:**
  1. POST với quantity = 10
- **Expected:**
  - Status 400 hoặc error message về stock không đủ

#### TC-SESSION-008: OrderSession TTL hết hạn

- **Mục đích:** Verify OrderSession tự động expire
- **Steps:**
  1. Tạo OrderSession
  2. Chờ TTL hết
  3. GET order-session
- **Expected:**
  - Status 404 hoặc null response
  - Seat hold cũng bị xóa

---

### **PHẦN 5: CHECKOUT & PAYMENT**

#### TC-CHECKOUT-001: Tạo payment URL thành công

- **Mục đích:** Verify checkout flow hoạt động
- **Precondition:**
  - User đã hold ghế
  - Có OrderSession trong Redis
- **Steps:**
  1. POST `/payment/checkout` với CheckoutRequest:
     - `userId`, `showtimeId`
     - `ticketIds` (từ OrderSession)
     - `concessions` (từ OrderSession)
     - `totalPrice`, `amount`, `discount`
     - `paymentCode` (VNPAY)
- **Expected:**
  - Status 200
  - Response có payment URL (VNPay)
  - Order được tạo trong DB với status PENDING
  - Tickets được gán vào Order
  - Payment được tạo với status PENDING
  - OrderConcessions được tạo
  - OrderSession và SeatHold TTL được extend

#### TC-CHECKOUT-002: Checkout với ticketIds không match OrderSession

- **Mục đích:** Verify validation
- **Precondition:** OrderSession có ticketIds [1,2,3]
- **Steps:**
  1. POST checkout với ticketIds [1,2,4]
- **Expected:**
  - Status 400 hoặc error
  - Không tạo Order

#### TC-CHECKOUT-003: Checkout với ghế đã bị release

- **Mục đích:** Verify không thể checkout ghế đã hết hold
- **Precondition:**
  - User hold ghế
  - Seat hold TTL hết hoặc bị release
- **Steps:**
  1. POST checkout
- **Expected:**
  - Status 400 hoặc error về seat không còn hold

#### TC-CHECKOUT-004: Checkout với totalPrice không đúng

- **Mục đích:** Verify price validation
- **Precondition:** Tổng thực tế = 200k
- **Steps:**
  1. POST checkout với totalPrice = 100k
- **Expected:**
  - Status 400 hoặc error về price mismatch

#### TC-CHECKOUT-005: Checkout với payment method không tồn tại

- **Mục đích:** Verify payment method validation
- **Steps:**
  1. POST checkout với paymentCode = "INVALID"
- **Expected:**
  - Status 404 hoặc error

#### TC-CHECKOUT-006: Checkout với discount (loyalty points)

- **Mục đích:** Verify discount được áp dụng
- **Precondition:** User có loyalty points
- **Steps:**
  1. POST checkout với discount > 0
- **Expected:**
  - Order có discount field
  - Total price = original - discount
  - (Points sẽ bị trừ sau khi payment thành công)

#### TC-CHECKOUT-007: Lấy danh sách payment methods

- **Mục đích:** Verify API trả về methods active
- **Steps:**
  1. GET `/bookings/payment-methods`
- **Expected:**
  - Status 200
  - Response là array PaymentMethodDTO
  - Chỉ trả về methods có `isActive = true`

---

### **PHẦN 6: VNPAY PAYMENT CALLBACK**

#### TC-PAYMENT-001: VNPay IPN callback - Thanh toán thành công

- **Mục đích:** Verify IPN xử lý payment success và WebSocket broadcast
- **Precondition:**
  - Order PENDING trong DB
  - Payment PENDING
  - Có clients đang subscribe WebSocket topic `/topic/seat/{showtimeId}`
- **Steps:**
  1. GET `/payment/ipn` với params từ VNPay:
     - `vnp_ResponseCode = "00"`
     - `vnp_TransactionStatus = "00"`
     - `vnp_TxnRef` = order code
     - `vnp_Amount` = amount
     - Valid checksum
- **Expected:**
  - Status 200
  - Response `RspCode = "00"`, `Message = "Confirm Success"`
  - Order status → COMPLETED
  - Payment status → COMPLETED
  - Tickets status → BOOKED
  - Concessions stock được trừ
  - User loyalty points được cập nhật (trừ points dùng + cộng points mới)
  - OrderSession và SeatHold bị xóa khỏi Redis
  - **WebSocket broadcast:**
    - Message được gửi đến topic `/topic/seat/{showtimeId}`
    - Message body chứa:
      - `seats`: Array các SeatTicketDTO với status = "BOOKED"
      - `status`: "BOOKED"
      - `showtimeId`: ID của showtime
    - Tất cả clients đang subscribe topic này nhận được message
    - Clients cập nhật UI để hiển thị ghế đã được đặt (BOOKED)

#### TC-PAYMENT-002: VNPay IPN callback - Thanh toán thất bại

- **Mục đích:** Verify IPN xử lý payment failure
- **Steps:**
  1. GET `/payment/ipn` với:
     - `vnp_ResponseCode != "00"` hoặc `vnp_TransactionStatus != "00"`
- **Expected:**
  - Status 200
  - Response `RspCode != "00"`
  - Order status → FAILED hoặc giữ PENDING
  - Payment status → FAILED
  - Tickets vẫn AVAILABLE (chưa BOOKED)
  - Redis keys được cleanup

#### TC-PAYMENT-003: VNPay IPN - Invalid checksum

- **Mục đích:** Verify security check
- **Steps:**
  1. GET `/payment/ipn` với checksum sai
- **Expected:**
  - Status 200
  - Response `RspCode = "97"`, `Message = "Invalid Checksum"`
  - Order không được update

#### TC-PAYMENT-004: VNPay IPN - Order not found

- **Mục đích:** Verify error handling
- **Steps:**
  1. GET `/payment/ipn` với txnRef không tồn tại
- **Expected:**
  - Status 200
  - Response `RspCode = "01"`, `Message = "Order not Found"`

#### TC-PAYMENT-005: VNPay IPN - Order already confirmed

- **Mục đích:** Verify idempotency
- **Precondition:** Order đã COMPLETED
- **Steps:**
  1. GET `/payment/ipn` lại với cùng txnRef
- **Expected:**
  - Status 200
  - Response `RspCode = "02"`, `Message = "Order already confirmed"`
  - Order không bị update lại

#### TC-PAYMENT-006: VNPay IPN - Amount mismatch

- **Mục đích:** Verify amount validation
- **Precondition:** Order amount = 200k
- **Steps:**
  1. GET `/payment/ipn` với vnp_Amount = 100k
- **Expected:**
  - Status 200
  - Response `RspCode != "00"` (amount mismatch)
  - Order không được confirm

#### TC-PAYMENT-007: VNPay Return URL - Success

- **Mục đích:** Verify return URL xử lý đúng
- **Precondition:** Payment đã thành công (IPN đã xử lý)
- **Steps:**
  1. GET `/payment/return` với params từ VNPay (sau khi user quay lại)
- **Expected:**
  - Status 200
  - Response có `status = "SUCCESS"`, `message = "Thanh toán thành công"`
  - Có `orderCode`
  - Redis keys được cleanup (nếu chưa)

#### TC-PAYMENT-008: VNPay Return URL - Failed

- **Mục đích:** Verify return URL với payment failed
- **Steps:**
  1. GET `/payment/return` với responseCode != "00"
- **Expected:**
  - Status 200
  - Response `status = "FAILED"`
  - Redis keys được cleanup

#### TC-PAYMENT-009: VNPay Return URL - Invalid checksum

- **Mục đích:** Verify security
- **Steps:**
  1. GET `/payment/return` với checksum sai
- **Expected:**
  - Status 200
  - Response `status = "FAILED"` hoặc error

#### TC-PAYMENT-010: WebSocket Broadcast khi thanh toán thành công

- **Mục đích:** Verify WebSocket message được gửi khi payment thành công
- **Precondition:**
  - Order PENDING với tickets
  - Có WebSocket client đang subscribe `/topic/seat/{showtimeId}`
- **Steps:**
  1. Setup WebSocket client subscribe topic `/topic/seat/{showtimeId}`
  2. GET `/payment/ipn` với payment success params
  3. Verify WebSocket message được nhận
- **Expected:**
  - WebSocket client nhận được message
  - Message có structure:
    ```json
    {
      "seats": [
        {
          "ticketId": 1,
          "rowIdx": 0,
          "columnIdx": 0,
          "seatType": "NORMAL",
          "status": "BOOKED"
        }
      ],
      "status": "BOOKED",
      "showtimeId": 123
    }
    ```
  - Tất cả tickets trong order được include trong message
  - Message được gửi ngay sau khi payment status được update thành COMPLETED
  - Frontend client cập nhật UI để hiển thị ghế đã BOOKED

---

### **PHẦN 7: ORDER CONFIRMATION & QUERY**

#### TC-ORDER-001: Lấy danh sách orders

- **Mục đích:** Verify query orders
- **Steps:**
  1. GET `/orders` với pagination
- **Expected:**
  - Status 200
  - Response có pagination info
  - Mỗi order có: `orderId`, `createdAt`, `userName`, `movieName`, `showtimeStart`, `roomName`, `seats`, `totalPrice`, `status`

#### TC-ORDER-002: Search orders theo date

- **Mục đích:** Verify filter by date
- **Steps:**
  1. POST `/orders/search-by-date` với date
- **Expected:**
  - Status 200
  - Chỉ trả về orders trong ngày đó

#### TC-ORDER-003: Search orders theo userId

- **Mục đích:** Verify filter by user
- **Steps:**
  1. POST `/orders/search-by-date` với userId
- **Expected:**
  - Status 200
  - Chỉ trả về orders của user đó

#### TC-ORDER-004: Lấy ticket details

- **Mục đích:** Verify có thể query ticket info
- **Precondition:** Order đã COMPLETED, có tickets
- **Steps:**
  1. GET `/bookings/tickets/details?ids=1,2,3`
- **Expected:**
  - Status 200
  - Response là array TicketDetailResponse
  - Mỗi ticket có đầy đủ thông tin: seat, showtime, movie, price

---

### **PHẦN 8: EDGE CASES & ERROR SCENARIOS**

#### TC-EDGE-001: User hold ghế nhưng không checkout trước khi TTL hết

- **Mục đích:** Verify cleanup mechanism
- **Steps:**
  1. User hold ghế
  2. Chờ TTL hết (không checkout)
  3. Cố checkout sau khi TTL hết
- **Expected:**
  - Checkout fail vì seat hold đã hết
  - OrderSession không còn

#### TC-EDGE-002: Multiple users checkout cùng lúc với ghế overlap

- **Mục đích:** Verify transaction isolation
- **Precondition:**
  - User A hold ghế 1,2
  - User B hold ghế 2,3 (ghế 2 conflict)
- **Steps:**
  1. User A checkout
  2. User B checkout (cùng lúc)
- **Expected:**
  - Chỉ 1 user thành công
  - User còn lại nhận error
  - Không có data corruption

#### TC-EDGE-003: Checkout với OrderSession đã expire

- **Mục đích:** Verify không thể checkout khi session hết hạn
- **Steps:**
  1. Tạo OrderSession
  2. Chờ TTL hết
  3. POST checkout
- **Expected:**
  - Status 400 hoặc error về session expired

#### TC-EDGE-004: Payment callback được gọi nhiều lần (duplicate)

- **Mục đích:** Verify idempotency
- **Precondition:** Order đã COMPLETED
- **Steps:**
  1. GET `/payment/ipn` lại với cùng params
- **Expected:**
  - Response "Order already confirmed"
  - Order không bị update lại
  - Không có duplicate transactions

#### TC-EDGE-005: User logout trong khi đang hold ghế

- **Mục đích:** Verify seat hold vẫn tồn tại (không phụ thuộc session)
- **Steps:**
  1. User login và hold ghế
  2. User logout (token invalid)
  3. User khác cố chọn ghế đó
- **Expected:**
  - Ghế vẫn bị hold bởi user đầu
  - User khác không thể chọn
  - (Seat hold chỉ expire theo TTL)

#### TC-EDGE-006: Network timeout khi đang checkout

- **Mục đích:** Verify transaction rollback
- **Steps:**
  1. POST checkout
  2. Simulate network timeout trước khi response
- **Expected:**
  - Order có thể ở trạng thái PENDING
  - Cần có mechanism để cleanup orders PENDING quá lâu

#### TC-EDGE-007: Redis down khi đang hold ghế

- **Mục đích:** Verify graceful degradation
- **Steps:**
  1. Simulate Redis connection error
  2. SELECT_SEAT
- **Expected:**
  - System handle error gracefully
  - User nhận error message
  - Không crash application

#### TC-EDGE-008: Database transaction rollback khi payment callback

- **Mục đích:** Verify transaction consistency
- **Steps:**
  1. Simulate DB error trong IPN callback
  2. GET `/payment/ipn`
- **Expected:**
  - Transaction rollback
  - Order không bị update một phần
  - Error được log

---

### **PHẦN 9: PERFORMANCE & CONCURRENCY**

#### TC-PERF-001: 100 users cùng chọn ghế trong 1 showtime

- **Mục đích:** Verify system handle concurrent load
- **Steps:**
  1. 100 concurrent requests SELECT_SEAT
- **Expected:**
  - Không có deadlock
  - Tất cả requests được xử lý
  - Không có race condition
  - Response time < 2s

#### TC-PERF-002: Stress test checkout endpoint

- **Mục đích:** Verify checkout performance
- **Steps:**
  1. 50 concurrent checkout requests
- **Expected:**
  - Tất cả được xử lý
  - Không có duplicate orders
  - Response time acceptable

#### TC-PERF-003: Load test payment callback

- **Mục đích:** Verify IPN endpoint performance
- **Steps:**
  1. 100 concurrent IPN callbacks
- **Expected:**
  - Tất cả được xử lý
  - Không có data corruption
  - Idempotency được đảm bảo

---

### **PHẦN 10: DATA VALIDATION**

#### TC-VALID-001: Checkout với userId không tồn tại

- **Mục đích:** Verify validation
- **Steps:**
  1. POST checkout với userId = 99999
- **Expected:**
  - Status 404 hoặc error

#### TC-VALID-002: Checkout với showtimeId không tồn tại

- **Mục đích:** Verify validation
- **Steps:**
  1. POST checkout với showtimeId = 99999
- **Expected:**
  - Status 404 hoặc error

#### TC-VALID-003: Checkout với ticketIds rỗng

- **Mục đích:** Verify business rule
- **Steps:**
  1. POST checkout với ticketIds = []
- **Expected:**
  - Status 400 hoặc error "Must select at least 1 seat"

#### TC-VALID-004: Checkout với amount < totalPrice

- **Mục đích:** Verify amount validation
- **Steps:**
  1. POST checkout với amount < totalPrice
- **Expected:**
  - Status 400 hoặc error

#### TC-VALID-005: Checkout với discount > totalPrice

- **Mục đích:** Verify discount validation
- **Steps:**
  1. POST checkout với discount > totalPrice
- **Expected:**
  - Status 400 hoặc error

---

## 📊 TỔNG KẾT

### **Số lượng test cases theo category:**

- **Authentication & Authorization:** 4 test cases
- **Browse Movies & Showtimes:** 5 test cases
- **Seat Selection:** 11 test cases
- **Order Session & Concessions:** 8 test cases
- **Checkout & Payment:** 7 test cases
- **VNPay Payment Callback:** 10 test cases (bao gồm WebSocket broadcast)
- **Order Confirmation & Query:** 4 test cases
- **Edge Cases & Error Scenarios:** 8 test cases
- **Performance & Concurrency:** 3 test cases
- **Data Validation:** 5 test cases

**TỔNG CỘNG: 65 test cases**

### **Priority:**

- **P0 (Critical):** TC-AUTH-001, TC-SEAT-001, TC-SEAT-003, TC-CHECKOUT-001, TC-PAYMENT-001, TC-PAYMENT-002
- **P1 (High):** Tất cả test cases trong phần 1-6
- **P2 (Medium):** Edge cases và performance tests
- **P3 (Low):** Validation tests (một số có thể cover bằng unit test)

### **Test Environment Requirements:**

1. **Database:** Test DB với test data (movies, showtimes, seats, users)
2. **Redis:** Test Redis instance cho seat hold và order session
3. **WebSocket:** Test WebSocket connection cho seat selection
4. **VNPay Sandbox:** Test payment với VNPay sandbox environment
5. **Mock Services:** Có thể mock VNPay nếu cần

### **Test Data Setup:**

- 1 test user với credentials hợp lệ
- 1 movie với showtimes trong tương lai
- 1 showtime với ít nhất 10 seats AVAILABLE
- 1 payment method VNPAY active
- 1-2 concessions với stock > 0

---

## 🔍 NOTES QUAN TRỌNG

1. **WebSocket Testing:**
   - Cần test WebSocket riêng hoặc mock service layer
   - Khi test payment callback, cần verify WebSocket message được gửi đến đúng topic
   - Có thể sử dụng `SimpMessagingTemplate` mock để verify `convertAndSend` được gọi với đúng parameters
   - Test case TC-PAYMENT-001 cần verify WebSocket broadcast khi thanh toán thành công
2. **Redis TTL:** Có thể mock hoặc dùng Redis với TTL ngắn cho test
3. **VNPay Callback:** Cần mock hoặc dùng VNPay sandbox
4. **Concurrent Tests:** Cần chạy với thread-safe assertions
5. **Transaction Rollback:** Đảm bảo test data được cleanup sau mỗi test
6. **Idempotency:** Đặc biệt quan trọng với payment callbacks
7. **WebSocket Broadcast on Payment:**
   - Khi payment thành công, hệ thống tự động gửi WebSocket message với status "BOOKED"
   - Message được gửi đến `/topic/seat/{showtimeId}` để tất cả clients đang xem showtime đó nhận được cập nhật
   - Frontend clients sẽ tự động cập nhật UI để hiển thị ghế đã được đặt
