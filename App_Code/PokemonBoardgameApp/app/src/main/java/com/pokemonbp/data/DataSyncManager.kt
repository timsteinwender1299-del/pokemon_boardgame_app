package com.pokemonbp.data

import android.content.Context
import android.os.Handler
import android.os.Looper
import java.io.File
import java.net.HttpURLConnection
import java.net.URL

object DataSyncManager {

    private const val BASE = "https://raw.githubusercontent.com/timsteinwender1299-del/pokemon_boardgame_app/main/"

    private val filesToSync = listOf(
        "Data/Trainers/Roark_Veit.txt",
        "Data/Trainers/Gardenia_Silvana.txt",
        "Data/Trainers/Maylene_Hilda.txt",
        "Data/Trainers/CrasherWake_Marinus.txt",
        "Data/Trainers/Fantina_Lamina.txt",
        "Data/Trainers/Byron_Adam.txt",
        "Data/Trainers/Candice_Frida.txt",
        "Data/Trainers/Volkner.txt",
        "Data/Trainers/Champions.txt",
        "Data/RoutesNormal.txt",
        "Data/RoutesLegendary.txt",
        "Data/Trainers/GalacticMars.txt",
        "Data/Trainers/GalacticJupiter.txt",
        "Data/Trainers/GalacticSaturn.txt",
        "Data/Trainers/GalacticCyrus.txt"
    )

    fun syncAll(context: Context, onComplete: (updated: Int, failed: Int) -> Unit) {
        Thread {
            var updated = 0
            var failed = 0
            for (remotePath in filesToSync) {
                try {
                    val text = download(BASE + remotePath)
                    val localFile = localFileFor(context, remotePath)
                    localFile.parentFile?.mkdirs()
                    localFile.writeText(text)
                    updated++
                } catch (e: Exception) {
                    failed++
                }
            }
            Handler(Looper.getMainLooper()).post { onComplete(updated, failed) }
        }.start()
    }

    private fun download(url: String): String {
        val conn = URL(url).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 15_000
        return try {
            conn.inputStream.bufferedReader().readText()
        } finally {
            conn.disconnect()
        }
    }

    fun localFileFor(context: Context, remotePath: String): File {
        val relative = remotePath.removePrefix("Data/")
        return File(context.filesDir, "sync/$relative")
    }

    fun trainerDir(context: Context) = File(context.filesDir, "sync/Trainers")
    fun routesNormalFile(context: Context) = File(context.filesDir, "sync/RoutesNormal.txt")
    fun routesLegendaryFile(context: Context) = File(context.filesDir, "sync/RoutesLegendary.txt")
}
