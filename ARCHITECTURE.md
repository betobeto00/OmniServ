# Arquitectura - OmniServ

## Visión General

La app sigue **Clean Architecture** con patrón **MVVM** separado en 3 capas:

```
┌─────────────────────────────────────────────┐
│                   UI Layer                   │
│  Screens (Compose) ──→ ViewModels ──→ State  │
├─────────────────────────────────────────────┤
│                Domain Layer                  │
│              Use Cases ──→ Models            │
├─────────────────────────────────────────────┤
│                 Data Layer                   │
│    Repository ──→ DAO ──→ Room Database      │
└─────────────────────────────────────────────┘
```

---

## Capa de Datos

### Base de datos Room

#### ClientEntity
```kotlin
@Entity(tableName = "clients")
data class ClientEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val telefono: String,
    val direccion: String,
    val fechaCreacion: Long
)
```

#### ServiceTypeEntity
```kotlin
@Entity(tableName = "service_types")
data class ServiceTypeEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val activo: Boolean = true,
    val fechaCreacion: Long
)
```

#### OperatorEntity
```kotlin
@Entity(tableName = "operators")
data class OperatorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val telefono: String,
    val especialidad: String = "",
    val activo: Boolean = true,
    val fechaCreacion: Long
)
```

#### ServiceEntity
```kotlin
@Entity(
    tableName = "services",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clienteId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ServiceTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceTypeId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class ServiceEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val clienteId: Long,
    val serviceTypeId: Long? = null,
    val tipoServicio: String,
    val fechaServicio: Long,
    val monto: Double,
    val estado: String = "PENDIENTE",
    val notas: String = "",
    val fechaCreacion: Long
)
```

#### ServiceOperatorEntity
```kotlin
@Entity(
    tableName = "service_operators",
    foreignKeys = [
        ForeignKey(
            entity = ServiceEntity::class,
            parentColumns = ["id"],
            childColumns = ["serviceId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = OperatorEntity::class,
            parentColumns = ["id"],
            childColumns = ["operatorId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["serviceId", "operatorId"], unique = true)]
)
data class ServiceOperatorEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val serviceId: Long,
    val operatorId: Long,
    val montoPago: Double = 0.0
)
```

#### LicenseEntity
```kotlin
@Entity(tableName = "license")
data class LicenseEntity(
    @PrimaryKey
    val id: Int = 1,
    val licenseKey: String,
    val empresaId: String,
    val fechaEmision: Long,
    val fechaExpiracion: Long,
    val modulosActivos: String,
    val firma: String
)
```

### DAOs

