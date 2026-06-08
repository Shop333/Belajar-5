package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.model.Contact
import com.example.data.model.Message
import kotlinx.coroutines.flow.Flow

class ChatRepository(private val chatDao: ChatDao) {

    val allContacts: Flow<List<Contact>> = chatDao.getAllContacts()

    fun getMessagesWithContact(myAddress: String, contactAddress: String): Flow<List<Message>> {
        return chatDao.getMessagesWithContact(myAddress, contactAddress)
    }

    suspend fun insertContact(contact: Contact) {
        chatDao.insertContact(contact)
    }

    suspend fun updateContact(contact: Contact) {
        chatDao.updateContact(contact)
    }

    suspend fun insertMessage(message: Message) {
        chatDao.insertMessage(message)
    }

    suspend fun getContactByAddress(address: String): Contact? {
        return chatDao.getContactByAddress(address)
    }

    suspend fun deleteContactByAddress(address: String) {
        chatDao.deleteContactByAddress(address)
    }

    suspend fun clearMessagesWithContact(myAddress: String, contactAddress: String) {
        chatDao.clearMessagesWithContact(myAddress, contactAddress)
    }
}
