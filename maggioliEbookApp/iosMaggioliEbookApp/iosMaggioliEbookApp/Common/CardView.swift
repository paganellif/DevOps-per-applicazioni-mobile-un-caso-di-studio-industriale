//
//  CardView.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 26/09/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import SwiftUI
import shared

struct CardView: View {
    
    var app: AppModule
    let book: Libro
    var viewModel: FavoriteViewModel
    
    var body: some View {
        HStack {
            CoverView(isbn: self.book.isbn!)
                .scaledToFill()
                .frame(width: 120, height: 180)
                .aspectRatio(3 / 2, contentMode: .fill)
                .cornerRadius(12)
                .padding(.vertical)
                .shadow(radius: 4)
                .onTapGesture {
                    self.app.library.library.openBook(self.book, forPresentation: true, sender: self.app.navControllerNC)
                        .receive(on: DispatchQueue.main)
                        .sink { completion in
                            if case .failure(let error) = completion {
                                self.app.presentError(error, from: self.app.navControllerNC)
                            }
                        } receiveValue: { pub in
                            self.app.libraryDidSelectPublication(pub, book: self.book){
                                print("DONE")
                            }
                        }
                        .store(in: &self.app.subscriptions)
                }
            
            VStack(alignment: .leading, spacing: 10) {
                Text(book.name!)
                    .font(.title3)
                    .bold()
                
                Text(book.isbn!)
                    .font(.caption2)
                                
                if book.autori != nil && !book.autori!.isEmpty {
                    Text(book.autori![0])
                        .font(.caption)
                }
                
                FavoriteIconView(viewModel: self.viewModel, isbn: self.book.isbn!)
            }
        }
    }
}
