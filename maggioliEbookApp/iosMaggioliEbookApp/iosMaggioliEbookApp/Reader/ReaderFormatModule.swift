//
//  ReaderFormatModule.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import R2Shared
import Combine

/// A ReaderFormatModule is a sub-module of ReaderModule that handles publication of a given format (eg. EPUB, CBZ).
protocol ReaderFormatModule {
    
    var delegate: ReaderFormatModuleDelegate? { get }
    
    /// Returns whether the given publication is supported by this module.
    func supports(_ publication: Publication) -> Bool
    
    /// Creates the view controller to present the publication.
    func makeReaderViewController(for publication: Publication, locator: Locator?, isbn: String, resourcesServer: ResourcesServer) throws -> UIViewController
}

protocol ReaderFormatModuleDelegate: AnyObject {
    
    /// Shows the reader's outline from the given links.
    func presentOutline(of publication: Publication, isbn: String, from viewController: UIViewController) -> AnyPublisher<Locator, Never>
    
    /// Shows the DRM management screen for the given DRM.
    func presentDRM(for publication: Publication, from viewController: UIViewController)
    
    func presentAlert(_ title: String, message: String, from viewController: UIViewController)
    func presentError(_ error: Error?, from viewController: UIViewController)
}
