//
//  Bookmark.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 17/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import R2Shared

extension Bookmark: Identifiable {
    
    var positionText: String? {
        if let position = self.toLocator().locations.position {
            return String(format: NSLocalizedString("reader_outline_position_label", comment: "Outline bookmark label when the position is available"), position)
        } else if let progression = self.toLocator().locations.progression {
            return String(format: NSLocalizedString("reader_outline_progression_label", comment: "Outline bookmark label when the progression is available"), progression * 100)
        } else {
            return nil
        }
    }
    
    ///
    func toLocator() -> Locator {
        return Locator(href: self.resourceHref,
                       type: self.resourceType,
                       title: self.resourceTitle,
                       locations: Locator.Locations(jsonString: self.location),
                       text: Locator.Text(jsonString: self.locatorText)
        )
    }
}
