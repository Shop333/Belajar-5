package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey val walletAddress: String, // ID unik kontak (0x...)
    val ensName: String?,                 // DNS Web3 (misal: budi.eth)
    val publicKeyString: String,           // Public key hex untuk E2EE
    val lastMessage: String? = null,
    val lastMessageTimestamp: Long = 0,
    val isOnline: Boolean = false
)

@Entity(tableName = "messages")
data class Message(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val senderAddress: String,
    val receiverAddress: String,
    val encryptedPayloadHex: String,      // JSON Payload terenkripsi
    val ivHex: String,                    // Initialization Vector AES
    val timestamp: Long = System.currentTimeMillis(),
    val isSent: Boolean = false,          // Relay network status
    val isRead: Boolean = false
)
