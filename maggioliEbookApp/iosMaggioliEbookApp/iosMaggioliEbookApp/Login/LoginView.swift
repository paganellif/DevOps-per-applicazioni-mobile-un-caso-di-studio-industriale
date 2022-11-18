//
//  LoginView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 25/09/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct LoginView: View {
    
    @ObservedObject var viewModel: LoginViewModel
    @Binding var selection: String?
    @Binding var isSidebarOpened: Bool
    
    var body: some View {
        VStack(spacing: 15.0) {
            Image("maggioli")
                .resizable()
                .frame(width: 200, height: 200, alignment: /*@START_MENU_TOKEN@*/.center/*@END_MENU_TOKEN@*/)
            ValidatedTextField(titleKey: "Username", secured: false, text: $viewModel.username)
            ValidatedTextField(titleKey: "Password", secured: true, text: $viewModel.password)
            Toggle("Remember me", isOn: $viewModel.rememberMe).tint(Color("AccentColor"))
            
            Button("Login") {
                viewModel.login()
            }
            .disabled(viewModel.username.isEmpty || viewModel.password.isEmpty)
        }
        .padding(.all)
        .navigationBarBackButtonHidden()
        .preferredColorScheme(.light)
    }
}

struct ValidatedTextField: View {
    let titleKey: String
    let secured: Bool
    
    @Binding
    var text: String

    @ViewBuilder
    var textField: some View {
        if secured {
            SecureField(titleKey, text: $text)
        }  else {
            TextField(titleKey, text: $text)
        }
    }

    var body: some View {
        ZStack {
            textField
                .textFieldStyle(RoundedBorderTextFieldStyle())
                .autocapitalization(.none)
        }
    }
}

