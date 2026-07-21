-- Fase 5: tablas futuras para Orden de Servicio e inventario clinico.
-- Script preparado para una migracion posterior. No se ejecuta automaticamente.

CREATE TABLE IF NOT EXISTS service_order (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    sale_id INT NULL,
    appointment_id INT NULL,
    customer_id INT NOT NULL,
    customer_name VARCHAR(180) NOT NULL,
    pet_id INT NOT NULL,
    pet_name VARCHAR(180) NOT NULL,
    status_name VARCHAR(40) NOT NULL,
    service_id INT NOT NULL,
    service_name VARCHAR(180) NOT NULL,
    veterinarian VARCHAR(180) NOT NULL,
    service_date DATE NOT NULL,
    service_time TIME NOT NULL,
    duration_minutes INT NOT NULL,
    observations VARCHAR(1000) NULL,
    service_amount DECIMAL(12,2) NOT NULL,
    products_amount DECIMAL(12,2) NOT NULL,
    subtotal DECIMAL(12,2) NOT NULL,
    tax DECIMAL(12,2) NOT NULL,
    discount DECIMAL(12,2) NOT NULL,
    total DECIMAL(12,2) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS service_order_item (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_order_id BIGINT NOT NULL,
    service_id INT NOT NULL,
    service_name VARCHAR(180) NOT NULL,
    price DECIMAL(12,2) NOT NULL,
    CONSTRAINT fk_service_order_item_order
        FOREIGN KEY (service_order_id) REFERENCES service_order(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS inventory_consumption (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_order_id BIGINT NOT NULL,
    inventory_item_id INT NOT NULL,
    sku VARCHAR(80) NULL,
    product_name VARCHAR(180) NOT NULL,
    quantity INT NOT NULL,
    unit_cost DECIMAL(12,2) NOT NULL,
    unit_price DECIMAL(12,2) NOT NULL,
    user_name VARCHAR(180) NULL,
    consumed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_inventory_consumption_order
        FOREIGN KEY (service_order_id) REFERENCES service_order(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS service_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_order_id BIGINT NOT NULL,
    status_name VARCHAR(40) NOT NULL,
    description VARCHAR(600) NOT NULL,
    user_name VARCHAR(180) NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_service_history_order
        FOREIGN KEY (service_order_id) REFERENCES service_order(id)
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS service_product_template (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    service_id INT NOT NULL,
    inventory_item_id INT NOT NULL,
    suggested_quantity INT NOT NULL DEFAULT 1,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
