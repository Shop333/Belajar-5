package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.model.Contact
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.DocsScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.EthereumPurple
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.WalletState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ChatViewModel = viewModel()
                val walletState by viewModel.walletState.collectAsState()

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (walletState) {
                        is WalletState.Disconnected, is WalletState.Connecting -> {
                            LoginScreen(
                                viewModel = viewModel,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        is WalletState.Connected -> {
                            MainNavigationController(viewModel = viewModel)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MainNavigationController(viewModel: ChatViewModel) {
    var currentTab by remember { mutableStateOf(0) } // 0 untuk Chats, 1 untuk Web3 Docs
    var selectedContactForChat by remember { mutableStateOf<Contact?>(null) }

    if (selectedContactForChat != null) {
        // Pindah ke ChatScreen jika kontak terpilih
        ChatScreen(
            viewModel = viewModel,
            contact = selectedContactForChat!!,
            onBack = { selectedContactForChat = null },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Tampilkan navigasi tab (Chats vs Docs)
        Scaffold(
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    modifier = Modifier.windowInsetsPadding(WindowInsets.navigationBars)
                ) {
                    NavigationBarItem(
                        selected = currentTab == 0,
                        onClick = { currentTab = 0 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Email,
                                contentDescription = "Chats"
                            )
                        },
                        label = { Text("Chats/Pesan") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EthereumPurple,
                            selectedTextColor = NeonCyan,
                            indicatorColor = EthereumPurple.copy(alpha = 0.2f),
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("tab_chats")
                    )
                    NavigationBarItem(
                        selected = currentTab == 1,
                        onClick = { currentTab = 1 },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Info,
                                contentDescription = "Architecture Docs"
                            )
                        },
                        label = { Text("Web3 Specs") },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = EthereumPurple,
                            selectedTextColor = NeonCyan,
                            indicatorColor = EthereumPurple.copy(alpha = 0.2f),
                            unselectedTextColor = Color.Gray,
                            unselectedIconColor = Color.Gray
                        ),
                        modifier = Modifier.testTag("tab_docs")
                    )
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentTab) {
                    0 -> {
                        HomeScreen(
                            viewModel = viewModel,
                            onContactSelected = { contact ->
                                selectedContactForChat = contact
                            },
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    1 -> {
                        DocsScreen(
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                }
            }
        }
    }
}
