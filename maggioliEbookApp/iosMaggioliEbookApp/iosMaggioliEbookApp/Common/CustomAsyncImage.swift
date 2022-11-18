//
//  CustomAsyncImage.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 03/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

struct CustomAsyncImage<Content: View, Placeholder: View>: View {
    
    @State var uiImage: UIImage?

    let isbn: String
    let getImage: (_ isbn: String) async -> UIImage?
    let content: (Image) -> Content
    let placeholder: () -> Placeholder

    init(
        isbn: String,
        getImage: @escaping (_ isbn: String) async -> UIImage?,
        @ViewBuilder content: @escaping (Image) -> Content,
        @ViewBuilder placeholder: @escaping () -> Placeholder
    ){
        self.isbn = isbn
        self.getImage = getImage
        self.content = content
        self.placeholder = placeholder
    }

    var body: some View {
        if let uiImage = uiImage {
            content(Image(uiImage: uiImage))
        }else {
            placeholder()
                .task {
                    self.uiImage = await getImage(self.isbn)
                }
        }
    }
  }
