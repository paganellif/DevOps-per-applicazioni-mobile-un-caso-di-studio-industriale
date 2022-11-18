//
//  BookmarkCellView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct BookmarkCellView: View {
    static var dateFormatter: DateFormatter {
        let formatter = DateFormatter()
        formatter.dateStyle = .medium
        return formatter
    }
    
    let bookmark: Bookmark
    var body: some View {
        HStack {
            VStack {
                Text(bookmark.toLocator().title ?? "")
                    .frame(maxWidth: .infinity, alignment: .leading)
                    .font(.headline)
                Text(bookmark.positionText ?? "")
                    .font(.footnote)
                    .frame(maxWidth: .infinity, alignment: .leading)
            }
                .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .leading)
            
            Text(BookmarkCellView.dateFormatter.string(from: Date(milliseconds: bookmark.createdDate!.int64Value)))
                .font(.footnote)
                .frame(maxHeight: .infinity, alignment: .bottomTrailing)
        }
        .padding()
    }
}
