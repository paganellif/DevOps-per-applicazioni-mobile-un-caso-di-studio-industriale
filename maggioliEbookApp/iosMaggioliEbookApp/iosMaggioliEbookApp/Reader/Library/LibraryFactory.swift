//
//  LibraryFactory.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import R2Shared


final class LibraryFactory {
    
    fileprivate let storyboard = UIStoryboard(name: "Library", bundle: nil)
    fileprivate let libraryService: LibraryService

    init(libraryService: LibraryService) {
        self.libraryService = libraryService
    }
}

extension LibraryFactory: DetailsTableViewControllerFactory {
    func make(publication: Publication) -> DetailsTableViewController {
        let controller = storyboard.instantiateViewController(withIdentifier: "DetailsTableViewController") as! DetailsTableViewController
        controller.publication = publication
        return controller
    }
}

