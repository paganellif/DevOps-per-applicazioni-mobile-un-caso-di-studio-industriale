//
//  DetailsTableViewController.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import UIKit
import R2Shared

protocol DetailsTableViewControllerFactory {
    func make(publication: Publication) -> DetailsTableViewController
}

final class DetailsTableViewController: UITableViewController {

    var publication: Publication!
    
    // Informations
    @IBOutlet weak var titleLabel: UILabel!
    @IBOutlet weak var idLabel: UILabel!

    override func viewDidLoad() {
        titleLabel.text = publication?.metadata.title
        idLabel.text = publication?.metadata.identifier
    }

}
