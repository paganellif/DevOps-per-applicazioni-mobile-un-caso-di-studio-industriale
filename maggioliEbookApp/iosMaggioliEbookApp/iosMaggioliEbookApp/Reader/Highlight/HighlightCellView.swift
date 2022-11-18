//
//  HighlightCellView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import UIKit
import SwiftUI
import shared

struct HighlightCellView: View {
    let highlight: Highlight
    
    var body: some View {
        HStack {
            Rectangle()
                .fill(Color(highlight.tintToUIColor()))
                .frame(maxWidth: 20, maxHeight: .infinity)
            
            Text(highlight.toLocator().text.sanitized().highlight ?? "")
                .frame(maxWidth: .infinity, alignment: .topLeading)
                .padding()
            
            Spacer()
        }
    }
}
