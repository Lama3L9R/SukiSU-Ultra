package zako.zako.zako.ui.viewmodel

import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.os.Parcelable
import android.os.SystemClock
import android.util.Log
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.parcelize.Parcelize
import zako.zako.zako.Natives
import zako.zako.zako.ksuApp
import zako.zako.zako.ui.util.HanziToPinyin
import java.text.Collator
import java.util.*
import com.dergoogler.mmrl.platform.Platform
import com.dergoogler.mmrl.platform.TIMEOUT_MILLIS
import zako.zako.zako.ui.webui.packageManager
import zako.zako.zako.ui.webui.userManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeoutOrNull

class SuperUserViewModel : ViewModel() {
    val isPlatformAlive get() = Platform.isAlive
    companion object {
        private const val TAG = "SuperUserViewModel"
        private var apps by mutableStateOf<List<AppInfo>>(emptyList())
    }

    @Parcelize
    data class AppInfo(
        val label: String,
        val packageInfo: PackageInfo,
        val profile: Natives.Profile?,
    ) : Parcelable {
        val packageName: String
            get() = packageInfo.packageName
        val uid: Int
            get() = packageInfo.applicationInfo!!.uid

        val allowSu: Boolean
            get() = profile != null && profile.allowSu
        val hasCustomProfile: Boolean
            get() {
                if (profile == null) {
                    return false
                }
                return if (profile.allowSu) {
                    !profile.rootUseDefault
                } else {
                    !profile.nonRootUseDefault
                }
            }
    }

    var search by mutableStateOf("")
    var showSystemApps by mutableStateOf(false)
    var isRefreshing by mutableStateOf(false)
        private set

    // 批量操作相关状态
    var showBatchActions by mutableStateOf(false)
        internal set
    var selectedApps by mutableStateOf<Set<String>>(emptySet())
        internal set

    private val sortedList by derivedStateOf {
        val comparator = compareBy<AppInfo> {
            when {
                it.allowSu -> 0
                it.hasCustomProfile -> 1
                else -> 2
            }
        }.then(compareBy(Collator.getInstance(Locale.getDefault()), AppInfo::label))
        apps.sortedWith(comparator).also {
            isRefreshing = false
        }
    }

    val appList by derivedStateOf {
        sortedList.filter {
            it.label.contains(search, true) || it.packageName.contains(
                search,
                true
            ) || HanziToPinyin.getInstance()
                .toPinyinString(it.label).contains(search, true)
        }.filter {
            it.uid == 2000 || showSystemApps || it.packageInfo.applicationInfo!!.flags.and(ApplicationInfo.FLAG_SYSTEM) == 0
        }
    }

    // 切换批量操作模式
    fun toggleBatchMode() {
        showBatchActions = !showBatchActions
        if (!showBatchActions) {
            clearSelection()
        }
    }

    // 切换应用选择状态
    fun toggleAppSelection(packageName: String) {
        selectedApps = if (selectedApps.contains(packageName)) {
            selectedApps - packageName
        } else {
            selectedApps + packageName
        }
    }

    // 清除所有选择
    fun clearSelection() {
        selectedApps = emptySet()
    }

    // 批量更新权限
    suspend fun updateBatchPermissions(allowSu: Boolean) {
        selectedApps.forEach { packageName ->
            val app = apps.find { it.packageName == packageName }
            app?.let {
                val profile = Natives.getAppProfile(packageName, it.uid)
                val updatedProfile = profile.copy(allowSu = allowSu)
                if (Natives.setAppProfile(updatedProfile)) {
                    apps = apps.map { app ->
                        if (app.packageName == packageName) {
                            app.copy(profile = updatedProfile)
                        } else {
                            app
                        }
                    }
                }
            }
        }
        clearSelection()
        showBatchActions = false // 批量操作完成后退出批量模式
        fetchAppList() // 刷新列表以显示最新状态
    }


    suspend fun fetchAppList() {
        isRefreshing = true

        withContext(Dispatchers.IO) {
            withTimeoutOrNull(TIMEOUT_MILLIS) {
                while (!isPlatformAlive) {
                    delay(500)
                }
            } ?: return@withContext // Exit early if timeout
            val pm = ksuApp.packageManager
            val start = SystemClock.elapsedRealtime()

            val userInfos = Platform.userManager.getUsers()
            val packages = mutableListOf<PackageInfo>()
            val packageManager = Platform.packageManager

            for (userInfo in userInfos) {
                Log.i(TAG, "fetchAppList: ${userInfo.id}")
                packages.addAll(packageManager.getInstalledPackages(0, userInfo.id))
            }

            apps = packages.map {
                val appInfo = it.applicationInfo
                val uid = appInfo!!.uid
                val profile = Natives.getAppProfile(it.packageName, uid)
                AppInfo(
                    label = appInfo.loadLabel(pm).toString(),
                    packageInfo = it,
                    profile = profile,
                )
            }.filter { it.packageName != ksuApp.packageName }
            Log.i(TAG, "load cost: ${SystemClock.elapsedRealtime() - start}")
        }
    }
}