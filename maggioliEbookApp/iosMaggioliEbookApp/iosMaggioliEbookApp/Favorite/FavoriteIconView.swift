//
//  FavoriteIconView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 17/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI

struct FavoriteIconView: View {
    @State var favorite: Bool
    var viewModel: FavoriteViewModel
    var isbn: String
    
    init(viewModel: FavoriteViewModel, isbn: String) {
        self.viewModel = viewModel
        self.isbn = isbn
        self.favorite = viewModel.favorites.contains(isbn)
    }
    
    var body: some View {
        HStack {
            if self.favorite {
                Image(systemName: "heart.fill")
                    .foregroundColor(Color("AccentColor"))
                    .padding(.trailing, 18)
            } else {
                Image(systemName: "heart")
                    .foregroundColor(Color("AccentColor"))
                    .padding(.trailing, 18)
            }
        }
        .onTapGesture {
            self.favorite.toggle()
            
            if self.favorite {
                self.viewModel.addFavorite(isbn: self.isbn)
            } else {
                self.viewModel.removeFavorite(isbn: self.isbn)
            }
        }
    }
}
