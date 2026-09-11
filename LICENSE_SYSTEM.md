# Sistema de Licenciamiento - OmniServ

## Visión General

OmniServ usa un sistema de licenciamiento RSA-2048 offline, integrado con **TOG Platform** (el backend de licencias de OmniMargen).

Una sola licencia puede cubrir múltiples apps: TOG Admin + OmniServ + futuros módulos.

```
┌─────────────────────────────────────────────────────┐
│                  TOG Platform                        │
│  (Backend de licencias - Node.js + SQLite)           │
│                                                      │
│  ┌─────────────┐    ┌──────────────┐                │
│  │  Empresas    │    │  Licencias   │                │
│  │  (register)  │───→│  (sign RSA)  │                │
│  └─────────────┘    └──────┬───────┘                │
│                             │                         │
│                    GET /api/empresas/:id/licencia     │
│                             │                         │
└─────────────────────────────┼───────────────────────┘
                              │
                    ┌─────────┴─────────┐
                    │                   │
              ┌─────▼─────┐     ┌──────▼──────┐
              │ TOG Admin  │     │  OmniServ   │
              │ (desktop)  │     │  (Android)  │
              └───────────┘     └─────────────┘
```

---

## Cómo Funciona

### Flujo de Activación

```
1. Usuario instala APK de OmniServ
         │
         ▼
2. Splash Screen → LicenseValidator.checkLocalLicense()
         │
         ├─ Licencia encontrada y válida → HomeScreen ✓
         ├─ Licencia expirada < 48 horas → HomeScreen (grace period) ⚠️
         ├─ Licencia expirada > 48 horas → ActivationScreen
         └─ Sin licencia → ActivationScreen
         │
         ▼
3. ActivationScreen (pantalla completa, sin bottom nav)
   - Usuario ingresa su api_key de TOG Platform
         │
         ▼
4. App llama a TOG Platform:
   GET http://localhost:3001/api/empresas/:id/licencia
   Headers: { "x-api-key": "ak_xxxxxxxxxxxxxxxx" }
         │
         ▼
5. TOG Platform retorna licencia firmada RSA:
   {
     "id": "a1b2c3d4e5f6",
     "cliente": "Empresa XYZ",
     "expira": "2025-12-31",
     "modules": ["omniserv"],
     "firma": "base64_rsa_signature..."
   }
         │
         ▼
6. App valida firma RSA localmente (public key embebida)
         │
         ├─ Firma válida → Guardar en Room DB → HomeScreen ✓
         └─ Firma inválida → Mostrar error de activación ✗
```

### Validación Periódica

```
Cada 24 horas (WorkManager):
         │
         ▼
LicenseWorker.checkLicense()
         │
         ├─ Válida → no hacer nada
         ├─ Expirada < 48h → mostrar warning al usuario
         └─ Expirada > 48h → bloquear app → ActivationScreen

Al cada reinicio de app:
         │
         ▼
LicenseValidator.checkLocalLicense()
         │
         ├─ Válida → continuar
         └─ Inválida → ActivationScreen
```

---

## Estructura de la Licencia (Payload)

```json
{
  "id": "a1b2c3d4e5f6",
  "cliente": "Empresa XYZ C.A.",
  "expira": "2025-12-31",
  "version": "1.0.0",
  "machineId": "hash_del_dispositivo",
  "modules": ["omniserv"],
  "max_pcs": 1,
  "emitida": "2024-01-15T10:30:00.000Z",
  "firma": "base64_rsa_signature_here..."
}
```

| Campo | Tipo | Descripción |
|-------|------|-------------|
| `id` | string | ID único de la licencia (8 bytes hex) |
| `cliente` | string | Nombre de la empresa |
| `expira` | string | Fecha de expiración (YYYY-MM-DD) |
| `version` | string | Versión del payload |
| `machineId` | string | Hash del dispositivo (opcional) |
| `modules` | array | Módulos activos: `["omniserv"]` |
| `max_pcs` | number | Máximo de PCs/dispositivos (1-20) |
| `emitida` | string | Fecha de emisión (ISO 8601) |
| `firma` | string | Firma RSA-2048 (base64) del payload sin `firma` |

---

## Firma RSA-2048

### Generación (TOG Platform)

```
Private Key (nunca sale del servidor)
         │
         ▼
Payload JSON (sin campo "firma")
         │
         ▼
SHA256withRSA (PKCS#1 v1.5)
         │
         ▼
Firma base64 → campo "firma" en el payload
```

