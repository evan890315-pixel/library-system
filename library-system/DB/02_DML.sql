-- ============================================================
-- Library Borrowing System - DML (Seed Data)
-- ============================================================

USE library_system;

-- ============================================================
-- Seed: book
-- ============================================================
INSERT INTO book (isbn, name, author, introduction, publisher, publish_date) VALUES
('9780132350884', 'Clean Code',
 'Robert C. Martin',
 '本書是軟體工程師必讀的經典著作，教導如何撰寫可維護、易讀的程式碼。',
 'Prentice Hall', '2008-08-01'),
('9780201633610', 'Design Patterns',
 'Gang of Four',
 '介紹23種經典設計模式，是物件導向設計的聖經。',
 'Addison-Wesley', '1994-10-21'),
('9780596517748', 'JavaScript: The Good Parts',
 'Douglas Crockford',
 '深入探討 JavaScript 的精華部分，幫助開發者寫出更好的程式。',
 'O''Reilly Media', '2008-05-01'),
('9781491950357', 'Learning React',
 'Alex Banks, Eve Porcello',
 '全面介紹 React 函式庫的使用方式，適合前端開發者學習。',
 'O''Reilly Media', '2017-05-01'),
('9781617294945', 'Spring in Action',
 'Craig Walls',
 '深入淺出介紹 Spring 框架的核心概念與實際應用。',
 'Manning Publications', '2018-10-01'),
('9780134757599', 'Refactoring',
 'Martin Fowler',
 '重構是改善現有程式碼設計的過程，本書提供系統性的重構技巧。',
 'Addison-Wesley', '2018-11-19'),
('9781492056300', 'Kubernetes: Up and Running',
 'Brendan Burns, Joe Beda, Kelsey Hightower',
 '學習如何使用 Kubernetes 容器編排平台管理雲端應用程式。',
 'O''Reilly Media', '2019-09-01'),
('9780201485677', 'The Pragmatic Programmer',
 'Andrew Hunt, David Thomas',
 '務實的程式設計師應具備的思維與技術，是軟體工程的必讀經典。',
 'Addison-Wesley', '1999-10-20');

-- ============================================================
-- Seed: inventory (每本書各放 2-3 冊)
-- ============================================================
INSERT INTO inventory (isbn, store_time, status, location) VALUES
-- Clean Code
('9780132350884', '2023-01-10 09:00:00', 'AVAILABLE', 'A-001'),
('9780132350884', '2023-01-10 09:00:00', 'AVAILABLE', 'A-002'),
-- Design Patterns
('9780201633610', '2023-01-15 10:00:00', 'AVAILABLE', 'A-010'),
('9780201633610', '2023-01-15 10:00:00', 'AVAILABLE', 'A-011'),
-- JavaScript: The Good Parts
('9780596517748', '2023-02-01 11:00:00', 'AVAILABLE', 'B-001'),
('9780596517748', '2023-02-01 11:00:00', 'AVAILABLE', 'B-002'),
-- Learning React
('9781491950357', '2023-02-15 09:30:00', 'AVAILABLE', 'B-010'),
('9781491950357', '2023-02-15 09:30:00', 'AVAILABLE', 'B-011'),
-- Spring in Action
('9781617294945', '2023-03-01 08:00:00', 'AVAILABLE', 'C-001'),
('9781617294945', '2023-03-01 08:00:00', 'AVAILABLE', 'C-002'),
-- Refactoring
('9780134757599', '2023-03-10 14:00:00', 'AVAILABLE', 'C-010'),
('9780134757599', '2023-03-10 14:00:00', 'AVAILABLE', 'C-011'),
-- Kubernetes
('9781492056300', '2023-04-01 10:00:00', 'AVAILABLE', 'D-001'),
('9781492056300', '2023-04-01 10:00:00', 'AVAILABLE', 'D-002'),
-- Pragmatic Programmer
('9780201485677', '2023-04-15 09:00:00', 'AVAILABLE', 'D-010'),
('9780201485677', '2023-04-15 09:00:00', 'AVAILABLE', 'D-011');
