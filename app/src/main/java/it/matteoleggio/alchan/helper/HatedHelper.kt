package it.matteoleggio.alchan.helper

import android.annotation.SuppressLint
import com.google.gson.Gson
import it.matteoleggio.alchan.data.response.Hated
import it.matteoleggio.alchan.data.response.HatedCharacter
import it.matteoleggio.alchan.ui.profile.bio.BioViewModel
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread

class HatedHelper(val bioOG: String?) {
    @SuppressLint("NewApi")
    fun getHatedCharactersSelf(): List<HatedCharacter> {
        val bio = bioOG?.split("\n")
        val hatedJsonEncoded = bio?.get(bio.size - 1)?.drop(3)?.dropLast(1)
        println("0: $hatedJsonEncoded")
        try {
            val hatedJson = String(Base64.getDecoder().decode(hatedJsonEncoded), Charsets.UTF_8)
            println("1: $hatedJson")
            return Gson().fromJson(hatedJson, Hated().javaClass).characters!!
        } catch (e: java.lang.IllegalArgumentException) {
            return listOf<HatedCharacter>()
        }
    }

    @SuppressLint("NewApi")
    fun removeHatedCharacter(character: HatedCharacter, accessToken: String) {
        val bio = bioOG?.split("\n")
        val hatedJsonEncoded = bio?.get(bio.size - 1)?.drop(3)?.dropLast(1)
        println("0: $hatedJsonEncoded")
        val hatedJson = String(Base64.getDecoder().decode(hatedJsonEncoded), Charsets.UTF_8)
        println("1: $hatedJson")
        val hated = Gson().fromJson(hatedJson, Hated().javaClass)
        val newHatedCharacters = mutableListOf<HatedCharacter>()
        for (h in hated.characters!!) {
            if (h == null) {
                continue
            }
            if (h.id == character.id) {
                continue
            }
            newHatedCharacters.add(h)
        }
        val newHated = Hated(characters = newHatedCharacters.toList())
        var newHatedJson = "{\"characters\": ["
        for (h in newHated.characters!!) {
            newHatedJson = "$newHatedJson{\"id\": ${h.id}, \"image\": \"${h.image}\"}, "
        }
        newHatedJson = "$newHatedJson]}"
        val newAboutB64 = String(Base64.getEncoder().encode(newHatedJson.toByteArray()), Charsets.UTF_8)
        val newAbout = bio?.dropLast(1)?.joinToString("\n") + "\n[](" + newAboutB64 + ")"
        val json = JSONObject()
        json.put("newAbout", newAbout)
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("query",
                  "mutation(${'$'}newAbout: String) {\n" +
                        "   UpdateUser(about: ${'$'}newAbout) {\n" +
                        "       about\n" +
                        "   }\n" +
                        "}"
            )
            .addFormDataPart("variables", json.toString())
            .build()
        var response: Response? = null
        thread(start = true) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder()
                .url(Constant.ANILIST_API_URL)
                .post(body)
                .header("Authorization", "Bearer ${accessToken}")
                .build()
            response = okHttpClient.newCall(request).execute()
        }.join()
        println(response?.code)
        println(response?.body?.string().toString())
        Constant.user_about = newAbout
    }

    @SuppressLint("NewApi")
    fun addHatedCharacter(id: Int, image: String, accessToken: String) {
        val bio = bioOG?.split("\n")
        val hatedJsonEncoded = bio?.get(bio.size - 1)?.drop(3)?.dropLast(1)
        println("0: $hatedJsonEncoded")
        var hatedJson: String? = null
        var hated: Hated? = Hated(listOf<HatedCharacter>())
        var firstTime = false
        try {
            hatedJson = String(Base64.getDecoder().decode(hatedJsonEncoded), Charsets.UTF_8)
            println("1: $hatedJson")
            hated = Gson().fromJson(hatedJson, Hated().javaClass)
        } catch (e: Exception) {
            firstTime = true
        }
        val newHatedCharacters = mutableListOf<HatedCharacter>()
        for (h in hated?.characters!!) {
            newHatedCharacters.add(h)
        }
        newHatedCharacters.add(HatedCharacter(image, id))
        val newHated = Hated(characters = newHatedCharacters.toList())
        var newHatedJson = "{\"characters\": ["
        for (h in newHated.characters!!) {
            if (h != null) {
                newHatedJson = "$newHatedJson{\"id\": ${h.id}, \"image\": \"${h.image}\"}, "
            }
        }
        newHatedJson = "$newHatedJson]}"
        val newAboutB64 = String(Base64.getEncoder().encode(newHatedJson.toByteArray()), Charsets.UTF_8)
        var newAbout = ""
        newAbout = if (!firstTime) {
            bio?.dropLast(1)?.joinToString("\n") + "\n[](" + newAboutB64 + ")"
        } else {
            "\n[]($newAboutB64)"
        }
        val json = JSONObject()
        json.put("newAbout", newAbout)
        val body = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("query",
                  "mutation(${'$'}newAbout: String) {\n" +
                        "   UpdateUser(about: ${'$'}newAbout) {\n" +
                        "       about\n" +
                        "   }\n" +
                        "}"
            )
            .addFormDataPart("variables", json.toString())
            .build()
        var response: Response? = null
        thread(start = true) {
            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(20, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .build()
            val request = Request.Builder()
                .url(Constant.ANILIST_API_URL)
                .post(body)
                .header("Authorization", "Bearer ${accessToken}")
                .build()
            response = okHttpClient.newCall(request).execute()
        }.join()
        println(response?.code)
        println(response?.body?.string().toString())
        Constant.user_about = newAbout
    }
}