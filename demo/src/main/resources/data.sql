INSERT INTO categories (name, color) VALUES ('Work', '#dc3545');
INSERT INTO categories (name, color) VALUES ('Home', '#198754');
INSERT INTO categories (name, color) VALUES ('Study', '#ffc107');

-- Initial users (password: password)
INSERT INTO users (username, password, role, enabled) VALUES
('admin', '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFXyu6UIK.y3o.f.LJraH/7As0DE7b4a', 'ADMIN', TRUE),
('user',  '$2a$10$e0MYzXyjpJS7Pd0RVvHwHeFXyu6UIK.y3o.f.LJraH/7As0DE7b4a', 'USER',  TRUE);
