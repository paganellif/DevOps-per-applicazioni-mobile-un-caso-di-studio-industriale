//
//  LibraryModule.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Combine
import Foundation
import R2Shared
import R2Streamer
import UIKit
import shared


/// The Library module handles the presentation of the bookshelf, and the publications' management.
protocol LibraryModuleAPI {
    
    var delegate: LibraryModuleDelegate? { get }
}

protocol LibraryModuleDelegate: ModuleDelegate {
    
    /// Called when the user tap on a publication in the library.
    func libraryDidSelectPublication(_ publication: Publication, book: Libro, completion: @escaping () -> Void)
}

final class LibraryModule: LibraryModuleAPI {

    weak var delegate: LibraryModuleDelegate?
    
    let library: LibraryService
    var subscriptions = Set<AnyCancellable>()

    init(delegate: LibraryModuleDelegate?, server: PublicationServer, httpClient: HTTPClient) {
        self.library = LibraryService(publicationServer: server, httpClient: httpClient)
        self.delegate = delegate
    }
}