```kotlin
@Dao
interface ClientDao {
    @Query("SELECT * FROM clients ORDER BY nombre ASC")
    fun getAll(): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE nombre LIKE '%' || :query || '%'")
    fun search(query: String): Flow<List<ClientEntity>>

    @Query("SELECT * FROM clients WHERE id = :id")
    suspend fun getById(id: Long): ClientEntity?

    @Insert
    suspend fun insert(client: ClientEntity): Long

    @Update
    suspend fun update(client: ClientEntity)

    @Delete
    suspend fun delete(client: ClientEntity)
}

@Dao
interface ServiceTypeDao {
    @Query("SELECT * FROM service_types WHERE activo = 1 ORDER BY nombre ASC")
    fun getActive(): Flow<List<ServiceTypeEntity>>

    @Query("SELECT * FROM service_types ORDER BY nombre ASC")
    fun getAll(): Flow<List<ServiceTypeEntity>>

    @Query("SELECT * FROM service_types WHERE id = :id")
    suspend fun getById(id: Long): ServiceTypeEntity?

    @Insert
    suspend fun insert(serviceType: ServiceTypeEntity): Long

    @Update
    suspend fun update(serviceType: ServiceTypeEntity)

    @Delete
    suspend fun delete(serviceType: ServiceTypeEntity)
}

@Dao
interface OperatorDao {
    @Query("SELECT * FROM operators WHERE activo = 1 ORDER BY nombre ASC")
    fun getActive(): Flow<List<OperatorEntity>>

    @Query("SELECT * FROM operators ORDER BY nombre ASC")
    fun getAll(): Flow<List<OperatorEntity>>

    @Query("SELECT * FROM operators WHERE id = :id")
    suspend fun getById(id: Long): OperatorEntity?

    @Insert
    suspend fun insert(operator: OperatorEntity): Long

    @Update
    suspend fun update(operator: OperatorEntity)

    @Delete
    suspend fun delete(operator: OperatorEntity)
}

@Dao
interface ServiceDao {
    @Query("SELECT * FROM services ORDER BY fechaServicio DESC")
    fun getAll(): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE fechaServicio BETWEEN :start AND :end ORDER BY fechaServicio ASC")
    fun getByDateRange(start: Long, end: Long): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE clienteId = :clientId ORDER BY fechaServicio DESC")
    fun getByClientId(clientId: Long): Flow<List<ServiceEntity>>

    @Query("SELECT * FROM services WHERE fechaServicio BETWEEN :now AND :twoDaysFromNow AND estado = 'PENDIENTE'")
    suspend fun getUpcoming(now: Long, twoDaysFromNow: Long): List<ServiceEntity>

    @Query("SELECT * FROM services WHERE id = :id")
    suspend fun getById(id: Long): ServiceEntity?

    @Insert
    suspend fun insert(service: ServiceEntity): Long

    @Update
    suspend fun update(service: ServiceEntity)

    @Delete
    suspend fun delete(service: ServiceEntity)
}

@Dao
interface ServiceOperatorDao {
    @Query("SELECT * FROM service_operators WHERE serviceId = :serviceId")
    fun getByServiceId(serviceId: Long): Flow<List<ServiceOperatorEntity>>

    @Query("SELECT * FROM service_operators WHERE operatorId = :operatorId")
    fun getByOperatorId(operatorId: Long): Flow<List<ServiceOperatorEntity>>

    @Insert
    suspend fun insert(serviceOperator: ServiceOperatorEntity): Long

    @Insert
    suspend fun insertAll(serviceOperators: List<ServiceOperatorEntity>)

    @Delete
    suspend fun delete(serviceOperator: ServiceOperatorEntity)

    @Query("DELETE FROM service_operators WHERE serviceId = :serviceId")
    suspend fun deleteByServiceId(serviceId: Long)
}

@Dao
interface LicenseDao {
    @Query("SELECT * FROM license WHERE id = 1")
    suspend fun getLicense(): LicenseEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(license: LicenseEntity)

    @Query("DELETE FROM license")
    suspend fun deleteAll()
}
```

### Repositorios

```kotlin
class ClientRepository @Inject constructor(
    private val clientDao: ClientDao
) {
    fun getAll(): Flow<List<Client>> = clientDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    fun search(query: String): Flow<List<Client>> = clientDao.search(query).map {
        it.map { entity -> entity.toDomain() }
    }

    suspend fun getById(id: Long): Client? = clientDao.getById(id)?.toDomain()

    suspend fun insert(client: Client): Long = clientDao.insert(client.toEntity())

    suspend fun update(client: Client) = clientDao.update(client.toEntity())

    suspend fun delete(client: Client) = clientDao.delete(client.toEntity())
}

class ServiceTypeRepository @Inject constructor(
    private val serviceTypeDao: ServiceTypeDao
) {
    fun getActive(): Flow<List<ServiceType>> = serviceTypeDao.getActive().map {
        it.map { entity -> entity.toDomain() }
    }

    fun getAll(): Flow<List<ServiceType>> = serviceTypeDao.getAll().map {
        it.map { entity -> entity.toDomain() }
    }

    suspend fun getById(id: Long): ServiceType? = serviceTypeDao.getById(id)?.toDomain()

    suspend fun insert(serviceType: ServiceType): Long =
        serviceTypeDao.insert(serviceType.toEntity())

    suspend fun update(serviceType: ServiceType) =
        serviceTypeDao.update(serviceType.toEntity())

    suspend fun delete(serviceType: ServiceType) =
        serviceTypeDao.delete(serviceType.toEntity())
}

class OperatorRepository @Inject constructor(
    private val operatorDao: OperatorDao
) {
    fun getActive(): Flow<List<Operator>> = operatorDao.getActive().map {
        it.map { entity -> entity.toDomain() }
    }

    fun getAll(): Flow<List<Operator>> = operatorDao.getAll().map {
        it.map { entity -> entity.toDomain() }
    }

    suspend fun getById(id: Long): Operator? = operatorDao.getById(id)?.toDomain()

    suspend fun insert(operator: Operator): Long = operatorDao.insert(operator.toEntity())

    suspend fun update(operator: Operator) = operatorDao.update(operator.toEntity())

    suspend fun delete(operator: Operator) = operatorDao.delete(operator.toEntity())
}

class ServiceRepository @Inject constructor(
    private val serviceDao: ServiceDao,
    private val serviceOperatorDao: ServiceOperatorDao
) {
    fun getAll(): Flow<List<Service>> = serviceDao.getAll().map { entities ->
        entities.map { it.toDomain() }
    }

    fun getByDateRange(start: Long, end: Long): Flow<List<Service>> =
        serviceDao.getByDateRange(start, end).map { entities ->
            entities.map { it.toDomain() }
        }

    suspend fun getUpcoming(now: Long, twoDaysFromNow: Long): List<Service> =
        serviceDao.getUpcoming(now, twoDaysFromNow).map { it.toDomain() }

    suspend fun getById(id: Long): Service? = serviceDao.getById(id)?.toDomain()

    suspend fun insert(service: Service): Long = serviceDao.insert(service.toEntity())

    suspend fun update(service: Service) = serviceDao.update(service.toEntity())

    suspend fun delete(service: Service) = serviceDao.delete(service.toEntity())

    suspend fun assignOperators(serviceId: Long, operators: List<ServiceOperator>) {
        serviceOperatorDao.deleteByServiceId(serviceId)
        serviceOperatorDao.insertAll(operators.map { it.toEntity(serviceId) })
    }

    fun getOperatorsByServiceId(serviceId: Long): Flow<List<ServiceOperator>> =
        serviceOperatorDao.getByServiceId(serviceId).map {
            it.map { entity -> entity.toDomain() }
        }
}

class LicenseRepository @Inject constructor(
    private val licenseDao: LicenseDao
) {
    suspend fun getLicense(): License? = licenseDao.getLicense()?.toDomain()

    suspend fun saveLicense(license: License) = licenseDao.insert(license.toEntity())

    suspend fun deleteLicense() = licenseDao.deleteAll()
}
```

