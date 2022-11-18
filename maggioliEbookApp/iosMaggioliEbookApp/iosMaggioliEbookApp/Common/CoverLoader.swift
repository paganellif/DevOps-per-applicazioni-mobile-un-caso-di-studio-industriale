//
//  CoverLoader.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 04/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

final class CoverLoader: ObservableObject {
    
    @Published var data: Data?
    
    private let bookCover = GetBookCoverUseCase.init()
    
    init(_ isbn: String) {
        self.bookCover.invokeNative(isbn: isbn) { result in
            
            DispatchQueue.main.async {
                if result != nil {
                    self.data = ByteArrayConverter.init().toData(byteArray: result!)
                }
            }
            
        } onError: { throwable in
            print(throwable.getStackTrace())
            self.data = Data()
        }
    }
}
