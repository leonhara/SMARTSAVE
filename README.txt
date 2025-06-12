SmartSave - Guía de Inicio Rápido
Soy Leonel y te doy la bienvenida a SmartSave. Para ejecutar la aplicación deberás presionar doble click en el "SmartSave.bat" en la carpeta target.

Para que la aplicación funcione perfectamente, solo tienes que asegurarte de cumplir un par de requisitos. Del resto se encarga la propia app.

Requisitos Previos (¡Muy importante!)
Para que la instalación automática funcione, tu sistema necesita:

Java 21 o superior: La aplicación está construida sobre la plataforma Java.
Python 3.13 o superior: Necesitas tener Python instalado y añadido al PATH de tu sistema.
SmartSave necesita esta versión para una función de su script que se conecta a la API de Mercadona. Además necesita poder ejecutar el comando pip.
Instalación de Dependencias se hacen al arrancar

Al arrancar, SmartSave intentará instalar automáticamente las librerías de Python (mercapy y requests) que necesita. Si tienes Python y una conexión a internet, la aplicación se configurará sola.

Si por algún motivo la instalación automática falla, la aplicación te mostrará una advertencia. En ese caso, puedes instalar las dependencias manualmente abriendo una terminal en la carpeta de la aplicación y ejecutando:

pip install -r requirements.txt

BASE DE DATOS:
Usuario: root
Contraseña: laco

Copiar y pegar el script de SQL:


-- Crear la base de datos
CREATE DATABASE IF NOT EXISTS smartsave
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE smartsave;

-- =====================================================
-- TABLA: modalidades_ahorro
-- =====================================================
CREATE TABLE IF NOT EXISTS modalidades_ahorro (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(50) NOT NULL UNIQUE,
    descripcion VARCHAR(500) NOT NULL,
    factor_presupuesto DECIMAL(3,2) NOT NULL,
    prioridad_precio INT NOT NULL,
    prioridad_nutricion INT NOT NULL,
    INDEX idx_modalidad_nombre (nombre)
) ENGINE=InnoDB;

-- =====================================================
-- TABLA: usuarios
-- =====================================================
CREATE TABLE IF NOT EXISTS usuarios (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    nombre VARCHAR(100) NOT NULL,
    apellidos VARCHAR(100) NOT NULL,
    contrasena_hash VARCHAR(255) NOT NULL,
    fecha_registro DATE DEFAULT (CURDATE()),
    ultimo_login DATE,
    modalidad_ahorro VARCHAR(50) DEFAULT 'Equilibrado',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_nombre_apellidos (nombre, apellidos),
    FOREIGN KEY (modalidad_ahorro) REFERENCES modalidades_ahorro(nombre) ON UPDATE CASCADE
) ENGINE=InnoDB;

-- =====================================================
-- TABLA: productos
-- =====================================================
CREATE TABLE IF NOT EXISTS productos (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nombre VARCHAR(255) NOT NULL,
    marca VARCHAR(100) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    precio DECIMAL(10,2) NOT NULL,
    supermercado VARCHAR(100) NOT NULL,
    disponible BOOLEAN DEFAULT TRUE,
    calorias DECIMAL(8,2) DEFAULT 0,
    proteinas DECIMAL(8,2) DEFAULT 0,
    carbohidratos DECIMAL(8,2) DEFAULT 0,
    grasas DECIMAL(8,2) DEFAULT 0,
    fibra DECIMAL(8,2) DEFAULT 0,
    sodio DECIMAL(8,2) DEFAULT 0,
    azucares DECIMAL(8,2) DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_nombre (nombre),
    INDEX idx_categoria (categoria),
    INDEX idx_supermercado (supermercado),
    INDEX idx_precio (precio),
    INDEX idx_disponible (disponible),
    FULLTEXT(nombre, marca)
) ENGINE=InnoDB;

