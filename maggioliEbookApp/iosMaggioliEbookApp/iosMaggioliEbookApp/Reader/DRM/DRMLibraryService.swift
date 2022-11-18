//
//  DRMLibraryService.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Combine
import Foundation
import R2Shared


struct DRMFulfilledPublication {
    let localURL: URL
    let suggestedFilename: String
}

protocol DRMLibraryService {
    
    /// Returns the `ContentProtection` which will be provided to the `Streamer`, to unlock
    /// publications.
    var contentProtection: ContentProtection? { get }
    
    /// Returns whether this DRM can fulfill the given file into a protected publication.
    func canFulfill(_ file: URL) -> Bool
    
    /// Fulfills the given file to the fully protected publication.
    func fulfill(_ file: URL) -> AnyPublisher<DRMFulfilledPublication?, Error>
    
}
