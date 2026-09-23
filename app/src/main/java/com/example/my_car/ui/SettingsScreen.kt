package com.example.my_car.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Nightlight
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.my_car.receiver.BootReceiver
import com.example.my_car.ui.theme.*

@SuppressLint("UseKtx")
@Composable
fun SettingsScreen(
    hasMediaPermission: Boolean,
    onRequestMediaPermission: () -> Unit,
    isTelemetryVisible: Boolean = true,
    showTelemetryOnAllDevices: Boolean = false,
    isHudEnabled: Boolean = true,
    hudTimeoutSec: Int = 10,
    onToggleTelemetryVisibility: (Boolean) -> Unit = {},
    onToggleShowTelemetryOnAllDevices: (Boolean) -> Unit = {},
    onToggleHudEnabled: (Boolean) -> Unit = {},
    onChangeHudTimeoutSec: (Int) -> Unit = {},
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val isDark = isSystemInDarkTheme()

    // 🚀 SINGLE SUB-SCREEN STATE: Default = false (Displays Main Settings Options)
    var isPermissionsViewOpen by remember { mutableStateOf(false) }

    // Dynamic Live Permission Status States
    var isAudioGranted by remember { mutableStateOf(checkAudioPermission(context)) }
    var isVideoGranted by remember { mutableStateOf(checkVideoPermission(context)) }
    var isPhotoGranted by remember { mutableStateOf(checkPhotoPermission(context)) }
    var isNotificationGranted by remember { mutableStateOf(checkNotificationPermission(context)) }
    var isOverlayGranted by remember { mutableStateOf(checkOverlayPermission(context)) }
    var isAutoStartGranted by remember { mutableStateOf(checkAutoStartPermission(context)) }
    var isWriteSettingsGranted by remember { mutableStateOf(Settings.System.canWrite(context)) }

    // Refresh states automatically when resuming screen
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                isAudioGranted = checkAudioPermission(context)
                isVideoGranted = checkVideoPermission(context)
                isPhotoGranted = checkPhotoPermission(context)
                isNotificationGranted = checkNotificationPermission(context)
                isOverlayGranted = checkOverlayPermission(context)
                isAutoStartGranted = checkAutoStartPermission(context)
                isWriteSettingsGranted = Settings.System.canWrite(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Single Permission Launchers
    val audioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isAudioGranted = granted || checkAudioPermission(context)
        if (!granted) openAppSettings(context)
    }

    val videoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isVideoGranted = granted || checkVideoPermission(context)
        if (!granted) openAppSettings(context)
    }

    val photoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isPhotoGranted = granted || checkPhotoPermission(context)
        if (!granted) openAppSettings(context)
    }

    val notificationLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        isNotificationGranted = granted || checkNotificationPermission(context)
    }

    val bgGradient = remember(isDark) {
        if (isDark) {
            Brush.verticalGradient(colors = listOf(Color(0xFF0B0F19), Color(0xFF151D2A)))
        } else {
            Brush.verticalGradient(colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
        }
    }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(bgGradient)
                .padding(12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalIconButton(
                        onClick = {
                            if (isPermissionsViewOpen) {
                                isPermissionsViewOpen = false
                            } else {
                                onBack()
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(38.dp),
                        colors = IconButtonDefaults.filledTonalIconButtonColors(
                            containerColor = if (isDark) Color(0xFF263345) else Color(0xFFCBD5E1),
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "الرجوع",
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Text(
                        text = if (isPermissionsViewOpen) "جدول إدارة الصلاحيات 🔐" else "إعدادات التطبيق ⚙️",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 18.sp),
                        color = MaterialTheme.colorScheme.onBackground
                    )
                }

                if (!isPermissionsViewOpen) {
                    // 🌟 MAIN SETTINGS VIEW: COMPACT ELEGANT CARDS

                    // Card 1: 🌙 Ambient HUD ScreenSaver Setting
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = AutomotiveCyanAccent.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = AutomotiveCyanAccent.copy(alpha = 0.18f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Nightlight,
                                                contentDescription = null,
                                                tint = AutomotiveCyanAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = "شاشة التوقف القيادية (HUD) 🌙",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "تحويل الشاشة لساعة رقمية سوداء وعداد سرعة مباشر عند عدم اللمس",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = isHudEnabled,
                                    onCheckedChange = { onToggleHudEnabled(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF0F172A),
                                        checkedTrackColor = AutomotiveCyanAccent
                                    )
                                )
                            }

                            if (isHudEnabled) {
                                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                                Column(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = "مهلة عدم اللمس لتفعيل شاشة التوقف: ($hudTimeoutSec ثانية)",
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )

                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        listOf(5, 10, 15, 30, 60).forEach { timeout ->
                                            FilterChip(
                                                selected = hudTimeoutSec == timeout,
                                                onClick = { onChangeHudTimeoutSec(timeout) },
                                                label = { Text("$timeout ثانية", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                                                shape = RoundedCornerShape(10.dp),
                                                colors = FilterChipDefaults.filterChipColors(
                                                    selectedContainerColor = AutomotiveCyanAccent,
                                                    selectedLabelColor = Color(0xFF0F172A)
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Card 2: Telemetry Gauge Visibility Switch
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .border(
                                width = 1.dp,
                                color = AutomotiveCyanAccent.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Surface(
                                        shape = RoundedCornerShape(10.dp),
                                        color = AutomotiveCyanAccent.copy(alpha = 0.18f),
                                        modifier = Modifier.size(38.dp)
                                    ) {
                                        Box(contentAlignment = Alignment.Center) {
                                            Icon(
                                                imageVector = Icons.Default.Speed,
                                                contentDescription = null,
                                                tint = AutomotiveCyanAccent,
                                                modifier = Modifier.size(20.dp)
                                            )
                                        }
                                    }

                                    Column {
                                        Text(
                                            text = "إظهار عدادات وقياسات السيارة 📊",
                                            style = MaterialTheme.typography.titleMedium.copy(
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 15.sp
                                            ),
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = "عرض لوحة السرعة، الحرارة، جهة البطارية والـ RPM بالواجهة الرئيسية",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }

                                Switch(
                                    checked = isTelemetryVisible,
                                    onCheckedChange = { onToggleTelemetryVisibility(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF0F172A),
                                        checkedTrackColor = AutomotiveCyanAccent
                                    )
                                )
                            }

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "عرض العدادات على كافة الهواتف أيضاً 📱",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "افتراضياً تظهر العدادات تلقائياً على شاشات السيارات فقط",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }

                                Switch(
                                    checked = showTelemetryOnAllDevices,
                                    onCheckedChange = { onToggleShowTelemetryOnAllDevices(it) },
                                    colors = SwitchDefaults.colors(
                                        checkedThumbColor = Color(0xFF0F172A),
                                        checkedTrackColor = AutomotiveCyanAccent
                                    )
                                )
                            }
                        }
                    }

                    // Card 3: Permissions Manager Entry Option (Compact 80.dp Height)
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { isPermissionsViewOpen = true }
                            .border(
                                width = 1.dp,
                                color = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                shape = RoundedCornerShape(16.dp)
                            ),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    shape = RoundedCornerShape(10.dp),
                                    color = if (isDark) AutomotiveCyanAccent.copy(alpha = 0.2f) else AutomotiveBluePrimaryLight.copy(alpha = 0.15f),
                                    modifier = Modifier.size(42.dp)
                                ) {
                                    Box(contentAlignment = Alignment.Center) {
                                        Icon(
                                            imageVector = Icons.Default.Security,
                                            contentDescription = "إدارة الصلاحيات",
                                            tint = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Column {
                                    Text(
                                        text = "إدارة الصلاحيات 🔐",
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 16.sp
                                        ),
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = "اضغط هنا لعرض وتفعيل كافة صلاحيات التطبيق والتشغيل التلقائي",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "فتح",
                                tint = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                                modifier = Modifier
                                    .size(20.dp)
                                    .graphicsLayer(rotationZ = 180f)
                            )
                        }
                    }
                } else {
                    // 🌟 FULL PERMISSIONS VIEW: Compact Permission Items List
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = BorderStroke(1.dp, AutomotiveCyanAccent.copy(alpha = 0.5f)),
                        shadowElevation = 3.dp
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "قائمة كافة الصلاحيات المطلوبة لمشغل السيارة:",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold, fontSize = 15.sp),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "تتيح لك هذه الواجهة التحكم الشامل بكافة الصلاحيات مع تحديث حالتها مباشرة.",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

                            // Item 1: Audio Storage Permission
                            PermissionStatusRowItem(
                                title = "1. وصول ملفات الموسيقى والصوتيات",
                                subtitle = "إكتشاف وعرض مسارات الصوت والأغاني من ذاكرة الجهاز",
                                icon = Icons.Default.MusicNote,
                                isGranted = isAudioGranted,
                                isDark = isDark,
                                onGrantClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        audioLauncher.launch(Manifest.permission.READ_MEDIA_AUDIO)
                                    } else {
                                        audioLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    }
                                }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // Item 2: Video Storage Permission
                            PermissionStatusRowItem(
                                title = "2. وصول مقاطع الفيديو والمرئيات",
                                subtitle = "عرض تشغيل مقاطع الفيديو المحلية في الذاكرة",
                                icon = Icons.Default.Movie,
                                isGranted = isVideoGranted,
                                isDark = isDark,
                                onGrantClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        videoLauncher.launch(Manifest.permission.READ_MEDIA_VIDEO)
                                    } else {
                                        videoLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    }
                                }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // Item 3: Photo Storage Permission
                            PermissionStatusRowItem(
                                title = "3. وصول الصور والاستوديو",
                                subtitle = "عرض الصور ومعرض اللقطات في مشغل السيارة",
                                icon = Icons.Default.Image,
                                isGranted = isPhotoGranted,
                                isDark = isDark,
                                onGrantClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        photoLauncher.launch(Manifest.permission.READ_MEDIA_IMAGES)
                                    } else {
                                        photoLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    }
                                }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // Item 4: Notification & Background Service Permission
                            PermissionStatusRowItem(
                                title = "4. إشعارات ومشغل الخلفية",
                                subtitle = "عرض شريط التحكم الثابت بمركز الإشعارات وشاشة القفل",
                                icon = Icons.Default.Notifications,
                                isGranted = isNotificationGranted,
                                isDark = isDark,
                                onGrantClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                                    } else {
                                        openAppSettings(context)
                                    }
                                }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // Item 5: System Overlay Permission (الظهور فوق التطبيقات)
                            PermissionStatusRowItem(
                                title = "5. الظهور فوق التطبيقات (Overlay)",
                                subtitle = "فتح شاشة المشغل مباشرة فوق التطبيقات فور تشغيل الشاشة",
                                icon = Icons.Default.PowerSettingsNew,
                                isGranted = isOverlayGranted,
                                isDark = isDark,
                                onGrantClick = {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        try {
                                            val intent = Intent(
                                                Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                                "package:${context.packageName}".toUri()
                                            )
                                            context.startActivity(intent)
                                        } catch (e: Exception) {
                                            openAppSettings(context)
                                        }
                                    }
                                }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // Item 6: Auto Start System Manager
                            PermissionStatusRowItem(
                                title = "6. التشغيل التلقائي للنظام (Auto-Start)",
                                subtitle = "إعطاء الأولوية للنظام للبدء التلقائي وإلغاء قيود البطارية",
                                icon = Icons.Default.DirectionsCar,
                                isGranted = isAutoStartGranted,
                                isDark = isDark,
                                overrideButtonText = "تفعيل التشغيل ⚡",
                                onGrantClick = { openAutoStartSettings(context) }
                            )

                            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                            // Item 7: Screen Brightness System Settings Permission (WRITE_SETTINGS)
                            PermissionStatusRowItem(
                                title = "7. التحكم بإضاءة النظام وشاشة السيارة",
                                subtitle = "تعديل إشراق وسطوع شاشة السيارة من داخل التطبيق",
                                icon = Icons.Default.WbSunny,
                                isGranted = isWriteSettingsGranted,
                                isDark = isDark,
                                overrideButtonText = "تفعيل الإضاءة ☀️",
                                onGrantClick = { openWriteSettings(context) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PermissionStatusRowItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isGranted: Boolean,
    isDark: Boolean,
    overrideButtonText: String? = null,
    onGrantClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isGranted) Color(0xFF10B981).copy(alpha = 0.18f) else Color(0xFFFFB300).copy(alpha = 0.18f),
                    modifier = Modifier.size(34.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) Color(0xFF10B981) else Color(0xFFFFB300),
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (isGranted) {
                // Status Badge: Granted (مفعلة ✅)
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF10B981).copy(alpha = 0.18f),
                    border = BorderStroke(1.dp, Color(0xFF10B981))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = Color(0xFF10B981),
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "مفعلة ✅",
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 10.sp),
                            color = Color(0xFF10B981)
                        )
                    }
                }
            } else {
                // Action Button: Grant Permission (منح الصلاحية 🔓)
                Button(
                    onClick = onGrantClick,
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                        contentColor = if (isDark) Color(0xFF0F172A) else Color.White
                    )
                ) {
                    Text(
                        text = overrideButtonText ?: "منح الصلاحية 🔓",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold, fontSize = 11.sp)
                    )
                }
            }
        }
    }
}

