//
//  EPUBModule.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import R2Shared


final class EPUBModule: ReaderFormatModule {
    
    weak var delegate: ReaderFormatModuleDelegate?
    
    init(delegate: ReaderFormatModuleDelegate?) {
        self.delegate = delegate
    }

    func supports(_ publication: Publication) -> Bool {
        return publication.conforms(to: .epub)
            || publication.readingOrder.allAreHTML
    }
    
    func makeReaderViewController(for publication: Publication, locator: Locator?, isbn: String, resourcesServer: ResourcesServer) throws -> UIViewController {
        guard publication.metadata.identifier != nil else {
            throw ReaderError.epubNotValid
        }
        
        let epubViewController = EPUBViewController(publication: publication, locator: locator, isbn: isbn, resourcesServer: resourcesServer)
        epubViewController.moduleDelegate = delegate
        return epubViewController
    }
    
}
