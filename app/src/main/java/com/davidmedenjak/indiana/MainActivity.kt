package com.davidmedenjak.indiana

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.entry
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSavedStateNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import androidx.navigation3.ui.rememberSceneSetupNavEntryDecorator
import com.davidmedenjak.indiana.screens.about.AboutGraph
import com.davidmedenjak.indiana.screens.about.AboutRoute
import com.davidmedenjak.indiana.screens.auth.AuthGraph
import com.davidmedenjak.indiana.screens.auth.AuthRoute
import com.davidmedenjak.indiana.screens.build.BuildDetailGraph
import com.davidmedenjak.indiana.screens.build.BuildDetailRoute
import com.davidmedenjak.indiana.screens.privacy.PrivacyGraph
import com.davidmedenjak.indiana.screens.privacy.PrivacyRoute
import com.davidmedenjak.indiana.screens.project.ProjectDetailGraph
import com.davidmedenjak.indiana.screens.project.ProjectDetailRoute
import com.davidmedenjak.indiana.screens.projects.ProjectsGraph
import com.davidmedenjak.indiana.screens.projects.ProjectsRoute
import com.davidmedenjak.indiana.screens.settings.DownloadCleanupGraph
import com.davidmedenjak.indiana.screens.settings.DownloadCleanupRoute
import com.davidmedenjak.indiana.screens.tracking.TrackingSettingsGraph
import com.davidmedenjak.indiana.screens.tracking.TrackingSettingsRoute
import com.davidmedenjak.indiana.theme.IndianaTheme
import com.davidmedenjak.indiana.theme.ui.atoms.Surface
import dagger.hilt.EntryPoint
import dagger.hilt.EntryPoints
import dagger.hilt.InstallIn
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.components.ActivityComponent

@EntryPoint
@InstallIn(ActivityComponent::class)
interface MainEntryPoint {
    val appBackStack: AppBackStack
}

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val activity = LocalActivity.current!!
            val appBackStack = remember {
                EntryPoints.get(activity, MainEntryPoint::class.java)
                    .appBackStack
            }

            val isInitializing by appBackStack.isInitializing.collectAsStateWithLifecycle()

            splashScreen.setKeepOnScreenCondition {
                isInitializing
            }

            IndianaTheme {
                Surface {
                    Log.d("backstack", appBackStack.backStack.joinToString())

                    NavDisplay(
                        backStack = appBackStack.backStack,
                        onBack = { appBackStack.remove() },
                        entryDecorators = listOf(
                            rememberSceneSetupNavEntryDecorator(),
                            rememberSavedStateNavEntryDecorator(),
                            rememberViewModelStoreNavEntryDecorator()
                        ),
                        entryProvider = entryProvider {
                            entry<AuthGraph> {
                                AuthRoute(
                                    it,
                                    onAboutSelected = { appBackStack.add(AboutGraph) },
                                    onPrivacySelected = { appBackStack.add(PrivacyGraph) },
                                )
                            }
                            entry<ProjectsGraph> {
                                ProjectsRoute(
                                    it,
                                    navigateToProject = {
                                        appBackStack.add(
                                            ProjectDetailGraph(
                                                title = it.name ?: "",
                                                slug = it.id,
                                            )
                                        )
                                    },
                                    onAboutSelected = { appBackStack.add(AboutGraph) },
                                    onPrivacySelected = { appBackStack.add(PrivacyGraph) },
                                    onDownloadCleanupSelected = { appBackStack.add(DownloadCleanupGraph) },
                                    onLogoutSelected = { appBackStack.logout() },
                                )
                            }
                            entry<ProjectDetailGraph> { key ->
                                ProjectDetailRoute(
                                    key,
                                    onNavigateUp = appBackStack::remove,
                                    navigateToBuild = {
                                        appBackStack.add(
                                            BuildDetailGraph(
                                                projectTitle = key.title,
                                                buildTitle = "${it.buildNumber} ${it.tag ?: it.branch}",
                                                appSlug = key.slug,
                                                buildSlug = it.slug!!,
                                            )
                                        )
                                    },
                                )
                            }
                            entry<BuildDetailGraph> {
                                BuildDetailRoute(
                                    it,
                                    onNavigateUp = appBackStack::remove,
                                    onNavigateToBuild = { appBackStack.add(it) },
                                )
                            }
                            entry<AboutGraph> {
                                AboutRoute(
                                    it,
                                    onNavigateUp = appBackStack::remove,
                                )
                            }
                            entry<PrivacyGraph> {
                                PrivacyRoute(
                                    it,
                                    onNavigateUp = appBackStack::remove,
                                    onNavigateToTrackingSettings = {
                                        appBackStack.add(
                                            TrackingSettingsGraph
                                        )
                                    },
                                )
                            }
                            entry<TrackingSettingsGraph> {
                                TrackingSettingsRoute(
                                    it,
                                    onNavigateUp = appBackStack::remove,
                                )
                            }
                            entry<DownloadCleanupGraph> {
                                DownloadCleanupRoute(
                                    onBack = appBackStack::remove,
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
