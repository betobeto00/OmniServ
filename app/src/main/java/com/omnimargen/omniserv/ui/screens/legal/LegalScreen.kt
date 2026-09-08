package com.omnimargen.omniserv.ui.screens.legal

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.omnimargen.omniserv.ui.components.BannerTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LegalScreen(
    onNavigateBack: () -> Unit
) {
    Scaffold(
        topBar = {
            BannerTopBar(
                title = "Información Legal",
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "OmniServ",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.primary
            )

            Text(
                text = "Un producto de OmniMargen",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            LegalSection(
                title = "Política de Privacidad",
                content = """
                    Última actualización: 8 de septiembre de 2026

                    OmniServ es una aplicación desarrollada por OmniMargen para la gestión de servicios múltiples.

                    INFORMACIÓN QUE RECOPILAMOS:
                    • Datos que usted ingresa (clientes, servicios, operarios)
                    • Identificador del dispositivo (para validación de licencia)
                    • Información del sistema operativo

                    USO DE LA INFORMACIÓN:
                    • Proporcionar y mantener el servicio
                    • Validar y gestionar su licencia
                    • Enviar notificaciones sobre servicios próximos
                    • Generar reportes PDF

                    ALMACENAMIENTO:
                    • Toda la información se almacena localmente en su dispositivo
                    • No enviamos sus datos a servidores externos

                    CONTACTO:
                    OmniMargen - soporte@omnimargen.com
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LegalSection(
                title = "Términos de Uso",
                content = """
                    Última actualización: 8 de septiembre de 2026

                    Al utilizar OmniServ, usted acepta estos términos.

                    LICENCIA DE USO:
                    • Licencia limitada, no exclusiva y revocable
                    • No puede copiar, modificar o distribuir la aplicación
                    • No puede realizar ingeniería inversa

                    USO ACEPTABLE:
                    • Uso únicamente para fines lícitos
                    • Responsabilidad de mantener confidencial su API Key
                    • Notificar uso no autorizado

                    PROPIEDAD INTELECTUAL:
                    • OmniServ es propiedad de OmniMargen
                    • Protegido por leyes de propiedad intelectual

                    CONTACTO:
                    OmniMargen - soporte@omnimargen.com
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(16.dp))

            LegalSection(
                title = "Licencia de Software",
                content = """
                    MIT License

                    Copyright (c) 2026 OmniMargen

                    Se concede permiso, libre de cargo, a cualquier persona que obtenga una copia de este software y los archivos de documentación asociados, para operar con el Software sin restricción, incluyendo sin limitación los derechos de uso, copia, modificación, fusión, publicación, distribución, sublicencia y/o venta de copias del Software.

                    EL SOFTWARE SE PROPORCIONA "TAL CUAL", SIN GARANTÍA DE NINGÚN TIPO.

                    Nota: La marca "OmniServ" y "OmniMargen" están protegidas por las leyes de marcas registradas.
                """.trimIndent()
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "© 2026 OmniMargen. Todos los derechos reservados.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "OmniServ es parte del ecosistema OmniMargen",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun LegalSection(title: String, content: String) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = content,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
