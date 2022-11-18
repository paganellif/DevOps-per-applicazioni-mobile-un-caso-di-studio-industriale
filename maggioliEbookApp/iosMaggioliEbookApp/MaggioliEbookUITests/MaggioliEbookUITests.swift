//
//  MaggioliEbookUITests.swift
//  MaggioliEbookUITests
//
//  Created by Filippo Paganelli on 30/09/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import XCTest

final class MaggioliEbookUITests: XCTestCase {

    override func setUp() {
        super.setUp()

        let app = XCUIApplication()
        setupSnapshot(app)
        app.launch()
    }

    func testExample() {
        snapshot("0Launch")

        XCUIDevice.shared.orientation = UIDeviceOrientation.landscapeLeft
        snapshot("1LandscapeLeft")

        XCUIDevice.shared.orientation = UIDeviceOrientation.landscapeRight
        snapshot("2LandscapeRight")

        XCUIDevice.shared.orientation = UIDeviceOrientation.portrait
        snapshot("3Portrait")
    }
}
