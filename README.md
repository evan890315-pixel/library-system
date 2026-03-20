library-system/
├── DB/
│   ├── 01_DDL.sql              # 建表語句（4張資料表）
│   ├── 02_DML.sql              # 種子資料（8本書、16筆庫存）
│   └── 03_StoredProcedures.sql # 7支 Stored Procedures
├── backend/                    # Spring Boot 3.2 (Java 17)
│   ├── pom.xml
│   └── src/main/java/com/library/
│       ├── common/             # 共用層：ApiResponse、JwtUtil、XssFilter
│       ├── config/             # SecurityConfig、JwtAuthFilter
│       ├── controller/         # 展示層：Auth / Book / Borrow
│       ├── service/            # 業務層：Auth / Book / Borrow
│       ├── repository/         # 資料層：透過 SimpleJdbcCall 呼叫 SP
│       ├── model/              # 實體：User / Book / BorrowingRecord
│       ├── dto/                # 請求/回應物件：AuthDto / BorrowDto
│       └── exception/          # GlobalExceptionHandler
└── frontend/                   # Vue 3 + Vite
    └── src/
        ├── api/index.js        # Axios instance + 所有 API 呼叫
        ├── stores/auth.js      # Pinia 狀態管理
        ├── router/index.js     # Vue Router（含 requiresAuth guard）
        ├── views/              # HomeView / LoginView / RegisterView / MyBooksView
        └── assets/main.css     # 設計系統 CSS 變數
