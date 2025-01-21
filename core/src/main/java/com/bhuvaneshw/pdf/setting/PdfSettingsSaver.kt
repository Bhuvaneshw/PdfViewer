package com.bhuvaneshw.pdf.setting

interface PdfSettingsSaver {
    fun save(key: String, value: String)
    fun save(key: String, value: Float)
    fun save(key: String, value: Int)
    fun save(key: String, value: Boolean)

    fun apply()

    fun getString(key: String, default: String): String
    fun getFloat(key: String, default: Float): Float
    fun getInt(key: String, default: Int): Int
    fun getBoolean(key: String, default: Boolean): Boolean

    fun remove(key: String)
    fun clearAll()
}
