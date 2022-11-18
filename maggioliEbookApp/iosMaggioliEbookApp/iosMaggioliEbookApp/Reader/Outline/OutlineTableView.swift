//
//  OutlineTableView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import Combine
import R2Shared
import shared

typealias OutlineTableViewAdapter = (UIHostingController<OutlineTableView>, AnyPublisher<Locator, Never>)

protocol OutlineTableViewControllerFactory {
    func make(publication: Publication, isbn: String) -> OutlineTableViewAdapter
}

enum OutlineSection: Int {
    case tableOfContents = 0, bookmarks, pageList, landmarks, highlights
}

struct OutlineTableView: View {
    @ObservedObject private var bookmarksModel: BookmarksViewModel
    @ObservedObject private var highlightsModel: HighlightsViewModel
    @State private var selectedSection: OutlineSection = .tableOfContents
    
    // Outlines (list of links) to display for each section.
    private var outlines: [OutlineSection: [(level: Int, link: R2Shared.Link)]] = [:]
    
    init(publication: Publication, isbn: String) {
     
        func flatten(_ links: [R2Shared.Link], level: Int = 0) -> [(level: Int, link: R2Shared.Link)] {
            return links.flatMap { [(level, $0)] + flatten($0.children, level: level + 1) }
        }
        
        outlines = [
            .tableOfContents: flatten(publication.tableOfContents),
            .landmarks: flatten(publication.landmarks),
            .pageList: flatten(publication.pageList)
        ]
        
        bookmarksModel = BookmarksViewModel(isbn: isbn)
        highlightsModel = HighlightsViewModel(isbn: isbn)
    }
    
    var body: some View {
        VStack {
            OutlineTablePicker(selectedSection: $selectedSection)
            
            switch selectedSection {
            case .tableOfContents, .pageList, .landmarks:
                if let outline = outlines[selectedSection] {
                    List(outline.indices, id: \.self) { index in
                        let item = outline[index]
                        Text(String(repeating: "  ", count: item.level) + (item.link.title ?? item.link.href))
                            .frame(maxWidth: .infinity, alignment: .leading)
                            .contentShape(Rectangle())
                            .onTapGesture {
                                locatorSubject.send(Locator(link: item.link))
                            }
                    }
                } else {
                    preconditionFailure("Outline \(selectedSection) can't be nil!")
                }
                
            case .bookmarks:
                
                List(bookmarksModel.bookmarks, id: \.self) { bookmark in
                    BookmarkCellView(bookmark: bookmark)
                        .contentShape(Rectangle())
                        .onTapGesture {
                            locatorSubject.send(bookmark.toLocator())
                        }
                }
                .onAppear { self.bookmarksModel.loadIfNeeded() }
                
            case .highlights:
                List(highlightsModel.highlights, id: \.self) { highlight in
                    HighlightCellView(highlight: highlight)
                        .contentShape(Rectangle())
                        .listRowInsets(EdgeInsets()) // to remove padding at the left side
                        .onTapGesture {
                            locatorSubject.send(highlight.toLocator())
                        }
                }
                .onAppear { self.highlightsModel.loadIfNeeded() }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .top)
    }
    
    private let locatorSubject = PassthroughSubject<Locator, Never>()
    var goToLocatorPublisher: AnyPublisher<Locator, Never> {
        return locatorSubject.eraseToAnyPublisher()
    }
}

struct OutlineTablePicker: View {
    @Binding var selectedSection: OutlineSection
    
    var body: some View {
        Picker("", selection: $selectedSection, content: {
            Text(OutlineTableViewConstants.tabContents).tag(OutlineSection.tableOfContents)
            Text(OutlineTableViewConstants.tabBookmarks).tag(OutlineSection.bookmarks)
            Text(OutlineTableViewConstants.tabPagelist).tag(OutlineSection.pageList)
            Text(OutlineTableViewConstants.tabLandmarks).tag(OutlineSection.landmarks)
            Text(OutlineTableViewConstants.tabHighlights).tag(OutlineSection.highlights)
        })
        .pickerStyle(SegmentedPickerStyle())
    }
}

enum OutlineTableViewConstants {
    static let tabContents = NSLocalizedString("reader_outline_tab_contents", comment: "Outline contents tab name")
    static let tabBookmarks = NSLocalizedString("reader_outline_tab_bookmarks", comment: "Outline bookmarks tab name")
    static let tabPagelist = NSLocalizedString("reader_outline_tab_pagelist", comment: "Outline pagelist tab name")
    static let tabLandmarks = NSLocalizedString("reader_outline_tab_landmarks", comment: "Outline landmarks tab name")
    static let tabHighlights = NSLocalizedString("reader_outline_tab_highlights", comment: "Outline highlights tab name")
}
