//
//  NavController.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 02/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

struct NavController: View {
    private var app: AppModule
    
    @State var isSidebarOpened = false
    @State private var selection: String? = "Login"
    
    @ObservedObject var loginViewModel: LoginViewModel
    
    init(app: AppModule, isSidebarOpened: Bool = false, selection: String? = "Login", loginViewModel: LoginViewModel) {
        self.app = app
        self.loginViewModel = loginViewModel
        self.isSidebarOpened = isSidebarOpened
        self.selection = selection
    }

    var body: some View {
        ZStack {
            // CONTENT
            NavigationView {
                if self.loginViewModel.isLoggedIn && self.selection == "Login"{
                    NavigationLink(destination: HomeView(app: app, isSidebarOpened: $isSidebarOpened), tag: "Home", selection: $selection){ }
                        .onAppear { self.selection = "Home" }
                } else {
                    switch selection {
                        case "Home":
                        NavigationLink(destination: HomeView(app: app, isSidebarOpened: $isSidebarOpened), tag: "Home", selection: $selection){ }
                        case "Logout":
                            NavigationLink(destination: LoginView(viewModel: loginViewModel, selection: $selection, isSidebarOpened: $isSidebarOpened), tag: "Logout", selection: $selection){ }.onAppear { self.selection = "Login" }
                        case "Login":
                            NavigationLink(destination: LoginView(viewModel: loginViewModel, selection: $selection, isSidebarOpened: $isSidebarOpened), tag: "Login", selection: $selection){ }.onAppear { self.selection = "Login" }
                        case "Favorite":
                            NavigationLink(destination: FavoriteView(app: app, isSidebarOpened: $isSidebarOpened), tag: "Favorite", selection: $selection){ }
                        case "Settings":
                            NavigationLink(destination: SettingsView(isSidebarOpened: $isSidebarOpened), tag: "Settings", selection: $selection){ }
                        case "About":
                            NavigationLink(destination: AboutView(isSidebarOpened: $isSidebarOpened), tag: "About", selection: $selection){ }
                        default:
                        NavigationLink(destination: HomeView(app: app, isSidebarOpened: $isSidebarOpened), tag: "Unknown", selection: $selection){ }
                    }
                }
            }
            .navigationBarBackButtonHidden()
            .preferredColorScheme(.light)
            
            // SIDEBAR
            Sidebar(isSidebarVisible: $isSidebarOpened, selection: $selection, viewModel: loginViewModel)
        }
    }
}

class NavControllerFactory {
    
    let app: AppModule
    let nc: NavController
    
    init(app: AppModule) {
        self.app = app
        self.nc = NavController(app: app, isSidebarOpened: false, selection: "Login", loginViewModel: LoginViewModel())
    }
    
    class NavControllerVC: UIHostingController<NavController> {
        
        required override init(coder aDecoder: NSCoder = NSCoder.empty(), rootView: NavController){
            super.init(coder: aDecoder, rootView: rootView)!
        }
        
        @MainActor required dynamic init?(coder aDecoder: NSCoder) {
            fatalError("init(coder:) has not been implemented")
        }
    }
    
    func make() -> UINavigationController {
        var nc = UINavigationController(rootViewController: NavControllerVC(rootView: self.nc))
        
        // Always adopt a light interface style.
        nc.overrideUserInterfaceStyle = .light
        
        return nc
    }
}
