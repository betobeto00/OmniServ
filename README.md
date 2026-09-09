# OmniServ

App Android para gestión de servicios múltiples. Gestiona clientes, agenda servicios, asigna operarios y controla pagos. Diseñada para técnicos y profesionales de cualquier giro: fumigación, reparación de aires acondicionados, servicios técnicos, reparación de neveras, y cualquier servicio que un profesional pueda ofrecer.

Parte del ecosistema **OmniMargen**.

**Distribución**: APK directo + auto-update desde GitHub Releases.
**Licenciamiento**: RSA-2048 offline vía TOG Platform.

---

## Funcionalidades Principales

### Gestión de Clientes
- Registro de clientes (nombre, teléfono, dirección)
- Edición y eliminación de clientes
- Listado con búsqueda rápida

### Tipos de Servicio (Abierto)
- El usuario crea sus propios tipos de servicio
- Ej: "Limpieza de AC", "Reparación de UMA", "Fumigación general", "Corte de césped"
- Gestión desde pantalla dedicada
- Crear nuevo tipo directamente desde el formulario de servicio

### Agenda de Servicios
- Crear servicios con fecha programada
- Asociar servicio a un cliente existente
- Seleccionar tipo de servicio (creado por el usuario)
- Registrar monto cobrado / costo del servicio
- Estados: PENDIENTE, EN_PROGRESO, REALIZADO, CANCELADO

### Asignación de Operarios
- Asignar uno o más operarios/técnicos a cada servicio
- Control de pagos individuales por operario
- Resumen de pagos pendientes y cobrados por operario
- Historial de pagos por operario
- Invoice numbers en servicios

### Gestión de Operarios/Técnicos
- Registro de técnicos (nombre, teléfono, especialidad)
- Toggle activo/inactivo
- CRUD completo

### Notificaciones
- Recordatorio automático **2 días antes** de cada servicio programado
- Verificación periódica cada 24 horas via WorkManager

### Historial
- Listado de servicios realizados (pasados)
- Resumen de ingresos por período
- Desglose de pagos por operario

### Exportación PDF
- Generar PDF de **clientes** (N°, Nombre, Teléfono, Dirección)
- Generar PDF de **servicios** (N°, Cliente, Fecha, Estado, Notas)
- Se abre automáticamente con FileProvider

### Auto-Update
- Chequeo automático al abrir la app contra GitHub Releases
- Diálogo "Nueva versión disponible" con notas de cambios
- Descarga e instalación directa desde la app
- Botón manual de actualización en la barra superior
- User-Agent header para compatibilidad con GitHub API
- Manejo de errores descriptivos (HTTP 403/429, sin conexión, etc.)

### Licenciamiento
- Activación con código RSA-2048
- Validación offline con grace period de 48h
- Integración con TOG Platform
- Una licencia puede cubrir TOG Admin + OmniServ

---

## Stack Técnico

| Capa | Tecnología |
|------|-----------|
| UI | Jetpack Compose + Material 3 |
| Arquitectura | MVVM + Clean Architecture |
| Base de datos | Room (SQLite local) |
| Navegación | Jetpack Navigation Compose |
| Notificaciones | WorkManager + NotificationManager |
| DI | Hilt (Dagger) |
| PDF | PdfDocument nativo |
| Auto-Update | GitHub API + DownloadManager + User-Agent |
| Licencias | RSA-2048 + TOG Platform |
| Lenguaje | Kotlin |

---

## Estructura del Proyecto

