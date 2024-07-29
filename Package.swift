// swift-tools-version: 5.9
import PackageDescription

let package = Package(
    name: "AppboxoCapacitorBoxoSdk",
    platforms: [.iOS(.v13)],
    products: [
        .library(
            name: "AppboxoCapacitorBoxoSdk",
            targets: ["AppboxoPlugin"])
    ],
    dependencies: [
        .package(url: "https://github.com/ionic-team/capacitor-swift-pm.git", branch: "main")
    ],
    targets: [
        .target(
            name: "AppboxoPlugin",
            dependencies: [
                .product(name: "Capacitor", package: "capacitor-swift-pm"),
                .product(name: "Cordova", package: "capacitor-swift-pm")
            ],
            path: "ios/Sources/AppboxoPlugin"),
        .testTarget(
            name: "AppboxoPluginTests",
            dependencies: ["AppboxoPlugin"],
            path: "ios/Tests/AppboxoPluginTests")
    ]
)