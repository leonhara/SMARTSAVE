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
