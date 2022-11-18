//
//  HighlightsViewModel.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import SwiftUI
import Combine
import shared

final class HighlightsViewModel: ObservableObject, OutlineViewModelLoaderDelegate {
    typealias T = Highlight
    
    @Published var highlights = [Highlight]()
    
    private let isbn: String
    
    /// Shared highlight use cases
    private let getHighlightsByIsbnUseCase = GetBookHighlightsUseCase.init()
    /*private let getHighlightUseCase = GetHighlightUseCase.init()
    private let addHighlightUseCase = AddHighlightUseCase.init()
    private let removeHighlightUseCase = RemoveHighlightUseCase.init()*/
        
    lazy private var loader: OutlineViewModelLoader<Highlight, HighlightsViewModel> = {
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
    
    var dataTask: AnyPublisher<[Highlight], Error> {
        return Future(on: DispatchQueue.main) { promise in
            self.getHighlightsByIsbnUseCase.invokeNative(isbn: self.isbn) { results in
                print("GET HIGHLIGHTS FOR BOOK \(self.isbn): \(results)")
                promise(.success(results))
            } onError: { throwable in
                print(throwable.getStackTrace())
                promise(.failure(throwable.asError()))
            }
        }.eraseToAnyPublisher()
    }
    
    func setLoadedValues(_ values: [Highlight]) {
        highlights = values
    }
}
