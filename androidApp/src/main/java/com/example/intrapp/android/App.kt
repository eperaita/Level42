package com.example.intrapp.android

import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext

import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.intrapp.Api42
import com.example.intrapp.SessionManager
import com.example.shared.VideoPlayer

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextAlign

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.intrapp.Project
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.style.TextOverflow
import androidx.lifecycle.viewModelScope
import com.example.intrapp.SelectedUserProfile
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

//-------------------------//APP NAVEGADOR//---------------------------//

@Composable
fun App(viewModel: ProfileViewModel) {
    // 1. Crear el NavController
    val navController = rememberNavController()

    // 2. Obtener el ViewModel
    val viewModel: ProfileViewModel = viewModel()

    // 3. Observar el estado de autenticación
    val authState by viewModel.authState.collectAsState()
    val searchState by viewModel.searchState.collectAsState()

    // 4. Navegar entre pantallas : autentificado ->profile, default ->login
    LaunchedEffect(authState) {
        when (authState) {
            is ProfileViewModel.AuthState.Success -> {
                navController.navigate("welcome") {
                    popUpTo("login") { inclusive = true }
                }
            }

            is ProfileViewModel.AuthState.Error -> {
                // Podrías mostrar un error o manejar la navegación de error aquí
            }

            else -> {}
        }
    }

    // Navegar cuando se encuentra un usuario en la búsqueda
    LaunchedEffect(searchState) {
        when (searchState) {
            is ProfileViewModel.SearchState.Success -> {
                navController.navigate("profile")
            }
            else -> {}
        }
    }

    // 5. Configurar NavHost
    NavHost(navController, startDestination = "login") {
        composable("loading") {
            LoadingScreen(
                onTimeout = {
                    navController.navigate("login") {
                        popUpTo(0) { inclusive = true }
                    }
                }
            )
        }
        composable("login") {
            LoginScreen(navController, viewModel)
        }
        composable("welcome") {
            WelcomeScreen(navController, viewModel)
        }
        composable("profile") {
            ProfileScreen(navController, viewModel)
        }
        composable("projects") {
            ProjectsScreen(navController, viewModel)
        }
        composable("selected_project") {
            // Recuperamos el ID como un entero
            val projectId = navController.previousBackStackEntry?.savedStateHandle?.get<Int>("projectId") ?: -1
            SelectedProjectScreen(navController, projectId)
        }
        composable("skills") {
            SkillsScreen(navController = navController, viewModel = viewModel<ProfileViewModel>())
        }
    }
}



//-------------------------//SCREENS//---------------------------//

