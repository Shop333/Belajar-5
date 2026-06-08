package com.example.data.crypto

import android.util.Base64
import java.security.*
import java.security.spec.ECGenParameterSpec
import java.security.spec.X509EncodedKeySpec
import javax.crypto.Cipher
import javax.crypto.KeyAgreement
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

object Web3Crypto {

    // Menghasilkan KeyPair Elliptic Curve (EC) secara acak untuk dompet/identitas baru
    fun generateKeyPair(): KeyPair {
        val keyPairGenerator = KeyPairGenerator.getInstance("EC")
        val ecSpec = ECGenParameterSpec("secp256r1") // Didukung penuh oleh JDK/Android native
        keyPairGenerator.initialize(ecSpec, SecureRandom())
        return keyPairGenerator.generateKeyPair()
    }

    // Mengubah public key menjadi string Base64 yang mudah ditransmisikan
    fun publicKeyToString(publicKey: PublicKey): String {
        return Base64.encodeToString(publicKey.encoded, Base64.NO_WRAP)
    }

    // Merekonstruksi public key dari string Base64
    fun stringToPublicKey(keyStr: String): PublicKey {
        val keyBytes = Base64.decode(keyStr, Base64.NO_WRAP)
        val keyFactory = KeyFactory.getInstance("EC")
        return keyFactory.generatePublic(X509EncodedKeySpec(keyBytes))
    }

    // Menghasilkan Kunci Simetris Bersama (Shared AES Key) menggunakan Elliptic Curve Diffie-Hellman (ECDH)
    fun generateSharedSecret(privateKey: PrivateKey, publicKey: PublicKey): SecretKeySpec {
        val keyAgreement = KeyAgreement.getInstance("ECDH")
        keyAgreement.init(privateKey)
        keyAgreement.doPhase(publicKey, true)
        val sharedSecret = keyAgreement.generateSecret()
        
        // Melakukan key derivation sederhana dengan hashing SHA-256 untuk mendapatkan kunci AES 256-bit yang tangguh
        val digest = MessageDigest.getInstance("SHA-256")
        val aesKeyBytes = digest.digest(sharedSecret)
        return SecretKeySpec(aesKeyBytes, "AES")
    }

    // Dienkripsi di sisi klien menggunakan AES-GCM-NoPadding
    fun encryptMessage(plainText: String, secretKey: SecretKeySpec): EncryptedData {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = ByteArray(12) // Standar GCM IV berukuran 12 byte
        SecureRandom().nextBytes(iv)
        val parameterSpec = GCMParameterSpec(128, iv) // Auth tag length 128-bit
        
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec)
        val cipherTextBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        
        val cipherTextHex = bytesToHex(cipherTextBytes)
        val ivHex = bytesToHex(iv)
        
        return EncryptedData(cipherTextHex, ivHex)
    }

    // Didekripsi di sisi klien menggunakan AES-GCM-NoPadding
    fun decryptMessage(cipherTextHex: String, ivHex: String, secretKey: SecretKeySpec): String {
        return try {
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            val iv = hexToBytes(ivHex)
            val cipherText = hexToBytes(cipherTextHex)
            val parameterSpec = GCMParameterSpec(128, iv)
            
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec)
            val decryptedBytes = cipher.doFinal(cipherText)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            "[Gagal Dekripsi: Kunci Tidak Sesuai atau Rusak]"
        }
    }

    // Helper konversi Byte <-> Hex
    private fun bytesToHex(bytes: ByteArray): String {
        val hexChars = "0123456789ABCDEF"
        val result = StringBuilder(bytes.size * 2)
        for (b in bytes) {
            val i = b.toInt() and 0xFF
            result.append(hexChars[i shr 4])
            result.append(hexChars[i and 0x0F])
        }
        return result.toString()
    }

    private fun hexToBytes(hex: String): ByteArray {
        val len = hex.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((Character.digit(hex[i], 16) shl 4) + Character.digit(hex[i + 1], 16)).toByte()
            i += 2
        }
        return data
    }
}

data class EncryptedData(
    val cipherTextHex: String,
    val ivHex: String
)
