//
//  ReaderFactory.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import R2Shared
import SwiftUI
import Combine

final class ReaderFactory {
    
    final class Storyboards {
        let drm = UIStoryboard(name: "DRM", bundle: nil)
    }
    
    let storyboards = Storyboards()
}

extension ReaderFactory: OutlineTableViewControllerFactory {
    func make(publication: Publication, isbn: String) -> OutlineTableViewAdapter {
        let view = OutlineTableView(publication: publication, isbn: isbn)
        let hostingVC = OutlineHostingController(rootView: view)
        hostingVC.title = publication.metadata.title
        
        hostingVC.overrideUserInterfaceStyle = .light
        
        return (hostingVC, view.goToLocatorPublisher)
    }
}

extension ReaderFactory: DRMManagementTableViewControllerFactory {
    func make(publication: Publication, delegate: ReaderModuleDelegate?) -> DRMManagementTableViewController {
        let controller =
            storyboards.drm.instantiateViewController(withIdentifier: "DRMManagementTableViewController") as! DRMManagementTableViewController
        controller.moduleDelegate = delegate
        controller.viewModel = DRMViewModel.make(publication: publication, presentingViewController: controller)
        return controller
    }
}

/// This is a wrapper for the "OutlineTableView" to encapsulate the  "Cancel" button behaviour
class OutlineHostingController: UIHostingController<OutlineTableView> {
    override public init(rootView: OutlineTableView) {
        super.init(rootView: rootView)
        self.overrideUserInterfaceStyle = .light
        
        self.navigationItem.setLeftBarButton(UIBarButtonItem(barButtonSystemItem: .cancel, target: self, action: #selector(cancelButtonPressed)), animated: true)
        self.navigationItem.titleView?.tintColor = UIColor.black
        self.navigationItem.titleView?.overrideUserInterfaceStyle = .light
    }
    
    @MainActor @objc required dynamic init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    @objc func cancelButtonPressed(_ sender: UIBarItem) {
        self.navigationController?.dismiss(animated: true, completion: nil)
    }
}