```
app/src/main/java/com/omnimargen/omniserv/
├── data/
│   ├── local/
│   │   ├── converter/
│   │   │   └── DateConverter.kt
│   │   ├── dao/
│   │   │   ├── ClientDao.kt
│   │   │   ├── ServiceDao.kt
│   │   │   ├── OperatorDao.kt
│   │   │   ├── ServiceTypeDao.kt
│   │   │   └── LicenseDao.kt
│   │   ├── db/
│   │   │   └── AppDatabase.kt
│   │   └── entity/
│   │       ├── ClientEntity.kt
│   │       ├── ServiceEntity.kt
│   │       ├── OperatorEntity.kt
│   │       ├── ServiceOperatorEntity.kt
│   │       ├── ServiceTypeEntity.kt
│   │       └── LicenseEntity.kt
│   ├── mapper/
│   │   ├── ClientMapper.kt
│   │   ├── ServiceMapper.kt
│   │   ├── OperatorMapper.kt
│   │   └── ServiceTypeMapper.kt
│   └── repository/
│       ├── ClientRepository.kt
│       ├── ServiceRepository.kt
│       ├── OperatorRepository.kt
│       ├── ServiceTypeRepository.kt
│       └── LicenseRepository.kt
├── di/
│   └── AppModule.kt
├── domain/
│   ├── model/
│   │   ├── Client.kt
│   │   ├── Service.kt
│   │   ├── Operator.kt
│   │   ├── ServiceType.kt
│   │   ├── ServiceStatus.kt
│   │   └── License.kt
│   └── usecase/
│       ├── client/
│       │   ├── AddClientUseCase.kt
│       │   ├── DeleteClientUseCase.kt
│       │   ├── GetClientsUseCase.kt
│       │   ├── SearchClientsUseCase.kt
│       │   └── UpdateClientUseCase.kt
│       ├── service/
│       │   ├── AddServiceUseCase.kt
│       │   ├── DeleteServiceUseCase.kt
│       │   ├── GetPendingServicesUseCase.kt
│       │   ├── GetServicesByDateRangeUseCase.kt
│       │   ├── GetServicesUseCase.kt
│       │   ├── GetUpcomingServicesUseCase.kt
│       │   ├── MarkServiceCompletedUseCase.kt
│       │   ├── AssignOperatorsUseCase.kt
│       │   └── UpdateServiceUseCase.kt
│       ├── operator/
│       │   ├── AddOperatorUseCase.kt
│       │   ├── DeleteOperatorUseCase.kt
│       │   ├── GetOperatorsUseCase.kt
│       │   ├── GetActiveOperatorsUseCase.kt
│       │   ├── ToggleOperatorActiveUseCase.kt
│       │   └── UpdateOperatorUseCase.kt
│       ├── serviceType/
│       │   ├── AddServiceTypeUseCase.kt
│       │   ├── DeleteServiceTypeUseCase.kt
│       │   ├── GetServiceTypesUseCase.kt
│       │   └── UpdateServiceTypeUseCase.kt
│       └── license/
│           ├── ValidateLicenseUseCase.kt
│           ├── ActivateLicenseUseCase.kt
│           └── GetLicenseStatusUseCase.kt
├── license/
│   ├── LicenseValidator.kt
│   ├── LicenseGenerator.kt
│   └── LicensePayload.kt
├── notification/
│   ├── NotificationHelper.kt
│   ├── NotificationScheduler.kt
│   └── ReminderWorker.kt
├── ui/
│   ├── navigation/
│   │   └── NavGraph.kt
│   ├── screens/
│   │   ├── activation/
│   │   │   ├── ActivationScreen.kt
│   │   │   ├── ActivationViewModel.kt
│   │   │   └── ActivationUiState.kt
│   │   ├── clients/
│   │   │   ├── ClientFormScreen.kt
│   │   │   ├── ClientListScreen.kt
│   │   │   ├── ClientUiState.kt
│   │   │   └── ClientViewModel.kt
│   │   ├── operators/
│   │   │   ├── OperatorFormScreen.kt
│   │   │   ├── OperatorListScreen.kt
│   │   │   ├── OperatorUiState.kt
│   │   │   └── OperatorViewModel.kt
│   │   ├── services/
│   │   │   ├── ServiceFormScreen.kt
│   │   │   ├── ServiceListScreen.kt
│   │   │   ├── ServiceUiState.kt
│   │   │   └── ServiceViewModel.kt
│   │   ├── serviceTypes/
│   │   │   ├── ServiceTypeFormScreen.kt
│   │   │   ├── ServiceTypeListScreen.kt
│   │   │   ├── ServiceTypeUiState.kt
│   │   │   └── ServiceTypeViewModel.kt
│   │   ├── history/
│   │   │   ├── HistoryScreen.kt
│   │   │   ├── HistoryUiState.kt
│   │   │   └── HistoryViewModel.kt
│   │   └── home/
│   │       ├── HomeScreen.kt
│   │       ├── HomeUiState.kt
│   │       └── HomeViewModel.kt
│   └── theme/
│       ├── Color.kt
│       └── Theme.kt
├── update/
│   ├── UpdateChecker.kt
│   ├── UpdateDialog.kt
│   ├── UpdateInstaller.kt
│   └── UpdateViewModel.kt
├── util/
│   ├── DateUtils.kt
│   └── PdfHelper.kt
└── OmniservApp.kt
```

---

## Instalación

### Para el usuario final
1. Descargar APK desde [Releases](https://github.com/betobeto00/OmniServ/releases) o desde la sección de Actualizaciones de la app
2. Instalar en el teléfono (permitir fuentes desconocidas)
3. Abrir la app e ingresar código de activación
4. La app chequea actualizaciones automáticamente al abrir
5. Descarga de 7 días gratis disponible desde [Google Drive](https://drive.google.com/drive/folders/1pERlTnQN8GAOnajv95v9I7m8i7vsnu0O?usp=sharing)

### Compilación (desarrollador)
```bash
# Clonar el repositorio
git clone https://github.com/betobeto00/OmniServ.git

# Compilar debug
gradlew.bat assembleDebug

# Compilar release
set JAVA_HOME="C:\Program Files\Microsoft\jdk-17.0.20.101-hotspot"
set ANDROID_HOME="C:\Users\<usuario>\AppData\Local\Android\Sdk"
gradlew.bat assembleRelease

# APK en: app/build/outputs/apk/release/app-release.apk
```

---

## Publicar nueva versión

```bash
# 1. Compilar
gradlew.bat assembleRelease

# 2. Crear tag
git tag -a v1.0 -m "v1.0 - Versión inicial"
git push origin master --tags

# 3. Crear release con APK
gh release create v1.0 app\build\outputs\apk\release\app-release.apk \
  --title "v1.0" --notes "Versión inicial de OmniServ"
```

El cliente recibe la notificación automáticamente al abrir la app.

---

## Sistema de Licencias

Ver [LICENSE_SYSTEM.md](LICENSE_SYSTEM.md) para documentación completa sobre:
- Cómo funciona el licenciamiento RSA-2048
- Cómo generar licencias
- Flujo de activación
- Integración con TOG Platform

---

## Requisitos

- Android 7.0+ (API 24)
- Gradle 8.x
- Kotlin 2.0+
- JDK 17
- Icono: Logo OmniServ (todas las densidades mipmap)

## Licencia

Proyecto privado - OmniMargen.