-- =====================================================
-- TABLA: perfiles_nutricionales
-- =====================================================
CREATE TABLE IF NOT EXISTS perfiles_nutricionales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    edad INT NOT NULL,
    peso DECIMAL(5,2) NOT NULL,
    altura DECIMAL(5,2) NOT NULL,
    sexo ENUM('M', 'F') NOT NULL,
    nivel_actividad VARCHAR(50) NOT NULL,
    calorias_diarias INT NOT NULL,
    imc DECIMAL(4,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_usuario (usuario_id),
    INDEX idx_imc (imc)
) ENGINE=InnoDB;

-- =====================================================
-- TABLA: restricciones_nutricionales
-- =====================================================
CREATE TABLE IF NOT EXISTS restricciones_nutricionales (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    perfil_id BIGINT NOT NULL,
    restriccion VARCHAR(100) NOT NULL,
    FOREIGN KEY (perfil_id) REFERENCES perfiles_nutricionales(id) ON DELETE CASCADE,
    UNIQUE KEY unique_perfil_restriccion (perfil_id, restriccion),
    INDEX idx_perfil (perfil_id)
) ENGINE=InnoDB;

-- =====================================================
-- TABLA: transacciones
-- =====================================================
CREATE TABLE IF NOT EXISTS transacciones (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    fecha DATE NOT NULL,
    descripcion VARCHAR(255) NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    monto DECIMAL(10,2) NOT NULL,
    tipo ENUM('Ingreso', 'Gasto') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    INDEX idx_usuario_fecha (usuario_id, fecha),
    INDEX idx_tipo (tipo),
    INDEX idx_categoria (categoria),
    INDEX idx_fecha (fecha)
) ENGINE=InnoDB;

-- =====================================================
-- TABLA: listas_compra
-- =====================================================
CREATE TABLE IF NOT EXISTS listas_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id BIGINT NOT NULL,
    nombre VARCHAR(255) NOT NULL,
    fecha_creacion DATE DEFAULT (CURDATE()),
    fecha_planificada DATE,
    modalidad_ahorro VARCHAR(50) NOT NULL,
    presupuesto_maximo DECIMAL(10,2) NOT NULL,
    completada BOOLEAN DEFAULT FALSE,
    transaccion_id_asociada BIGINT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (usuario_id) REFERENCES usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (modalidad_ahorro) REFERENCES modalidades_ahorro(nombre) ON UPDATE CASCADE,
    FOREIGN KEY (transaccion_id_asociada) REFERENCES transacciones(id) ON DELETE SET NULL,
    INDEX idx_usuario (usuario_id),
    INDEX idx_completada (completada),
    INDEX idx_fecha_planificada (fecha_planificada)
) ENGINE=InnoDB;

-- =====================================================
-- TABLA: items_compra
-- =====================================================
CREATE TABLE IF NOT EXISTS items_compra (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    lista_id BIGINT NOT NULL,
    producto_id BIGINT NOT NULL,
    cantidad INT NOT NULL DEFAULT 1,
    comprado BOOLEAN DEFAULT FALSE,
    notas TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (lista_id) REFERENCES listas_compra(id) ON DELETE CASCADE,
    FOREIGN KEY (producto_id) REFERENCES productos(id) ON DELETE CASCADE,
    INDEX idx_lista (lista_id),
    INDEX idx_producto (producto_id),
    INDEX idx_comprado (comprado),
    UNIQUE KEY unique_lista_producto (lista_id, producto_id)
) ENGINE=InnoDB;

-- =====================================================
-- VISTAS ÚTILES
-- =====================================================

DROP VIEW IF EXISTS vista_resumen_financiero;
CREATE VIEW vista_resumen_financiero AS
SELECT
    u.id as usuario_id,
    u.nombre,
    u.apellidos,
    COALESCE(ingresos.total, 0) as total_ingresos,
    COALESCE(gastos.total, 0) as total_gastos,
    COALESCE(ingresos.total, 0) - COALESCE(gastos.total, 0) as balance
