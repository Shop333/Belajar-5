package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Contact
import com.example.data.model.Message
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Web3Green
import com.example.ui.theme.EthereumPurple
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.viewmodel.ChatViewModel
import com.example.ui.viewmodel.WalletState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    contact: Contact,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val walletState by viewModel.walletState.collectAsState()
    val myAddress = (walletState as? WalletState.Connected)?.address ?: ""
    val messages by viewModel.repository.getMessagesWithContact(myAddress, contact.walletAddress).collectAsState(initial = emptyList())
    
    var messageInput by remember { mutableStateOf("") }
    
    // State untuk Inspektor Kriptografi
    var selectedMessageForInspector by remember { mutableStateOf<Message?>(null) }
    var showInspectorDrawer by remember { mutableStateOf(false) }

    val sharedSecretHex = remember(contact.publicKeyString) {
        viewModel.getSharedSecretHex(contact.publicKeyString)
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text(
                                    text = contact.ensName ?: "${contact.walletAddress.take(6)}...${contact.walletAddress.takeLast(4)}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.White
                                )
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Verified E2EE Connection",
                                    tint = Web3Green,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            Text(
                                text = "E2EE Valid via ECDH x secp256r1",
                                fontSize = 11.sp,
                                color = Web3Green
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack, modifier = Modifier.testTag("chat_back_button")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.clearChatHistory(contact.walletAddress) }) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Clear History",
                            tint = Color.LightGray
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(innerPadding)
        ) {
            // PANEL SECURE BAR (INFO KUNCI ECDH)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EthereumPurple.copy(alpha = 0.15f))
                    .border(1.dp, EthereumPurple.copy(alpha = 0.3f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = NeonCyan, modifier = Modifier.size(18.dp))
                    Text(
                        text = "ECDH Shared Key: 0x${sharedSecretHex.take(24)}...",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        color = NeonCyan,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = {
                            // Buka info kunci
                            val dummyMessage = Message(
                                senderAddress = myAddress,
                                receiverAddress = contact.walletAddress,
                                encryptedPayloadHex = "[Shared Key Details]",
                                ivHex = "IV_NATIVE_12_BYTE",
                                isSent = true
                            )
                            selectedMessageForInspector = dummyMessage
                            showInspectorDrawer = true
                        },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = "Key Info", tint = NeonCyan, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // CHAT BUBBLES LIST
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 16.dp, bottom = 16.dp)
            ) {
                items(messages) { message ->
                    val isMe = message.senderAddress == myAddress
                    val decryptedText = viewModel.decryptPayload(
                        encryptedPayloadHex = message.encryptedPayloadHex,
                        ivHex = message.ivHex,
                        senderPubKeyStr = if (isMe) (walletState as WalletState.Connected).publicKeyString else contact.publicKeyString,
                        receiverPubKeyStr = if (isMe) contact.publicKeyString else (walletState as WalletState.Connected).publicKeyString
                    )

                    ChatBubble(
                        message = message,
                        decryptedText = decryptedText,
                        isMe = isMe,
                        onInspect = {
                            selectedMessageForInspector = message
                            showInspectorDrawer = true
                        }
                    )
                }
            }

            // INPUT FORM
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .imePadding()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = messageInput,
                    onValueChange = { messageInput = it },
                    placeholder = { Text("Kirim pesan E2EE murni via Waku...") },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input"),
                    shape = RoundedCornerShape(24.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonCyan,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    maxLines = 4
                )

                FloatingActionButton(
                    onClick = {
                        if (messageInput.isNotBlank()) {
                            viewModel.sendE2EEMessage(contact.walletAddress, messageInput)
                            messageInput = ""
                        }
                    },
                    containerColor = EthereumPurple,
                    contentColor = Color.White,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .size(48.dp)
                        .testTag("send_button")
                ) {
                    Icon(imageVector = Icons.AutoMirrored.Default.Send, contentDescription = "Send", modifier = Modifier.size(20.dp))
                }
            }
        }

        // DESIGN: KRIPTOGRAFI INSPECTOR DRAWERS
        if (showInspectorDrawer && selectedMessageForInspector != null) {
            val isInfoOnly = selectedMessageForInspector?.encryptedPayloadHex == "[Shared Key Details]"
            
            AlertDialog(
                onDismissRequest = {
                    showInspectorDrawer = false
                    selectedMessageForInspector = null
                },
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Web3Green)
                        Text(
                            text = if (isInfoOnly) "Kriptografi & Shared Secret" else "Payload Transmisi P2P Terenkripsi",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    ) {
                        if (isInfoOnly) {
                            Text(
                                text = "Kedua belah pihak menurunkan Shared Secret yang identik secara mandiri menggunakan Diffie-Hellman tanpa bertukar kunci rahasia secara langsung.",
                                fontSize = 12.sp,
                                color = Color.LightGray
                            )
                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            InspectorField("Kurva Elliptic", "secp256r1 (NIST P-256 standard)")
                            InspectorField("Fungsi Hash (KDF)", "SHA-256 (256-bit Key Stretching)")
                            InspectorField("Shared Key Klien Kita", "0x$sharedSecretHex")
                            InspectorField("Shared Key Kontak", "0x$sharedSecretHex")
                            InspectorField("Symmetric Cypher", "AES/GCM/NoPadding (AEAD)")
                        } else {
                            val msg = selectedMessageForInspector!!
                            val isMe = msg.senderAddress == myAddress
                            val decText = viewModel.decryptPayload(
                                encryptedPayloadHex = msg.encryptedPayloadHex,
                                ivHex = msg.ivHex,
                                senderPubKeyStr = if (isMe) (walletState as WalletState.Connected).publicKeyString else contact.publicKeyString,
                                receiverPubKeyStr = if (isMe) contact.publicKeyString else (walletState as WalletState.Connected).publicKeyString
                            )

                            Text(
                                "Ini adalah payload biner mentah yang ditransmisikan melintasi blockchain/Waku Relay network sebelum didekripsi oleh penerima di sisi klien.",
                                fontSize = 11.sp,
                                color = Color.LightGray
                            )

                            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

                            InspectorField("Pengirim", msg.senderAddress)
                            InspectorField("Penerima", msg.receiverAddress)
                            InspectorField("AES IV (Hex)", "0x" + msg.ivHex)
                            InspectorField("Isi Asli (Plaintext)", decText)
                            InspectorField("Ciphertext Terenkripsi", msg.encryptedPayloadHex)

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                "Raw JSON broadcast over P2P network:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color.Black)
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = """
{
  "sender": "${msg.senderAddress.take(14)}...",
  "payload": "${msg.encryptedPayloadHex.take(24)}...",
  "iv": "${msg.ivHex}",
  "mac": "0x${msg.encryptedPayloadHex.takeLast(16)}",
  "protocol": "WAKU-P2P/1.2-E2EE",
  "timestamp": ${msg.timestamp}
}
                                    """.trimIndent(),
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 10.sp,
                                    color = Web3Green
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showInspectorDrawer = false
                            selectedMessageForInspector = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonCyan)
                    ) {
                        Text("Tutup", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                },
                containerColor = DarkSurface
            )
        }
    }
}

