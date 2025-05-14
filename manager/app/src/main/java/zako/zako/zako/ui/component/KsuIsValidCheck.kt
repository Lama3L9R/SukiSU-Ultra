package zako.zako.zako.ui.component

import androidx.compose.runtime.Composable
import zako.zako.zako.Natives
import zako.zako.zako.ksuApp

@Composable
fun KsuIsValid(
    content: @Composable () -> Unit
) {
    val isManager = Natives.becomeManager(ksuApp.packageName)
    val ksuVersion = if (isManager) Natives.version else null

    if (ksuVersion != null) {
        content()
    }
}