FROM usuarios u
LEFT JOIN (
    SELECT usuario_id, SUM(monto) as total
    FROM transacciones
    WHERE tipo = 'Ingreso'
    GROUP BY usuario_id
) ingresos ON u.id = ingresos.usuario_id
LEFT JOIN (
    SELECT usuario_id, SUM(monto) as total
    FROM transacciones
    WHERE tipo = 'Gasto'
    GROUP BY usuario_id
) gastos ON u.id = gastos.usuario_id;

DROP VIEW IF EXISTS vista_listas_resumen;
CREATE VIEW vista_listas_resumen AS
SELECT
    lc.id,
    lc.nombre,
    lc.usuario_id,
    lc.fecha_creacion,
    lc.fecha_planificada,
    lc.modalidad_ahorro,
    lc.presupuesto_maximo,
    lc.completada,
    COUNT(ic.id) as total_items,
    SUM(CASE WHEN ic.comprado = TRUE THEN 1 ELSE 0 END) as items_comprados,
    ROUND((SUM(CASE WHEN ic.comprado = TRUE THEN 1 ELSE 0 END) * 100.0 / COUNT(ic.id)), 2) as porcentaje_progreso,
    SUM(p.precio * ic.cantidad) as coste_total
FROM listas_compra lc
LEFT JOIN items_compra ic ON lc.id = ic.lista_id
LEFT JOIN productos p ON ic.producto_id = p.id
GROUP BY lc.id, lc.nombre, lc.usuario_id, lc.fecha_creacion, lc.fecha_planificada, lc.modalidad_ahorro, lc.presupuesto_maximo, lc.completada;

DROP PROCEDURE IF EXISTS CalcularNutricionLista;
DELIMITER //
CREATE PROCEDURE CalcularNutricionLista(IN p_lista_id BIGINT)
BEGIN
    SELECT
        SUM(p.calorias * ic.cantidad) as calorias_totales,
        SUM(p.proteinas * ic.cantidad) as proteinas_totales,
        SUM(p.carbohidratos * ic.cantidad) as carbohidratos_totales,
        SUM(p.grasas * ic.cantidad) as grasas_totales
    FROM items_compra ic
    JOIN productos p ON ic.producto_id = p.id
    WHERE ic.lista_id = p_lista_id;
END //
DELIMITER ;

DROP PROCEDURE IF EXISTS GastosPorCategoria;
DELIMITER //
CREATE PROCEDURE GastosPorCategoria(IN p_usuario_id BIGINT, IN p_fecha_inicio DATE, IN p_fecha_fin DATE)
BEGIN
    SELECT
        categoria,
        SUM(monto) as total_gastado,
        COUNT(*) as numero_transacciones
    FROM transacciones
    WHERE usuario_id = p_usuario_id
    AND tipo = 'Gasto'
    AND fecha BETWEEN p_fecha_inicio AND p_fecha_fin
    GROUP BY categoria
    ORDER BY total_gastado DESC;
END //
DELIMITER ;

-- =====================================================
-- TRIGGERS
-- =====================================================
DROP TRIGGER IF EXISTS trigger_actualizar_imc;
DELIMITER //
CREATE TRIGGER trigger_actualizar_imc
    BEFORE UPDATE ON perfiles_nutricionales
    FOR EACH ROW
BEGIN
    IF NEW.peso != OLD.peso OR NEW.altura != OLD.altura THEN
        IF NEW.altura > 0 THEN
            SET NEW.imc = NEW.peso / POWER(NEW.altura / 100, 2);
        ELSE
            SET NEW.imc = 0;
        END IF;
    END IF;
END //
DELIMITER ;

DROP TRIGGER IF EXISTS trigger_calcular_imc_insert;
DELIMITER //
CREATE TRIGGER trigger_calcular_imc_insert
    BEFORE INSERT ON perfiles_nutricionales
    FOR EACH ROW
BEGIN
    IF NEW.altura > 0 THEN
        SET NEW.imc = NEW.peso / POWER(NEW.altura / 100, 2);
    ELSE
        SET NEW.imc = 0;
    END IF;
END //
DELIMITER ;

