//
//  Highlight.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared
import UIKit
import R2Shared

enum HighlightColor: Int32, Codable {
    case red = 1
    case green = 2
    case blue = 3
    case yellow = 4
}

extension HighlightColor {
    var uiColor: UIColor {
        switch self {
        case .red:
            return .red
        case .green:
            return .green
        case .blue:
            return .blue
        case .yellow:
            return .yellow
        }
    }
}

extension Highlight: Identifiable {
    
    ///
    var color : HighlightColor {
        switch self.tint {
        case 1:
            return HighlightColor.red
        case 2:
            return HighlightColor.green
        case 3:
            return HighlightColor.blue
        case 4:
            return HighlightColor.yellow
        default:
            return HighlightColor.red
        }
    }
    
    ///
    func tintToUIColor() -> UIColor {
        switch self.color {
        case HighlightColor.red:
            return UIColor.red
        case HighlightColor.green:
            return UIColor.green
        case HighlightColor.blue:
            return UIColor.blue
        case HighlightColor.yellow:
            return UIColor.yellow
        }
    }
    
    ///
    func toLocator() -> Locator {
        return Locator(href: self.href,
                       type: self.type,
                       title: self.title,
                       locations: Locator.Locations(jsonString: self.location),
                       text: Locator.Text(jsonString: self.text)
        )
    }
}