---

## Capa de Dominio

### Modelos de dominio

```kotlin
data class Client(
    val id: Long = 0,
    val nombre: String,
    val telefono: String,
    val direccion: String,
    val fechaCreacion: Date = Date()
)

data class ServiceType(
    val id: Long = 0,
    val nombre: String,
    val activo: Boolean = true,
    val fechaCreacion: Date = Date()
)

data class Operator(
    val id: Long = 0,
    val nombre: String,
    val telefono: String,
    val especialidad: String = "",
    val activo: Boolean = true,
    val fechaCreacion: Date = Date()
)

data class Service(
    val id: Long = 0,
    val clienteId: Long,
    val clienteNombre: String = "",
    val serviceTypeId: Long? = null,
    val tipoServicio: String,
    val fechaServicio: Date,
    val monto: Double,
    val estado: ServiceStatus = ServiceStatus.PENDIENTE,
    val operarios: List<ServiceOperator> = emptyList(),
    val notas: String = "",
    val fechaCreacion: Date = Date()
)

data class ServiceOperator(
    val id: Long = 0,
    val serviceId: Long = 0,
    val operatorId: Long,
    val operatorNombre: String = "",
    val montoPago: Double = 0.0
)

enum class ServiceStatus {
    PENDIENTE,
    EN_PROGRESO,
    REALIZADO,
    CANCELADO
}

data class License(
    val id: Int = 1,
    val licenseKey: String,
    val empresaId: String,
    val fechaEmision: Date,
    val fechaExpiracion: Date,
    val modulosActivos: List<String>,
    val firma: String
)

sealed class LicenseStatus {
    object Valid : LicenseStatus()
    object Expired : LicenseStatus()
    object NotActivated : LicenseStatus()
    object InvalidSignature : LicenseStatus()
    data class GracePeriod(val daysRemaining: Int) : LicenseStatus()
}
```

### Use Cases

Cada operación es un Use Case con una única responsabilidad:

