package com.omnimargen.omniserv

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.omnimargen.omniserv.domain.model.LicenseStatus
import com.omnimargen.omniserv.domain.usecase.license.GetLicenseStatusUseCase
import com.omnimargen.omniserv.ui.navigation.OmniServNavGraph
import com.omnimargen.omniserv.ui.screens.activation.ActivationScreen
import com.omnimargen.omniserv.ui.screens.splash.SplashScreen
import com.omnimargen.omniserv.ui.theme.OmniServTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var getLicenseStatusUseCase: GetLicenseStatusUseCase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
        enableEdgeToEdge()
        setContent {
            OmniServTheme {
                var showSplash by remember { mutableStateOf(true) }
                var licenseStatus by remember { mutableStateOf<LicenseStatus?>(null) }

                if (showSplash) {
                    SplashScreen(
                        onSplashFinished = {
                            showSplash = false
                            // Verificar licencia después del splash
                            licenseStatus = runBlocking { getLicenseStatusUseCase() }
                        }
                    )
                } else {
                    when (licenseStatus) {
                        is LicenseStatus.Valid -> {
                            OmniServNavGraph()
                        }
                        is LicenseStatus.GracePeriod -> {
                            // En período de gracia, permitir acceso
                            OmniServNavGraph()
                        }
                        is LicenseStatus.TrialPeriod -> {
                            // Trial activo: mostrar ActivationScreen para que el
                            // usuario vea el estado del trial y pueda registrarse/pagar.
                            ActivationScreen(
                                onActivationSuccess = {
                                    licenseStatus = LicenseStatus.Valid
                                }
                            )
                        }
                        else -> {
                            // Sin licencia o expirada, mostrar pantalla de activación
                            ActivationScreen(
                                onActivationSuccess = {
                                    licenseStatus = LicenseStatus.Valid
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