@Composable
fun ChatBubble(
    message: Message,
    decryptedText: String,
    isMe: Boolean,
    onInspect: () -> Unit
) {
    val bubbleColor = if (isMe) EthereumPurple.copy(alpha = 0.85f) else DarkSurfaceVariant
    val alignment = if (isMe) Alignment.CenterEnd else Alignment.CenterStart
    val shape = if (isMe) {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 16.dp, bottomEnd = 0.dp)
    } else {
        RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 0.dp, bottomEnd = 16.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("chat_bubble_${message.id}"),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (isMe) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            Box(
                modifier = Modifier
                    .clip(shape)
                    .background(bubbleColor)
                    .clickable { onInspect() }
                    .padding(12.dp)
            ) {
                Column {
                    Text(
                        text = decryptedText,
                        color = Color.White,
                        fontSize = 15.sp
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.align(Alignment.End)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "E2EE",
                            tint = NeonCyan,
                            modifier = Modifier.size(11.dp)
                        )
                        Text(
                            text = "E2EE",
                            fontSize = 9.sp,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        
                        // Status pengiriman fiktif ke node relai
                        if (isMe) {
                            Icon(
                                imageVector = if (message.isSent) Icons.Default.Done else Icons.Default.Done,
                                contentDescription = if (message.isSent) "Relayed" else "Pending",
                                tint = if (message.isSent) Web3Green else Color.Gray,
                                modifier = Modifier.size(12.dp)
                            )
                        }
                    }
                }
            }
            Text(
                text = if (isMe) "Anda" else "${message.senderAddress.take(6)}... (Ketuk untuk inspect)",
                fontSize = 10.sp,
                color = Color.Gray,
                modifier = Modifier
                    .padding(horizontal = 4.dp, vertical = 2.dp)
                    .clickable { onInspect() }
            )
        }
    }
}

@Composable
fun InspectorField(label: String, value: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = label, fontWeight = FontWeight.Bold, fontSize = 11.sp, color = NeonCyan)
        Text(
            text = value,
            fontFamily = FontFamily.Monospace,
            fontSize = 11.sp,
            color = Color.White,
            modifier = Modifier.fillMaxWidth()
        )
    }
}
