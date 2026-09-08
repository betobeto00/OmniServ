# Roadmap - OmniServ

---

## Fase 0: Setup del Proyecto
**Estado: COMPLETADO**

- [x] Crear proyecto Android con estructura MVVM + Clean Architecture
- [x] Configurar `build.gradle.kts` con dependencias (Compose, Room, Hilt, Navigation, WorkManager)
- [x] Configurar `libs.versions.toml` (version catalog)
- [x] Configurar estructura de paquetes base
- [x] Theme con colores Omnimargen (azul `#3b82f6`, cyan `#0ea5e9`)
- [x] Verificar que compila y ejecuta

---

## Fase 1: Capa de Datos
**Estado: COMPLETADO**

### Entidades Room
- [x] `ClientEntity` - id, nombre, telefono, direccion, fechaCreacion
- [x] `ServiceTypeEntity` - id, nombre, activo, fechaCreacion
- [x] `OperatorEntity` - id, nombre, telefono, especialidad, activo, fechaCreacion
- [x] `ServiceEntity` - id, clienteId (FK), serviceTypeId (FK), tipoServicio, fechaServicio, monto, estado, notas, fechaCreacion
- [x] `ServiceOperatorEntity` - id, serviceId (FK), operatorId (FK), montoPago
- [x] `LicenseEntity` - id (=1), licenseKey, empresaId, fechaEmision, fechaExpiracion, modulosActivos, firma

### DAOs
- [x] `ClientDao` - CRUD + búsqueda por nombre
- [x] `ServiceTypeDao` - CRUD + getActive
- [x] `OperatorDao` - CRUD + getActive
- [x] `ServiceDao` - CRUD + filtro por fecha + por cliente + próximos
- [x] `ServiceOperatorDao` - insert/delete por serviceId
- [x] `LicenseDao` - get/save/delete

### Database
- [x] `AppDatabase` con 6 tablas y version = 1
- [x] `DateConverter` para timestamps

### Repositorios
- [x] `ClientRepository` - operaciones de cliente
- [x] `ServiceTypeRepository` - operaciones de tipo de servicio
- [x] `OperatorRepository` - operaciones de operario
- [x] `ServiceRepository` - operaciones de servicio + asignación de operarios
- [x] `LicenseRepository` - operaciones de licencia

### Mappers
- [x] `ClientMapper.kt` - Entity ↔ Domain
- [x] `ServiceTypeMapper.kt` - Entity ↔ Domain
- [x] `OperatorMapper.kt` - Entity ↔ Domain
- [x] `ServiceMapper.kt` - Entity ↔ Domain

---

## Fase 2: Dominio (Use Cases)
**Estado: COMPLETADO**

### Clientes
- [x] `GetClientsUseCase` - listar todos
- [x] `SearchClientsUseCase` - buscar por nombre
- [x] `AddClientUseCase` - crear cliente
- [x] `UpdateClientUseCase` - editar cliente
- [x] `DeleteClientUseCase` - eliminar cliente

### Tipos de Servicio
- [x] `GetServiceTypesUseCase` - listar activos
- [x] `GetAllServiceTypesUseCase` - listar todos (admin)
- [x] `AddServiceTypeUseCase` - crear tipo
- [x] `UpdateServiceTypeUseCase` - editar tipo
- [x] `DeleteServiceTypeUseCase` - eliminar tipo

### Operarios
- [x] `GetOperatorsUseCase` - listar todos
- [x] `GetActiveOperatorsUseCase` - listar activos
- [x] `AddOperatorUseCase` - crear operario
- [x] `UpdateOperatorUseCase` - editar operario
- [x] `DeleteOperatorUseCase` - eliminar operario
- [x] `ToggleOperatorActiveUseCase` - activar/desactivar

### Servicios
- [x] `GetServicesUseCase` - listar todos
- [x] `GetUpcomingServicesUseCase` - próximos servicios
- [x] `GetPendingServicesUseCase` - servicios pendientes
- [x] `GetServicesByDateRangeUseCase` - por rango de fechas
- [x] `AddServiceUseCase` - crear servicio + programar notificación
- [x] `UpdateServiceUseCase` - editar servicio + reprogramar notificación
- [x] `DeleteServiceUseCase` - eliminar servicio + cancelar notificación
- [x] `MarkServiceCompletedUseCase` - marcar como realizado
- [x] `AssignOperatorsUseCase` - asignar operarios a servicio
- [x] `UpdateOperatorPaymentUseCase` - actualizar pago de operario

### Licencia
- [x] `ValidateLicenseUseCase` - validar licencia local
- [x] `ActivateLicenseUseCase` - activar con api_key
- [x] `GetLicenseStatusUseCase` - obtener estado actual

---

## Fase 3: UI - Clients + ServiceTypes
**Estado: COMPLETADO**

### Clientes
- [x] `ClientListScreen` - lista con búsqueda + FAB
- [x] `ClientFormScreen` - formulario crear/editar
- [x] `ClientViewModel` + `ClientUiState`

### Tipos de Servicio
- [x] `ServiceTypeListScreen` - lista con CRUD
- [x] `ServiceTypeFormScreen` - formulario crear/editar
- [x] `ServiceTypeViewModel` + `ServiceTypeUiState`
- [x] NavGraph con BottomNavigation (5 tabs)

---

## Fase 4: UI - Operators
**Estado: COMPLETADO**

- [x] `OperatorListScreen` - lista con toggle activo/inactivo
- [x] `OperatorFormScreen` - formulario crear/editar
- [x] `OperatorViewModel` + `OperatorUiState`

---

## Fase 5: UI - Services (con operarios)
**Estado: COMPLETADO**

