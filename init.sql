-- Создаем схему если она не существует
CREATE SCHEMA IF NOT EXISTS meat_shop;

-- Даем все права пользователю meat_admin
GRANT ALL ON SCHEMA meat_shop TO meat_admin;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA meat_shop TO meat_admin;

-- Создаем таблицу администраторов (если не существует)
CREATE TABLE IF NOT EXISTS meat_shop.admin_users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(255),
    role VARCHAR(50) DEFAULT 'ADMIN',
    is_active BOOLEAN DEFAULT true,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);

-- Создаем таблицу товаров (если не существует)
CREATE TABLE IF NOT EXISTS meat_shop.products (
    id BIGSERIAL PRIMARY KEY,
    article VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL DEFAULT 0,
    weight NUMERIC(5, 3),
    category VARCHAR(100),
    is_on_sale BOOLEAN DEFAULT false,
    sale_price NUMERIC(10, 2),
    image_url VARCHAR(500),
    is_deleted BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP
);

-- Создаем первого администратора (пароль: admin123)
-- Пароль должен быть захеширован с помощью BCrypt
-- Чтобы получить хеш пароля "admin123": https://www.bcryptcalculator.com/
INSERT INTO meat_shop.admin_users
(username, email, password_hash, full_name, role, is_active)
VALUES
('admin', 'admin@meatshop.ru', '$2a$10$YourHashedPasswordHere', 'Главный администратор', 'SUPER_ADMIN', true)
ON CONFLICT (username) DO NOTHING;

-- Добавляем тестовые товары
INSERT INTO meat_shop.products
(article, name, description, price, quantity, category, weight)
VALUES
('BEEF-001', 'Говядина вырезка', 'Свежая говяжья вырезка высшего сорта', 850.00, 50, 'Говядина', 1.000),
('PORK-001', 'Свиная шея', 'Нежная свиная шея для шашлыка', 650.00, 30, 'Свинина', 1.500),
('CHICK-001', 'Куриное филе', 'Филе куриной грудки без кожи', 350.00, 100, 'Птица', 0.500)
ON CONFLICT (article) DO NOTHING;