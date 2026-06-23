package com.jkn.mobile.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.QuestionAnswer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState

@Composable
fun BottomNavigationBar(navController: NavController) {
    val navBackStackEntry = navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry.value?.destination?.route
    
    val selectedColor = Color(0xFF1976D2)
    val unselectedColor = Color(0xFF9E9E9E)

    Box(modifier = Modifier.fillMaxWidth()) {
        // Shadow/Divider on top
        HorizontalDivider(
            color = Color(0xFFE0E0E0),
            thickness = 1.dp,
            modifier = Modifier.align(Alignment.TopCenter)
        )

        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter),
            containerColor = Color.White,
            tonalElevation = 4.dp
        ) {
            val navigate = { route: String ->
                if (currentRoute != route) {
                    navController.navigate(route) {
                        popUpTo(navController.graph.startDestinationId) { saveState = true }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            }

            NavigationBarItem(
                selected = currentRoute == "home",
                onClick = { navigate("home") },
                icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                label = { Text("Home") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = Color.Transparent
                )
            )

            NavigationBarItem(
                selected = currentRoute == "berita",
                onClick = { navigate("berita") },
                icon = { Icon(Icons.Default.Article, contentDescription = "Berita") },
                label = { Text("Berita") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = Color.Transparent
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            NavigationBarItem(
                selected = currentRoute == "faq",
                onClick = { navigate("faq") },
                icon = { Icon(Icons.Default.QuestionAnswer, contentDescription = "FAQ") },
                label = { Text("FAQ") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = Color.Transparent
                )
            )

            NavigationBarItem(
                selected = currentRoute == "profil",
                onClick = { navigate("profil") },
                icon = { Icon(Icons.Default.Person, contentDescription = "Profil") },
                label = { Text("Profil") },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = selectedColor,
                    selectedTextColor = selectedColor,
                    unselectedIconColor = unselectedColor,
                    unselectedTextColor = unselectedColor,
                    indicatorColor = Color.Transparent
                )
            )
        }

        // FAB Kartu
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .offset(y = (-16).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2))
                    .clickable { 
                        if (currentRoute != "kartu") {
                            navController.navigate("kartu") {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                     }
                    .shadow(8.dp, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CreditCard,
                    contentDescription = "Kartu",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                "Kartu",
                style = MaterialTheme.typography.labelSmall,
                color = if (currentRoute == "kartu") selectedColor else unselectedColor
            )
        }
    }
}
