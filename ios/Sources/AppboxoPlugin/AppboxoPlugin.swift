import Foundation
import Capacitor
import AppBoxoSDK

/**
 * Please read the Capacitor iOS Plugin Development Guide
 * here: https://capacitorjs.com/docs/plugins/ios
 */
@objc(AppboxoPlugin)
public class AppboxoPlugin: CAPPlugin, CAPBridgedPlugin {
    private let CUSTOM_EVENT = "custom_event"
    private let PAYMENT_EVENT = "payment_event"
    private let MINIAPP_LIFECYCLE_EVENT = "miniapp_lifecycle"

    public let identifier = "AppboxoPlugin"
    public let jsName = "Appboxo"
    public let pluginMethods: [CAPPluginMethod] = [
        CAPPluginMethod(name: "setConfig", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "openMiniapp", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "setAuthCode", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "closeMiniapp", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "sendCustomEvent", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "sendPaymentEvent", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "getMiniapps", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "hideMiniapps", returnType: CAPPluginReturnPromise),
        CAPPluginMethod(name: "logout", returnType: CAPPluginReturnPromise)
    ]

    @objc func setConfig(_ call: CAPPluginCall) {
        let clientId = call.getString("clientId") ?? ""
        let userId = call.getString("userId") ?? ""
        let language = call.getString("language") ?? "en"
        let sandboxMode = call.getBool("sandboxMde", false)
        let theme = call.getString("theme", "system")
        let showPermissionsPage = call.getBool("showPermissionsPage", true)
        let showClearCache = call.getBool("showClearCache", true)
        var globalTheme : Theme = .System
        switch (theme) {
        case "dark":
            globalTheme = .Dark
        case "light":
            globalTheme = .Light
        default:
            globalTheme = .System
        }

        let config = Config(clientId: clientId, theme: globalTheme)
        config.language = language
        config.sandboxMode = sandboxMode
        config.permissionsPage = showPermissionsPage
        config.showClearCache = showClearCache
        config.setUserId(id: userId)

        Appboxo.shared.setConfig(config: config)
    }

    @objc func openMiniapp(_ call: CAPPluginCall) {
        let appId = call.getString("appId") ?? ""
        let data = call.getObject("data")?.toMap() ?? nil
        let theme = call.getString("theme")
        let extraUrlParams = call.getObject("extraUrlParams")?.toMap() ?? nil
        let colors = call.getObject("colors")?.toMap() ?? nil
        let enableSplash = call.getBool("enableSplash")
        let saveState = call.getBool("saveState", true)

        let miniApp = Appboxo.shared.getMiniapp(appId: appId)
        miniApp.setData(data: data)
        miniApp.delegate = self

        let miniappConfig = MiniappConfig()
        miniappConfig.saveState = saveState

        if let enableSplash = enableSplash {
            miniappConfig.enableSplash(isSplashEnabled: enableSplash)
        }

        if let theme = theme {
            switch theme {
            case "dark":
                miniappConfig.setTheme(theme: .Dark)
            case "light":
                miniappConfig.setTheme(theme: .Light)
            case "system":
                miniappConfig.setTheme(theme: .System)
            default:
                break
            }
        }

        if let extraUrlParams = extraUrlParams as? [String : String] {
            miniappConfig.setExtraParams(extraParams: extraUrlParams)
        }

        if let colors = colors {
            miniappConfig.setColor(color: MiniappColor(primary: colors["primary_color"] as? String ?? "", secondary: colors["secondary_color"] as? String ?? "", tertiary: colors["tertiary_color"] as? String ?? ""))
        }

        miniApp.setConfig(config: miniappConfig)

        guard let viewController = bridge?.viewController else { return }
        DispatchQueue.main.async {
            miniApp.open(viewController: viewController)
        }

    }

    @objc func setAuthCode(_ call: CAPPluginCall) {
        let appId = call.getString("appId") ?? ""
        let authCode = call.getString("authCode") ?? ""

        DispatchQueue.main.async {
            Appboxo.shared.getMiniapp(appId: appId).setAuthCode(authCode: authCode)
        }
    }

    @objc func closeMiniapp(_ call: CAPPluginCall) {
        let appId = call.getString("appId") ?? ""

        if let miniapp = Appboxo.shared.getExistingMiniapp(appId: appId) {
            DispatchQueue.main.async {
                miniapp.close()
            }
        }
    }

    @objc func sendCustomEvent(_ call: CAPPluginCall) {
        let appId = call.getString("appId") ?? ""

        let customEvent = CustomEvent()
        customEvent.requestId = call.getInt("requestId", 0)
        customEvent.type = call.getString("type") ?? ""
        customEvent.errorType = call.getString("errorType") ?? ""
        customEvent.payload = call.getObject("payload")?.toMap()

        DispatchQueue.main.async {
            Appboxo.shared.getMiniapp(appId: appId).sendCustomEvent(customEvent: customEvent)
        }
    }

