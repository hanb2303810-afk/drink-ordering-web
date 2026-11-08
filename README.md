# Drink Ordering Web — CT240 Nhóm 04 

**Học phần:** Nguyên lý xây dựng phần mềm (CT240) · **Nguồn:** web hóa đề tài console
[Hệ thống quản lý và đặt nước tại quán](https://github.com/duyb2405024/CT240T-2026-Group04-Project)

---

## 1. Giới thiệu

Web **client-server** đặt nước tại quán: khách đặt nước, đơn vào hàng đợi, nhiều
barista xử lý **song song bằng đa luồng** — tái sử dụng domain Java nguyên vẹn
(`Drink/Coffee/MilkTea`, `Order/OrderItem/OrderStatus`, `OrderQueue`, `Barista`,
`InMemoryRepository`, `DrinkService`, `OrderService`, exception tùy chỉnh) có sẵn
coverage ~98.8% từ repo console của nhóm.

- **Server:** Spring Boot 3.5 (compiled ở mức **Java 17** như đề bài), Maven, JUnit 5,
  Mockito, JaCoCo — giữ ngưỡng **Line ≥ 80%, Branch ≥ 75%**.
- **Client:** Vite + React + TypeScript + React Router, gọi API cùng origin qua proxy dev.
- **Realtime:** server phát SSE `/api/stream/orders` — một luồng poll snapshot đơn,
  đơn đổi trạng thái (do thread Barista) được đẩy tới mọi client trong ~300ms.

Domain Java được tái sử dụng nguyên vẹn từ repo console; phía web chỉ bọc REST + SSE.

## 2. Cấu trúc

```
drink-ordering-web/
├── server/                     # Spring Boot
│   └── src/main/java/com/group04/
│       ├── *.java              # DOMAIN: tái sử dụng nguyên ven từ repo console (không sửa logic)
│       ├── config/             # Bean domain, CORS, interceptor, seed menu/tài khoản, SSE
│       ├── auth/               # JwtUtil (HMAC-SHA256 tự viết), AuthInterceptor, Role
│       ├── controller/         # Auth, Drink, Order, Admin, Stream(SSE), GlobalExceptionHandler
│       ├── dto/                # record + mapper domain → JSON
│       ├── realtime/           # RealtimeService: poll + phát SSE
│       └── store/              # UserAccount/UserStore phân quyền in-memory
└── client/                     # Vite + React + TS
    └── src/pages/              # MenuPage (khách), BaristaPage, AdminPage, LoginPage
```

## 3. Phân quyền

| Ai | Gì |
|---|---|
| Khách vãng lai | xem menu, đặt nước, theo dõi đơn realtime (SSE public) |
| `SERVER` (phục vụ) | đặt đơn, xem đơn (tài khoản demo `phucvu1/pass123`) |
| `BARISTA` | quầy pha chế, xem tổng quát đơn (`barista1/pass123`) |
| `ADMIN` | CRUD menu, gán vai trò, lịch sử đơn, thống kê (`admin/admin123`) |

Server chặn bằng `AuthInterceptor`:
`GET /api/drinks/**` + `/api/stream/**` + `/api/auth/**` public;
`/api/admin/**` chỉ ADMIN; còn lại yêu cầu đăng nhập.

## 4. API

| Method | Path | Ý nghĩa |
|---|---|---|
| POST | `/api/auth/login` | đăng nhập, trả JWT nhẹ |
| GET | `/api/drinks` (`/{id}`) | menu (public) |
| POST/PUT/DELETE | `/api/drinks` | CRUD menu (ADMIN) |
| POST | `/api/orders` | đặt đơn: tạo + thêm món + đẩy `OrderQueue` |
| GET | `/api/orders`, `/{id}` | danh sách/chi tiết đơn |
| GET | `/api/stream/orders` | **SSE realtime** bảng đơn |
| GET | `/api/admin/drinks` | menu (ADMIN) |
| GET/PUT | `/api/admin/users`, `/{u}/role` | tài khoản & vai trò |
| GET | `/api/admin/orders`, `/stats` | lịch sử + thống kê |

## 5. Chạy

Yêu cầu JDK 21/25 (deploy thật đề nghị JDK 21 theo đề bài; `mvn` hoặc dùng Maven
đi kèm IntelliJ). Không dùng DB — toàn bộ dữ liệu in-memory nên restart mất danh sách đơn.

```bash
# Server (8080)
cd server
mvn spring-boot:run       # hoặc chạy class DrinkOrderingApplication trong IDE

# Client (5173, proxy /api -> 8080)
cd client
npm install
npm run dev
```

Mở http://localhost:5173 — đặt nước ở "Bảng món", mở `/barista` (đăng nhập barista)
nhiều tab để thấy 2+ barista nhận đơn song song, `/admin` (admin) quản menu/vai trò/thống kê.

## 6. Kiểm thử

```bash
cd server
mvn verify    # chạy toàn bộ test + JaCoCo, báo lỗi nếu coverage dưới ngưỡng 80%/75%
```

- 12 lớp test domain của nhóm giữ nguyên hành vi (chỉ thêm `package com.group04`).
- Bổ sung: `OrderProcessingServiceTest` (lớp domain nhóm chưa có test), test web
  (`ServerApiTest`, `JwtUtilTest`, `RealtimeServiceTest`) chừng ~40 test REST/auth/SSE.
- **Lưu ý JaCoCo:** pom gốc nhóm pin 0.8.11 (máy nhóm JDK 21). Máy phát triển này chỉ
  có JDK 25 nên dùng 0.8.14; ngưỡng không đổi, trên máy JDK 21 hạ về 0.8.11 vẫn đạt.

## 7. Quy trình Git

Giữ nguyên Feature Branch Workflow của nhóm: `feature/`, `fix/`, `docs/`…
commit `feat(<scope>): <mô tả>`, PR + review, không commit thẳng `main`.
Mỗi PR tự chạy CI (mục 13): server `mvn verify` + client build phải xanh.

## 8. Khác biệt so với kịch bản console gốc

- Không dùng Spring Security (bộ starter nặng): JWT nhẹ tự viết + interceptor,
  đủ cho phân quyền demo; password lưu plain-text **chỉ demo** (chạy thật cần BCrypt).
- Barista xử lý **tự động** từ `OrderQueue` (đúng domain), không có endpoint
  "claim đơn" như bản kế hoạch — trang barista chỉ quan sát realtime.
- Số barista mặc định 2 (cấu hình `app.baristas` trong `application.yml`).

## 9. Cài đặt môi trường cho thành viên (làm 1 lần)

| Cài | Phiên bản | Dùng cho |
|---|---|---|
| Git | bản mới nhất | clone, branch, PR |
| JDK 21 (Temurin) | **21** — tránh JDK 25 để khớp JaCoCo 0.8.11/0.8.14 nhóm | build + chạy server |
| IntelliJ IDEA Community | bản mới | mở `server/` (Maven tích hợp sẵn, không cần cài Maven) |
| Node.js LTS | ≥ 20 | client (npm kèm theo Node) |

**Không cần cài database** — toàn bộ món/đơn/tài khoản là in-memory, seed lúc khởi động.

## 10. Chạy lần đầu (onboarding mỗi người)

```bash
git clone https://github.com/hanb2303810-afk/drink-ordering-web.git
cd drink-ordering-web

# Terminal 1 — server (8080), dùng IDE mở file server/pom.xml chạy
# DrinkOrderingApplication, hoặc dùng lệnh:
cd server
mvn spring-boot:run

# Terminal 2 — client (5173, tự proxy /api về 8080)
cd client
npm install
npm run dev
```

**Kiểm tra thành công:** mở http://localhost:5173 thấy menu tiếng Việt → chọn món,
nhập tên, "Đặt nước" → đơn hiện ngay trong bảng "Theo dõi pha chế" → ~2 giây sau
thành "Hoàn thành". Mở http://localhost:5173/barista để xem quầy barista.

**Hỗ trợ server bằng IDE:** IntelliJ các thao tác Maven chỉ cần mở panel Maven →
Lifecycle → `verify` để chạy test + kiểm coverage thay cho `mvn verify`.

## 11. Sử dụng — các vai trò trong demo

| Vai trò | Đăng nhập | Làm gì trên web |
|---|---|---|
| Khách vãng lai | không cần | "Bảng món" đặt nước, theo dõi đơn realtime |
| Nhân viên phục vụ | `phucvu1/pass123` | đặt nước hộ khách, theo dõi toàn đơn |
| Barista | `barista1/pass123` | `/barista` — quầy pha chế, thấy đơn chảy qua PENDING → PREPARING → COMPLETED |
| Quản trị | `admin/admin123` | `/admin` — CRUD menu, gán vai trò, lịch sử đơn, thống kê bán chạy |

Demo điểm chính của đề tài (đa luồng): mở **2 tab barista** (barista1 + barista2),
đặt 4–6 đơn liên tục → thấy nhiều barista nhận đơn song song, `orderId` không trùng.

## 12. Bản đồ vùng code — ai sửa đâu

| Vùng | File | Nguyên tắc |
|---|---|---|
| **DOMAIN** (chính là phần bài nộp console) | `server/src/main/java/com/group04/{Drink,Order,OrderQueue,Barista,...}.java` | **hạn chế sửa**; cần đổi phải báo cả nhóm vì đồng bộ với repo console |
| API + SSE + auth | `server/.../controller/`, `auth/`, `realtime/`, `store/`, `dto/` | vùng tính năng web mới — thoải mái mở branch riêng |
| Trang khách / barista | `client/src/pages/MenuPage.tsx`, `BaristaPage.tsx`, `App.tsx` | |
| Trang quản trị | `server/.../controller/AdminController.java`, `client/src/pages/AdminPage.tsx` | |
| Giao diện/tokens | `client/src/index.css` | đổi màu/chi tiết chung cẩn thận đụng nhiều trang |

Trước khi tạo PR luôn phải xanh cả hai: `mvn verify` (server) và `npm run build` (client).

## 13. CI tự động (GitHub Actions)

Workflow `.github/workflows/ci.yml` chạy 2 job song song cho mỗi PR/commit vào `main`:

- **server**: JDK 21 (Temurin) + cache Maven → `mvn verify` (test toàn bộ + chặn coverage ≥ 80%/75%)
- **client**: Node 20 + cache npm → `npm ci && npm run build`

PR đỏ trên checks: sửa đến khi xanh rồi mới xin review — reviewer chỉ nhìn dấu check.

