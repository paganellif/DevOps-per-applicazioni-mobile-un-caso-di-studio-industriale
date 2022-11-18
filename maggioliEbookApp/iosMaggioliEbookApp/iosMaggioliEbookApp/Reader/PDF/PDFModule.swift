//
//  PDFModule.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import R2Navigator
import R2Shared


/// The PDF module is only available on iOS 11 and more, since it relies on PDFKit.
@available(iOS 11.0, *)
final class PDFModule: ReaderFormatModule {

    weak var delegate: ReaderFormatModuleDelegate?
    
    init(delegate: ReaderFormatModuleDelegate?) {
        self.delegate = delegate
    }
    
    func supports(_ publication: Publication) -> Bool {
        return publication.conforms(to: .pdf)
    }
    
    func makeReaderViewController(for publication: Publication, locator: Locator?, isbn: String, resourcesServer: ResourcesServer) throws -> UIViewController {
        let viewController = PDFViewController(publication: publication, locator: locator, isbn: isbn)
        viewController.moduleDelegate = delegate
        return viewController
    }
    
}