    @objc func sendPaymentEvent(_ call: CAPPluginCall) {
        let appId = call.getString("appId") ?? ""

        let paymentData = PaymentData()
        paymentData.transactionToken = call.getString("transactionToken") ?? ""
        paymentData.miniappOrderId = call.getString("miniappOrderId") ?? ""
        paymentData.amount = call.getDouble("amount", 0.0)
        paymentData.currency = call.getString("currency") ?? ""
        paymentData.status = call.getString("status") ?? ""
        paymentData.hostappOrderId = call.getString("hostappOrderId") ?? ""
        paymentData.extraParams = call.getObject("extraParams")?.toMap()

        DispatchQueue.main.async {
            Appboxo.shared.getMiniapp(appId: appId).sendPaymentEvent(paymentData: paymentData)
        }
    }

    @objc func getMiniapps(_ call: CAPPluginCall) {
        Appboxo.shared.getMiniapps { miniapps, error in
            if let error = error {
                call.reject(error)
            } else {
                var list = JSArray()

                miniapps.forEach { miniappData in
                    var data = JSObject()
                    data["appId"] = miniappData.appId
                    data["name"] = miniappData.name
                    data["category"] = miniappData.category
                    data["logo"] = miniappData.logo
                    data["description"] = miniappData.longDescription
                    list.append(data)
                }
                call.resolve(["miniapps" : list])
            }
        }
    }

    @objc func hideMiniapps(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Appboxo.shared.hideMiniapps()
        }
    }

    @objc func logout(_ call: CAPPluginCall) {
        DispatchQueue.main.async {
            Appboxo.shared.logout()
        }
    }
}

extension AppboxoPlugin : MiniappDelegate {
    public func didReceivePaymentEvent(miniapp: Miniapp, paymentData: PaymentData) {
        let dict = [
            "appId" : miniapp.appId,
            "transactionToken" : paymentData.transactionToken,
            "miniappOrderId" : paymentData.miniappOrderId,
            "amount" : paymentData.amount,
            "currency" : paymentData.currency,
            "status" : paymentData.status,
            "hostappOrderId" : paymentData.hostappOrderId,
            "extraParams" : paymentData.extraParams ?? [String : Any]()
        ] as [String: Any]

        notifyListeners(PAYMENT_EVENT, data: dict)
    }

    public func didReceiveCustomEvent(miniapp: Miniapp, customEvent: CustomEvent) {
        let dict = [
            "appId" : miniapp.appId,
            "requestId" : customEvent.requestId,
            "type" : customEvent.type,
            "errorType" : customEvent.errorType,
            "payload" : customEvent.payload ?? [String : Any]()
        ] as [String: Any]

        notifyListeners(CUSTOM_EVENT, data: dict)
    }

    public func onLaunch(miniapp: Miniapp) {
        let dict = [
            "appId" : miniapp.appId,
            "lifecycle" : "onLaunch"
        ]

        notifyListeners(MINIAPP_LIFECYCLE_EVENT, data: dict)
    }

    public func onResume(miniapp: Miniapp) {
        let dict = [
            "appId" : miniapp.appId,
            "lifecycle" : "onResume"
        ]

        notifyListeners(MINIAPP_LIFECYCLE_EVENT, data: dict)
    }

    public func onPause(miniapp: Miniapp) {
        let dict = [
            "appId" : miniapp.appId,
            "lifecycle" : "onPause"
        ]

        notifyListeners(MINIAPP_LIFECYCLE_EVENT, data: dict)
    }

    public func onClose(miniapp: Miniapp) {
        let dict = [
            "appId" : miniapp.appId,
            "lifecycle" : "onClose"
        ]

        notifyListeners(MINIAPP_LIFECYCLE_EVENT, data: dict)
    }

    public func onError(miniapp: Miniapp, message: String) {
        let dict = [
            "appId" : miniapp.appId,
            "lifecycle" : "onError",
            "error" : message
        ]

        notifyListeners(MINIAPP_LIFECYCLE_EVENT, data: dict)
    }

    public func onUserInteraction(miniapp: Miniapp) {
        let dict = [
            "appId" : miniapp.appId,
            "lifecycle" : "onUserInteraction"
        ]

        notifyListeners(MINIAPP_LIFECYCLE_EVENT, data: dict)
    }

    public func onAuth(miniapp: Miniapp) {
        let dict = [
            "appId" : miniapp.appId,
            "lifecycle" : "onAuth"
        ]

        notifyListeners(MINIAPP_LIFECYCLE_EVENT, data: dict)
    }
}

extension JSObject {
    func toMap() -> [String : Any] {
        var dict = [String : Any]()

        keys.forEach { key in
            if let object = self[key] as? JSObject {
                dict[key] = object.toMap()
            }

            dict[key] = self[key]
        }

        return dict
    }
}