@Composable
fun LoginScreen(navController: NavController, viewModel: ProfileViewModel) {

    val context = LocalContext.current
    var videoFinished by remember { mutableStateOf(false) }

    // Mostrar error si existe
    LaunchedEffect(Unit) {
        SessionManager.lastAuthError?.let { error ->
            Toast.makeText(context, error, Toast.LENGTH_LONG).show()
            SessionManager.lastAuthError = null
        }
    }

    MaterialTheme {

        //VIDEO FONDO

        Box(modifier = Modifier.fillMaxSize().padding(0.dp)) {

            // Usa el @componente VideoPlayer
            VideoPlayer(
                videoFileName = "loginvideo.mp4",
                modifier = Modifier.fillMaxSize(),
                onVideoFinished = { videoFinished = true } // Callback cuando el video termina

            )
            if (videoFinished) {

        //BOTON
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.BottomEnd // Alinear en la esquina inferior derecha
                ) {
                    Button(
                        onClick = {
                            viewModel.clearAuthError()//  Limpia errores previos
                            // Navegar a la pantalla de carga
                            navController.navigate("loading")
                            // Iniciar flujo OAuth
                            val url = Api42().getURI()
                            Log.d("App", "URI for Intent: $url")
                            // Abrir el navegador con la URL de OAuth
                            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                            context.startActivity(browserIntent)
                        },
                        modifier = Modifier
                            .size(100.dp)
                            .align(Alignment.BottomEnd)
                            .offset(x = (-16).dp, y = (-100).dp), // Ajustar la posición
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Yellow
                        )
                    ) {
                        Text(
                            text = "LOG\nIN",
                            color = Color.Black,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            fontFamily = FontFamily.SansSerif,
                            modifier = Modifier
                                .fillMaxSize()
                                .wrapContentSize(Alignment.Center)
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun WelcomeScreen(navController: NavController, viewModel: ProfileViewModel) {
    val userLogin = SessionManager.user_login ?: run {
        LaunchedEffect(Unit) {
            navController.navigate("login") { popUpTo(0) { inclusive = true } }
        }
        return
    }
    val userImageUrl = SessionManager.user_image_url

    var searchQuery by remember { mutableStateOf("") }
    val searchState by viewModel.searchState.collectAsState()

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // Header
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 32.dp)
                ) {
                    Text(
                        text = "Welcome $userLogin!",
                        color = Color.Yellow,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Avatar
                    AsyncImage(
                        model = userImageUrl ?: "https://cdn.intra.42.fr/users/${userLogin}.jpg",
                        contentDescription = "User avatar",
                        modifier = Modifier
                            .size(150.dp)
                            .clip(CircleShape)
                            .border(2.dp, Color.Yellow, CircleShape),
                        contentScale = ContentScale.Crop
                    )
                }

                // Search Section
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    // Search Field Component
                    SearchField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        onSearch = { viewModel.searchForUser(searchQuery) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    // My Profile Button
                    Button(
                        onClick = { viewModel.searchForUser(userLogin) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp, bottom = 6.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        ),
                        border = BorderStroke(2.dp, Color.Yellow),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Text(
                            text = "MY PROFILE",
                            color = Color.Yellow,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                }

                // Logout Button
                Box(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .clickable {
                            viewModel.resetState()
                            SessionManager.clearSession()
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        .size(110.dp)
                        .background(Color.Yellow, CircleShape)
                        .border(2.dp, Color.Black, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "LOG OUT",
                        color = Color.Black,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        textAlign = TextAlign.Center
                    )
                }
            }

            // Handle Search State
            LaunchedEffect(searchState) {
                when (searchState) {
                    is ProfileViewModel.SearchState.Success -> {
                        navController.navigate("profile")
                    }
                    is ProfileViewModel.SearchState.Error -> {
                        // Show error if needed
                    }
                    else -> {}
                }
            }

            // Loading Indicator
            if (searchState is ProfileViewModel.SearchState.Loading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.7f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(48.dp),
                        color = Color.Yellow
                    )
                }
            }
        }
    }
}

@Composable
fun ProfileScreen(navController: NavController, viewModel: ProfileViewModel) {

    //Si no existe usuario , redirigir
    val selectedUser = SessionManager.selectedUserProfile ?: run {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("User not found", color = Color.White)
            ButtonBack(navController = navController, onBackClick = {
                viewModel.clearSearch()
                navController.popBackStack()
            })
        }
        return
    }

    MaterialTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .systemBarsPadding()
        ) {

            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Contenido del perfil
                UserProfileContent(user = selectedUser, navController = navController)

            }
            ButtonBack(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(16.dp),
                onBackClick = {
                    viewModel.clearSearch()
                    navController.navigate("welcome") {
                        popUpTo("welcome") { inclusive = true }
                    }
                }
            )
        }
    }
}

