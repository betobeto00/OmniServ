# Base de Datos - OmniServ

## Diagrama de Relaciones

```
clients ──────┐
              ├──→ services ──→ service_operators ──→ operators
service_types ┘         │
                        └──→ service_types (FK)

license (independiente)
```

---

## Tablas

### `clients`

Almacena los clientes del usuario.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Identificador único |
| `nombre` | TEXT | NOT NULL | Nombre del cliente |
| `telefono` | TEXT | NOT NULL | Teléfono de contacto |
| `direccion` | TEXT | NOT NULL | Dirección del cliente |
| `fechaCreacion` | INTEGER | NOT NULL | Timestamp de creación |

**Índices**: Ninguno adicional (búsqueda por nombre via LIKE).

---

### `service_types`

Tipos de servicio creados por el usuario. Tabla abierta.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Identificador único |
| `nombre` | TEXT | NOT NULL UNIQUE | Nombre del tipo de servicio |
| `activo` | INTEGER | NOT NULL DEFAULT 1 | 1 = activo, 0 = inactivo |
| `fechaCreacion` | INTEGER | NOT NULL | Timestamp de creación |

**Ejemplos de registros**:
- "Limpieza de aire acondicionado"
- "Reparación de UMA"
- "Fumigación general"
- "Servicio técnico computarizado"
- "Reparación de nevera"

---

### `operators`

Técnicos/operarios que prestan servicios.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Identificador único |
| `nombre` | TEXT | NOT NULL | Nombre del operario |
| `telefono` | TEXT | NOT NULL | Teléfono de contacto |
| `especialidad` | TEXT | DEFAULT '' | Especialidad (opcional) |
| `activo` | INTEGER | NOT NULL DEFAULT 1 | 1 = activo, 0 = inactivo |
| `fechaCreacion` | INTEGER | NOT NULL | Timestamp de creación |

---

### `services`

Servicios programados. Tabla principal.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Identificador único |
| `clienteId` | INTEGER | FOREIGN KEY → clients.id CASCADE | Cliente asociado |
| `serviceTypeId` | INTEGER | FOREIGN KEY → service_types.id SET NULL | Tipo de servicio |
| `tipoServicio` | TEXT | NOT NULL | Nombre del tipo (cache para display rápido) |
| `fechaServicio` | INTEGER | NOT NULL | Timestamp del día programado |
| `monto` | REAL | NOT NULL | Monto cobrado al cliente |
| `estado` | TEXT | NOT NULL DEFAULT 'PENDIENTE' | Estado del servicio |
| `notas` | TEXT | DEFAULT '' | Notas adicionales |
| `fechaCreacion` | INTEGER | NOT NULL | Timestamp de creación del registro |

**Valores de `estado`**:
- `PENDIENTE` — Servicio programado, aún no inicia
- `EN_PROGRESO` — Servicio en ejecución
- `REALIZADO` — Servicio completado
- `CANCELADO` — Servicio cancelado

**Nota**: `tipoServicio` es redundante con `serviceTypeId` pero permite mostrar el nombre del tipo sin JOIN, y sobrevive si el tipo se elimina (SET NULL en FK).

---

### `service_operators`

Relación N:M entre servicios y operarios. Controla quién trabaja en cada servicio y cuánto se le paga.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY AUTOINCREMENT | Identificador único |
| `serviceId` | INTEGER | FOREIGN KEY → services.id CASCADE | Servicio asociado |
| `operatorId` | INTEGER | FOREIGN KEY → operators.id CASCADE | Operario asociado |
| `montoPago` | REAL | DEFAULT 0 | Monto a pagar al operario |

**Constraints**:
- `UNIQUE(serviceId, operatorId)` — Un operario no puede estar asignado dos veces al mismo servicio
- `CASCADE` en ambos FK — Si se elimina el servicio o el operario, se elimina la asignación

---

### `license`

Licencia de activación de la app. Solo un registro por dispositivo.

