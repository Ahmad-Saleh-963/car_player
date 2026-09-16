package com.example.my_car.ui

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.my_car.ui.theme.AutomotiveBluePrimaryLight
import com.example.my_car.ui.theme.AutomotiveCyanAccent

@Composable
fun PermissionScreen(
    onRequestPermission: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    var canDrawOverlays by remember {
        mutableStateOf(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                Settings.canDrawOverlays(context)
            } else true
        )
    }

    val bgGradient = if (isDark) {
        Brush.verticalGradient(colors = listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
    } else {
        Brush.verticalGradient(colors = listOf(Color(0xFFF1F5F9), Color(0xFFE2E8F0)))
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(bgGradient)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .wrapContentHeight()
                .border(
                    width = 1.dp,
                    color = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                    shape = RoundedCornerShape(24.dp)
                ),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "🚗 إعدادات تشغيل مشغل السيارة",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "لتوفير تجربة تشغيل تلقائي حقيقية عند تشغيل شاشة السيارة، يتطلب التطبيق صلاحيات الوصول للملفات الصوتية والمرئية بالإضافة لصلاحية الظهور للتشغيل التلقائي عند الإقلاع.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )

                Button(
                    onClick = onRequestPermission,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isDark) AutomotiveCyanAccent else AutomotiveBluePrimaryLight,
                        contentColor = if (isDark) Color(0xFF0F172A) else Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                ) {
                    Text(
                        text = "1. منح صلاحية الوصول للملفات 🔓",
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !canDrawOverlays) {
                    OutlinedButton(
                        onClick = {
                            try {
                                val intent = Intent(
                                    Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                    Uri.parse("package:${context.packageName}")
                                )
                                context.startActivity(intent)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                    ) {
                        Text(
                            text = "2. تفعيل التشغيل التلقائي عند بدء التشغيل ⚡",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}
