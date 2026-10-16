-- ============================================================
-- Datos iniciales para la aplicacion de gestion de productos
-- Se ejecuta al iniciar la aplicacion (modo desarrollo/pruebas)
-- ============================================================

INSERT INTO productos (nombre, descripcion, precio, stock, categoria, activo, fecha_creacion, fecha_actualizacion)
VALUES
    ('Laptop Pro 15', 'Laptop de alto rendimiento con procesador Intel Core i7 y 16GB RAM', 1299.99, 25, 'Electronica', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Teclado Mecanico RGB', 'Teclado mecanico con retroiluminacion RGB y switches Cherry MX', 89.99, 50, 'Perifericos', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Monitor 4K 27"', 'Monitor UHD con panel IPS y frecuencia de refresco de 144Hz', 499.99, 15, 'Electronica', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Raton Inalambrico', 'Raton ergonomico con sensor de 4000 DPI y bateria de larga duracion', 45.50, 80, 'Perifericos', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Silla Ergonomica', 'Silla de oficina con soporte lumbar ajustable y reposabrazos 4D', 350.00, 10, 'Mobiliario', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Auriculares Noise Cancelling', 'Auriculares bluetooth con cancelacion activa de ruido y 30h de bateria', 199.99, 30, 'Audio', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Disco SSD 1TB', 'Disco solido NVMe M.2 con velocidades de lectura hasta 7000 MB/s', 129.99, 3, 'Almacenamiento', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('Webcam Full HD', 'Camara web 1080p con microfono integrado y autofocus', 79.99, 0, 'Perifericos', false, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