### Verificación (OmniServ Android)

```
Payload JSON completo
         │
         ├── Extraer "firma"
         └── Extraer todo lo demás (dataToVerify)
         │
         ▼
Public Key (embebida en la app)
         │
         ▼
SHA256withRSA.verify(dataToVerify, firma)
         │
         ├─ true → Licencia válida ✓
         └─ false → Licencia inválida ✗
```

### Claves

| Clave | Ubicación | Acceso |
|-------|-----------|--------|
| Private Key | TOG Platform (`LICENSE_PRIVATE_KEY_PATH`) | Solo el servidor |
| Public Key | OmniServ Android (hardcoded) | Embebida en la app |

**La private key nunca sale del servidor.** La public key está embebida en la app y es de conocimiento público (no es secreta).

---

## Generación de Licencias

### Prerequisitos

1. TOG Platform corriendo (ver `tog-platform/`)
2. Par de claves RSA generadas
3. Empresa registrada en TOG Platform

### Paso 1: Generar par de claves RSA (solo una vez)

```bash
# En tog-platform/
openssl genpkey -algorithm RSA -out private_key.pem -pkeyopt rsa_keygen_bits:2048
openssl rsa -in private_key.pem -pubout -out public_key.pem
```

Configurar la ruta de la private key en TOG Platform:
```bash
export LICENSE_PRIVATE_KEY_PATH="/path/to/private_key.pem"
```

La public key se copia a OmniServ Android (en `LicenseValidator.kt`).

### Paso 2: Registrar Empresa

```bash
curl -X POST http://localhost:3000/api/empresas \
  -H "Content-Type: application/json" \
  -H "x-admin-api-key: tu_admin_key" \
  -d '{
    "nombre": "Empresa del Cliente",
    "pais": "VE",
    "documento": "J-12345678-9",
    "email_contacto": "cliente@empresa.com"
  }'
```

**Response:**
```json
{
  "id": 1,
  "nombre": "Empresa del Cliente",
  "api_key": "ak_a1b2c3d4e5f6g7h8"
}
```

**Guardar la `api_key`** — el usuario la necesita para activar la app.

### Paso 3: Emitir Licencia

```bash
curl -X POST http://localhost:3000/api/empresas/1/licencias \
  -H "Content-Type: application/json" \
  -H "x-admin-api-key: tu_admin_key" \
  -d '{
    "modules": ["omniserv"],
    "expires_at": "2025-12-31",
    "max_pcs": 1
  }'
```

**Response:**
```json
{
  "id": 1,
  "empresa_id": 1,
  "payload_json": "{...licencia firmada completa...}",
  "modules": ["omniserv"],
  "expires_at": "2025-12-31"
}
```

### Paso 4: Entregar al Usuario

El usuario necesita:
1. El **APK** de OmniServ
2. Su **api_key** de TOG Platform

**Desde la app:**
1. Abrir OmniServ
2. Ir a ActivationScreen
3. Ingresar la api_key
4. La app descarga y valida la licencia automáticamente

---

## Compatibilidad con TOG Platform

### Cambios Necesarios (mínimos)

Solo **2 archivos** necesitan cambios para soportar OmniServ:

### Estado actual (10-Sep-2026)

`omniserv` **ya está incluido** en `MODULE_IDS` de `tog-platform/src/sign.js` (línea 6). No se necesitan cambios adicionales para habilitar el módulo OmniServ en el backend.

### Lo que NO necesita cambios

- Base de datos de TOG Platform → almacena modules como JSON array, genérico
- Endpoint de licencias → sirve cualquier módulo
- Stripe checkout → auto-detecta nuevos módulos si configuras `STRIPE_PRICE_OMNISERV`
- Verificación de firma → es genérica, verifica cualquier payload
- Grace period → funciona igual
- Anti-tampering → funciona igual

### Una Licencia, Múltiples Apps

Una sola licencia puede contener:
```json
{
  "modules": ["comercializador", "distribuidor", "omniserv"]
}
```

Esto permite que un usuario tenga TOG Admin + OmniServ con una sola licencia.

---

## Validación en la App

### LicenseValidator.kt

