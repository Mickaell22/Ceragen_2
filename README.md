# Ceragen 2

Aplicación JavaFX con arquitectura MVC y conexión a MySQL.

## Estructura del Proyecto

```
src/main/java/com/example/ceragen_2/
├── model/          # Modelos de datos y entidades
├── view/           # Clases relacionadas con la interfaz (si se usan)
├── controller/     # Controladores de la aplicación
├── config/         # Configuraciones (DatabaseConfig, etc.)
└── util/          # Utilidades y helpers

src/main/resources/com/example/ceragen_2/
├── views/         # Archivos FXML
├── css/           # Hojas de estilo CSS
└── images/        # Recursos de imágenes
```

## Librerías Incluidas

- **JavaFX**: Framework principal para la interfaz gráfica
- **ControlsFX**: Controles adicionales para JavaFX
- **FormsFX**: Framework para formularios
- **ValidatorFX**: Validación de formularios
- **Ikonli**: Iconos para JavaFX
- **BootstrapFX**: Estilos Bootstrap para JavaFX
- **TilesFX**: Componentes de tipo tile/dashboard
- **MySQL Connector**: Conexión a base de datos MySQL
- **Dotenv Java**: Manejo de variables de entorno

## Configuración

El proyecto ya está configurado para conectarse a MySQL en Railway. Las credenciales están en el archivo `.env`.

**Credenciales de acceso:**
- Usuario admin: `admin`
- Contraseña: `admin`

## Ejecución

### Desde Maven
```bash
mvn clean javafx:run
```

### Desde IntelliJ IDEA
Usar la configuración de ejecución "MainApplication" incluida en el proyecto.

## Requisitos

- Java 17 (configurado en el proyecto)
- Maven 3.6 o superior (incluido con Maven Wrapper)
- MySQL 8.0 o superior

## Notas

El proyecto está configurado para Java 17 debido a compatibilidades con las dependencias. IntelliJ IDEA detectará automáticamente la configuración de ejecución "MainApplication" en el menú superior.
