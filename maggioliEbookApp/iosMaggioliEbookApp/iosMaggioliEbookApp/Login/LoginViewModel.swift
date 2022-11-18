//
//  LoginViewModel.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 26/09/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import Combine

class LoginViewModel: ObservableObject {
    
    @Published var isLoggedIn = false
    @Published var isLoggedOut = true
    @Published var username = ""
    @Published var password = ""
    @Published var rememberMe = true

    private var dummyUser = User(activated: false, authorities: nil, email: "john.doe@maggioli.it", firstName: "John", lastName: "Doe", langKey: nil, login: "false")
    
    @Published var user: User = User(activated: false, authorities: nil, email: "john.doe@maggioli.it", firstName: "John", lastName: "Doe", langKey: nil, login: "false")
    
    private let userLogin = UserLoginUseCase.init()
    private let userLogout = UserLogoutUseCase.init()
    private let isUserLoggedIn = CheckUserLoggedUseCase.init()
    private let userInfo = GetUserInfoUseCase.init()
    
    init() {
        checkUserIsLoggedIn()
    }
    
    ///
    ///
    ///
    func login() {
        userLogin.invokeNative(username: self.username, password: self.password, rememberMe: self.rememberMe) { result in
                DispatchQueue.main.async {
                    self.isLoggedIn = result.boolValue
                    self.isLoggedOut = !self.isLoggedIn
                    
                    if self.isLoggedIn == true {
                        self.userAccountInfo()
                    }
                }
        } onError: { error in
            print(error.getStackTrace())
        }
    }
    
    ///
    ///
    ///
    func checkUserIsLoggedIn() {
        isUserLoggedIn.invokeNative() { result in
                DispatchQueue.main.async {
                    self.isLoggedIn = result.boolValue
                    self.isLoggedOut = !self.isLoggedIn
                    
                    if self.isLoggedIn == true && self.user == self.dummyUser {
                        self.userAccountInfo()
                    }
                }
        } onError: { error in
            print(error.getStackTrace())
        }
    }
    
    ///
    ///
    ///
    func userAccountInfo() {
        userInfo.invokeNative() { result in
                DispatchQueue.main.async {
                    print(result)
                    self.user = result
                }
        } onError: { error in
            print(error.getStackTrace())
        }
    }
    
    ///
    ///
    ///
    func logout() {
        userLogout.invokeNative() { result in
            DispatchQueue.main.async {
                self.isLoggedOut = result.boolValue
                self.isLoggedIn = !self.isLoggedOut
                self.user = self.dummyUser
            }
        } onError: { error in
            print(error.getStackTrace())
        }
    }
    
}
