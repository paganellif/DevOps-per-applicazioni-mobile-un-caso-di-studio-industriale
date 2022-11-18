//
//  HomeView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 25/09/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct HomeView: View {
    
    var app: AppModule
    
    @ObservedObject var viewModel: HomeViewModel = HomeViewModel()
    var favoriteViewModel: FavoriteViewModel = FavoriteViewModel()
    
    @Binding var isSidebarOpened: Bool

    var body: some View {
        GeometryReader { dimensions in
            BookListContainer(app: app, viewModel: viewModel, favoriteViewModel: favoriteViewModel)
        }
        .navigationBarBackButtonHidden()
        .toolbar {
            Text("Home").font(.title
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
                .preferredColorScheme(.dark)
                .searchable(text: $viewModel.query, placement: .navigationBarDrawer, prompt: "Search")
                .onSubmit {
                    viewModel.flushData()
                    viewModel.fetchNextPageIfPossible()
                }
                .onChange(of: viewModel.query) { _ in
                    viewModel.flushData()
                    viewModel.fetchNextPageIfPossible()
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

struct BookListContainer: View {
    var app: AppModule
    @ObservedObject var viewModel: HomeViewModel
    var favoriteViewModel: FavoriteViewModel

    var body: some View {
        BookList(
            app: app,
            viewModel: viewModel,
            favoriteViewModel: favoriteViewModel,
            books: $viewModel.books,
            isLoading: $viewModel.canLoadNextPage,
            onScrolledAtBottom: viewModel.fetchNextPageIfPossible
        )
        .onAppear(perform: viewModel.fetchNextPageIfPossible)
    }
}

struct BookList: View {
    var app: AppModule
    @ObservedObject var viewModel: HomeViewModel
    var favoriteViewModel: FavoriteViewModel
    @Binding var books: [Libro]
    @Binding var isLoading: Bool
    let onScrolledAtBottom: () -> Void
    
    var body: some View {
        List {
            booksList
            if isLoading {
                ProgressView()
                    .frame(idealWidth: .infinity, maxWidth: .infinity, alignment: .center)
            }
        }
    }
    
    private var booksList: some View {
        ForEach(books) { book in
            CardView(app: app, book: book, viewModel: favoriteViewModel).onAppear {
                if self.books.last == book {
                    self.onScrolledAtBottom()
                }
            }
        }
    }
}
