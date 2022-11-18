//
//  Toast.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import UIKit
import MBProgressHUD

/// Displays the given text in the view for the given duration.
func toast(_ text: String, on view: UIView, duration: TimeInterval) {
    let hud = MBProgressHUD.showAdded(to: view, animated: true)
    hud.mode = .text
    hud.label.text = text
    hud.hide(animated: true, afterDelay: duration)
}

/// Displays an activity indicator in the view.
///
/// - Returns: A closure to be called when the toast needs to be hidden.
func toastActivity(on view: UIView) -> () -> () {
    let hud = MBProgressHUD.showAdded(to: view, animated: true)
    hud.mode = .indeterminate
    return {
        hud.hide(animated: true)
    }
}
