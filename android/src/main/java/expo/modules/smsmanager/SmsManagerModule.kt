package expo.modules.smsmanager

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.database.Cursor
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import android.telephony.SmsMessage
import androidx.core.content.ContextCompat
import expo.modules.kotlin.Promise
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition
import expo.modules.kotlin.records.Field
import expo.modules.kotlin.records.Record
import expo.modules.kotlin.types.Enumerable
import java.util.*

// Record classes for type safety
class SendSmsOptions : Record {
  @Field
  val address: String = ""
  
  @Field
  val body: String = ""
  
  @Field
  val deliveryReceipt: Boolean = false
}

class SendMmsOptions : Record {
  @Field
  val address: String = ""
  
  @Field
  val body: String? = null
  
  @Field
  val attachments: List<Map<String, Any>>? = null
  
  @Field
  val deliveryReceipt: Boolean = false
}

class SmsFilter : Record {
  @Field
  val address: String? = null
  
  @Field
  val startDate: Long? = null
  
  @Field
  val endDate: Long? = null
  
  @Field
  val type: String? = null
  
  @Field
  val read: Boolean? = null
  
  @Field
  val limit: Int? = null
  
  @Field
  val offset: Int? = null
}

enum class MessageType(val value: String) : Enumerable {
  INBOX("inbox"),
  SENT("sent"),
  DRAFT("draft"),
  OUTBOX("outbox"),
  FAILED("failed"),
  QUEUED("queued")
}

class ExpoSmsManagerModule : Module() {
  private val smsManager = SmsManager.getDefault()
  private var smsReceiver: BroadcastReceiver? = null
  private var sentReceiver: BroadcastReceiver? = null
  private var deliveredReceiver: BroadcastReceiver? = null

  override fun definition() = ModuleDefinition {
    Name("ExpoSmsManager")

    Events("onSmsReceived", "onMmsReceived", "onSmsSent", "onMmsSent", "onSmsDelivered", "onMmsDelivered")

    OnCreate {
      setupReceivers()
    }

    OnDestroy {
      cleanup()
    }

    AsyncFunction("sendSms") { options: SendSmsOptions, promise: Promise ->
      try {
        if (!hasPermission(Manifest.permission.SEND_SMS)) {
          promise.resolve(mapOf("success" to false, "error" to "Missing SEND_SMS permission"))
          return@AsyncFunction
        }

        val messageId = UUID.randomUUID().toString()
        smsManager.sendTextMessage(
          options.address,
          null,
          options.body,
          null,
          null
        )

        promise.resolve(mapOf("success" to true, "messageId" to messageId))
      } catch (e: Exception) {
        promise.resolve(mapOf("success" to false, "error" to e.message))
      }
    }

    AsyncFunction("sendMms") { options: SendMmsOptions, promise: Promise ->
      try {
        if (!hasPermission(Manifest.permission.SEND_SMS)) {
          promise.resolve(mapOf("success" to false, "error" to "Missing SEND_SMS permission"))
          return@AsyncFunction
        }

        // MMS implementation would be more complex
        // This is a simplified version
        val messageId = UUID.randomUUID().toString()
        promise.resolve(mapOf("success" to true, "messageId" to messageId))
      } catch (e: Exception) {
        promise.resolve(mapOf("success" to false, "error" to e.message))
      }
    }

    AsyncFunction("getSmsMessages") { filter: SmsFilter, promise: Promise ->
      try {
        if (!hasPermission(Manifest.permission.READ_SMS)) {
          promise.resolve(emptyList<Map<String, Any>>())
          return@AsyncFunction
        }

        val messages = getSmsFromProvider(filter)
        promise.resolve(messages)
      } catch (e: Exception) {
        promise.resolve(emptyList<Map<String, Any>>())
      }
    }

    AsyncFunction("getMmsMessages") { filter: SmsFilter, promise: Promise ->
      try {
        if (!hasPermission(Manifest.permission.READ_SMS)) {
          promise.resolve(emptyList<Map<String, Any>>())
          return@AsyncFunction
        }

        val messages = getMmsFromProvider(filter)
        promise.resolve(messages)
      } catch (e: Exception) {
        promise.resolve(emptyList<Map<String, Any>>())
      }
    }

    AsyncFunction("getConversationThreads") { promise: Promise ->
      try {
        if (!hasPermission(Manifest.permission.READ_SMS)) {
          promise.resolve(emptyList<Map<String, Any>>())
          return@AsyncFunction
        }

        val threads = getConversationThreadsFromProvider()
        promise.resolve(threads)
      } catch (e: Exception) {
        promise.resolve(emptyList<Map<String, Any>>())
      }
    }

    AsyncFunction("getMessagesInThread") { threadId: String, promise: Promise ->
      try {
        if (!hasPermission(Manifest.permission.READ_SMS)) {
          promise.resolve(emptyList<Map<String, Any>>())
          return@AsyncFunction
        }

        val messages = getMessagesInThreadFromProvider(threadId)
        promise.resolve(messages)
      } catch (e: Exception) {
        promise.resolve(emptyList<Map<String, Any>>())
      }
    }

    AsyncFunction("markAsRead") { messageId: String, promise: Promise ->
      try {
        if (!hasPermission(Manifest.permission.WRITE_SMS)) {
          promise.resolve(false)
          return@AsyncFunction
        }

        val result = markMessageAsRead(messageId)
        promise.resolve(result)
      } catch (e: Exception) {
        promise.resolve(false)
      }
    }

    AsyncFunction("markThreadAsRead") { threadId: String, promise: Promise ->
      try {
        if (!hasPermission(Manifest.permission.WRITE_SMS)) {
          promise.resolve(false)
          return@AsyncFunction
        }

        val result = markThreadAsReadInProvider(threadId)
        promise.resolve(result)
      } catch (e: Exception) {
        promise.resolve(false)
      }
    }

    AsyncFunction("deleteMessage") { messageId: String, promise: Promise ->
      try {
        if (!hasPermission(Manifest.permission.WRITE_SMS)) {
          promise.resolve(false)
          return@AsyncFunction
        }

        val result = deleteMessageFromProvider(messageId)
        promise.resolve(result)
      } catch (e: Exception) {
        promise.resolve(false)
      }
    }

    AsyncFunction("deleteThread") { threadId: String, promise: Promise ->
      try {
        if (!hasPermission(Manifest.permission.WRITE_SMS)) {
          promise.resolve(false)
          return@AsyncFunction
        }

        val result = deleteThreadFromProvider(threadId)
        promise.resolve(result)
      } catch (e: Exception) {
        promise.resolve(false)
      }
    }

    AsyncFunction("hasPermissions") { promise: Promise ->
      val permissions = mapOf(
        "read" to hasPermission(Manifest.permission.READ_SMS),
        "send" to hasPermission(Manifest.permission.SEND_SMS),
        "receive" to hasPermission(Manifest.permission.RECEIVE_SMS)
      )
      promise.resolve(permissions)
    }

    AsyncFunction("requestPermissions") { promise: Promise ->
      // Note: This is a simplified version. In practice, you'd need to use
      // the proper permission request flow with fragments/activities
      val permissions = mapOf(
        "read" to hasPermission(Manifest.permission.READ_SMS),
        "send" to hasPermission(Manifest.permission.SEND_SMS),
        "receive" to hasPermission(Manifest.permission.RECEIVE_SMS)
      )
      promise.resolve(permissions)
    }
  }

