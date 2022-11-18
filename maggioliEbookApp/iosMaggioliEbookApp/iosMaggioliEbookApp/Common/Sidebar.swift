//
//  SideBar.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 30/09/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct MenuItem: Identifiable {
    var id: Int
    var icon: String
    var text: String
}

var menuItems: [MenuItem] = [
    MenuItem(id: 4001, icon: "books.vertical", text: "Home"),
    MenuItem(id: 4002, icon: "heart.fill", text: "Favorite"),
    MenuItem(id: 4004, icon: "wrench.and.screwdriver.fill", text: "Settings"),
    MenuItem(id: 4003, icon: "info.bubble", text: "About"),
]

var logoutMenuItem = MenuItem(id: 4005, icon: "iphone.and.arrow.forward", text: "Logout")

struct Sidebar: View {
    @Binding var isSidebarVisible: Bool
    
    @Binding var selection: String?
        
    var sideBarWidth = UIScreen.main.bounds.size.width * 0.7
    var menuColor = Color("AccentColor")

    @ObservedObject var viewModel: LoginViewModel
        
    var body: some View {
        ZStack {
            GeometryReader { _ in
                EmptyView()
            }
            .background(.black.opacity(0.6))
            .opacity(isSidebarVisible ? 1 : 0)
            .animation(.easeInOut.delay(0.2), value: isSidebarVisible)
            .onTapGesture {
                isSidebarVisible.toggle()
            }
            
            HStack(alignment: .top) {
                ZStack(alignment: .top) {
                    menuColor
                    
                    VStack(alignment: .leading, spacing: 20) {
                        userProfile
                        Divider()
                        MenuLinks(items: menuItems, viewModel: viewModel, selection: $selection, isSidebarVisibile: $isSidebarVisible)
                        Spacer()
                        MenuLinks(items: [logoutMenuItem], viewModel: viewModel, selection: $selection, isSidebarVisibile: $isSidebarVisible)
                        Divider()
                    }
                    .padding(.top, 80)
                    .padding(.horizontal, 40)
                }
                .frame(width: sideBarWidth)
                .offset(x: isSidebarVisible ? 0 : -sideBarWidth)
                .animation(.default, value: isSidebarVisible)
                
                Spacer()
            }
        }.edgesIgnoringSafeArea(.all)
    }
    
    var userProfile: some View {
        VStack(alignment: .leading) {
            VStack(alignment: .leading, spacing: 6) {
                HStack {
                    Image("maggioli_white")
                      .resizable()
                      .frame(width: 80, height: 80, alignment: .center)
                      .clipShape(Rectangle())
                      .aspectRatio(3 / 2, contentMode: .fill)
                      .shadow(radius: 4)
                      .padding(.trailing, 18)
                }
                
                Text(verbatim: viewModel.user.email!)
                    .foregroundColor(.white)
                    .font(.title3)
                Text(self.viewModel.user.firstName! + " " + self.viewModel.user.lastName!)
                    .foregroundColor(.white)
                    .bold()
                    .font(.caption)
            }
            .padding(.bottom, 20)
        }
    }
}

struct MenuLinks: View {
    var items: [MenuItem]
    var viewModel: LoginViewModel
    @Binding var selection: String?
    @Binding var isSidebarVisibile: Bool
    
    var body: some View {
        VStack(alignment: .leading, spacing: 30) {
            ForEach(items) { item in
                menuLink(icon: item.icon, text: item.text, viewModel: self.viewModel, selection: $selection, isSidebarVisibile: $isSidebarVisibile)
            }
        }
        .padding(.vertical, 14)
        .padding(.leading, 8)
    }
}

struct menuLink: View {
    var icon: String
    var text: String
    @ObservedObject var viewModel: LoginViewModel
    @Binding var selection: String?
    @Binding var isSidebarVisibile: Bool
    
    var body: some View {
        HStack {
            Image(systemName: icon)
                .resizable()
                .frame(width: 20, height: 20)
                .foregroundColor(.white)
                .padding(.trailing, 18)
            Text(text)
                .foregroundColor(.white)
                .font(.body)
        }
        .onTapGesture {
            print("Tapped on \(text)")
            
            if text == "Logout" {
                viewModel.logout()
                isSidebarVisibile = false
            }
            
            selection = text
        }
    }
}
