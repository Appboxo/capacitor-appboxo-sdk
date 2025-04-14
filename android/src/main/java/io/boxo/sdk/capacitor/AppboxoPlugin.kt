package io.boxo.sdk.capacitor

import android.os.Handler
import android.os.Looper
import io.boxo.data.models.MiniappData
import io.boxo.js.params.CustomEvent
import io.boxo.js.params.PaymentData
import io.boxo.sdk.Boxo
import io.boxo.sdk.Config
import io.boxo.sdk.Miniapp
import io.boxo.sdk.MiniappConfig
import io.boxo.sdk.MiniappListCallback
import io.boxo.ui.main.BoxoActivity
import com.getcapacitor.JSArray
import com.getcapacitor.JSObject
import com.getcapacitor.Plugin
import com.getcapacitor.PluginCall
import com.getcapacitor.PluginMethod
import com.getcapacitor.annotation.CapacitorPlugin

@CapacitorPlugin(name = "Appboxo")
class AppboxoPlugin : Plugin(), Miniapp.LifecycleListener,
    Miniapp.CustomEventListener, Miniapp.AuthListener,
    Miniapp.PaymentEventListener {

    private var handler: Handler? = null

    override fun load() {
        super.load()
        Boxo.init(bridge.activity.application)
        handler = Handler(Looper.getMainLooper())
    }

    @PluginMethod
    fun setConfig(call: PluginCall) {
        val clientId = call.getString("clientId")!!
        val userId = call.getString("userId") ?: ""
        val language = call.getString("language") ?: "en"
        val sandboxMode = call.getBoolean("sandboxMde", false)!!
        val enableMultitaskMode = call.getBoolean("enableMultitaskMode", false)!!
        val theme = call.getString("theme", "system")!!
        val isDebug = call.getBoolean("isDebug", false)!!
        val showPermissionsPage = call.getBoolean("showPermissionsPage", true)!!
        val showClearCache = call.getBoolean("showClearCache", true)!!
        val showAboutPage = call.getBoolean("showAboutPage", true)!!
        val miniappSettingsExpirationTime = call.getInt("miniappSettingsExpirationTime", 60)!!
        val globalTheme: Config.Theme = when (theme) {
            "light" -> Config.Theme.LIGHT
            "dark" -> Config.Theme.DARK
            else -> Config.Theme.SYSTEM
        }
        Boxo
            .setConfig(
                Config.Builder()
                    .setClientId(clientId)
                    .setUserId(userId)
                    .sandboxMode(sandboxMode)
                    .multitaskMode(enableMultitaskMode)
                    .setTheme(globalTheme)
                    .setLanguage(language)
                    .permissionsPage(showPermissionsPage)
                    .showClearCache(showClearCache)
                    .showAboutPage(showAboutPage)
                    .setMiniappSettingsExpirationTime(miniappSettingsExpirationTime)
                    .debug(isDebug)
                    .build()
            )
    }

    @PluginMethod
    fun openMiniapp(call: PluginCall) {
        val appId = call.getString("appId")!!
        val data = call.getObject("data")
        val theme = call.getString("theme")
        val extraUrlParams = call.getObject("extraUrlParams")
        val urlSuffix = call.getString("urlSuffix")
        val colors = call.getObject("colors")
        val enableSplash = call.getBoolean("enableSplash")
        val saveState = call.getBoolean("saveState") ?: true
        handler?.post {
            val miniapp: Miniapp = Boxo.getMiniapp(appId)
                .setCustomEventListener(this)
                .setPaymentEventListener(this)
                .setAuthListener(this)
                .setLifecycleListener(this)
            if (data != null) miniapp.setData(data.toMap())
            val configBuilder = MiniappConfig.Builder()
            if (theme != null) {
                val miniappTheme: Config.Theme? = when (theme) {
                    "light" -> Config.Theme.LIGHT
                    "dark" -> Config.Theme.DARK
                    "system" -> Config.Theme.SYSTEM
                    else -> null
                }
                if (miniappTheme != null) {
                    configBuilder.setTheme(miniappTheme)
                }
            }
            if (extraUrlParams != null) {
                val map: Map<String, Any> = extraUrlParams.toMap()
                val stringMap: MutableMap<String, String> = HashMap()
                for ((key, value) in map) stringMap[key] = value.toString()
                configBuilder.setExtraUrlParams(stringMap)
            }
            urlSuffix?.also { suffix -> configBuilder.setUrlSuffix(suffix) }
            if (colors != null) {
                configBuilder.setColors(
                    colors.getString("primaryColor") ?: "",
                    colors.getString("secondaryColor") ?: "",
                    colors.getString("tertiaryColor") ?: "",
                )
            }
            if (enableSplash != null)
                configBuilder.enableSplash(enableSplash)
            configBuilder.saveState(saveState)
            miniapp.setConfig(configBuilder.build())
            miniapp.open(bridge.activity)
        }
    }

    @PluginMethod
    fun setAuthCode(call: PluginCall) {
        val appId = call.getString("appId")!!
        val authCode = call.getString("authCode")!!
        Boxo.getMiniapp(appId)
            .setAuthCode(authCode)
    }

    @PluginMethod
    fun closeMiniapp(call: PluginCall) {
        val appId = call.getString("appId")!!
        handler?.post { Boxo.getExistingMiniapp(appId)?.close() }
    }

    override fun handle(
        miniAppActivity: BoxoActivity,
        miniapp: Miniapp,
        customEvent: CustomEvent
    ) {
        val params = JSObject()
        params.put("appId", miniapp.appId)
        try {
            params.put("requestId", customEvent.requestId ?: 0)
            params.put("type", customEvent.type)
            params.put("errorType", customEvent.errorType)
            params.put("payload", customEvent.payload)
            sendEvent(CUSTOM_EVENTS_EVENT_NAME, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onLaunch(miniapp: Miniapp) {
        val params = JSObject()
        params.put("appId", miniapp.appId)
        params.put("lifecycle", "onLaunch")
        sendEvent(MINIAPP_LIFECYCLE_EVENT_NAME, params)
    }

    override fun onResume(miniapp: Miniapp) {
        val params = JSObject()
        params.put("appId", miniapp.appId)
        params.put("lifecycle", "onResume")
        sendEvent(MINIAPP_LIFECYCLE_EVENT_NAME, params)
    }

    override fun onPause(miniapp: Miniapp) {
        val params = JSObject()
        params.put("appId", miniapp.appId)
        params.put("lifecycle", "onPause")
        sendEvent(MINIAPP_LIFECYCLE_EVENT_NAME, params)
    }

    override fun onClose(miniapp: Miniapp) {
        val params = JSObject()
        params.put("appId", miniapp.appId)
        params.put("lifecycle", "onClose")
        sendEvent(MINIAPP_LIFECYCLE_EVENT_NAME, params)
    }

    override fun onError(miniapp: Miniapp, message: String) {
        val params = JSObject()
        params.put("appId", miniapp.appId)
        params.put("lifecycle", "onError")
        params.put("error", message)
        sendEvent(MINIAPP_LIFECYCLE_EVENT_NAME, params)
    }

    override fun onUserInteraction(miniapp: Miniapp) {
        val params = JSObject()
        params.put("appId", miniapp.appId)
        params.put("lifecycle", "onUserInteraction")
        sendEvent(MINIAPP_LIFECYCLE_EVENT_NAME, params)
    }

    override fun onAuth(boxoActivity: BoxoActivity, miniapp: Miniapp) {
        val params = JSObject()
        params.put("appId", miniapp.appId)
        params.put("lifecycle", "onAuth")
        sendEvent(MINIAPP_LIFECYCLE_EVENT_NAME, params)
    }

    @PluginMethod
    fun sendCustomEvent(call: PluginCall) {
        try {
            val appId: String = call.getString("appId")!!
            val event = CustomEvent(
                call.getInt("requestId"),
                call.getString("type")!!,
                call.getString("errorType"),
                call.getObject("payload")?.toMap() ?: emptyMap()
            )
            handler?.post { Boxo.getExistingMiniapp(appId)?.sendEvent(event) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @PluginMethod
    fun sendPaymentEvent(call: PluginCall) {
        try {
            val appId: String = call.getString("appId")!!
            val data = PaymentData(
                call.getString("transactionToken") ?: "",
                call.getString("miniappOrderId") ?: "",
                call.getDouble("amount") ?: 0.0,
                call.getString("currency") ?: "",
                call.getString("status") ?: "",
                call.getString("hostappOrderId") ?: "",
                call.getObject("extraParams")?.toMap()
            )
            handler?.post { Boxo.getExistingMiniapp(appId)?.sendPaymentResult(data) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @PluginMethod
    fun getMiniapps(call: PluginCall) {
        Boxo.getMiniapps(object : MiniappListCallback {
            override fun onSuccess(miniapps: List<MiniappData>) {
                try {
                    val list = JSArray()
                    for (i in miniapps.indices) {
                        val miniappData: MiniappData = miniapps[i]
                        val data = JSObject()
                        data.put("appId", miniappData.appId)
                        data.put("name", miniappData.name)
                        data.put("category", miniappData.category)
                        data.put("logo", miniappData.logo)
                        data.put("description", miniappData.description)
                        list.put(data)
                    }
                    val params = JSObject()
                    params.put("miniapps", list)
                    call.resolve(params)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onFailure(e: Exception) {
                val result = JSObject()
                result.put("error", e.message)
                call.resolve(result)
            }
        })
    }

    @PluginMethod
    fun hideMiniapps(call: PluginCall) {
        Boxo.hideMiniapps()
    }

    @PluginMethod
    fun logout(call: PluginCall) {
        Boxo.logout()
    }

    private fun sendEvent(name: String, data: JSObject) {
        notifyListeners(name, data)
    }

    override fun handle(
        boxoActivity: BoxoActivity,
        miniapp: Miniapp,
        paymentData: PaymentData
    ) {
        val params = JSObject()
        params.put("appId", miniapp.appId)
        try {
            params.put("transactionToken", paymentData.transactionToken)
            params.put("miniappOrderId", paymentData.miniappOrderId)
            params.put("amount", paymentData.amount)
            params.put("currency", paymentData.currency)
            params.put("status", paymentData.status)
            params.put("hostappOrderId", paymentData.hostappOrderId)
            if (paymentData.extraParams != null) {
                params.put("extraParams", paymentData.extraParams)
            }
            sendEvent(PAYMENT_EVENTS_EVENT_NAME, params)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    companion object {
        private const val CUSTOM_EVENTS_EVENT_NAME = "custom_event"
        private const val PAYMENT_EVENTS_EVENT_NAME = "payment_event"
        private const val MINIAPP_LIFECYCLE_EVENT_NAME = "miniapp_lifecycle"

        fun JSObject.toMap(): Map<String, Any> {
            val map: MutableMap<String, Any> = HashMap()
            val iterator: Iterator<String> = this.keys()

            while (iterator.hasNext()) {
                val key = iterator.next()
                var value: Any = this.get(key)

                if (value is JSObject) {
                    value = value.toMap()
                }
                if (value is JSArray) {
                    value = value.toList<Any>()
                }
                map[key] = value
            }

            return map
        }
    }
}