```kotlin
class LicenseValidator @Inject constructor(
    private val licenseRepository: LicenseRepository,
    @ApplicationContext private val context: Context
) {
    companion object {
        // Public key embebida (RSA-2048)
        private const val PUBLIC_KEY = "MIIBIjANBgkqh..."
    }

    // Verificar firma RSA
    fun verifySignature(license: License): Boolean {
        val record = license.toMap()
        val firma = record["firma"] as String
        val dataToVerify = record.filterKeys { it != "firma" }

        val verify = Signature.getInstance("SHA256withRSA")
        verify.initVerify(loadPublicKey())
        verify.update(JSON.toJSONString(dataToVerify).toByteArray())
        return verify.verify(Base64.decode(firma, Base64.DEFAULT))
    }

    // Verificar expiración
    fun isExpired(license: License): Boolean {
        return Date().after(license.fechaExpiracion)
    }

    // Verificar si está en grace period (48h después de expiración)
    fun isInGracePeriod(license: License): Boolean {
        val now = Date()
        val expiry = license.fechaExpiracion
        val graceEnd = Date(expiry.time + (48 * 60 * 60 * 1000))
        return now.after(expiry) && now.before(graceEnd)
    }

    // Verificar módulo activo
    fun hasModule(license: License, moduleId: String): Boolean {
        return license.modulosActivos.contains(moduleId)
    }

    // Chequeo completo
    suspend fun validate(): LicenseStatus {
        val license = licenseRepository.getLicense()
            ?: return LicenseStatus.NotActivated

        if (!verifySignature(license)) {
            return LicenseStatus.InvalidSignature
        }

        if (!hasModule(license, "omniserv")) {
            return LicenseStatus.InvalidSignature
        }

        return when {
            !isExpired(license) -> LicenseStatus.Valid
            isInGracePeriod(license) -> {
                val daysRemaining = calculateGraceDays(license)
                LicenseStatus.GracePeriod(daysRemaining)
            }
            else -> LicenseStatus.Expired
        }
    }
}
```

### LicenseGenerator.kt (Device Fingerprint)

```kotlin
class LicenseGenerator @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun generateDeviceFingerprint(): String {
        val androidId = Settings.Secure.getString(
            context.contentResolver,
            Settings.Secure.ANDROID_ID
        )
        val buildInfo = "${Build.BOARD}${Build.BRAND}${Build.DEVICE}${Build.MANUFACTURER}"
        val combined = "$androidId:$buildInfo"

        val digest = MessageDigest.getInstance("SHA-256")
        val hash = digest.digest(combined.toByteArray())
        return hash.joinToString("") { "%02x".format(it) }
    }
}
```

---

## Seguridad

| Aspecto | Implementación |
|---------|---------------|
| Private key | Nunca en el cliente, solo en TOG Platform |
| Public key | Embebida en la app (no es secreta, es pública) |
| Firma | RSA-2048 SHA256withRSA (PKCS#1 v1.5) |
| Almacenamiento | Room DB (SQLite local, encriptado por Android) |
| Transporte | HTTPS (TOG Platform) |
| Offline | Grace period de 48 horas |
| Anti-tampering | Verificación de firma en cada inicio |
| Dispositivo | Device fingerprint para limitar dispositivos |

---

## Troubleshooting

### "Licencia inválida"
- Verificar que la api_key sea correcta
- Verificar conexión a internet la primera vez
- Verificar que TOG Platform esté corriendo
- Revisar logs del servidor

### "Licencia expirada"
- Solicitar nueva licencia a OmniMargen
- O re-activar con la misma api_key (si se emitió una nueva licencia)

### "Máximo de dispositivos alcanzado"
- La licencia tiene `max_pcs` limitado
- Contactar OmniMargen para aumentar el límite
- O revocar licencia en otro dispositivo

### "Error de firma"
- La licencia fue modificada o corrupta
- Re-descargar desde TOG Platform
- Si persiste, contactar soporte

### "Error de conexión"
- Verificar que TOG Platform esté accesible
- Verificar conexión a internet
- La app funciona offline después de la primera activación (grace period 48h)

---

## Endpoints de TOG Platform para OmniServ

| Método | Ruta | Auth | Descripción |
|--------|------|------|-------------|
| `GET` | `/api/health` | none | Health check |
| `POST` | `/api/empresas` | admin | Registrar empresa (devuelve api_key) |
| `POST` | `/api/empresas/:id/licencias` | admin | Emitir licencia firmada |
| `GET` | `/api/empresas/:id/licencia` | api_key | Obtener licencia activa (desde app) |

La app solo usa `GET /api/empresas/:id/licencia` con la api_key del usuario.

> **Nota:** TOG Platform corre en el puerto **3001** (configurable via `PORT` en `.env`).
