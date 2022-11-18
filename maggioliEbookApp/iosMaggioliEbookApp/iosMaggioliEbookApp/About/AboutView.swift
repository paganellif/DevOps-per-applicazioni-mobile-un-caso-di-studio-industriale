//
//  AboutView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 04/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

struct AboutView: View {
    
    @ObservedObject var viewModel: AboutViewModel = AboutViewModel()
    @Binding var isSidebarOpened: Bool
    
    var body: some View {
        VStack {
            VStack(alignment: .leading, spacing: 10) {
                HStack{
                    Text("App Version")
                        .font(.title)
                        .bold()
                        .multilineTextAlignment(.center)
                    
                    Text(self.viewModel.appVersion)
                        .font(.title3)
                        .multilineTextAlignment(.center)
                }
                
                Divider()
                
                HStack{
                    Text("Bundle Version")
                        .font(.title)
                        .bold()
                        .multilineTextAlignment(.center)
                    
                    Text(self.viewModel.bundleVersion)
                        .font(.title3)
                        .multilineTextAlignment(.center)
                }
                
                Divider()
                
                HStack {
                    Text("Copyright")
                        .font(.title)
                        .bold()
                        .multilineTextAlignment(.leading)
                    
                    Text(self.viewModel.copyright)
                        .font(.title3)
                        .multilineTextAlignment(.leading)
                }
                
                Divider()
                
                HStack {
                    Text("Author")
                        .font(.title)
                        .bold()
                        .multilineTextAlignment(.leading)
                                        
                    VStack(alignment: .center) {
                        Text(self.viewModel.author)
                            .font(.title3)
                            .multilineTextAlignment(.leading)
                        
                        Text(self.viewModel.authorMail)
                            .bold()
                            .font(.caption)
                            .multilineTextAlignment(.leading)
                    }
                }
            }
            
            //Divider()
            Spacer()
            
            HStack {
                Image(self.viewModel.logo)
                    .resizable()
                    .frame(width: 200, height: 200, alignment: .center)
                    .aspectRatio(3 / 2, contentMode: .fill)
            }
        }
        .padding(10)
        .preferredColorScheme(.light)
        .navigationBarBackButtonHidden()
        .toolbar {
            Text("About").font(.title
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


struct AboutView_Previews: PreviewProvider {
        
    static var previews: some View {
        AboutView(isSidebarOpened: .constant(false))
    }
}
