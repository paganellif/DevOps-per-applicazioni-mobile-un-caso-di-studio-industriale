//
//  BookmarksViewModel.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import Combine

final class BookmarksViewModel: ObservableObject, OutlineViewModelLoaderDelegate {
    typealias T = Bookmark
    
    @Published var bookmarks = [Bookmark]()
    private let isbn: String
    
    /// Shared bookmark use cases
    private let getBookmarksByIsbnUseCase = GetBookBookmarksUseCase.init()
    /*private let addBookmarkUseCase = AddBookmarkForBookUseCase.init()
    private let removeBookmarkUseCase = RemoveBookmarkForBookUseCase.init()*/
    
    lazy private var loader: OutlineViewModelLoader<Bookmark, BookmarksViewModel> = {
        OutlineViewModelLoader(delegate: self)
    }()
    
    init(isbn: String) {
        self.isbn = isbn
    }
    
    func load() {
        loader.load()
    }

    func loadIfNeeded() {
        loader.loadIfNeeded()
    }
    
    var dataTask: AnyPublisher<[Bookmark], Error> {
        return Future(on: DispatchQueue.main) { promise in
            self.getBookmarksByIsbnUseCase.invokeNative(isbn: self.isbn) { results in
                promise(.success(results))
            } onError: { throwable in
                print(throwable.getStackTrace())
                promise(.failure(throwable.asError()))
            }
        }.eraseToAnyPublisher()
    }
    
    func setLoadedValues(_ values: [Bookmark]) {
        bookmarks = values
    }
}
