

package com.example.remoteserver

import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.CookieHandler
import java.net.CookieManager
import java.net.CookiePolicy
import java.net.HttpURLConnection
import java.net.URL

data class StatusResult(
    val internet: Boolean,
    val battery: Int?,
    val charging: Boolean
)

data class ApiResult(
    val success: Boolean,
    val message: String
)

object RemoteApi {

    /*
     * CHANGE THIS TO YOUR CURRENT NGROK URL
     *
     * Example:
     * https://abc123.ngrok-free.app
     */
    private const val BASE_URL =
        "ngrok link"

    private val cookieManager = CookieManager(
        null,
        CookiePolicy.ACCEPT_ALL
    )

    init {
        CookieHandler.setDefault(cookieManager)
    }

    private fun connection(
        endpoint: String,
        method: String
    ): HttpURLConnection {

        val url = URL(BASE_URL + endpoint)

        return (url.openConnection() as HttpURLConnection).apply {
            requestMethod = method
            connectTimeout = 10000
            readTimeout = 15000
            useCaches = false

            setRequestProperty(
                "Accept",
                "application/json"
            )
        }
    }

    private fun readResponse(
        connection: HttpURLConnection
    ): String {

        val stream =
            if (connection.responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

        return BufferedReader(
            InputStreamReader(stream)
        ).use {
            it.readText()
        }
    }

    fun checkAuthentication(): Boolean {

        val connection =
            connection("/api/auth/status", "GET")

        return try {

            connection.connect()

            connection.responseCode in 200..299

        } finally {
            connection.disconnect()
        }
    }

    fun login(key: String): ApiResult {

        val connection =
            connection("/api/auth/login", "POST")

        return try {

            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            val json = JSONObject()
            json.put("key", key)

            OutputStreamWriter(
                connection.outputStream
            ).use {
                it.write(json.toString())
            }

            val response = readResponse(connection)

            if (connection.responseCode in 200..299) {

                ApiResult(
                    true,
                    "Authentication successful"
                )

            } else {

                val message = try {
                    JSONObject(response)
                        .optString(
                            "error",
                            "Authentication failed"
                        )
                } catch (e: Exception) {
                    "Authentication failed"
                }

                ApiResult(
                    false,
                    message
                )
            }

        } finally {
            connection.disconnect()
        }
    }

    fun getStatus(): StatusResult {

        val connection =
            connection("/api/status", "GET")

        return try {

            connection.connect()

            val response =
                readResponse(connection)

            if (connection.responseCode !in 200..299) {
                throw Exception("Unable to get status")
            }

            val json = JSONObject(response)

            val batteryValue =
                if (
                    json.isNull("battery")
                ) {
                    null
                } else {
                    json.optInt("battery")
                }

            StatusResult(
                internet =
                    json.optBoolean(
                        "internet",
                        false
                    ),

                battery =
                    batteryValue,

                charging =
                    json.optBoolean(
                        "charging",
                        false
                    )
            )

        } finally {
            connection.disconnect()
        }
    }

    fun performAction(
        action: String
    ): ApiResult {

        val connection =
            connection("/api/action", "POST")

        return try {

            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            val json = JSONObject()
            json.put("action", action)

            OutputStreamWriter(
                connection.outputStream
            ).use {
                it.write(json.toString())
            }

            val response =
                readResponse(connection)

            if (connection.responseCode in 200..299) {

                val message =
                    try {
                        JSONObject(response)
                            .optString(
                                "message",
                                "Action completed"
                            )
                    } catch (e: Exception) {
                        "Action completed"
                    }

                ApiResult(
                    true,
                    message
                )

            } else {

                val message =
                    try {
                        JSONObject(response)
                            .optString(
                                "error",
                                "Request failed"
                            )
                    } catch (e: Exception) {
                        "Request failed"
                    }

                ApiResult(
                    false,
                    message
                )
            }

        } finally {
            connection.disconnect()
        }
    }

    fun getScreenshot(): ByteArray {

        val connection =
            connection(
                "/api/screenshot",
                "GET"
            )

        return try {

            connection.connect()

            if (connection.responseCode !in 200..299) {

                val error =
                    try {
                        readResponse(connection)
                    } catch (e: Exception) {
                        "Screenshot request failed"
                    }

                throw Exception(error)
            }

            connection.inputStream.use {
                it.readBytes()
            }

        } finally {
            connection.disconnect()
        }
    }
}