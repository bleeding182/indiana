package com.davidmedenjak.indiana.screens.tracking

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.davidmedenjak.indiana.R
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.LargeFlexible
import com.davidmedenjak.indiana.theme.ui.atoms.Scaffold
import com.davidmedenjak.indiana.theme.ui.atoms.Surface
import com.davidmedenjak.indiana.theme.ui.atoms.Switch
import com.davidmedenjak.indiana.theme.ui.atoms.Text
import com.davidmedenjak.indiana.theme.ui.preview.PreviewSurface

@Composable
fun TrackingSettingsScreen(
    onNavigateUp: () -> Unit,
    viewModel: TrackingSettingsViewModel = hiltViewModel(),
) {
    Scaffold(
        topBar = {
            LargeFlexible(
                title = { Text(stringResource(R.string.tracking_settings_title)) },
                actions = {},
                navigationIcon = { Up(onNavigateUp) }
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = stringResource(R.string.tracking_settings_description),
                modifier = Modifier.padding(bottom = 24.dp).padding(horizontal = 16.dp)
            )

            TrackingSettingItem(
                title = stringResource(R.string.tracking_analytics_title),
                description = stringResource(R.string.tracking_analytics_description),
                enabled = viewModel.analytics,
                onToggle = { viewModel.setAnalyticsEnabled(it) }
            )

            TrackingSettingItem(
                title = stringResource(R.string.tracking_crashlytics_title),
                description = stringResource(R.string.tracking_crashlytics_description),
                enabled = viewModel.crashlytics,
                onToggle = { viewModel.setCrashlyticsEnabled(it) }
            )

            TrackingSettingItem(
                title = stringResource(R.string.tracking_performance_title),
                description = stringResource(R.string.tracking_performance_description),
                enabled = viewModel.performance,
                onToggle = { viewModel.setPerformanceEnabled(it) }
            )
        }
    }
}

@Composable
private fun TrackingSettingItem(
    title: String,
    description: String,
    enabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val interactionSource = remember { MutableInteractionSource() }
    
    Surface(
        modifier = modifier.padding(vertical = 8.dp)
    ) {
        Column(
            modifier = Modifier
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Switch
                ) { onToggle(!enabled) }
                .padding(16.dp)
        ) {
            androidx.compose.foundation.layout.Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.SpaceBetween,
                verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = IndianaTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = IndianaTheme.typography.bodyMedium,
                        color = IndianaTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
                Switch(
                    checked = enabled,
                    onCheckedChange = null,
                    interactionSource = interactionSource,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

@PreviewLightDark
@Composable
private fun Preview() {
    PreviewSurface {
        TrackingSettingsScreenPreview()
    }
}

@Composable
private fun TrackingSettingsScreenPreview() {
    var analyticsEnabled by remember { mutableStateOf(true) }
    var crashlyticsEnabled by remember { mutableStateOf(true) }
    var performanceEnabled by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            LargeFlexible(
                title = { Text("Tracking Settings") },
                actions = {},
                navigationIcon = { Up {} }
            )
        }, modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = "We use these services to improve our app. You can disable them at any time.",
                modifier = Modifier.padding(bottom = 24.dp)
            )

            TrackingSettingItem(
                title = "Analytics",
                description = "Help us understand how you use the app to improve your experience",
                enabled = analyticsEnabled,
                onToggle = { analyticsEnabled = it }
            )

            TrackingSettingItem(
                title = "Crash Reporting",
                description = "Automatically report crashes to help us fix bugs faster",
                enabled = crashlyticsEnabled,
                onToggle = { crashlyticsEnabled = it }
            )

            TrackingSettingItem(
                title = "Performance Monitoring",
                description = "Monitor app performance to identify and fix slowdowns",
                enabled = performanceEnabled,
                onToggle = { performanceEnabled = it }
            )
        }
    }
}