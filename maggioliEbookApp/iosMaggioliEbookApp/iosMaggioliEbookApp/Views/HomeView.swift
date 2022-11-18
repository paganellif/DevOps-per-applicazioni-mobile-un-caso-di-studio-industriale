//
//  HomeView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 25/09/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

// https://apoorv487.medium.com/pagination-in-swiftui-5a90ea952876

struct HomeView: View {
    @ObservedObject var viewModel: DataLoader
    
    var body: some View {
        NavigationView{
            ScrollView {
                LazyVStack {
                    ForEach(usersVM.users, id: \.self) { user in
                        UserView(user: user)
                            .onAppear(){
                                usersVM.loadMoreContent(currentItem: user)
                            }
                    }
                }
            }
            .navigationTitle("Home")
        }
    }
}

struct HomeView_Previews: PreviewProvider {
    static var previews: some View {
        HomeView(viewModel: DataLoader())
    }
}