@Composable
private fun UserProfileContent(
    user: SelectedUserProfile,
    navController: NavController,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .padding(bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // AVATAR
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .background(Color.Yellow, CircleShape)
                    .clip(CircleShape)
                    .background(Color.Black)
            ) {
                AsyncImage(
                    model = user.image?.link,
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // PROFILE INFO
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = user.login,
                    color = Color.White,
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "${user.first_name ?: ""} ${user.last_name ?: ""}",
                    style = profileTextStyle
                )
                Text(
                    text = "email: ${user.email}",
                    style = profileTextStyle
                )
                Text(
                    text = "Level: ${user.level ?: "N/A"}",
                    style = profileTextStyle
                )
                Text(
                    text = "Wallet: ${user.wallet}",
                    style = profileTextStyle
                )
            }
        }

        // BOTONES
        Column(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Botón PROJECTS
            Button(
                onClick = {
                    navController.navigate("projects")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                border = BorderStroke(2.dp, Color.Yellow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "PROJECTS",
                    color = Color.Yellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }

            // Botón SKILLS
            Button(
                onClick = {
                    navController.navigate("skills")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, bottom = 6.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Black
                ),
                border = BorderStroke(2.dp, Color.Yellow),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = "SKILLS",
                    color = Color.Yellow,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}

private val profileTextStyle = TextStyle(
    color = Color.White,
    fontSize = 20.sp,
    fontFamily = FontFamily.Default,
    letterSpacing = 0.5.sp,
    lineHeight = 28.sp
)


@Composable
fun LoadingScreen(onTimeout: () -> Unit = {}) {

    // TIMEOUT
    LaunchedEffect(Unit) {
        delay(30000)
        onTimeout()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            color = Color.Black,
            modifier = Modifier.size(100.dp)
        )

    }
}

@Composable
fun ProjectsScreen(navController: NavController, viewModel: ProfileViewModel) {
    var isLoading by remember { mutableStateOf(true) }
    var showError by remember { mutableStateOf(false) }
    var isFullyVisible by remember { mutableStateOf(false) }

    // Obtener los proyectos desde el usuario seleccionado
    val projects = SessionManager.selectedUserProfile?.projects ?: emptyList()


    // Efecto para cargar proyectos si no existen
    LaunchedEffect(Unit) {
        delay(100) // Pequeño delay para animación
        isFullyVisible = true

        if (projects.isEmpty()) {
            try {
                viewModel.loadProjectsForUser(SessionManager.selectedUserProfile?.login ?: "")
                isLoading = false
            } catch (e: Exception) {
                showError = true
                isLoading = false
            }
        } else {
            isLoading = false
        }
    }

    MaterialTheme {
        Box(modifier = Modifier.fillMaxSize().background(Color(0xFFFFFC00))) {
            if (isFullyVisible) {
                when {
                    // Mostrar proyectos si existen
                    projects.isNotEmpty() -> {
                        ScrollableCircularProjectCarousel(
                            projects = projects,
                            navController = navController
                        )
                    }

                    // Mostrar error si ocurrió
                    showError -> {
                        ErrorRetryView(
                            onRetry = {
                                showError = false
                                isLoading = true
                                viewModel.viewModelScope.launch {
                                    try {
                                        viewModel.loadProjectsForUser(
                                            SessionManager.selectedUserProfile?.login ?: ""
                                        )
                                        showError = false
                                    } catch (e: Exception) {
                                        showError = true
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        )
                    }

                    // Mostrar loading mientras carga
                    isLoading -> {
                        LoadingScreen()
                    }
                }
            }

            // Botón Atrás
            ButtonBack(
                navController = navController,
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .offset(16.dp, 50.dp)
            )
        }
    }
}

@Composable
private fun ErrorRetryView(onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Error cargando proyectos",
            color = Color.Red,
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRetry,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            )
        ) {
            Text(
                text = "Reintentar",
                color = Color.Yellow
            )
        }
    }
}

@Composable
fun SelectedProjectScreen(
    navController: NavController,
    projectId: Int // ID del proyecto seleccionado
) {
    val project = remember(projectId) {
        SessionManager.selectedUserProfile?.projects?.find { it.project.id == projectId }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
    ) {
        if (project != null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // PROJECT TITLE
                Box(
                    modifier = Modifier
                        .size(250.dp)
                        .background(Color.Black, CircleShape)
                        .padding(bottom = 24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = project.project.name,
                        color = Color.White,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // INFO
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(24.dp)
                ) {
                    Text(
                        text = "Final Mark: ${project.finalMark ?: "No available"}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Status: ${project.status}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Text(
                        text = "Updated At:\n${project.updatedAt}",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Proyecto no encontrado",
                    color = Color.Red,
                    fontSize = 20.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { navController.popBackStack() },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.Black
                    )
                ) {
                    Text("Volver", color = Color.Yellow)
                }
            }
        }

        // BOTON ATRAS
        ButtonBack(
            navController = navController,
            modifier = Modifier
                .align(Alignment.TopStart)
                .offset(16.dp, 50.dp)
        )
    }
}

@Composable
fun ScrollableCircularProjectCarousel(projects: List<Project>, navController: NavController) {

    val listState = rememberLazyListState()    // Mantener el estado de la lista
    val screenHeight = LocalConfiguration.current.screenHeightDp.dp
    val itemHeight = 100.dp
    val circleHeight = 80.dp
    val padding = 12.dp

    //STYLES
    val spacerHeight = (screenHeight - itemHeight) / 2

    // Calcular el seleccionado
    val centerIndex by remember {
        derivedStateOf {
            val layoutInfo = listState.layoutInfo
            val visibleItemsInfo = layoutInfo.visibleItemsInfo
            if (visibleItemsInfo.isEmpty()) return@derivedStateOf 0

            val center = layoutInfo.viewportStartOffset + (layoutInfo.viewportEndOffset - layoutInfo.viewportStartOffset) / 2

            var closestIndex = 0
            var minDistance = Int.MAX_VALUE

            for (itemInfo in visibleItemsInfo) {
                val itemCenter = itemInfo.offset + itemInfo.size / 2
                val distance = kotlin.math.abs(itemCenter - center)
                if (distance < minDistance) {
                    minDistance = distance
                    closestIndex = itemInfo.index
                }
            }

            closestIndex
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
    ) {

    // CARRUSEL
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            contentPadding = PaddingValues(
                top = spacerHeight,
                bottom = spacerHeight
            )
        ) {
            itemsIndexed(projects) { index, project ->
                val isSelected = index == centerIndex

                if (isSelected) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = padding)
                            .height(itemHeight)
                            .background(Color.Black)
                            .clickable {
                                // Pasar el ID del proyecto como argumento de navegación
                                navController.navigate("selected_project") {
                                    launchSingleTop = true
                                    // Pasamos el ID como un entero
                                    navController.currentBackStackEntry?.savedStateHandle?.set("projectId", project.project.id)
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = project.project.name,
                            color = Color.Yellow,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .padding(vertical = 6.dp)
                            .size(circleHeight)
                            .background(Color.Black, shape = CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = project.project.name,
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(projects) {
        if (projects.isNotEmpty()) {
            // Empezar con el primer proyecto seleccionado (índice 0)
            listState.scrollToItem(0)
        }
    }
}

@Composable
fun SkillsScreen(
    navController: NavController,
    viewModel: ProfileViewModel
) {
    val selectedUser = SessionManager.selectedUserProfile
    val skills = selectedUser?.cursus_users
        ?.firstOrNull { it.cursus.id == 21 }
        ?.skills?.sortedByDescending { it.level } ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Yellow)
            .systemBarsPadding()
            .padding(16.dp)
    ) {
        if (selectedUser == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = "Perfil no disponible",
                        color = Color.Black,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Button(
                        onClick = { navController.popBackStack() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color.Black
                        )
                    ) {
                        Text("Volver", color = Color.Yellow)
                    }
                }
            }
        } else {
            Text(
                text = "SKILLS",
                color = Color.Black,
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(vertical = 24.dp)
            )

            if (skills.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No skills found",
                        color = Color.Black,
                        fontSize = 18.sp
                    )
                }
            } else {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    LazyRow(
                        modifier = Modifier.fillMaxSize(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp),
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        verticalAlignment = Alignment.Bottom
                    ) {
                        items(skills) { skill ->
                            val percentage = ((skill.level / 10f) * 100).toInt().coerceIn(0, 100)
                            VerticalSkillItem(name = skill.name, percentage = percentage)
                        }
                    }
                }
            }
        }

        Button(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Black
            ),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(
                text = "BACK",
                color = Color.Yellow,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                modifier = Modifier.padding(8.dp)
            )
        }
    }
}

