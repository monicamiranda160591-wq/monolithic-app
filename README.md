# producto-app

Aplicación monolítica de gestión de productos construida con **Spring Boot 3.2.5** y **Java 17**, con arquitectura por capas (Controller → Service → Repository → Model), base de datos H2 en memoria, pruebas automatizadas con JUnit 5 y cobertura con JaCoCo.

---

## 📋 Tabla de Contenidos

- [Requisitos](#-requisitos)
- [Estructura del Proyecto](#-estructura-del-proyecto)
- [Compilar](#-compilar)
- [Ejecutar Pruebas](#-ejecutar-pruebas)
- [Reporte JaCoCo](#-reporte-de-cobertura-jacoco)
- [Ejecutar la Aplicación](#-ejecutar-la-aplicación)
- [API REST](#-api-rest)
- [Consola H2](#-consola-h2)
- [CI/CD con Jenkins](#-cicd-con-jenkins)
- [Configuración de Servicios Externos](#-configuración-de-servicios-externos)

---

## 🔧 Requisitos

| Herramienta | Versión mínima |
|-------------|---------------|
| Java (JDK)  | 17            |
| Maven       | 3.8+          |
| Git         | Cualquiera    |

> **Nota:** No se requiere base de datos externa. La aplicación usa H2 en memoria.

---

## 📁 Estructura del Proyecto

```
producto-app/
├── src/
│   ├── main/
│   │   ├── java/com/example/productos/
│   │   │   ├── ProductosApplication.java      ← Punto de entrada
│   │   │   ├── controller/
│   │   │   │   └── ProductoController.java    ← REST API endpoints
│   │   │   ├── service/
│   │   │   │   └── ProductoService.java       ← Lógica de negocio
│   │   │   ├── repository/
│   │   │   │   └── ProductoRepository.java    ← Acceso a datos (JPA)
│   │   │   └── model/
│   │   │       └── Producto.java              ← Entidad JPA
│   │   └── resources/
│   │       ├── application.properties         ← Configuración
│   │       └── data.sql                       ← Datos iniciales
│   └── test/
│       └── java/com/example/productos/
│           ├── ProductosApplicationTests.java ← Test de integración
│           ├── controller/
│           │   └── ProductoControllerTest.java ← Tests capa web
│           └── service/
│               └── ProductoServiceTest.java   ← Tests unitarios
├── pom.xml
├── Jenkinsfile
└── README.md
```

---

## 🔨 Compilar

```bash
# Compilar sin ejecutar pruebas
mvn clean compile

# Compilar y empaquetar (genera JAR en target/)
mvn clean package -DskipTests
```

---

## ✅ Ejecutar Pruebas

```bash
# Ejecutar todas las pruebas (JUnit 5 + genera reporte JaCoCo)
mvn clean verify

# Ejecutar solo las pruebas unitarias (sin verificación de cobertura)
mvn test

# Ejecutar una clase de prueba específica
mvn test -Dtest=ProductoServiceTest

# Ejecutar un método de prueba específico
mvn test -Dtest=ProductoServiceTest#debeCrearProductoCuandoNombreEsUnico
```

### Tipos de Pruebas

| Clase | Tipo | Descripción |
|-------|------|-------------|
| `ProductosApplicationTests` | Integración | Verifica carga del contexto Spring |
| `ProductoServiceTest` | Unitaria | Lógica de negocio con Mockito |
| `ProductoControllerTest` | Web Layer | Endpoints REST con MockMvc |

---

## 📊 Reporte de Cobertura JaCoCo

Después de ejecutar `mvn clean verify`, el reporte se genera en:

```
target/site/jacoco/index.html    ← Reporte HTML interactivo
target/site/jacoco/jacoco.xml    ← Reporte XML (usado por SonarQube)
target/jacoco.exec               ← Datos binarios de cobertura
```

**Umbral mínimo configurado: 70% de cobertura de líneas**

Para abrir el reporte en el navegador:
```bash
# Windows
start target\site\jacoco\index.html

# macOS/Linux
open target/site/jacoco/index.html
```

---

## 🚀 Ejecutar la Aplicación

```bash
# Opción 1: Con Maven (modo desarrollo)
mvn spring-boot:run

# Opción 2: Ejecutar el JAR directamente
java -jar target/producto-app-1.0.0-SNAPSHOT.jar

# Opción 3: Con perfil específico
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

La aplicación estará disponible en: **http://localhost:8080**

### Verificar que está funcionando:

```bash
# Health check
curl http://localhost:8080/actuator/health

# Info de la aplicación
curl http://localhost:8080/actuator/info
```

---

## 🌐 API REST

Base URL: `http://localhost:8080/api/v1/productos`

### Endpoints disponibles

| Método | Endpoint | Descripción |
|--------|----------|-------------|
| `GET` | `/api/v1/productos` | Obtener todos los productos |
| `GET` | `/api/v1/productos?soloActivos=true` | Obtener solo activos |
| `GET` | `/api/v1/productos/{id}` | Obtener producto por ID |
| `GET` | `/api/v1/productos/buscar?nombre=laptop` | Buscar por nombre |
| `GET` | `/api/v1/productos/categoria/{cat}` | Filtrar por categoría |
| `GET` | `/api/v1/productos/precio?min=10&max=500` | Filtrar por rango de precio |
| `GET` | `/api/v1/productos/stock-bajo?umbral=5` | Productos con stock bajo |
| `POST` | `/api/v1/productos` | Crear nuevo producto |
| `PUT` | `/api/v1/productos/{id}` | Actualizar producto completo |
| `PATCH` | `/api/v1/productos/{id}/stock` | Actualizar solo el stock |
| `DELETE` | `/api/v1/productos/{id}` | Eliminación lógica (desactivar) |
| `DELETE` | `/api/v1/productos/{id}/fisico` | Eliminación física |

### Ejemplos de uso con cURL

```bash
# Obtener todos los productos
curl -X GET http://localhost:8080/api/v1/productos

# Crear un producto
curl -X POST http://localhost:8080/api/v1/productos \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Nuevo Producto",
    "descripcion": "Descripción del producto",
    "precio": 99.99,
    "stock": 50,
    "categoria": "Electronica"
  }'

# Actualizar el stock
curl -X PATCH http://localhost:8080/api/v1/productos/1/stock \
  -H "Content-Type: application/json" \
  -d '{"stock": 100}'

# Eliminar lógicamente
curl -X DELETE http://localhost:8080/api/v1/productos/1
```

### Estructura del JSON de Producto

```json
{
  "id": 1,
  "nombre": "Laptop Pro 15",
  "descripcion": "Laptop de alto rendimiento",
  "precio": 1299.99,
  "stock": 25,
  "categoria": "Electronica",
  "activo": true,
  "fechaCreacion": "2024-01-15T10:00:00",
  "fechaActualizacion": "2024-01-15T10:00:00"
}
```

---

## 🗄️ Consola H2

Durante el desarrollo, la consola web de H2 está disponible en:

**URL:** http://localhost:8080/h2-console

| Campo | Valor |
|-------|-------|
| JDBC URL | `jdbc:h2:mem:productosdb` |
| Usuario | `sa` |
| Contraseña | *(vacía)* |

---

## 🔄 CI/CD con Jenkins

### Pipeline Stages

```
Checkout → Build → Testing → Sonar Analysis → Quality Gate → Package → Publish to Artifactory
```

| Stage | Descripción | Herramientas |
|-------|-------------|--------------|
| **Checkout** | Obtiene código fuente | Git |
| **Build** | Compila el proyecto | Maven |
| **Testing** | Pruebas JUnit + cobertura JaCoCo | JUnit 5, JaCoCo |
| **Sonar Analysis** | Análisis de calidad de código | SonarQube Scanner |
| **Quality Gate** | Verifica umbrales de calidad | SonarQube |
| **Package** | Genera el JAR ejecutable | Maven |
| **Publish** | Publica en Artifactory | JFrog Artifactory |

> ⚠️ La publicación en Artifactory **solo ocurre** si todos los stages anteriores son exitosos y si la rama es `main` o `master`.

---

## ⚙️ Configuración de Servicios Externos

### Plugins de Jenkins requeridos

| Plugin | Propósito |
|--------|-----------|
| Pipeline | Soporte para Declarative Pipeline |
| Git Plugin | Checkout del código fuente |
| Maven Integration | Ejecución de Maven |
| JaCoCo Plugin | Publicar reportes de cobertura |
| SonarQube Scanner for Jenkins | Integración con SonarQube |
| Artifactory Plugin (JFrog) | Publicación de artefactos |
| Credentials Binding | Gestión segura de credenciales |
| HTML Publisher | Publicar reporte JaCoCo HTML |
| Timestamper | Timestamps en consola |
| AnsiColor | Colores en consola |

### Configuración de Jenkins (Manage Jenkins > Configure System)

#### 1. JDK
```
Nombre: JDK-17
JAVA_HOME: /ruta/a/jdk-17
```

#### 2. Maven
```
Nombre: Maven-3.9
MAVEN_HOME: /ruta/a/maven-3.9
```

#### 3. SonarQube Server
```
Nombre del servidor: SonarQube   ← debe coincidir con Jenkinsfile
URL: http://tu-sonarqube.empresa.com
Token de autenticación: (seleccionar credential 'sonar-auth-token')
```

### Credenciales requeridas en Jenkins

| Credential ID | Tipo | Descripción |
|---------------|------|-------------|
| `sonar-auth-token` | Secret Text | Token de acceso a SonarQube |
| `artifactory-credentials` | Username/Password | Usuario y contraseña de Artifactory |

> 🔒 **Importante:** Nunca incluir tokens, contraseñas ni URLs específicas en el código fuente. Todas las credenciales se gestionan exclusivamente a través de Jenkins Credentials Store.

### Variables de entorno en el agente Jenkins

```bash
# Configurar en el agente Jenkins o en las variables de entorno del pipeline
ARTIFACTORY_URL=https://tu-artifactory.empresa.com/artifactory
```

---

## 📦 Parámetros del Pipeline

El Jenkinsfile expone los siguientes parámetros para ejecuciones manuales:

| Parámetro | Tipo | Default | Descripción |
|-----------|------|---------|-------------|
| `SKIP_SONAR` | Boolean | `false` | Omite el análisis de SonarQube |
| `SKIP_PUBLISH` | Boolean | `false` | Omite la publicación en Artifactory |

---

## 📝 Notas de Desarrollo

- La base de datos H2 se recrea en cada inicio de la aplicación (`ddl-auto=create-drop`)
- Los datos de prueba en `data.sql` se cargan automáticamente al iniciar
- El umbral de cobertura JaCoCo está configurado al **70% de líneas**
- La eliminación de productos es **lógica** por defecto (campo `activo=false`)
- El endpoint `/fisico` permite eliminación permanente de la base de datos
