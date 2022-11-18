//
//  SettingsView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 04/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

struct SettingsView: View {
    @Binding var isSidebarOpened: Bool
    
    var body: some View {
        VStack{
            Text("Settings")
        }
        .preferredColorScheme(.light)
        .navigationBarBackButtonHidden()
        .toolbar {
            Text("Settings").font(.title
                .weight(.bold)
            ).foregroundColor(Color.white)
            
            Button {
                isSidebarOpened.toggle()
            } label: {
                Label("Toggle SideBar",
                      systemImage: "line.3.horizontal")
                .foregroundColor(Color.white)
                .font(.body.bold())
            }
        }
        .listStyle(.inset)
        .navigationBarTitleDisplayMode(.inline)
        .preferredColorScheme(.light)
        .toolbar(.visible, for: .navigationBar)
        .toolbarBackground(Color("AccentColor"), for: .navigationBar)
        .toolbarBackground(.visible, for: .navigationBar)
        .onAppear {
            self.isSidebarOpened = false
        }
    }
}