-- =====================================================
-- INSERTAR DATOS INICIALES
-- =====================================================
INSERT INTO modalidades_ahorro (nombre, descripcion, factor_presupuesto, prioridad_precio, prioridad_nutricion) VALUES
('Máximo', 'Prioriza el ahorro por encima de todo. Busca las opciones más económicas aunque sacrifique algo de calidad nutricional.', 0.70, 9, 4),
('Equilibrado', 'Busca un balance entre ahorro y nutrición. Recomendado para la mayoría de usuarios.', 0.85, 6, 7),
('Estándar', 'Enfocado en la calidad nutricional manteniendo un presupuesto razonable.', 1.00, 3, 9)
ON DUPLICATE KEY UPDATE nombre=nombre;


-- Inserción de datos de respaldo para la tabla 'productos'
-- Supermercado por defecto: Mercadona
-- Disponibilidad por defecto: 1 (true)

-- =====================================================
-- CATEGORÍA: Lácteos (Leche)
-- =====================================================
INSERT INTO `productos` (`nombre`, `marca`, `categoria`, `precio`, `supermercado`, `disponible`, `calorias`, `proteinas`, `carbohidratos`, `grasas`, `fibra`, `sodio`, `azucares`) VALUES
('Leche entera UHT', 'Hacendado', 'Lácteos', 0.90, 'Mercadona', 1, 61.00, 3.1, 4.7, 3.6, 0.0, 0.12, 4.7),
('Leche semidesnatada', 'Pascual', 'Lácteos', 1.25, 'Mercadona', 1, 47.00, 3.2, 4.8, 1.6, 0.0, 0.13, 4.8),
('Leche desnatada', 'Central Lechera Asturiana', 'Lácteos', 1.35, 'Mercadona', 1, 35.00, 3.4, 4.9, 0.1, 0.0, 0.13, 4.9),
('Leche sin lactosa semidesnatada', 'Hacendado', 'Lácteos', 1.10, 'Mercadona', 1, 46.00, 3.1, 4.7, 1.5, 0.0, 0.12, 4.7),
('Leche entera fresca pasteurizada', 'Hacendado', 'Lácteos', 1.05, 'Mercadona', 1, 64.00, 3.2, 4.8, 3.7, 0.0, 0.11, 4.8),
('Bebida de avena', 'Alitey', 'Lácteos', 1.40, 'Mercadona', 1, 45.00, 1.0, 8.1, 0.8, 0.4, 0.10, 3.8),
('Bebida de almendras zero azúcares', 'Hacendado', 'Lácteos', 1.55, 'Mercadona', 1, 13.00, 0.5, 0.0, 1.1, 0.4, 0.11, 0.0),
('Leche evaporada', 'Hacendado', 'Lácteos', 1.50, 'Mercadona', 1, 137.00, 6.7, 9.8, 8.1, 0.0, 0.30, 9.8),
('Leche en polvo desnatada', 'Hacendado', 'Lácteos', 3.50, 'Mercadona', 1, 358.00, 35.0, 52.0, 1.0, 0.0, 1.20, 52.0),
('Leche fresca semidesnatada', 'Priégola', 'Lácteos', 1.80, 'Mercadona', 1, 47.00, 3.3, 4.9, 1.7, 0.0, 0.12, 4.9);

