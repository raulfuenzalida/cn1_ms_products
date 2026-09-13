CREATE TABLE IF NOT EXISTS products (
    id_product BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(2000) NULL,
    id_filament BIGINT NOT NULL,
    filament_grams DECIMAL(10, 2) NOT NULL,
    printing_hours DECIMAL(10, 2) NOT NULL,
    profit_percentage DECIMAL(7, 2) NOT NULL,
    filament_price_snapshot DECIMAL(12, 4) NOT NULL,
    electricity_price_snapshot DECIMAL(12, 4) NOT NULL,
    consumption_kwh_snapshot DECIMAL(12, 4) NOT NULL,
    material_cost DECIMAL(12, 4) NOT NULL,
    electricity_cost DECIMAL(12, 4) NOT NULL,
    base_cost DECIMAL(12, 4) NOT NULL,
    profit_amount DECIMAL(12, 4) NOT NULL,
    final_price DECIMAL(12, 0) NOT NULL,
    status VARCHAR(20) NOT NULL,
    price_status VARCHAR(20) NOT NULL,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT chk_filament_grams_positive CHECK (filament_grams > 0),
    CONSTRAINT chk_printing_hours_positive CHECK (printing_hours > 0),
    CONSTRAINT chk_profit_percentage_range CHECK (profit_percentage >= 0),
    CONSTRAINT chk_product_status_valid CHECK (status IN ('ACTIVE', 'INACTIVE')),
    CONSTRAINT chk_price_status_valid CHECK (price_status IN ('CURRENT', 'OUTDATED'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_products_public_catalog ON products (status, price_status, final_price);
CREATE INDEX idx_products_id_filament ON products (id_filament);

CREATE TABLE IF NOT EXISTS tags (
    id_tag BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    UNIQUE KEY uk_tags_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_tags (
    id_product BIGINT NOT NULL,
    id_tag BIGINT NOT NULL,
    PRIMARY KEY (id_product, id_tag),
    CONSTRAINT fk_product_tags_product FOREIGN KEY (id_product) REFERENCES products (id_product),
    CONSTRAINT fk_product_tags_tag FOREIGN KEY (id_tag) REFERENCES tags (id_tag)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS product_images (
    id_product_image BIGINT AUTO_INCREMENT PRIMARY KEY,
    id_product BIGINT NOT NULL,
    image_type VARCHAR(20) NOT NULL,
    s3_key VARCHAR(512) NOT NULL,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_product_images_product FOREIGN KEY (id_product) REFERENCES products (id_product),
    CONSTRAINT chk_image_type_valid CHECK (image_type IN ('FRONT', 'SIDE', 'TOP'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_product_images_product_type ON product_images (id_product, image_type, is_active);
