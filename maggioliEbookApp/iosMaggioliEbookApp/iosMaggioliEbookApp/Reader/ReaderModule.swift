//
//  ReaderModule.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import R2Shared
import Combine
import shared

/// The ReaderModule handles the presentation of publications to be read by the user.
/// It contains sub-modules implementing ReaderFormatModule to handle each format of publication (eg. CBZ, EPUB).
protocol ReaderModuleAPI {
    
    var delegate: ReaderModuleDelegate? { get }
    
    /// Presents the given publication to the user, inside the given navigation controller.
    /// - Parameter completion: Called once the publication is presented, or if an error occured.
    func presentPublication(publication: Publication, book: Libro, in navigationController: UINavigationController, completion: @escaping () -> Void)
    
}

protocol ReaderModuleDelegate: ModuleDelegate {}

final class ReaderModule: ReaderModuleAPI {
    
    weak var delegate: ReaderModuleDelegate?
    private let resourcesServer: ResourcesServer
    /// Sub-modules to handle different publication formats (eg. EPUB, CBZ)
    var formatModules: [ReaderFormatModule] = []
    private let factory = ReaderFactory()
    private let getBookProgression = GetBookProgressionUseCase.init()
    
    init(delegate: ReaderModuleDelegate?, resourcesServer: ResourcesServer) {
        self.delegate = delegate
        self.resourcesServer = resourcesServer
        
        formatModules = [
            //CBZModule(delegate: self),
            EPUBModule(delegate: self),
        ]
        
        /*if #available(iOS 11.0, *) {
            formatModules.append(PDFModule(delegate: self))
        }*/
    }
    
    func presentPublication(publication: Publication, book: Libro, in navigationController: UINavigationController, completion: @escaping () -> Void) {
        guard let delegate = delegate, let bookId = book.isbn else {
            fatalError("Reader delegate not set")
        }
        
        func present(_ viewController: UIViewController) {
            let backItem = UIBarButtonItem()
            backItem.title = ""
            viewController.navigationItem.backBarButtonItem = backItem
            viewController.hidesBottomBarWhenPushed = true
            navigationController.pushViewController(viewController, animated: true)
        }
        
        guard let module = self.formatModules.first(where:{ $0.supports(publication) }) else {
            delegate.presentError(ReaderError.formatNotSupported, from: navigationController)
            completion()
            return
        }

        getBookProgression.invokeNative(isbn: bookId) { result in
            var progression: String? = nil
            var locator: Locator? = nil
            
            DispatchQueue.main.async {
                do {
                    
                    if !result.isEmpty {
                        progression = result[0].progression
                        locator = try Locator(jsonString: progression!)
                        print("Book progression \(progression!)")
                        print("Book locator \(locator!)")
                    }
                    
                    let readerViewController = try module.makeReaderViewController(for: publication,
                                                                                   locator: locator,
                                                                                   isbn: bookId,
                                                                                   resourcesServer: self.resourcesServer)
                    
                    present(readerViewController)
                } catch {
                    print(error)
                    delegate.presentError(error, from: navigationController)
                }
                
                completion()
            }
            
        } onError: { throwable in
            print(throwable.getStackTrace())
        }
    }
}

extension ReaderModule: ReaderFormatModuleDelegate {

    func presentDRM(for publication: Publication, from viewController: UIViewController) {
        let drmViewController: DRMManagementTableViewController = factory.make(publication: publication, delegate: delegate)
        let backItem = UIBarButtonItem()
        backItem.title = ""
        drmViewController.navigationItem.backBarButtonItem = backItem
        viewController.navigationController?.pushViewController(drmViewController, animated: true)
    }
    
    func presentOutline(of publication: Publication, isbn: String, from viewController: UIViewController) -> AnyPublisher<Locator, Never> {
        let outlineAdapter = factory.make(publication: publication, isbn: isbn)
        let outlineLocatorPublisher = outlineAdapter.1
        
        viewController.present(UINavigationController(rootViewController: outlineAdapter.0), animated: true)
        
        return outlineLocatorPublisher
    }
    
    func presentAlert(_ title: String, message: String, from viewController: UIViewController) {
        delegate?.presentAlert(title, message: message, from: viewController)
    }
    
    func presentError(_ error: Error?, from viewController: UIViewController) {
        delegate?.presentError(error, from: viewController)
    }
}