-- =====================================================
-- CATEGORÍA: Carnes
-- =====================================================
INSERT INTO `productos` (`nombre`, `marca`, `categoria`, `precio`, `supermercado`, `disponible`, `calorias`, `proteinas`, `carbohidratos`, `grasas`, `fibra`, `sodio`, `azucares`) VALUES
('Pechuga de pollo fileteada', 'Hacendado', 'Carnes', 4.50, 'Mercadona', 1, 110.00, 23.0, 0.0, 1.5, 0.0, 0.1, 0.0),
('Carne picada de vacuno', 'Hacendado', 'Carnes', 5.25, 'Mercadona', 1, 250.00, 18.0, 0.0, 20.0, 0.0, 0.2, 0.0),
('Solomillo de cerdo', 'ElPozo', 'Carnes', 6.95, 'Mercadona', 1, 143.00, 22.0, 0.0, 6.0, 0.0, 0.1, 0.0),
('Costillas de cerdo adobadas', 'Hacendado', 'Carnes', 5.50, 'Mercadona', 1, 290.00, 16.0, 1.0, 25.0, 0.5, 1.5, 1.0),
('Contramuslos de pollo sin piel', 'Hacendado', 'Carnes', 4.10, 'Mercadona', 1, 175.00, 20.0, 0.0, 10.0, 0.0, 0.1, 0.0),
('Cinta de lomo de cerdo adobada', 'Incarlopsa', 'Carnes', 5.80, 'Mercadona', 1, 150.00, 21.0, 0.8, 7.0, 0.2, 1.3, 0.5),
('Hamburguesa de ternera', 'Hacendado', 'Carnes', 3.90, 'Mercadona', 1, 220.00, 17.0, 2.0, 16.0, 1.0, 1.2, 1.0),
('Salchichas frescas de pavo', 'Hacendado', 'Carnes', 2.80, 'Mercadona', 1, 180.00, 15.0, 3.0, 12.0, 1.0, 1.8, 1.0),
('Bistec de ternera primera A', 'Covap', 'Carnes', 9.50, 'Mercadona', 1, 131.00, 21.0, 0.0, 5.0, 0.0, 0.1, 0.0),
('Secreto de cerdo ibérico', 'Hacendado', 'Carnes', 8.50, 'Mercadona', 1, 350.00, 15.0, 0.0, 32.0, 0.0, 0.1, 0.0);

-- =====================================================
-- CATEGORÍA: Huevos
-- =====================================================
INSERT INTO `productos` (`nombre`, `marca`, `categoria`, `precio`, `supermercado`, `disponible`, `calorias`, `proteinas`, `carbohidratos`, `grasas`, `fibra`, `sodio`, `azucares`) VALUES
('Huevos frescos clase M (12 uds)', 'Hacendado', 'Huevos', 2.10, 'Mercadona', 1, 155.00, 13.0, 1.1, 11.0, 0.0, 0.12, 1.1),
('Huevos camperos clase L (6 uds)', 'Hacendado', 'Huevos', 1.95, 'Mercadona', 1, 160.00, 13.0, 1.2, 12.0, 0.0, 0.13, 1.2),
('Huevos ecológicos clase M (6 uds)', 'Pazo de Vilane', 'Huevos', 2.95, 'Mercadona', 1, 158.00, 12.8, 1.0, 11.5, 0.0, 0.14, 1.0),
('Claras de huevo pasteurizadas', 'Hacendado', 'Huevos', 2.50, 'Mercadona', 1, 49.00, 11.0, 1.1, 0.0, 0.0, 0.35, 1.1),
('Huevos de codorniz (12 uds)', 'Hacendado', 'Huevos', 1.85, 'Mercadona', 1, 158.00, 13.1, 0.4, 11.1, 0.0, 0.14, 0.4),
('Huevos frescos clase L (12 uds)', 'Hacendado', 'Huevos', 2.50, 'Mercadona', 1, 155.00, 13.0, 1.1, 11.0, 0.0, 0.12, 1.1),
('Huevos camperos clase M (12 uds)', 'Dagu', 'Huevos', 3.80, 'Mercadona', 1, 160.00, 13.0, 1.2, 12.0, 0.0, 0.13, 1.2),
('Yemas de huevo pasteurizadas', 'Hacendado', 'Huevos', 2.20, 'Mercadona', 1, 322.00, 16.0, 3.6, 27.0, 0.0, 0.15, 0.3),
('Huevos de corral XL (6 uds)', 'Hacendado', 'Huevos', 2.15, 'Mercadona', 1, 156.00, 12.9, 1.1, 11.2, 0.0, 0.13, 1.1),
('Huevo líquido pasteurizado', 'Hacendado', 'Huevos', 3.10, 'Mercadona', 1, 149.00, 12.0, 2.0, 10.0, 0.0, 0.30, 1.0);

