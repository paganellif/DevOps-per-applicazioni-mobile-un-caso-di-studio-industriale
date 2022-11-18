//
//  AboutViewModel.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 04/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation

class AboutViewModel: ObservableObject {
    
    @Published var author: String = NSLocalizedString("author_name", comment: "Caption for the author name in About screen")
    
    @Published var authorMail: String = "(\(NSLocalizedString("author_mail", comment: "Caption for the author mail in About screen")))"
    
    @Published var appVersion: String = (Bundle.main.infoDictionary?["CFBundleShortVersionString"] as? String) ?? ""
    
    @Published var bundleVersion: String = (Bundle.main.infoDictionary?["CFBundleVersion"] as? String) ?? ""
    
    @Published var copyright: String = NSLocalizedString("copyright", comment: "Caption for the copyright in About screen")
    
    @Published var logo: String = "maggioli"
}
