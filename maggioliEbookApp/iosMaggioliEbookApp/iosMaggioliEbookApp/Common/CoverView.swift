//
//  CoverView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 04/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

let placeholder = UIImage(named: "default_cover_image")!

struct CoverView: View {
    
    @ObservedObject private var coverLoader: CoverLoader
    
    init(isbn: String) {
        self.coverLoader = CoverLoader(isbn)
    }
    
    var image: UIImage? {
        coverLoader.data.flatMap(UIImage.init)
        
    }
    
    var body: some View {
        Image(uiImage: image ?? placeholder)
            .resizable()
    }
}