@Composable
fun VerticalSkillItem(name: String, percentage: Int) {
    var animationPlayed by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        animationPlayed = true
    }

    val animatedProgress by animateFloatAsState(
        targetValue = if (animationPlayed) percentage / 100f else 0f,
        animationSpec = tween(
            durationMillis = 1500,
            easing = FastOutSlowInEasing
        )
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(100.dp) // Barras más anchas (para que entren ~3 por pantalla)
    ) {
        Box(
            modifier = Modifier
                .width(70.dp) // Barra más gruesa
                .height(300.dp) // Barra más alta
                .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                .background(Color.DarkGray)
        ) {
            // ANIMACION
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .fillMaxHeight(animatedProgress)
                    .clip(RoundedCornerShape(topStart = 8.dp, topEnd = 8.dp))
                    .background(Color.Black)
            ) {
                // PORCENTAJE
                if (percentage > 10) {
                    Text(
                        text = "$percentage%",
                        color = Color.White,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 8.dp)
                    )
                }
            }
        }

        // SKILL TITLE
        Text(
            text = name,
            color = Color.Black,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier
                .width(100.dp)
                .padding(top = 12.dp)
        )
    }
}

//-------------------------//REUTILIZABLES//---------------------------//

@Composable
fun ButtonBack(
    navController: NavController,
    modifier: Modifier = Modifier,
    onBackClick: (() -> Unit)? = null
) {
    Box(modifier = modifier) {
        IconButton(
            onClick = {
                onBackClick?.invoke() ?: navController.navigateUp()
            },
            modifier = Modifier
                .size(60.dp)
                .background(
                    color = Color.Black,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector = Icons.Default.KeyboardArrowLeft,
                contentDescription = "Back",
                tint = Color(0xFFFFFC00),
                modifier = Modifier.size(56.dp)
            )
        }
    }
}

