package ru.zapasli.app.data.auth

import android.annotation.SuppressLint
import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ru.zapasli.app.domain.auth.AuthSession
import ru.zapasli.app.domain.auth.AuthUser
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

internal interface SessionStorage {
    fun read(): AuthSession?

    fun write(session: AuthSession)

    fun clear()
}

@Singleton
internal class SecureSessionStorage @Inject constructor(
    @ApplicationContext context: Context,
    private val json: Json,
) : SessionStorage {
    private val preferences = context.getSharedPreferences(PREFERENCES_NAME, Context.MODE_PRIVATE)

    @Synchronized
    override fun read(): AuthSession? {
        val envelope = preferences.getString(SESSION_KEY, null) ?: return null
        return runCatching {
            val (encodedIv, encodedCiphertext) = envelope.split(SEPARATOR, limit = 2)
            val cipher = Cipher.getInstance(TRANSFORMATION).apply {
                init(
                    Cipher.DECRYPT_MODE,
                    getOrCreateKey(),
                    GCMParameterSpec(GCM_TAG_LENGTH_BITS, Base64.decode(encodedIv, BASE64_FLAGS)),
                )
            }
            val plaintext = cipher.doFinal(Base64.decode(encodedCiphertext, BASE64_FLAGS))
                .toString(Charsets.UTF_8)
            json.decodeFromString<StoredSession>(plaintext).toDomain()
        }.getOrElse {
            clear()
            null
        }
    }

    @Synchronized
    @SuppressLint("UseKtx") // commit() result is intentionally verified before returning.
    override fun write(session: AuthSession) {
        val cipher = Cipher.getInstance(TRANSFORMATION).apply {
            init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        }
        val plaintext = json.encodeToString(StoredSession.fromDomain(session)).toByteArray()
        val envelope = listOf(
            Base64.encodeToString(cipher.iv, BASE64_FLAGS),
            Base64.encodeToString(cipher.doFinal(plaintext), BASE64_FLAGS),
        ).joinToString(SEPARATOR)
        check(preferences.edit().putString(SESSION_KEY, envelope).commit()) {
            "Could not persist the protected session"
        }
    }

    @Synchronized
    @SuppressLint("UseKtx") // apply() is intentional for best-effort removal during recovery.
    override fun clear() {
        preferences.edit().remove(SESSION_KEY).apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }

        return KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, ANDROID_KEYSTORE).run {
            init(
                KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .build(),
            )
            generateKey()
        }
    }

    private companion object {
        const val PREFERENCES_NAME = "zapasli_secure_session"
        const val SESSION_KEY = "encrypted_session"
        const val KEY_ALIAS = "zapasli_auth_session_v1"
        const val ANDROID_KEYSTORE = "AndroidKeyStore"
        const val TRANSFORMATION = "AES/GCM/NoPadding"
        const val GCM_TAG_LENGTH_BITS = 128
        const val BASE64_FLAGS = Base64.NO_WRAP
        const val SEPARATOR = "."
    }
}

@Serializable
private data class StoredSession(
    val userId: String,
    val displayName: String,
    val locale: String,
    val accessToken: String,
    val accessTokenExpiresAt: String,
    val refreshToken: String,
) {
    fun toDomain() = AuthSession(
        user = AuthUser(userId, displayName, locale),
        accessToken = accessToken,
        accessTokenExpiresAt = accessTokenExpiresAt,
        refreshToken = refreshToken,
    )

    companion object {
        fun fromDomain(session: AuthSession) = StoredSession(
            userId = session.user.id,
            displayName = session.user.displayName,
            locale = session.user.locale,
            accessToken = session.accessToken,
            accessTokenExpiresAt = session.accessTokenExpiresAt,
            refreshToken = session.refreshToken,
        )
    }
}