fun checkAudioPermission(context: Context): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_AUDIO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun checkVideoPermission(context: Context): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_VIDEO
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun checkPhotoPermission(context: Context): Boolean {
    val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.READ_MEDIA_IMAGES
    } else {
        Manifest.permission.READ_EXTERNAL_STORAGE
    }
    return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED
}

fun checkNotificationPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
    } else {
        true
    }
}

fun checkOverlayPermission(context: Context): Boolean {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else true
}

fun checkAutoStartPermission(context: Context): Boolean {
    val isBootEnabled = try {
        val componentName = ComponentName(context, BootReceiver::class.java)
        val setting = context.packageManager.getComponentEnabledSetting(componentName)
        setting == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                setting == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT
    } catch (e: Exception) {
        true
    }

    val isIgnoringBattery = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        powerManager?.isIgnoringBatteryOptimizations(context.packageName) ?: false
    } else {
        true
    }

    val canOverlay = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        Settings.canDrawOverlays(context)
    } else {
        true
    }

    return isBootEnabled && (isIgnoringBattery || canOverlay)
}

fun openWriteSettings(context: Context) {
    try {
        val intent = Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        Toast.makeText(context, "يرجى التفعيل للسماح للتطبيق بتعديل إضاءة الشاشة تلقائياً", Toast.LENGTH_LONG).show()
    } catch (e: Exception) {
        openAppSettings(context)
    }
}

