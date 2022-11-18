//
//  FavoriteViewModel.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 04/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

class FavoriteViewModel: ObservableObject {
    @Published var favorites: [String] = []
    
    @Published var query: String = ""
    
    @Published var favoriteBooks: [Libro] = []
    
    private let getFavoriteBooks = GetAllFavoriteBooksUseCase.init()
    private let addFavoriteBook = SetBookAsFavoriteUseCase.init()
    private let removeFavoriteBook = UnsetBookAsFavoriteUseCase.init()
    private let getBookMetadata = GetBookMetadataUseCase.init()
    
    init() {
        self.fetchFavoriteBooks()
    }
    
    ///
    func fetchFavoriteBooks() {
        getFavoriteBooks.invokeNative { favorites in
            self.flushData()
            
            DispatchQueue.main.async {
                self.favorites = favorites.compactMap { $0.isbn }
            }
            
            DispatchQueue.main.async {
                for isbn in self.favorites {
                    self.fetchBookMetadata(isbn: isbn)
                }
            }
            
            if self.query != "" {
                DispatchQueue.main.async {
                    self.favoriteBooks = self.favoriteBooks.filter { libro in
                        libro.isbn!.contains(self.query) ||
                        libro.name!.contains(self.query) ||
                        libro.autori!.contains(self.query)
                    }
                }
            }
        } onError: { throwable in
            print(throwable.getStackTrace())
        }
    }
    
    ///
    private func fetchBookMetadata(isbn: String) {
        getBookMetadata.invokeNative(isbn: isbn) { libro in
            DispatchQueue.main.async {
                if libro != nil && !self.favoriteBooks.contains(libro!) {
                    self.favoriteBooks.append(libro!)
                }
            }
        } onError: { throwable in
            print(throwable.getStackTrace())
        }
    }
    
    ///
    func removeFavorite(isbn: String) {
        removeFavoriteBook.invokeNative(isbn: isbn) { _ in
            DispatchQueue.main.async {
                self.favorites = self.favorites.filter { tmp in
                    tmp != isbn
                }
                
                self.favoriteBooks = self.favoriteBooks.filter { book in
                    book.isbn! != isbn
                }
                
                print("Removed Favorite book \(isbn)")
            }
        } onError: { throwable in
            print(throwable.getStackTrace())
        }
    }
    
    ///
    func addFavorite(isbn: String) {
        addFavoriteBook.invokeNative(isbn: isbn) { _ in
            DispatchQueue.main.async {
                self.favorites.append(isbn)
                print("Added Favorite book \(isbn)")
                self.fetchBookMetadata(isbn: isbn)
            }
        } onError: { throwable in
            print(throwable.getStackTrace())
        }
    }
    
    ///
    func flushData() {
        self.favorites = []
        self.favoriteBooks = []
    }
}
