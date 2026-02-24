# 🛒 SmartSave - Gestión Inteligente de Finanzas y Compras

![Java](https://img.shields.io/badge/Java-21-orange?style=for-the-badge&logo=java) ![JavaFX](https://img.shields.io/badge/JavaFX-23-blue?style=for-the-badge&logo=javafx) ![Hibernate](https://img.shields.io/badge/Hibernate-ORM-green?style=for-the-badge&logo=hibernate) ![Python](https://img.shields.io/badge/Python-3.13-yellow?style=for-the-badge&logo=python) ![H2](https://img.shields.io/badge/Database-H2-darkblue?style=for-the-badge)

**SmartSave** es una aplicación de escritorio avanzada para optimizar el ahorro doméstico. Combina el control de finanzas con un motor de búsqueda en tiempo real de productos de supermercado (Mercadona), permitiendo generar listas inteligentes según presupuesto y perfil nutricional.

---

## 🚀 Características Principales

- **Dashboard Financiero:** Gráficos dinámicos de ingresos, gastos y balances mensuales.
- **Microservicio de Scrapeo:** Integración nativa con un servidor **Flask (Python)** y la librería `mercapy` para obtener datos reales.
- **Listas Inteligentes:** Modos de *Ahorro Máximo*, *Equilibrado* y *Nutrición Prioritaria*.
- **Seguridad:** Autenticación robusta con cifrado de contraseñas mediante **BCrypt**.
- **Portabilidad:** Base de datos **H2** embebida para ejecución sin configuraciones externas.

---

## 🛠️ Stack Tecnológico

- **Frontend:** JavaFX (FXML + CSS).
- **Backend:** Java 21 (JDK 21).
- **Persistencia:** Hibernate 6 (JPA) + HikariCP.
- **Base de Datos:** H2 Database Engine.
- **Microservicio:** Python 3.13 + Flask.

---

## 🔧 Instalación y Ejecución

1. **Clonar repositorio:** `git clone https://github.com/tu-usuario/SMARTSAVE.git`
2. **Compilar e instalar:** `mvn clean install`
3. **Ejecutar aplicación:** `mvn javafx:run`

---

## 📈 Mejoras Profesionales (Refactorización)

- **Migración a H2:** Se eliminó MySQL para facilitar la portabilidad "Plug & Play".
- **Gestión de Procesos:** *Shutdown Hooks* para el cierre limpio de subprocesos Python.
- **Seguridad:** Implementación de `JBCrypt` para hashing seguro de credenciales.
- **Multithreading:** Uso de `CompletableFuture` para peticiones asíncronas.
- **Corrección UX:** Solución de bloqueos visuales y errores de layout dinámico.

---

## 📁 Estructura del Proyecto

```text
src/main/java/smartsave/
├── api/          # Interoperabilidad Java-Python (Mercadona API)
├── app/          # Ciclo de vida y arranque de la aplicación
├── config/       # Configuración de Hibernate y persistencia H2
├── controlador/  # Controladores MVC de la interfaz JavaFX
├── modelo/       # Entidades JPA (Usuario, Producto, Transaccion)
├── servicio/     # Lógica de negocio y gestión de datos
└── utilidad/     # Validaciones, cifrado y gestión de estilos CSS

Leonel - Desarrollador de Aplicaciones Multiplataforma
LinkedIn | GitHub