- [x] `ServiceListScreen` - lista con filtros (estado, tipo)
- [x] `ServiceFormScreen`:
  - [x] Selector de cliente (dropdown)
  - [x] Selector de tipo de servicio (dropdown + crear nuevo)
  - [x] Fecha con DatePicker
  - [x] Monto
  - [x] Multi-select de operarios
  - [x] Notas
- [x] `ServiceDetailScreen` - vista detallada del servicio
- [x] `ServiceViewModel` + `ServiceUiState`

---

## Fase 6: UI - History + Dashboard
**Estado: COMPLETADO**

### Home (Dashboard)
- [x] `HomeScreen` - resumen de servicios pendientes
- [x] Servicios de hoy destacados
- [x] Próximos servicios
- [x] Resumen de pagos a operarios
- [x] FAB para nuevo servicio
- [x] `HomeViewModel` + `HomeUiState`

### Historial
- [x] `HistoryScreen` - servicios completados
- [x] Resumen de ingresos
- [x] Desglose por operario
- [x] `HistoryViewModel` + `HistoryUiState`

---

## Fase 7: Sistema de Licencias
**Estado: COMPLETADO**

### Domain
- [x] `License` data class
- [x] `LicenseStatus` sealed class
- [x] `ValidateLicenseUseCase`
- [x] `ActivateLicenseUseCase`
- [x] `GetLicenseStatusUseCase`

### Data
- [x] `LicenseEntity` + `LicenseDao` + `LicenseRepository`
- [x] `LicenseMapper.kt`

### License Module
- [x] `LicenseValidator.kt` - validación RSA + expiración + grace period
- [x] `LicenseGenerator.kt` - device fingerprint
- [x] `LicensePayload.kt` - data class del payload

### UI
- [x] `ActivationScreen` - pantalla de activación
- [x] `ActivationViewModel` + `ActivationUiState`

### Integración
- [x] Grace period 48h offline
- [ ] Integración con TOG Platform API (pendiente backend)
- [ ] Chequeo periódico (WorkManager) - Fase 8
- [ ] Bloqueo de app si licencia inválida > 48h

---

## Fase 8: Notificaciones + PDF
**Estado: COMPLETADO**

### Notificaciones
- [x] `NotificationHelper` - crear canal, mostrar/cancelar notificaciones
- [x] `NotificationScheduler` - programar WorkManager
- [x] `ReminderWorker` - verificar servicios próximos cada 24h

### PDF
- [x] `PdfHelper` - generador de PDFs nativo
- [x] PDF de clientes (N°, Nombre, Teléfono, Dirección)
- [x] PDF de servicios (N°, Cliente, Fecha, Tipo, Estado, Notas)
- [x] FileProvider setup
- [ ] Botón de exportación en listados (pendiente UI)

---

## Fase 9: Auto-Update + Release
**Estado: COMPLETADO**

### Auto-Update
- [x] `UpdateChecker` - chequeo contra GitHub API releases
- [x] `UpdateInstaller` - descarga e instalación via DownloadManager
- [x] `UpdateDialog` - interfaz de actualización
- [x] `UpdateViewModel` - manejo de estado
- [x] Chequeo automático al abrir la app
- [x] Botón manual en HomeScreen

### Release
- [x] `release.ps1` - script de release automatizado
- [ ] GitHub repo configurado: `betobeto00/OmniServ` (pendiente)
- [ ] Keystore de release configurado (pendiente)
- [ ] Tag v1.0 + Release v1.0 publicada (pendiente)

---

## Fase 10: Logo + Branding + Testing
**Estado: COMPLETADO**

### Branding
- [x] Logo OmniServ (Conexión de Servicios - hexágono con "OS")
- [x] Adaptive icon (todas las densidades)
- [x] Splash screen
- [x] Colores Omnimargen en Theme.kt

### Testing
- [ ] Testing manual completo de todas las pantallas (pendiente usuario)
- [ ] Verificar licenciamiento end-to-end (pendiente backend)
- [ ] Verificar notificaciones (pendiente usuario)
- [ ] Verificar PDF export (pendiente usuario)
- [ ] Verificar auto-update (pendiente usuario)
- [ ] Verificar en múltiples dispositivos (pendiente usuario)

### Documentación
- [x] README.md actualizado
- [x] ARCHITECTURE.md actualizado
- [x] LICENSE_SYSTEM.md revisado
- [x] ROADMAP.md con todos los checks completados

---

## Resumen de Estado

| Fase | Estado |
|------|--------|
| Fase 0 - Setup | COMPLETADO |
| Fase 1 - Datos | COMPLETADO |
| Fase 2 - Dominio | COMPLETADO |
| Fase 3 - UI Clients+Types | COMPLETADO |
| Fase 4 - UI Operators | COMPLETADO |
| Fase 5 - UI Services | COMPLETADO |
| Fase 6 - UI History+Dashboard | COMPLETADO |
| Fase 7 - Licencias | COMPLETADO |
| Fase 8 - Notificaciones+PDF | COMPLETADO |
| Fase 9 - Auto-Update | COMPLETADO |
| Fase 10 - Branding | COMPLETADO |

---

## Ideas Futuras (no priorizadas)

- [ ] Modo oscuro
- [ ] Backup automático a Google Drive
- [ ] Estadísticas de ingresos por mes con gráficos
- [ ] Filtros avanzados en historial
- [ ] Exportar datos a Excel/CSV
- [ ] Multi-idioma (ES/EN)
- [ ] Modo offline mejorado
- [ ] Sincronización con TOG Admin
- [ ] Mapa de ubicación de servicios
- [ ] Calendario visual de servicios
- [ ] Firma digital de clientes al recibir servicio
- [ ] Fotos antes/después del servicio
- [ ] Calculadora de costos por servicio
- [ ] Recordatorios personalizables (1 día, 3 días, 1 semana)