| Columna | Tipo | Constraints | Descripción |
|---------|------|-------------|-------------|
| `id` | INTEGER | PRIMARY KEY (= 1 siempre) | Identificador fijo |
| `licenseKey` | TEXT | NOT NULL | Clave de licencia |
| `empresaId` | TEXT | NOT NULL | ID de empresa en TOG Platform |
| `fechaEmision` | INTEGER | NOT NULL | Timestamp de emisión |
| `fechaExpiracion` | INTEGER | NOT NULL | Timestamp de expiración |
| `modulosActivos` | TEXT | NOT NULL | JSON array: `["omniserv"]` |
| `firma` | TEXT | NOT NULL | Firma RSA-2048 (base64) |

**Nota**: El `id` siempre es 1 porque solo se almacena una licencia por dispositivo. Se usa `REPLACE` en el DAO para sobreescribir.

---

## Migraciones

### v1 (inicial)

Crea las 6 tablas con las siguientes FOREIGN KEYs:

```sql
-- clients
CREATE TABLE clients (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    telefono TEXT NOT NULL,
    direccion TEXT NOT NULL,
    fechaCreacion INTEGER NOT NULL
);

-- service_types
CREATE TABLE service_types (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL UNIQUE,
    activo INTEGER NOT NULL DEFAULT 1,
    fechaCreacion INTEGER NOT NULL
);

-- operators
CREATE TABLE operators (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    nombre TEXT NOT NULL,
    telefono TEXT NOT NULL,
    especialidad TEXT DEFAULT '',
    activo INTEGER NOT NULL DEFAULT 1,
    fechaCreacion INTEGER NOT NULL
);

-- services
CREATE TABLE services (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    clienteId INTEGER NOT NULL REFERENCES clients(id) ON DELETE CASCADE,
    serviceTypeId INTEGER REFERENCES service_types(id) ON DELETE SET NULL,
    tipoServicio TEXT NOT NULL,
    fechaServicio INTEGER NOT NULL,
    monto REAL NOT NULL,
    estado TEXT NOT NULL DEFAULT 'PENDIENTE',
    notas TEXT DEFAULT '',
    fechaCreacion INTEGER NOT NULL
);

-- service_operators
CREATE TABLE service_operators (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    serviceId INTEGER NOT NULL REFERENCES services(id) ON DELETE CASCADE,
    operatorId INTEGER NOT NULL REFERENCES operators(id) ON DELETE CASCADE,
    montoPago REAL DEFAULT 0,
    UNIQUE(serviceId, operatorId)
);

-- license
CREATE TABLE license (
    id INTEGER PRIMARY KEY,
    licenseKey TEXT NOT NULL,
    empresaId TEXT NOT NULL,
    fechaEmision INTEGER NOT NULL,
    fechaExpiracion INTEGER NOT NULL,
    modulosActivos TEXT NOT NULL,
    firma TEXT NOT NULL
);
```

### Futuras migraciones

- Agregar columnas según necesidad
- Nunca borrar datos existentes
- Usar `ALTER TABLE ADD COLUMN` cuando sea posible
- Para cambios complejos: crear tabla nueva → migrar datos → eliminar tabla vieja

---

## Conversores de Tipo

### DateConverter

```kotlin
class DateConverter {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? = value?.let { Date(it) }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? = date?.time
}
```

Las fechas se almacenan como `Long` (timestamp en milisegundos) y se convierten a `Date` para el dominio.

---

## Consultas Comunes

### Servicios pendientes de hoy
```sql
SELECT * FROM services
WHERE fechaServicio BETWEEN :startOfDay AND :endOfDay
AND estado = 'PENDIENTE'
ORDER BY fechaServicio ASC
```

### Servicios con operarios (JOIN)
```sql
SELECT s.*, so.operatorId, so.montoPago, o.nombre as operatorName
FROM services s
LEFT JOIN service_operators so ON s.id = so.serviceId
LEFT JOIN operators o ON so.operatorId = o.id
WHERE s.id = :serviceId
```

### Resumen de pagos por operario
```sql
SELECT o.nombre, SUM(so.montoPago) as totalPagado
FROM operators o
JOIN service_operators so ON o.id = so.operatorId
JOIN services s ON so.serviceId = s.id
WHERE s.estado = 'REALIZADO'
AND s.fechaServicio BETWEEN :start AND :end
GROUP BY o.id
```

### Ingresos totales por período
```sql
SELECT SUM(monto) as totalIngresos
FROM services
WHERE estado = 'REALIZADO'
AND fechaServicio BETWEEN :start AND :end
```
