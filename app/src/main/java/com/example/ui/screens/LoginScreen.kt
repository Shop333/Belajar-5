package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Web3Green
import com.example.ui.theme.EthereumPurple
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.WalletState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: ChatViewModel,
    modifier: Modifier = Modifier
) {
    val walletState by viewModel.walletState.collectAsState()
    val siweChallenge by viewModel.siweChallenge.collectAsState()
    val siweSignature by viewModel.siweSignature.collectAsState()

    var customWalletInput by remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.height(30.dp))

        // Web3 Glowing Logo
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Wallet Logo",
            tint = NeonCyan,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Web3 Chat dApp",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center
        )

        Text(
            text = "Kedaulatan Data Mandiri & Enkripsi E2EE Klien Tanpa Server Terpusat",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(40.dp))

        when (walletState) {
            is WalletState.Disconnected -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Hubungkan Dompet Kripto",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Gunakan alamat dompet Anda sebagai identitas publik global. Aman, tanpa nomor HP.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        // Custom Wallet Address Input (Optional)
                        OutlinedTextField(
                            value = customWalletInput,
                            onValueChange = { customWalletInput = it },
                            label = { Text("Alamat Dompet (Opsional, ketik kustom)") },
                            placeholder = { Text("0x1234...abcd") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("wallet_input"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { viewModel.initiateWalletLogin(customWalletInput) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("connect_wallet_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = EthereumPurple),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Hubungkan Wallet (Simultaneous Connect)",
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            is WalletState.Connecting -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(32.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator(color = NeonCyan)
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Meminta Koneksi dari MetaMask / Trust Wallet...",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            is WalletState.Connected -> {
                // Should redirect but if here, show connected status
                val connectedState = walletState as WalletState.Connected
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success",
                        tint = Web3Green,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Wallet Terkoneksi!",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Web3Green
                    )
                    Text(
                        text = connectedState.address,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = NeonCyan
                    )
                }
            }
        }

        // SIWE CHALLENGE POPUP / PANEL
        AnimatedVisibility(
            visible = siweChallenge.isNotBlank() && walletState == WalletState.Connecting,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Spacer(modifier = Modifier.height(20.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, NeonCyan.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NeonCyan)
                        Text(
                            text = "Sign-In with Ethereum (SIWE)",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(140.dp)
                            .verticalScroll(rememberScrollState())
                            .background(Color.Black)
                            .padding(8.dp)
                    ) {
                        Text(
                            text = siweChallenge,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 10.sp,
                            color = Color.LightGray
                        )
                    }
                    Spacer(modifier = Modifier.height(16.dp))

                    if (siweSignature.isBlank()) {
                        Button(
                            onClick = { viewModel.signSiweChallenge() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("sign_siwe_button"),
                            colors = ButtonDefaults.buttonColors(containerColor = Web3Green),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                "Sign Message (Tanda Tangani Klien)",
                                fontWeight = FontWeight.Bold,
                                color = Color.Black
                            )
                        }
                    } else {
                        Column {
                            Text(
                                text = "Tanda Tangan ECDSA (v, r, s) Berhasil Terbuat:",
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold,
                                color = Web3Green
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = siweSignature.take(30) + "...",
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }
        }
    }
}
