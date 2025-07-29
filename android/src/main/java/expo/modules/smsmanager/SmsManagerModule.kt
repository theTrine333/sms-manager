package expo.modules.smsmanager

import android.content.ContentResolver
import android.content.UriMatcher
import android.net.Uri
import android.provider.ContactsContract
import android.telephony.SmsManager as AndroidSmsManager
import android.database.Cursor
import com.facebook.react.bridge.*
import expo.modules.kotlin.modules.Module
import expo.modules.kotlin.modules.ModuleDefinition

class SmsManagerModule : Module() {
  override fun definition() = ModuleDefinition {
    Name("SmsManager")

    Function("getAllSms") {
      val result = Arguments.createArray()
      val cursor = appContext.reactContext?.contentResolver?.query(
        Uri.parse("content://sms"), null, null, null, "date DESC"
      )
      cursor?.use {
        while (it.moveToNext()) {
          val sms = mapFromCursor(it)
          result.pushMap(sms)
        }
      }
      result
    }

    Function("getRecentThreads") {
      val seen = mutableSetOf<String>()
      val result = Arguments.createArray()
      val cursor = appContext.reactContext?.contentResolver?.query(
        Uri.parse("content://sms"),
        arrayOf("_id", "thread_id", "address", "body", "date", "type"),
        null, null, "date DESC"
      )
      cursor?.use {
        while (it.moveToNext()) {
          val threadId = it.getString(it.getColumnIndexOrThrow("thread_id"))
          if (threadId != null && !seen.contains(threadId)) {
            seen.add(threadId)
            val sms = mapFromCursor(it)
            sms.putString("contactName", getContactName(it.getString(it.getColumnIndexOrThrow("address"))))
            result.pushMap(sms)
          }
        }
      }
      result
    }

    Function("sendSms") { number: String, message: String ->
      AndroidSmsManager.getDefault().sendTextMessage(number, null, message, null, null)
      true
    }

    Function("deleteSms") { id: String ->
      appContext.reactContext?.contentResolver?.delete(Uri.parse("content://sms/$id"), null, null) ?: 0
    }
  }

  private fun mapFromCursor(cursor: Cursor): WritableMap {
    val m = Arguments.createMap()
    m.putString("id", cursor.getString(cursor.getColumnIndexOrThrow("_id")))
    m.putString("threadId", cursor.getString(cursor.getColumnIndexOrThrow("thread_id")))
    m.putString("address", cursor.getString(cursor.getColumnIndexOrThrow("address")))
    m.putString("body", cursor.getString(cursor.getColumnIndexOrThrow("body")))
    m.putDouble("date", cursor.getLong(cursor.getColumnIndexOrThrow("date")).toDouble())
    m.putInt("type", cursor.getInt(cursor.getColumnIndexOrThrow("type")))
    return m
  }

  private fun getContactName(address: String?): String? {
    if (address.isNullOrBlank()) return null
    val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(address))
    val cursor = appContext.reactContext?.contentResolver?.query(uri, arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME), null, null, null)
    cursor?.use {
      if (it.moveToFirst()) {
        val name = it.getString(0)
        if (!name.isNullOrEmpty()) return name
      }
    }
    return null
  }
}
