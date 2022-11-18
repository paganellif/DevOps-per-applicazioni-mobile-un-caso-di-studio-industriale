//
//  HomeViewModel.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 26/09/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import Combine
import RxSwift
import KMPNativeCoroutinesRxSwift
import KMPNativeCoroutinesCore

class HomeViewModel: ObservableObject {
    
    @Published var query: String = ""
    
    @Published var page: KotlinInt = 0
    
    @Published var books: [Libro] = []
        
    @Published var canLoadNextPage = true
    
    private let searchBooks = SearchBookUseCase.init()
    
    init(query: String = "", page: KotlinInt = 0, canLoadNextPage: Bool = true) {
        self.query = query
        self.page = page
        self.canLoadNextPage = canLoadNextPage
        
        fetchNextPageIfPossible()
    }
    
    ///
    ///
    ///
    func fetchNextPageIfPossible() {
        guard self.canLoadNextPage else { return }
        
        searchBooks.invokeNative(query: self.query, type: nil, id: nil, did: nil, limit: nil, size: nil, page: self.page, sort: nil) { books in
            DispatchQueue.main.async {
                self.onReceive(books!)
            }
        } onError: { throwable in
            print(throwable.getStackTrace())
        }

    }
    
    ///
    ///
    ///
    private func onReceive(_ batch: [Libro]) {
        self.books += batch
        self.page = KotlinInt(value: Int32(1 + self.page.intValue))
        self.canLoadNextPage = batch.count != 0
    }
    
    ///
    ///
    ///
    func flushData() {
        self.page = 0
        self.books = []
        self.canLoadNextPage = true
    }
}
