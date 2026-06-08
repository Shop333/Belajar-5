package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Web3Green
import com.example.ui.theme.EthereumPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.WalletState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: ChatViewModel,
    onContactSelected: (Contact) -> Unit,
    modifier: Modifier = Modifier
) {
    val walletState by viewModel.walletState.collectAsState()
    val contacts by viewModel.contacts.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var targetAddressInput by remember { mutableStateOf("") }
    var targetEnsInput by remember { mutableStateOf("") }

    val userAddress = (walletState as? WalletState.Connected)?.address ?: "0x..."
    val userEns = (walletState as? WalletState.Connected)?.ensName ?: "user.eth"

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Web3 Messenger",
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.White
                        )
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(Web3Green)
                            )
                            Text(
                                text = "Connected: ${userEns} (${userAddress.take(6)}...${userAddress.takeLast(4)})",
                                style = MaterialTheme.typography.bodySmall,
                                color = Web3Green,
                                fontSize = 11.sp
                            )
                        }
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.disconnectWallet() },
                        modifier = Modifier.testTag("logout_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Disconnect Wallet",
                            tint = Color.Red
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = NeonCyan,
                contentColor = Color.Black,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("add_contact_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = "Add Contact")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            if (contacts.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Email,
                            contentDescription = "No Chats",
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Belum Ada Obrolan",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.LightGray
                        )
                        Text(
                            text = "Klik tombol + untuk memulai interaksi dengan dompet kripto lain di jaringan Waku P2P.",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color.Gray,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(contacts) { contact ->
                        ContactItemCard(
                            contact = contact,
                            onClick = { onContactSelected(contact) }
                        )
                    }
                }
            }
        }

        // ADD WEB3 CONTACT DIALOG
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = {
                    Text(
                        "Mulai Obrolan Web3 Baru",
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(
                            "Masukkan Alamat Dompet Crypto target (sebagai ID pesan) dan domain ENS opsional.",
                            fontSize = 12.sp,
                            color = Color.LightGray
                        )
                        OutlinedTextField(
                            value = targetAddressInput,
                            onValueChange = { targetAddressInput = it },
                            label = { Text("Wallet Address (0x...)") },
                            placeholder = { Text("0x71C249E...") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                        OutlinedTextField(
                            value = targetEnsInput,
                            onValueChange = { targetEnsInput = it },
                            label = { Text("ENS Domain (Opsional)") },
                            placeholder = { Text("budi.eth") },
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonCyan,
                                unfocusedBorderColor = MaterialTheme.colorScheme.outline
                            )
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            if (targetAddressInput.isNotBlank()) {
                                viewModel.addNewContact(targetAddressInput, targetEnsInput)
                                showAddDialog = false
                                targetAddressInput = ""
                                targetEnsInput = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Mulai Chat", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Batal", color = Color.Gray)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}

@Composable
fun ContactItemCard(
    contact: Contact,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .testTag("contact_card_${contact.walletAddress}"),
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Profile Web3 representation
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(EthereumPurple.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = null,
                    tint = EthereumPurple,
                    modifier = Modifier.size(24.dp)
                )

                // Network indicator
                if (contact.isOnline) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(Web3Green)
                    )
                }
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = contact.ensName ?: "${contact.walletAddress.take(6)}...${contact.walletAddress.takeLast(4)}",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 16.sp,
                    )
                    
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "E2EE",
                        tint = Web3Green,
                        modifier = Modifier.size(14.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = contact.lastMessage ?: "Mulai obrolan E2EE sejati...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                )
            }

            // Arrow Indicator
            Icon(
                imageVector = Icons.Default.ArrowForward,
                contentDescription = null,
                tint = Color.Gray
            )
        }
    }
}
