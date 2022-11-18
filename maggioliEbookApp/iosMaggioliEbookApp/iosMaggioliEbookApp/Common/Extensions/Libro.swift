//
//  Libro.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 04/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import Combine
import shared

///
///
///
extension Libro: Identifiable {
    public var id: String {
        isbn!
    }
    
    func url() -> URL {
        do {
            // Path relative to Documents/.
            let files = FileManager.default
            let documents = try files.url(for: .documentDirectory, in: .userDomainMask, appropriateFor: nil, create: true)
            
            return documents.appendingPathComponent("\(self.isbn!).epub")
        } catch {
            print(error)
            return URL(fileReferenceLiteralResourceName: "")
        }
    }
}

