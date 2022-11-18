//
//  MaggioliEbookUITestsLaunchTests.swift
//  MaggioliEbookUITests
//
//  Created by Filippo Paganelli on 30/09/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import XCTest

final class MaggioliEbookUITestsLaunchTests: XCTestCase {

    func testLaunch() throws {
        let app = XCUIApplication()
        setupSnapshot(app)
        app.launch()

        let attachment = XCTAttachment(screenshot: app.screenshot())
        attachment.name = "Launch Screen"
        attachment.lifetime = .keepAlways
        add(attachment)
    }
}
