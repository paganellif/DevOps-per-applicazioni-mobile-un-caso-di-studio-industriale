import SwiftUI
import shared
import Combine
import UIKit

@UIApplicationMain
class AppDelegate: UIResponder, UIApplicationDelegate {
    
    var window: UIWindow?
    private var app: AppModule!
    private var subscriptions = Set<AnyCancellable>()

    func application(_ application: UIApplication, didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]?) -> Bool {
        app = try! AppModule()
        
        window = UIWindow(frame: UIScreen.main.bounds)
        window?.rootViewController = app.navControllerNC
        window?.makeKeyAndVisible()
        
        return true
    }
}
