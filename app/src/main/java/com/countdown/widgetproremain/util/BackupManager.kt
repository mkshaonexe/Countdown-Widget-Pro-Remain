package com.countdown.widgetproremain.util

import com.countdown.widgetproremain.data.model.CountdownEvent
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject

/**
 * Utility class for backing up and restoring countdown events via JSON
 */
object BackupManager {
    
    private const val KEY_VERSION = "version"
    private const val KEY_TIMESTAMP = "backupTimestamp"
    private const val KEY_EVENTS = "events"
    private const val BACKUP_VERSION = 1
    
    // CountdownEvent field keys
    private const val KEY_ID = "id"
    private const val KEY_TITLE = "title"
    private const val KEY_TARGET_DATE = "targetDate"
    private const val KEY_IS_ALL_DAY = "isAllDay"
    private const val KEY_INCLUDE_TIME = "includeTime"
    private const val KEY_IS_COUNT_UP = "isCountUp"
    private const val KEY_RECURRENCE = "recurrence"
    private const val KEY_COLOR = "color"
    private const val KEY_NOTES = "notes"
    private const val KEY_CREATED_AT = "createdAt"
    private const val KEY_IS_PINNED = "isPinned"
    
    /**
     * Exports a list of CountdownEvents to a JSON string
     * @param events List of events to export
     * @return JSON string representation of the backup
     */
    fun exportToJson(events: List<CountdownEvent>): String {
        val rootObject = JSONObject()
        rootObject.put(KEY_VERSION, BACKUP_VERSION)
        rootObject.put(KEY_TIMESTAMP, System.currentTimeMillis())
        
        val eventsArray = JSONArray()
        events.forEach { event ->
            val eventObject = JSONObject().apply {
                put(KEY_ID, event.id)
                put(KEY_TITLE, event.title)
                put(KEY_TARGET_DATE, event.targetDate)
                put(KEY_IS_ALL_DAY, event.isAllDay)
                put(KEY_INCLUDE_TIME, event.includeTime)
                put(KEY_IS_COUNT_UP, event.isCountUp)
                put(KEY_RECURRENCE, event.recurrence)
                put(KEY_COLOR, event.color)
                put(KEY_NOTES, event.notes ?: "")
                put(KEY_CREATED_AT, event.createdAt)
                put(KEY_IS_PINNED, event.isPinned)
            }
            eventsArray.put(eventObject)
        }
        
        rootObject.put(KEY_EVENTS, eventsArray)
        return rootObject.toString(2) // Pretty print with 2-space indent
    }
    
    /**
     * Imports CountdownEvents from a JSON string
     * @param jsonString JSON backup string
     * @return Result containing list of events or error message
     */
    fun importFromJson(jsonString: String): Result<List<CountdownEvent>> {
        return try {
            val rootObject = JSONObject(jsonString)
            
            // Validate backup version
            val version = rootObject.optInt(KEY_VERSION, 0)
            if (version != BACKUP_VERSION) {
                return Result.failure(Exception("Unsupported backup version: $version"))
            }
            
            val eventsArray = rootObject.getJSONArray(KEY_EVENTS)
            val events = mutableListOf<CountdownEvent>()
            
            for (i in 0 until eventsArray.length()) {
                val eventObject = eventsArray.getJSONObject(i)
                
                try {
                    val event = CountdownEvent(
                        id = 0, // Reset ID to allow auto-generation on import
                        title = eventObject.getString(KEY_TITLE),
                        targetDate = eventObject.getLong(KEY_TARGET_DATE),
                        isAllDay = eventObject.getBoolean(KEY_IS_ALL_DAY),
                        includeTime = eventObject.getBoolean(KEY_INCLUDE_TIME),
                        isCountUp = eventObject.getBoolean(KEY_IS_COUNT_UP),
                        recurrence = eventObject.getString(KEY_RECURRENCE),
                        color = eventObject.getInt(KEY_COLOR),
                        notes = eventObject.optString(KEY_NOTES).ifEmpty { null },
                        createdAt = eventObject.getLong(KEY_CREATED_AT),
                        isPinned = eventObject.getBoolean(KEY_IS_PINNED)
                    )
                    events.add(event)
                } catch (e: JSONException) {
                    // Skip malformed events but continue processing
                    android.util.Log.w("BackupManager", "Skipping malformed event at index $i: ${e.message}")
                }
            }
            
            if (events.isEmpty()) {
                Result.failure(Exception("No valid events found in backup"))
            } else {
                Result.success(events)
            }
            
        } catch (e: JSONException) {
            Result.failure(Exception("Invalid backup file format: ${e.message}"))
        } catch (e: Exception) {
            Result.failure(Exception("Failed to import backup: ${e.message}"))
        }
    }
    
    /**
     * Validates that a JSON string is a valid backup file
     * @param jsonString JSON string to validate
     * @return true if valid, false otherwise
     */
    fun isValidBackupFile(jsonString: String): Boolean {
        return try {
            val rootObject = JSONObject(jsonString)
            rootObject.has(KEY_VERSION) && 
            rootObject.has(KEY_EVENTS) &&
            rootObject.getJSONArray(KEY_EVENTS).length() >= 0
        } catch (e: Exception) {
            false
        }
    }
}
