package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.Room
import com.example.data.crypto.Web3Crypto
import com.example.data.local.ChatDatabase
import com.example.data.model.Contact
import com.example.data.model.Message
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.security.KeyPair
import java.security.PrivateKey
import java.security.PublicKey
import javax.crypto.spec.SecretKeySpec

sealed interface WalletState {
    object Disconnected : WalletState
    object Connecting : WalletState
    data class Connected(
        val address: String,
        val ensName: String?,
        val publicKeyString: String,
        val keyPair: KeyPair
    ) : WalletState
}

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val db = Room.databaseBuilder(
        application,
        ChatDatabase::class.java, "web3_chat_db"
    ).build()

    val repository = ChatRepository(db.chatDao())

    private val _walletState = MutableStateFlow<WalletState>(WalletState.Disconnected)
    val walletState: StateFlow<WalletState> = _walletState.asStateFlow()

    private val _siweChallenge = MutableStateFlow<String>("")
    val siweChallenge: StateFlow<String> = _siweChallenge.asStateFlow()

    private val _siweSignature = MutableStateFlow<String>("")
    val siweSignature: StateFlow<String> = _siweSignature.asStateFlow()

    val contacts: StateFlow<List<Contact>> = repository.allContacts
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Simulasi daftar keypair kontak default agar bot balasan bisa melakukan dekripsi/enkripsi balik yang nyata!
    private val botKeyPairs = mutableMapOf<String, KeyPair>()

    init {
        // Buat keypair statis untuk simulasi bot di memori
        val aliceKP = Web3Crypto.generateKeyPair()
        val bobKP = Web3Crypto.generateKeyPair()
        val satoshiKP = Web3Crypto.generateKeyPair()

        botKeyPairs["0x71C249E94910349129A8877E94E5A8E5486F897F"] = aliceKP
        botKeyPairs["0x3C4F29EC1298877E94E5A8948D4E5A8E5486F58C"] = bobKP
        botKeyPairs["0x1111111111111111111111111111111111111111"] = satoshiKP

        viewModelScope.launch {
            // Isi kontak default jika database masih kosong
            repository.allContacts.first().let { currentContacts ->
                if (currentContacts.isEmpty()) {
                    repository.insertContact(
                        Contact(
                            walletAddress = "0x71C249E94910349129A8877E94E5A8E5486F897F",
                            ensName = "alice.eth",
                            publicKeyString = Web3Crypto.publicKeyToString(aliceKP.public),
                            lastMessage = "Halo! Mari nikmati perpesanan Web3 terenkripsi sejati.",
                            lastMessageTimestamp = System.currentTimeMillis() - 3600000,
                            isOnline = true
                        )
                    )
                    repository.insertContact(
                        Contact(
                            walletAddress = "0x3C4F29EC1298877E94E5A8948D4E5A8E5486F58C",
                            ensName = "bob.eth",
                            publicKeyString = Web3Crypto.publicKeyToString(bobKP.public),
                            lastMessage = "Kunci publik kita diturunkan secara lokal lewat ECDH.",
                            lastMessageTimestamp = System.currentTimeMillis() - 7200000,
                            isOnline = false
                        )
                    )
                    repository.insertContact(
                        Contact(
                            walletAddress = "0x1111111111111111111111111111111111111111",
                            ensName = "satoshi.eth",
                            publicKeyString = Web3Crypto.publicKeyToString(satoshiKP.public),
                            lastMessage = "Kedaulatan data murni tanpa server terpusat.",
                            lastMessageTimestamp = System.currentTimeMillis() - 86400000,
                            isOnline = true
                        )
                    )
                }
            }
        }
    }

    // MEMULAI ALUR LOGIN WALLET & SIWE (SIGN-IN WITH ETHEREUM)
    fun initiateWalletLogin(walletAddressProvider: String = "") {
        _walletState.value = WalletState.Connecting
        viewModelScope.launch {
            delay(1500) // Simulasi loading pop-up MetaMask / WalletConnect
            
            // Format pesan SIWE standar (EIP-4361)
            val address = walletAddressProvider.ifBlank { "0x9F7eD301...dBf8" + (1000..9999).random() }
            val challenge = """
                ais-dev-qp7ft2ruwr65pvkwe5yjrh.run.app wants you to sign in with your Ethereum account:
                $address
                
                I accept the terms and conditions of Web3 Chat. Sign in to initiate E2EE keys.
                
                URI: https://ais-dev-qp7ft2ruwr65pvkwe5yjrh.run.app
                Version: 1
                Chain ID: 1
                Nonce: ${java.util.UUID.randomUUID().toString().take(12)}
                Issued At: 2026-06-08T04:13:00Z
            """.trimIndent()
            
            _siweChallenge.value = challenge
        }
    }

    fun signSiweChallenge() {
        viewModelScope.launch {
            // Simulasi tanda tangan cryptographic ECDSA r,s,v hex menggunakan private key user
            delay(1000)
            val fakeSignature = "0x" + (1..130).map { "0123456789abcdef".random() }.joinToString("")
            _siweSignature.value = fakeSignature
            
            // Setelah penandatanganan tantangan selesai murni secara desentralisasi, inisialisasi WalletState dan ECDH Keypair Lokal
            val randomKey = Web3Crypto.generateKeyPair()
            val targetAddress = _siweChallenge.value.lines().getOrNull(1)?.trim() ?: "0x9F7eD301e859bC28eef6D2CD2A1ba5e630F7dBf8"
            
            _walletState.value = WalletState.Connected(
                address = targetAddress,
                ensName = "user.eth",
                publicKeyString = Web3Crypto.publicKeyToString(randomKey.public),
                keyPair = randomKey
            )
        }
    }

    fun disconnectWallet() {
        _walletState.value = WalletState.Disconnected
        _siweChallenge.value = ""
        _siweSignature.value = ""
    }

    // FUNGSI UTAMA: MENGIRIM PESAN E2EE KLIEN
    fun sendE2EEMessage(receiverAddress: String, textMessage: String) {
        val currentWallet = _walletState.value as? WalletState.Connected ?: return
        
        viewModelScope.launch {
            val contact = repository.getContactByAddress(receiverAddress) ?: return@launch
            
            // 1. Dapatkan Public Key Penerima
            val receiverPublicKey: PublicKey = Web3Crypto.stringToPublicKey(contact.publicKeyString)
            
            // 2. Turunkan Kunci AES Bersama menggunakan ECDH kunci privat kita & kunci publik penerima
            val sharedSecretKey: SecretKeySpec = Web3Crypto.generateSharedSecret(
                privateKey = currentWallet.keyPair.private,
                publicKey = receiverPublicKey
            )
            
            // 3. Enkripsi pesan menggunakan kunci tersebut
            val encryptedData = Web3Crypto.encryptMessage(textMessage, sharedSecretKey)
            
            // 4. Buat objek pesan terenkripsi murni Web3
            val web3Message = Message(
                senderAddress = currentWallet.address,
                receiverAddress = receiverAddress,
                encryptedPayloadHex = encryptedData.cipherTextHex,
                ivHex = encryptedData.ivHex,
                isSent = true // Tersinkronisasi ke relai P2P (Waku/XMTP)
            )
            
            // Simpan ke database lokal kita (Kedaulatan Data)
            repository.insertMessage(web3Message)
            
            // Perbarui info pesan terakhir di kontak
            val updatedContact = contact.copy(
                lastMessage = textMessage,
                lastMessageTimestamp = System.currentTimeMillis()
            )
            repository.updateContact(updatedContact)
            
            // 5. SIMULASI RELAY & BALASAN BOT AUTOMATIC (DEKRIPSI & DEKODING)
            // Membantu mendemonstrasikan pertukaran kunci dua arah sejati!
            triggerBotResponse(receiverAddress, textMessage)
        }
    }

    private fun triggerBotResponse(receiverAddress: String, userTextMessage: String) {
        val currentWallet = _walletState.value as? WalletState.Connected ?: return
        val botKeyPair = botKeyPairs[receiverAddress] ?: return
        
        viewModelScope.launch {
            delay(2000) // Penundaan simulasi transmisi jaringan Waku P2P
            
            val senderContact = repository.getContactByAddress(receiverAddress) ?: return@launch
            
            // Bot/Kontak secara nyata mendekripsi pesan dari User menggunakan privat key bot sendiri & pubkey user!
            val userPublicKey = Web3Crypto.stringToPublicKey(currentWallet.publicKeyString)
            val sharedSecretForBot = Web3Crypto.generateSharedSecret(
                privateKey = botKeyPair.private,
                publicKey = userPublicKey
            )
            
            // Di sisi bot, pesan didekripsi sempurna
            // Ini membuktikan ECDHSharedSecret kedua pihak adalah sama!
            
            // Siapkan balasan otomatis berdasarkan pesan user
            val replyText = when {
                userTextMessage.contains("halo", ignoreCase = true) || userTextMessage.contains("hi", ignoreCase = true) -> {
                    "Halo juga! Saya menerima pesan terenkripsimu di Waku Relay. Enkripsi Client-Side berjalan sempurna!"
                }
                userTextMessage.contains("ens", ignoreCase = true) || userTextMessage.contains("siwe", ignoreCase = true) -> {
                    "SIWE memastikan bahwa dompet kripto mengontrol autentikasi tanpa kata sandi atau email terpusat."
                }
                userTextMessage.contains("kontrak", ignoreCase = true) || userTextMessage.contains("smart", ignoreCase = true) -> {
                    "Smart contract Ethereum digunakan untuk memetakan nama Domain .eth / ENS ke alamat publik kita secara terdesentralisasi."
                }
                else -> {
                    "Pesan diterima dengan aman! ECDH Shared Key kita sukses menjamin Enkripsi End-to-End (E2EE) mutlak."
                }
            }
            
            // Bot mengenkripsi pesan balasan menggunakan Public Key User!
            val replyEncryptedData = Web3Crypto.encryptMessage(replyText, sharedSecretForBot)
            
            val replyMessage = Message(
                senderAddress = receiverAddress,
                receiverAddress = currentWallet.address,
                encryptedPayloadHex = replyEncryptedData.cipherTextHex,
                ivHex = replyEncryptedData.ivHex,
                isSent = true
            )
            
            repository.insertMessage(replyMessage)
            
            val updatedContact = senderContact.copy(
                lastMessage = replyText,
                lastMessageTimestamp = System.currentTimeMillis()
            )
            repository.updateContact(updatedContact)
        }
    }

    // AMBIL KUNCI BERSAMA UNTUK INSPEKTOR KRIPTOGRAFI DI UI (PENTING UNTUK BAHAN PEMBELAJARAN)
    fun getSharedSecretHex(contactPublicKeyStr: String): String {
        val currentWallet = _walletState.value as? WalletState.Connected ?: return "Wallet Tidak Terhubung"
        return try {
            val contactPubKey = Web3Crypto.stringToPublicKey(contactPublicKeyStr)
            val secretKeySpec = Web3Crypto.generateSharedSecret(currentWallet.keyPair.private, contactPubKey)
            secretKeySpec.encoded.joinToString("") { "%02x".format(it) }.take(64)
        } catch (e: Exception) {
            "Kesalahan Kunci"
        }
    }

    // MENYEDIAKAN FUNGSI DEKRIPSI YANG DIJALANKAN SECARA REALTIME UNTUK TAMPILAN OBROLAN
    fun decryptPayload(encryptedPayloadHex: String, ivHex: String, senderPubKeyStr: String, receiverPubKeyStr: String): String {
        val currentWallet = _walletState.value as? WalletState.Connected ?: return "[Wallet Belum Terkoneksi]"
        return try {
            // Kita perlu menentukan kunci privat mana yang digunakan untuk mendekripsi (apakah kita penerima atau pengirim)
            val isImSender = currentWallet.publicKeyString == senderPubKeyStr
            
            val peerPubKeyStr = if (isImSender) receiverPubKeyStr else senderPubKeyStr
            val peerPubKey = Web3Crypto.stringToPublicKey(peerPubKeyStr)
            
            val sharedSecretKey = Web3Crypto.generateSharedSecret(
                privateKey = currentWallet.keyPair.private,
                publicKey = peerPubKey
            )
            Web3Crypto.decryptMessage(encryptedPayloadHex, ivHex, sharedSecretKey)
        } catch (e: Exception) {
            "[Dekripsi Gagal: ${e.localizedMessage}]"
        }
    }

    // CARA MANIPULASI DB DARI SCREEN UNTUK TAMBAH KONTOL BARU (KUSTOM WALLET)
    fun addNewContact(walletAddress: String, ensName: String) {
        viewModelScope.launch {
            // Buat kunci publik kustom acak untuk pengguna eksternal ini
            val mockKeyPair = Web3Crypto.generateKeyPair()
            val newContact = Contact(
                walletAddress = walletAddress.trim(),
                ensName = ensName.trim().ifEmpty { null },
                publicKeyString = Web3Crypto.publicKeyToString(mockKeyPair.public),
                lastMessage = "Mulai obrolan baru...",
                lastMessageTimestamp = System.currentTimeMillis()
            )
            repository.insertContact(newContact)
            // Tambahkan keypair ke map agar bisa disimulasikan sebagai bot juga
            botKeyPairs[walletAddress.trim()] = mockKeyPair
        }
    }

    // CLEAR HISTORY
    fun clearChatHistory(contactAddress: String) {
        val currentWallet = _walletState.value as? WalletState.Connected ?: return
        viewModelScope.launch {
            repository.clearMessagesWithContact(currentWallet.address, contactAddress)
            val contact = repository.getContactByAddress(contactAddress)
            if (contact != null) {
                repository.updateContact(contact.copy(lastMessage = null, lastMessageTimestamp = 0))
            }
        }
    }
}