```kotlin
class AddServiceUseCase @Inject constructor(
    private val serviceRepository: ServiceRepository,
    private val notificationHelper: NotificationHelper
) {
    suspend operator fun invoke(service: Service): Long {
        val id = serviceRepository.insert(service)
        notificationHelper.scheduleReminder(
            serviceId = id,
            scheduledDate = service.fechaServicio
        )
        return id
    }
}

class AssignOperatorsUseCase @Inject constructor(
    private val serviceRepository: ServiceRepository
) {
    suspend operator fun invoke(serviceId: Long, operators: List<ServiceOperator>) {
        serviceRepository.assignOperators(serviceId, operators)
    }
}

class ActivateLicenseUseCase @Inject constructor(
    private val licenseRepository: LicenseRepository,
    private val licenseValidator: LicenseValidator
) {
    suspend operator fun invoke(apiKey: String): LicenseStatus {
        val license = licenseValidator.fetchLicenseFromServer(apiKey)
        return if (license != null) {
            licenseRepository.saveLicense(license)
            licenseValidator.validate()
        } else {
            LicenseStatus.InvalidSignature
        }
    }
}
```

---

## Capa de UI

### Patrón MVVM

```
Screen (Compose) ──observes──→ ViewModel ──calls──→ UseCase ──calls──→ Repository
      │                              │
      └──renders State───────────────┘
```

### Ejemplo ViewModel

```kotlin
@HiltViewModel
class ServiceViewModel @Inject constructor(
    private val getServicesUseCase: GetServicesUseCase,
    private val addServiceUseCase: AddServiceUseCase,
    private val deleteServiceUseCase: DeleteServiceUseCase,
    private val assignOperatorsUseCase: AssignOperatorsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServiceUiState())
    val uiState: StateFlow<ServiceUiState> = _uiState.asStateFlow()

    init {
        loadServices()
    }

    private fun loadServices() {
        viewModelScope.launch {
            getServicesUseCase().collect { services ->
                _uiState.update { it.copy(services = services) }
            }
        }
    }

    fun addService(service: Service) {
        viewModelScope.launch {
            addServiceUseCase(service)
        }
    }

    fun assignOperators(serviceId: Long, operators: List<ServiceOperator>) {
        viewModelScope.launch {
            assignOperatorsUseCase(serviceId, operators)
        }
    }
}

data class ServiceUiState(
    val services: List<Service> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)
```

### Navegación

```
BottomNavigation (5 tabs)
├── Inicio (HomeScreen)
├── Clientes (ClientListScreen → ClientFormScreen)
├── Servicios (ServiceListScreen → ServiceFormScreen)
├── Operarios (OperatorListScreen → OperatorFormScreen)
└── Historial (HistoryScreen)

Pantallas sin bottom nav:
├── ActivationScreen (pre-login)
└── ServiceTypeListScreen → ServiceTypeFormScreen
```

Rutas:
- `home` - Dashboard principal
- `clients` - Lista de clientes
- `clients/new` - Crear cliente
- `clients/{id}/edit` - Editar cliente
- `services` - Lista de servicios
- `services/new` - Crear servicio
- `services/{id}/edit` - Editar servicio
- `operators` - Lista de operarios
- `operators/new` - Crear operario
- `operators/{id}/edit` - Editar operario
- `service-types` - Lista de tipos de servicio
- `service-types/new` - Crear tipo
- `service-types/{id}/edit` - Editar tipo
- `history` - Historial de servicios completados
- `activation` - Activación de licencia

---

## Sistema de Licencias

### Flujo de Activación

```
App instalada
    │
    ▼
Splash Screen
    │
    ▼
LicenseValidator.checkLocalLicense()
    │
    ├─ Licencia válida → HomeScreen
    ├─ Licencia expirada < 48h → HomeScreen (grace period)
    ├─ Licencia expirada > 48h → ActivationScreen
    └─ Sin licencia → ActivationScreen
    │
    ▼
ActivationScreen
    │
    ▼
Usuario ingresa api_key
    │
    ▼
GET /api/empresas/:id/licencia
    │
    ▼
TOG Platform retorna licencia firmada RSA
    │
    ▼
LicenseValidator.verifySignature()
    │
    ├─ Firma válida → Guardar en Room DB → HomeScreen
    └─ Firma inválida → Mostrar error
```

### Validación Periódica

```
WorkManager (cada 24 horas)
    │
    ▼
LicenseWorker.checkLicense()
    │
    ├─ Válida → no hacer nada
    ├─ Expirada < 48h → mostrar warning
    └─ Expirada > 48h → bloquear app → ActivationScreen
```

### Integración con TOG Platform