fun openAppSettings(context: Context) {
    try {
        Toast.makeText(context, "تم توجيهك لإعدادات التطبيق للتحكم بالصلاحيات", Toast.LENGTH_SHORT).show()
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:${context.packageName}".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun openAutoStartSettings(context: Context) {
    // 1. Try requesting Battery Optimization Exclusion directly
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val powerManager = context.getSystemService(Context.POWER_SERVICE) as? PowerManager
        if (powerManager != null && !powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            try {
                val batteryIntent = Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                    data = "package:${context.packageName}".toUri()
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(batteryIntent)
                Toast.makeText(context, "يرجى اختيار 'سماح' لإلغاء قيود البطارية وضمان التشغيل التلقائي", Toast.LENGTH_LONG).show()
                return
            } catch (e: Exception) {
                // Fallback to vendor intents
            }
        }
    }

    // 2. Vendor AutoStart Intents (Xiaomi, Huawei, Oppo, Vivo, Samsung, etc.)
    val vendorIntents = listOf(
        Intent().setComponent(ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
        Intent().setComponent(ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
        Intent().setComponent(ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
        Intent().setComponent(ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity")),
        Intent().setComponent(ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.appcontrol.activity.StartupAppControlActivity")),
        Intent().setComponent(ComponentName("com.samsung.android.looper", "com.samsung.android.sm.ui.battery.BatteryActivity"))
    )

    for (intent in vendorIntents) {
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            Toast.makeText(context, "تم فتح صفحة التشغيل التلقائي للنظام", Toast.LENGTH_SHORT).show()
            return
        } catch (e: Exception) {
            // Try next
        }
    }

    openAppSettings(context)
}
