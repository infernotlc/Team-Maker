    package com.yusuf.navigation

    import androidx.compose.foundation.layout.Box
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.LaunchedEffect
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.hilt.navigation.compose.hiltViewModel
    import androidx.navigation.NavHostController
    import androidx.navigation.compose.NavHost
    import androidx.navigation.compose.composable
    import androidx.compose.runtime.key
    import androidx.navigation.NavBackStackEntry
    import androidx.navigation.NavType
    import androidx.navigation.navArgument
    import com.google.gson.Gson
    import com.yusuf.component.LoadingLottie
    import com.yusuf.feature.R
    import com.yusuf.feature.player_list.PlayerListScreen
    import com.yusuf.feature.auth.forgot_password.ForgotPasswordScreen
    import com.yusuf.feature.create_match.CreateMatchScreen
    import com.yusuf.feature.home.ChooseSportScreen
    import com.yusuf.feature.auth.login.LoginScreen
    import com.yusuf.feature.auth.register.RegisterScreen
    import com.yusuf.feature.onboarding.OnBoardingScreen
    import com.yusuf.feature.options.OptionsScreen
    import com.yusuf.feature.splash_screen.SplashScreen
    import com.yusuf.feature.match_detail.MatchDetailScreen
    import com.yusuf.navigation.main_viewmodel.MainViewModel
    import com.yusuf.utils.Competition


    @Composable
    fun TeamMakerNavigation(
        navController: NavHostController,
        mainViewModel: MainViewModel = hiltViewModel(),
        onTitleChange: (String) -> Unit,
        key: Int,) {

        key(key) {
            LaunchedEffect(mainViewModel.isLoading) {
                if (!mainViewModel.isLoading) {
                    navController.navigate(mainViewModel.startDestination) {
                        popUpTo(navController.graph.startDestinationId) { inclusive = true }
                    }
                }
            }


        if (mainViewModel.isSplashScreenVisible) {
            SplashScreen()
        } else if (mainViewModel.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                LoadingLottie(resId = R.raw.loading_anim)
            }
        } else {
            NavHost(
                navController = navController,
                startDestination = mainViewModel.startDestination
            ) {
                composable(NavigationGraph.ONBOARDING_SCREEN.route) {
                    OnBoardingScreen(mainViewModel, navController)
                }
                composable(NavigationGraph.LOGIN.route) {
                    LoginScreen(navController)
                }
                composable(NavigationGraph.REGISTER.route) {
                    RegisterScreen(navController)
                }
                composable(NavigationGraph.FORGOT_PASSWORD.route) {
                    ForgotPasswordScreen(navController)
                }
                composable(NavigationGraph.CHOOSE_SPORT.route) {
                    ChooseSportScreen(navController)
                    onTitleChange("Choose Sport")
                }
                composable(
                    route = NavigationGraph.OPTIONS.route,
                    arguments = listOf(navArgument("competitionJson") { type = NavType.StringType })
                ) { backStackEntry ->
                    val gson = Gson()
                    val competitionJson = backStackEntry.arguments?.getString("competitionJson")
                    val competition = gson.fromJson(competitionJson, Competition::class.java)
                    OptionsScreen(
                        navController = navController,
                        competition = competition
                    )
                    onTitleChange("Options")
                }

                composable(NavigationGraph.PLAYER_LIST.route) {
                    PlayerListScreen()
                    onTitleChange("Player List")
                }
                composable(NavigationGraph.CREATE_MATCH.route) {
                    CreateMatchScreen(navController)
                    onTitleChange("Create Match")
                }
                composable(NavigationGraph.MATCH_DETAIL.route) {
                    MatchDetailScreen()
                    onTitleChange("Match Detail")
                }
            }
        }
    }
    }