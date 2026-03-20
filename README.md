# 圖書借閱系統

線上圖書借閱系統，提供使用者註冊、登入、借書、還書及借閱紀錄查詢功能。

---

## 技術架構

| 層級 | 技術 |
|---|---|
| 前端 | Vue 3 + Vite + Pinia + Vue Router |
| 後端 | Spring Boot 3.2 + Spring Security + JWT |
| 資料庫 | MySQL 8.0 |
| 建置工具 | Maven |

三層式架構：展示層（Controller）→ 業務層（Service）→ 資料層（Repository），資料存取全程透過 Stored Procedure。

---

## 專案結構

```
library-system/
├── DB/
│   ├── 01_DDL.sql                  # 建立資料表
│   ├── 02_DML.sql                  # 種子資料
│   └── 03_StoredProcedures.sql     # Stored Procedures
├── backend/                        # Spring Boot
│   ├── pom.xml
│   └── src/main/java/com/library/
│       ├── common/                 # 共用層：ApiResponse、JwtUtil、XssFilter
│       ├── config/                 # SecurityConfig、JwtAuthFilter
│       ├── controller/             # 展示層
│       ├── service/                # 業務層
│       ├── repository/             # 資料層
│       ├── model/                  # 實體類別
│       ├── dto/                    # 請求與回應物件
│       └── exception/              # 全域例外處理
└── frontend/                       # Vue 3
    └── src/
        ├── api/                    # Axios 封裝
        ├── stores/                 # Pinia 狀態管理
        ├── router/                 # Vue Router
        ├── views/                  # 頁面元件
        └── assets/                 # 全域樣式
```

---

## 環境需求

- Java 17 以上
- Node.js 18 以上
- MySQL 8.0 以上
- Maven 3.6 以上

---

## 啟動步驟

### 1. 建立資料庫

在 MySQL 依序執行以下三個檔案：

```sql
DB/01_DDL.sql               -- 建立資料表
DB/02_DML.sql               -- 匯入種子資料
DB/03_StoredProcedures.sql  -- 建立 Stored Procedures
```

### 2. 設定後端

編輯 `backend/src/main/resources/application.yml`：

```yaml
server:
  port: 8083                        # 可依需求修改

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/library_system?useUnicode=true&serverTimezone=Asia/Taipei&useSSL=false&allowPublicKeyRetrieval=true
    username: root                  # 改成你的 MySQL 帳號
    password: your_password         # 改成你的 MySQL 密碼
```

### 3. 啟動後端

```bash
cd backend
mvn spring-boot:run
```

看到以下訊息表示啟動成功：

```
Started LibraryApplication in x.xxx seconds
```

### 4. 設定前端

編輯 `frontend/vite.config.js`，確認 proxy 指向後端 port：

```js
proxy: {
  '/api': {
    target: 'http://localhost:8083',  // 與 application.yml 的 port 一致
    changeOrigin: true
  }
}
```

### 5. 啟動前端

```bash
cd frontend
npm install
npm run dev
```

啟動後開啟瀏覽器：`http://localhost:5173`

---

## API 端點

| 方法 | 路徑 | 說明 | 需登入 |
|---|---|---|---|
| POST | `/api/auth/register` | 註冊 | 否 |
| POST | `/api/auth/login` | 登入 | 否 |
| GET | `/api/books` | 取得所有書籍 | 否 |
| POST | `/api/borrow` | 借書 | 是 |
| POST | `/api/borrow/return` | 還書 | 是 |
| GET | `/api/borrow/my` | 我的借閱紀錄 | 是 |

### 請求範例

**註冊**
```json
POST /api/auth/register
{
  "phoneNumber": "0912345678",
  "password": "password123",
  "userName": "王小明"
}
```

**登入**
```json
POST /api/auth/login
{
  "phoneNumber": "0912345678",
  "password": "password123"
}
```

**借書**
```json
POST /api/borrow
Authorization: Bearer {token}
{
  "isbn": "9780132350884"
}
```

**還書**
```json
POST /api/borrow/return
Authorization: Bearer {token}
{
  "inventoryId": 1
}
```

---

## 資料庫設計

| 資料表 | 說明 |
|---|---|
| `users` | 使用者資料，密碼以 BCrypt 儲存 |
| `book` | 書籍基本資料（ISBN、書名、作者、簡介） |
| `inventory` | 書籍庫存，每筆代表一本實體書，狀態包含在庫／出借中／整理中等 |
| `borrowing_record` | 借閱紀錄，記錄借出與歸還時間 |

---

## 安全性設計

- **密碼儲存**：BCrypt（cost factor 12），不儲存明碼
- **身份驗證**：JWT Token，有效期 24 小時
- **SQL Injection 防護**：全程透過 Stored Procedure 參數化存取，不拼接 SQL
- **XSS 防護**：XssFilter 對所有請求參數進行 HTML 轉義
- **Transaction**：借書與還書操作使用資料庫交易確保資料一致性，並透過 `SELECT FOR UPDATE` 防止並發問題

---

## 注意事項

- 每位使用者同一本書只能借閱一次，還書後才能再借
- 借閱期限為 30 天
- 還書後書籍狀態會先變為「整理中」，而非直接變回「可借閱」
