//
//  FavoriteView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 04/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct FavoriteView: View {
    var app: AppModule
    @ObservedObject var favoriteViewModel: FavoriteViewModel  = FavoriteViewModel()
    @Binding var isSidebarOpened: Bool
    
    var body: some View {
        GeometryReader { dimensions in
            FavoriteBookListContainer(app: app, viewModel: favoriteViewModel)
        }
        .preferredColorScheme(.light)
        .navigationBarBackButtonHidden()
        .toolbar {
            Text("Favorite").font(.title
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
            
            Text("")
                .foregroundColor(Color.white) // TODO: fix
                .searchable(text: $favoriteViewModel.query, placement: .navigationBarDrawer, prompt: "Search")
                .onSubmit {
                    favoriteViewModel.flushData()
                    favoriteViewModel.fetchFavoriteBooks()
                }
                .onChange(of: favoriteViewModel.query) { _ in
                    favoriteViewModel.flushData()
                    favoriteViewModel.fetchFavoriteBooks()
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

struct FavoriteBookListContainer: View {
    var app: AppModule
    @ObservedObject var viewModel: FavoriteViewModel

    var body: some View {
        FavoriteBookList(
            app: app,
            viewModel: viewModel,
            books: $viewModel.favoriteBooks
        )
    }
}

struct FavoriteBookList: View {
    var app: AppModule
    @ObservedObject var viewModel: FavoriteViewModel
    @Binding var books: [Libro]
    
    var body: some View {
        List {
            ForEach(books) { book in
                CardView(app: app, book: book, viewModel: viewModel)
            }
        }
    }
}