@Composable
fun LogoutButton(onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        modifier = modifier
            .size(100.dp)
            .background(Color.Yellow, CircleShape),
        colors = ButtonDefaults.buttonColors(containerColor = Color.Yellow)
    ) {
        Text(
            text = "LOG OUT",
            color = Color.Black,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
fun SearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .background(Color.Yellow.copy(alpha = 0.2f), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                cursorColor = Color.Yellow,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            placeholder = { Text("Search user...", color = Color.Gray) },
            singleLine = true

        )

        IconButton(
            onClick = onSearch,
            enabled = value.isNotEmpty()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = "Search",
                tint = if (value.isNotEmpty()) Color.Yellow else Color.Gray
            )
        }
    }
}

@Composable
fun ProgressBar(level: Double, maxLevel: Int = 21, modifier: Modifier = Modifier) {
    val progress = level.toFloat() / maxLevel.toFloat()

    //BARRA GRIS
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(20.dp)
            .padding(horizontal = 16.dp)
            .background(Color.DarkGray, RoundedCornerShape(10.dp))
    ) {
        //BARRA AMARILLA
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(progress)
                .background(Color.Yellow, RoundedCornerShape(10.dp))
        ) {
            Text(
                text = "$level",
                color = Color.Black,
                style = TextStyle(
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.align(Alignment.Center)
            )
        }
    }
}

