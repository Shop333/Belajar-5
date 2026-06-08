package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.Web3Green
import com.example.ui.theme.EthereumPurple

@Composable
fun DocsScreen(modifier: Modifier = Modifier) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
            .verticalScroll(scrollState),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // HEADER
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = "Docs Icon",
                tint = NeonCyan,
                modifier = Modifier.size(32.dp)
            )
            Column {
                Text(
                    text = "Arsitektur & Spesifikasi dApp",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = "Standardisasi Protokol Chatting Desentralisasi E2EE",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

        // 1. WORKFLOW SECTION
        DocCard(
            title = "1. Alur Kerja Web3 (E2EE Workflow)",
            icon = Icons.Default.Refresh,
            iconTint = NeonCyan
        ) {
            Text(
                text = "Bagaimana pesan teks dikirimkan secara desentralisasi tanpa server pusat:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            WorkflowStep("A. Koneksi Non-Custodial Wallet", "Pengguna melakukan login menggunakan alamat dompet kripto (Public Key/Wallet Address) tanpa email atau nomor telepon.")
            WorkflowStep("B. Sign-In With Ethereum (SIWE)", "Pesan tantangan ditandatangani di sisi klien demi membuktikan kepemilikan dompet kripto secara kriptografis.")
            WorkflowStep("C. Kunci ECDH (Elliptic Curve Diffie-Hellman)", "Setiap kali chat baru dimulai, perangkat pengguna melakukan pertukaran kunci Diffie-Hellman secara lokal dengan mengambil kunci publik lawan dari relai desentralisasi, menghasilkan Shared Secret Key 256-bit di kedua belah pihak secara mandiri.")
            WorkflowStep("D. Enkripsi Client-Side (AES-GCM)", "Sebelum dikirim ke jaringan relai P2P (seperti Waku/XMTP), pesan teks biasa dienkripsi di sisi klien dengan kunci bersama tersebut. Isi pesan terenkripsi murni disiarkan ke relai desentralisasi.")
            WorkflowStep("E. Dekripsi Mandiri", "Satu-satunya entitas yang dapat mendekripsi pesan adalah pemegang kunci privat dompet penerima.")
        }

        // 2. SMART CONTRACT & ARCHITECTURE
        DocCard(
            title = "2. Arsitektur Smart Contract & Protokol",
            icon = Icons.Default.Share,
            iconTint = EthereumPurple
        ) {
            Text(
                text = "Desain Kontrak Cerdas (Solidity) untuk Registrasi Identitas & Kunci Publik:",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            CodeSnippet(
                code = """
// SPDX-License-Identifier: MIT
pragma solidity ^0.8.20;

contract Web3IdentityRegistry {
    struct Identity {
        string ensName;
        string publicKey; // Digunakan untuk inisiasi enkripsi ECDH
        uint256 registeredAt;
    }

    mapping(address => Identity) private registries;
    mapping(string => address) private ensToAddress;

    event IdentityRegistered(address indexed user, string ensName, string publicKey);

    function registerIdentity(string calldata _ensName, string calldata _publicKey) external {
        if (bytes(_ensName).length > 0) {
            require(ensToAddress[_ensName] == address(0), "ENS Name sudah terdaftar!");
            ensToAddress[_ensName] = msg.sender;
        }
        registries[msg.sender] = Identity(_ensName, _publicKey, block.timestamp);
        emit IdentityRegistered(msg.sender, _ensName, _publicKey);
    }

    function getIdentity(address _user) external view returns (string memory, string memory) {
        Identity memory id = registries[_user];
        return (id.ensName, id.publicKey);
    }
}
                """.trimIndent()
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Protokol Pesan (Network Layer):",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
            )
            Text(
                text = "• XMTP (Extensible Message Transport Protocol): Menyediakan API perpesanan instan aman terintegrasi blockchain.\n" +
                        "• Waku (P2P Mesh Network): Protokol relai gosip terdesentralisasi ringan tanpa server.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // 3. IMPLEMENTATION CODE
        DocCard(
            title = "3. Implementasi Kriptografi (Kotlin)",
            icon = Icons.Default.CheckCircle,
            iconTint = Web3Green
        ) {
            Text(
                text = "Potongan kode riil enkripsi klien AES-GCM dengan Shared Secret di Kotlin (digunakan di dApp ini):",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Spacer(modifier = Modifier.height(8.dp))
            CodeSnippet(
                code = """
// Menurunkan kunci AES dari Shared Secret ECDH
fun deriveKeyBytes(sharedSecret: ByteArray): SecretKeySpec {
    val digest = MessageDigest.getInstance("SHA-256")
    val aesKeyBytes = digest.digest(sharedSecret)
    return SecretKeySpec(aesKeyBytes, "AES")
}

// Enkripsi AES-GCM di Sisi Klien (Client-Side E2EE)
fun encrypt(plainText: String, secretKey: SecretKeySpec): EncryptedData {
    val cipher = Cipher.getInstance("AES/GCM/NoPadding")
    val iv = ByteArray(12) // GCM IV 12 byte
    SecureRandom().nextBytes(iv)
    cipher.init(Cipher.ENCRYPT_MODE, secretKey, GCMParameterSpec(128, iv))
    
    val cipherTextBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
    return EncryptedData(bytesToHex(cipherTextBytes), bytesToHex(iv))
}
                """.trimIndent()
            )
        }

        // 4. SECURITY NOTES
        DocCard(
            title = "4. Catatan Keamanan (Security Notes)",
            icon = Icons.Default.Lock,
            iconTint = Color.Red
        ) {
            SecurityPoint("A. Manajemen Kunci Privat Non-Custodial", "Jangan pernah meloloskan kunci privat dompet ke server mana pun. Gunakan Android Keystore System yang didukung perangkat keras (TEE/StrongBox) untuk menyimpan kunci enkripsi ECDH.")
            SecurityPoint("B. Serangan Man-in-the-Middle (MitM)", "Pastikan kunci publik lawan dikonfigurasi melalui tanda tangan digital yang diverifikasi oleh Smart Contract Registry atau layanan DNS ens.eth terpercaya untuk mencegah spoofing.")
            SecurityPoint("C. Metadata Sovereignty", "Meskipun teks pesan dienkripsi penuh secara mutlak, informasi pengirim-penerima (metadata) masih dapat disadap dalam jaringan terbuka. Gunakan protokol rute bayangan seperti Waku/IPFS relay untuk mengacak jejak metadata obrolan.")
        }
    }
}

@Composable
fun DocCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    iconTint: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 12.dp)
            ) {
                Icon(imageVector = icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(24.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = iconTint
                )
            }
            content()
        }
    }
}

@Composable
fun WorkflowStep(step: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Text(text = step, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = NeonCyan)
        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
fun SecurityPoint(title: String, desc: String) {
    Column(modifier = Modifier.padding(vertical = 4.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            Icon(imageVector = Icons.Default.Warning, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
            Text(text = title, style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = Color.White)
        }
        Text(text = desc, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.padding(start = 22.dp))
    }
}

@Composable
fun CodeSnippet(code: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black)
            .padding(12.dp)
    ) {
        Text(
            text = code,
            style = MaterialTheme.typography.bodySmall.copy(
                fontFamily = FontFamily.Monospace,
                fontSize = 11.sp
            ),
            color = Web3Green
        )
    }
}