```
TOG Platform (tog-platform)
    │
    ├── /api/empresas          → Registrar empresa
    ├── /api/empresas/:id/licencias  → Emitir licencia
    └── /api/empresas/:id/licencia   → Obtener licencia (desde app)
    │
    ▼
Licencia firmada con módulo "omniserv"
    │
    ▼
OmniServ valida firma RSA localmente
```

---

## Sistema de Notificaciones

### Flujo

```
App crea servicio
    │
    ▼
AddServiceUseCase
    │
    ▼
NotificationHelper.scheduleReminder(serviceId, fechaServicio)
    │
    ▼
WorkManager.enqueueUniqueWork("reminder_$serviceId")
    │
    ▼
WorkManager ejecuta cada 24h (PeriodicWorkRequest)
    │
    ▼
ReminderWorker.checkUpcomingServices()
    │
    ▼
Si hay servicio en 2 días → mostrar notificación
```

---

## Sistema de Exportación PDF

### PdfHelper

Genera documentos PDF nativos usando `PdfDocument` de Android:

- **PDF de clientes**: N°, Nombre, Teléfono, Dirección
- **PDF de servicios**: N°, Cliente, Fecha, Tipo, Estado, Notas
- Se abre automáticamente via FileProvider

---

## Sistema de Auto-Update

### Flujo

```
HomeScreen (al abrir)
    │
    ▼
UpdateViewModel.checkForUpdate()
    │
    ▼
UpdateChecker.checkForUpdate()
    │
    ├── HttpURLConnection + User-Agent: OmniServ/Android
    ├── connectTimeout: 10s, readTimeout: 10s
    ├── GET https://api.github.com/repos/betobeto00/OmniServ/releases/latest
    │
    ├── HTTP 200 → parse JSON → comparar versiones
    │   ├── Nueva versión → UpdateInfo(versionName, apkUrl, ...)
    │   └── Misma versión → null (estás al día)
    │
    ├── HTTP 403 → IOException("HTTP 403: rate limit")
    ├── HTTP 429 → IOException("HTTP 429: too many requests")
    ├── Timeout → IOException("timeout")
    └── Sin red → IOException("Unable to resolve host")
    │
    ▼
UpdateViewModel (maneja errores con mensajes específicos)
    │
    ├── UpdateInfo → UpdateDialog → DownloadManager → PackageInstaller
    ├── Error de red → "Sin conexión. Verifica tu red."
    ├── HTTP 403 → "Límite de solicitudes de GitHub."
    ├── HTTP 429 → "Demasiadas solicitudes. Espera unos minutos."
    └── Otro error → "Error: [detalle del HTTP status]"
```

---

## Inyección de Dependencias (Hilt)

```kotlin
@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "omniserv.db"
        ).build()
    }

    @Provides
    fun provideClientDao(db: AppDatabase): ClientDao = db.clientDao()

    @Provides
    fun provideServiceTypeDao(db: AppDatabase): ServiceTypeDao = db.serviceTypeDao()

    @Provides
    fun provideOperatorDao(db: AppDatabase): OperatorDao = db.operatorDao()

    @Provides
    fun provideServiceDao(db: AppDatabase): ServiceDao = db.serviceDao()

    @Provides
    fun provideServiceOperatorDao(db: AppDatabase): ServiceOperatorDao =
        db.serviceOperatorDao()

    @Provides
    fun provideLicenseDao(db: AppDatabase): LicenseDao = db.licenseDao()
}
```

---

## Decisiones de Diseño

| Decisión | Justificación |
|----------|--------------|
| **Room** | Base de datos local robusta, soporte Flow, migrations |
| **Hilt** | DI estándar en Android, fácil testing |
| **WorkManager** | Sobrevive reinicios, confiable para recordatorios |
| **Compose** | UI moderna, menos boilerplate, state-driven |
| **Clean Architecture** | Separación de responsabilidades, testeable |
| **MVVM** | Patrón estándar Android, soporte lifecycle |
| **Use Cases** | Orquestan lógica, facilitan testing unitario |
| **PdfDocument** | PDF nativo sin dependencias externas |
| **DownloadManager** | Descargas nativas con notificación de progreso |
| **GitHub Releases** | Distribución simple, sin servidor propio |
| **RSA-2048 offline** | Validación sin internet, compatible con TOG Platform |
| **Service types abiertos** | Flexible para cualquier giro de negocio |
| **ServiceOperator junction** | Permite N:M entre servicios y operarios |
