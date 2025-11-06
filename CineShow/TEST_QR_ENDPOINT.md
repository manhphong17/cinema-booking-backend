# Test QR Payload Endpoint

## Endpoint đã fix
✅ `GET /api/orders/{id}/qr-payload`

## Những gì đã sửa
1. **@Transactional annotation**: Đổi sang `@Transactional(readOnly = true)`
2. **LocalDateTime serialization**: Convert sang ISO string (`toString()`) để tránh lỗi Jackson
3. **Missing variables**: Định nghĩa đầy đủ `ticketCodes`, `paymentMethods`, `qrExpiryAt`, `payloadJson`
4. **QR secret config**: Thêm `qr.secret` vào `application.yml`

## Cách test

### 1. Start application
```bash
# Nếu có Maven cài sẵn
mvn spring-boot:run

# Hoặc chạy từ IDE (Run CineShowApplication.java)
```

### 2. Test endpoint
```bash
# Thay {id} bằng ID order thực tế trong DB
curl -X GET http://localhost:8885/api/orders/1/qr-payload
```

### 3. Response mẫu
```json
{
  "orderId": 1,
  "userId": 123,
  "createdAt": "2025-10-31T09:00:00",
  "totalPrice": 150000.0,
  "status": "COMPLETED",
  "orderCode": "ORD001",
  "reservationCode": "TXN123456",
  "movieName": "Avengers",
  "roomName": "Room 1",
  "showtimeStart": "2025-11-01T19:00:00",
  "showtimeEnd": "2025-11-01T21:30:00",
  "seats": ["I7", "I8"],
  "ticketCodes": [],
  "paymentMethods": ["VNPAY"],
  "qrAvailable": true,
  "qrExpired": false,
  "regenerateAllowed": true,
  "graceMinutes": 30,
  "qrExpiryAt": "2025-10-31T09:30:00Z",
  "qrJwt": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "qrImageUrl": null,
  "payloadJson": "{\"ver\":1,\"nonce\":\"...\",\"exp\":...,\"order\":{...},\"showtime\":{...},\"seats\":[...]}",
  "nonce": "uuid-string",
  "version": 1
}
```

## Điều kiện để tạo QR thành công
- ✅ Order tồn tại (ID hợp lệ)
- ✅ Order không bị CANCELED
- ✅ Có dữ liệu vé (tickets) để lấy showtime, ghế
- ✅ Config `qr.secret` đã được set

## JWT Payload structure
```json
{
  "ver": 1,
  "nonce": "uuid",
  "exp": 1730360400,
  "order": {
    "orderId": 1,
    "orderCode": "ORD001",
    "reservationCode": "TXN123456",
    "status": "COMPLETED"
  },
  "showtime": {
    "movie": "Avengers",
    "room": "Room 1",
    "start": "2025-11-01T19:00:00",
    "end": "2025-11-01T21:30:00"
  },
  "seats": ["I7", "I8"]
}
```

## Lưu ý
- `reservationCode` ưu tiên lấy từ `payments.transactionNo` hoặc `txnRef`, nếu không có sẽ fallback về `order.code` hoặc `order.id`
- QR JWT được ký bằng HS256 với secret từ config
- Thời gian hết hạn QR mặc định: 30 phút (có thể config)
- Server port: **8885** (theo application.yml)