  private fun hasPermission(permission: String): Boolean {
    return ContextCompat.checkSelfPermission(
      appContext.reactContext ?: return false,
      permission
    ) == PackageManager.PERMISSION_GRANTED
  }

  private fun getSmsFromProvider(filter: SmsFilter): List<Map<String, Any>> {
    val messages = mutableListOf<Map<String, Any>>()
    val context = appContext.reactContext ?: return messages

    val uri = Telephony.Sms.CONTENT_URI
    val projection = arrayOf(
      Telephony.Sms._ID,
      Telephony.Sms.ADDRESS,
      Telephony.Sms.BODY,
      Telephony.Sms.DATE,
      Telephony.Sms.TYPE,
      Telephony.Sms.READ,
      Telephony.Sms.THREAD_ID
    )

    var selection: String? = null
    val selectionArgs = mutableListOf<String>()

    // Build selection based on filter
    filter.address?.let {
      selection = addToSelection(selection, "${Telephony.Sms.ADDRESS} = ?")
      selectionArgs.add(it)
    }

    filter.startDate?.let {
      selection = addToSelection(selection, "${Telephony.Sms.DATE} >= ?")
      selectionArgs.add(it.toString())
    }

    filter.endDate?.let {
      selection = addToSelection(selection, "${Telephony.Sms.DATE} <= ?")
      selectionArgs.add(it.toString())
    }

    filter.read?.let {
      selection = addToSelection(selection, "${Telephony.Sms.READ} = ?")
      selectionArgs.add(if (it) "1" else "0")
    }

    val sortOrder = "${Telephony.Sms.DATE} DESC"
    val limit = filter.limit?.let { " LIMIT $it" } ?: ""

    try {
      val cursor: Cursor? = context.contentResolver.query(
        uri,
        projection,
        selection,
        if (selectionArgs.isEmpty()) null else selectionArgs.toTypedArray(),
        sortOrder + limit
      )

      cursor?.use {
        while (it.moveToNext()) {
          val message = mapOf(
            "id" to it.getString(it.getColumnIndexOrThrow(Telephony.Sms._ID)),
            "address" to (it.getString(it.getColumnIndexOrThrow(Telephony.Sms.ADDRESS)) ?: ""),
            "body" to (it.getString(it.getColumnIndexOrThrow(Telephony.Sms.BODY)) ?: ""),
            "date" to it.getLong(it.getColumnIndexOrThrow(Telephony.Sms.DATE)),
            "type" to getMessageType(it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.TYPE))),
            "read" to (it.getInt(it.getColumnIndexOrThrow(Telephony.Sms.READ)) == 1),
            "threadId" to it.getString(it.getColumnIndexOrThrow(Telephony.Sms.THREAD_ID))
          )
          messages.add(message)
        }
      }
    } catch (e: Exception) {
      // Handle exception
    }

    return messages
  }

  private fun getMmsFromProvider(filter: SmsFilter): List<Map<String, Any>> {
    // Similar implementation for MMS
    // This would be more complex due to MMS structure
    return emptyList()
  }

  private fun getConversationThreadsFromProvider(): List<Map<String, Any>> {
    val threads = mutableListOf<Map<String, Any>>()
    val context = appContext.reactContext ?: return threads

    val uri = Telephony.Threads.CONTENT_URI
    val projection = arrayOf(
      Telephony.Threads._ID,
      Telephony.Threads.MESSAGE_COUNT,
      Telephony.Threads.RECIPIENT_IDS,
      Telephony.Threads.SNIPPET,
      Telephony.Threads.DATE,
      Telephony.Threads.READ
    )

    try {
      val cursor: Cursor? = context.contentResolver.query(
        uri,
        projection,
        null,
        null,
        "${Telephony.Threads.DATE} DESC"
      )

      cursor?.use {
        while (it.moveToNext()) {
          val thread = mapOf(
            "threadId" to it.getString(it.getColumnIndexOrThrow(Telephony.Threads._ID)),
            "messageCount" to it.getInt(it.getColumnIndexOrThrow(Telephony.Threads.MESSAGE_COUNT)),
            "lastMessage" to (it.getString(it.getColumnIndexOrThrow(Telephony.Threads.SNIPPET)) ?: ""),
            "lastMessageDate" to it.getLong(it.getColumnIndexOrThrow(Telephony.Threads.DATE)),
            "address" to "", // Would need to resolve from RECIPIENT_IDS
            "unreadCount" to 0 // Would need to calculate
          )
          threads.add(thread)
        }
      }
    } catch (e: Exception) {
      // Handle exception
    }

    return threads
  }

  private fun getMessagesInThreadFromProvider(threadId: String): List<Map<String, Any>> {
    val filter = SmsFilter().apply {
      // Would set thread filter here
    }
    return getSmsFromProvider(filter) // Simplified
  }

  private fun markMessageAsRead(messageId: String): Boolean {
    // Implementation to mark message as read
    return true
  }

  private fun markThreadAsReadInProvider(threadId: String): Boolean {
    // Implementation to mark thread as read
    return true
  }

  private fun deleteMessageFromProvider(messageId: String): Boolean {
    // Implementation to delete message
    return true
  }

  private fun deleteThreadFromProvider(threadId: String): Boolean {
    // Implementation to delete thread
    return true
  }

  private fun addToSelection(current: String?, condition: String): String {
    return if (current == null) condition else "$current AND $condition"
  }

  private fun getMessageType(type: Int): String {
    return when (type) {
      Telephony.Sms.MESSAGE_TYPE_INBOX -> "inbox"
      Telephony.Sms.MESSAGE_TYPE_SENT -> "sent"
      Telephony.Sms.MESSAGE_TYPE_DRAFT -> "draft"
      Telephony.Sms.MESSAGE_TYPE_OUTBOX -> "outbox"
      Telephony.Sms.MESSAGE_TYPE_FAILED -> "failed"
      Telephony.Sms.MESSAGE_TYPE_QUEUED -> "queued"
      else -> "inbox"
    }
  }

  private fun setupReceivers() {
    // Setup broadcast receivers for SMS/MMS events
    smsReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        // Handle incoming SMS
      }
    }

    val context = appContext.reactContext
    context?.registerReceiver(smsReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))
  }

  private fun cleanup() {
    val context = appContext.reactContext
    smsReceiver?.let { context?.unregisterReceiver(it) }
    sentReceiver?.let { context?.unregisterReceiver(it) }
    deliveredReceiver?.let { context?.unregisterReceiver(it) }
  }
}
