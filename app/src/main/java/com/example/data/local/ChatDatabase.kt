package com.example.data.local

import androidx.room.*
import com.example.data.model.Contact
import com.example.data.model.Message
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatDao {

    // KONTOD (CONTACTS) CRUD
    @Query("SELECT * FROM contacts ORDER BY lastMessageTimestamp DESC")
    fun getAllContacts(): Flow<List<Contact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertContact(contact: Contact)

    @Update
    suspend fun updateContact(contact: Contact)

    @Query("SELECT * FROM contacts WHERE walletAddress = :address LIMIT 1")
    suspend fun getContactByAddress(address: String): Contact?

    @Query("DELETE FROM contacts WHERE walletAddress = :address")
    suspend fun deleteContactByAddress(address: String)

    // PESAN (MESSAGES) CRUD
    @Query("SELECT * FROM messages WHERE (senderAddress = :myAddress AND receiverAddress = :contactAddress) OR (senderAddress = :contactAddress AND receiverAddress = :myAddress) ORDER BY timestamp ASC")
    fun getMessagesWithContact(myAddress: String, contactAddress: String): Flow<List<Message>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: Message)

    @Query("DELETE FROM messages WHERE (senderAddress = :myAddress AND receiverAddress = :contactAddress) OR (senderAddress = :contactAddress AND receiverAddress = :myAddress)")
    suspend fun clearMessagesWithContact(myAddress: String, contactAddress: String)

    @Query("SELECT * FROM messages ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMessage(): Message?
}

@Database(entities = [Contact::class, Message::class], version = 1, exportSchema = false)
abstract class ChatDatabase : RoomDatabase() {
    abstract fun chatDao(): ChatDao
}