-- =====================================================
-- CATEGORÍA: Panadería
-- =====================================================
INSERT INTO `productos` (`nombre`, `marca`, `categoria`, `precio`, `supermercado`, `disponible`, `calorias`, `proteinas`, `carbohidratos`, `grasas`, `fibra`, `sodio`, `azucares`) VALUES
('Pan de molde blanco', 'Hacendado', 'Panadería', 1.10, 'Mercadona', 1, 265.00, 9.0, 49.0, 3.0, 2.5, 1.1, 3.5),
('Pan de molde integral', 'Bimbo', 'Panadería', 2.50, 'Mercadona', 1, 250.00, 10.0, 43.0, 4.0, 7.0, 1.0, 3.8),
('Pan de pueblo hogaza', 'Hacendado', 'Panadería', 1.80, 'Mercadona', 1, 258.00, 8.5, 52.0, 1.5, 3.0, 1.2, 2.0),
('Panecillos de leche', 'Hacendado', 'Panadería', 1.50, 'Mercadona', 1, 320.00, 9.5, 55.0, 6.0, 2.8, 0.9, 8.0),
('Pan de centeno 50%', 'Hacendado', 'Panadería', 1.90, 'Mercadona', 1, 259.00, 8.0, 48.0, 1.7, 6.0, 1.1, 2.5),
('Biscotes integrales', 'Hacendado', 'Panadería', 1.45, 'Mercadona', 1, 390.00, 13.0, 70.0, 5.0, 10.0, 1.3, 5.0),
('Pan de molde sin corteza', 'Panrico', 'Panadería', 2.10, 'Mercadona', 1, 270.00, 8.5, 52.0, 3.5, 2.0, 1.1, 4.0),
('Pan de hamburguesa brioche', 'Hacendado', 'Panadería', 1.60, 'Mercadona', 1, 310.00, 9.0, 50.0, 8.0, 2.5, 1.0, 7.0),
('Pan de pita', 'Hacendado', 'Panadería', 1.35, 'Mercadona', 1, 275.00, 9.0, 56.0, 1.5, 2.5, 1.2, 2.0),
('Pan de molde con semillas', 'Hacendado', 'Panadería', 1.85, 'Mercadona', 1, 280.00, 11.0, 45.0, 6.0, 8.0, 1.0, 4.0);

-- =====================================================
-- CATEGORÍA: Cuidado personal (Crema de manos)
-- =====================================================
INSERT INTO `productos` (`nombre`, `marca`, `categoria`, `precio`, `supermercado`, `disponible`, `calorias`, `proteinas`, `carbohidratos`, `grasas`, `fibra`, `sodio`, `azucares`) VALUES
('Crema de manos hidratante', 'Deliplus', 'Cuidado personal', 1.50, 'Mercadona', 1, 0.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('Crema de manos reparadora', 'Neutrogena', 'Cuidado personal', 5.95, 'Mercadona', 1, 0.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('Crema de manos anti-manchas Q10', 'Nivea', 'Cuidado personal', 4.50, 'Mercadona', 1, 0.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('Crema de manos con aceite de oliva', 'Deliplus', 'Cuidado personal', 2.10, 'Mercadona', 1, 0.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('Crema de manos y uñas', 'Cien', 'Cuidado personal', 1.99, 'Mercadona', 1, 0.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('Crema de manos nutritiva karité', 'Deliplus', 'Cuidado personal', 1.80, 'Mercadona', 1, 0.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('Crema de manos absorción rápida', 'Dove', 'Cuidado personal', 3.25, 'Mercadona', 1, 0.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('Crema de manos con aloe vera', 'Deliplus', 'Cuidado personal', 1.75, 'Mercadona', 1, 0.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('Crema de manos anti-edad', 'Deliplus', 'Cuidado personal', 2.50, 'Mercadona', 1, 0.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
('Crema de manos concentrada', 'La Toja', 'Cuidado personal', 3.95, 'Mercadona', 1, 0.00, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);