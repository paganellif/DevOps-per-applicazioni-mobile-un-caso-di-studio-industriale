//
//  EPUBViewController.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import UIKit
import R2Shared
import R2Navigator
import shared

class EPUBViewController: ReaderViewController {
    var popoverUserconfigurationAnchor: UIBarButtonItem?
    var userSettingNavigationController: UserSettingsNavigationController
    
    init(publication: Publication, locator: Locator?, isbn: String, resourcesServer: ResourcesServer) {
        var navigatorEditingActions = EditingAction.defaultActions
        navigatorEditingActions.append(EditingAction(title: "Highlight", action: #selector(highlightSelection)))
        var navigatorConfig = EPUBNavigatorViewController.Configuration()
        navigatorConfig.editingActions = navigatorEditingActions
        
        let navigator = EPUBNavigatorViewController(publication: publication, initialLocation: locator, resourcesServer: resourcesServer, config: navigatorConfig)

        let settingsStoryboard = UIStoryboard(name: "UserSettings", bundle: nil)
        userSettingNavigationController = settingsStoryboard.instantiateViewController(withIdentifier: "UserSettingsNavigationController") as! UserSettingsNavigationController
        userSettingNavigationController.fontSelectionViewController =
            (settingsStoryboard.instantiateViewController(withIdentifier: "FontSelectionViewController") as! FontSelectionViewController)
        userSettingNavigationController.advancedSettingsViewController =
            (settingsStoryboard.instantiateViewController(withIdentifier: "AdvancedSettingsViewController") as! AdvancedSettingsViewController)
        
        super.init(navigator: navigator, publication: publication, isbn: isbn)
        
        navigator.delegate = self
    }
    
    var epubNavigator: EPUBNavigatorViewController {
        return navigator as! EPUBNavigatorViewController
    }

    override func viewDidLoad() {
        super.viewDidLoad()
  
        /// Set initial UI appearance.
        if let appearance = publication.userProperties.getProperty(reference: ReadiumCSSReference.appearance.rawValue) {
            setUIColor(for: appearance)
        }
        
        let userSettings = epubNavigator.userSettings
        userSettingNavigationController.userSettings = userSettings
        userSettingNavigationController.modalPresentationStyle = .popover
        userSettingNavigationController.usdelegate = self
        userSettingNavigationController.userSettingsTableViewController.publication = publication
        

        publication.userSettingsUIPresetUpdated = { [weak self] preset in
            guard let `self` = self, let presetScrollValue:Bool = preset?[.scroll] else {
                return
            }
            
            if let scroll = self.userSettingNavigationController.userSettings.userProperties.getProperty(reference: ReadiumCSSReference.scroll.rawValue) as? Switchable {
                if scroll.on != presetScrollValue {
                    self.userSettingNavigationController.scrollModeDidChange()
                }
            }
        }
        
        // Always adopt a light interface style.
        self.overrideUserInterfaceStyle = .light
    }
    
    override func viewDidAppear(_ animated: Bool) {
        super.viewDidAppear(animated)
    }

    override open func viewWillDisappear(_ animated: Bool) {
        super.viewWillDisappear(animated)
        
        epubNavigator.userSettings.save()
    }

    override func makeNavigationBarButtons() -> [UIBarButtonItem] {
        var buttons = super.makeNavigationBarButtons()

        // User configuration button
        let userSettingsButton = UIBarButtonItem(image: #imageLiteral(resourceName: "settingsIcon"), style: .plain, target: self, action: #selector(presentUserSettings))
        buttons.insert(userSettingsButton, at: 1)
        popoverUserconfigurationAnchor = userSettingsButton

        return buttons
    }
    
    override var currentBookmark: Bookmark? {
        guard let locator = navigator.currentLocation else {
            return nil
        }
        
        let resource = publication.readingOrder.firstIndex(withHREF: locator.href)!
        let createdDate = KotlinLong(value: Date().millisecondsSince1970)
        
        return Bookmark(id: nil, createdDate: createdDate, isbn: isbn, publicationId: publication.metadata.identifier ?? publication.metadata.title, resourceIndex: Int64(resource), resourceHref: locator.href, resourceType: locator.type, resourceTitle: locator.title ?? "", location: locator.jsonString ?? "{}", locatorText: locator.text.jsonString ?? "{}")
    }
    
    @objc func presentUserSettings() {
        let popoverPresentationController = userSettingNavigationController.popoverPresentationController!
        
        popoverPresentationController.delegate = self
        popoverPresentationController.barButtonItem = popoverUserconfigurationAnchor

        userSettingNavigationController.publication = publication
        present(userSettingNavigationController, animated: true) {
            // Makes sure that the popover is dismissed also when tapping on one of the other UIBarButtonItems.
            // ie. http://karmeye.com/2014/11/20/ios8-popovers-and-passthroughviews/
            popoverPresentationController.passthroughViews = nil
        }
    }

    @objc func highlightSelection() {
        if let navigator = navigator as? SelectableNavigator, let selection = navigator.currentSelection {
            //let highlight = Highlight(bookId: bookId, locator: selection.locator, color: .yellow)
            let createdDate = KotlinLong(value: Date().millisecondsSince1970)
            let color = HighlightColor.yellow.rawValue
            let highlight = Highlight(id: nil, createdDate: createdDate, isbn: isbn, style: "", tint: color, href: selection.locator.href, type: selection.locator.type, title: selection.locator.title, totalProgression: 0, location: selection.locator.jsonString ?? "{}", text: selection.locator.text.jsonString ?? "{}", annotation: "")
            saveHighlight(highlight)
            
            navigator.clearSelection()
        }
    }
}

extension EPUBViewController: EPUBNavigatorDelegate {}

extension EPUBViewController: UIGestureRecognizerDelegate {
    
    func gestureRecognizer(_ gestureRecognizer: UIGestureRecognizer, shouldRecognizeSimultaneouslyWith otherGestureRecognizer: UIGestureRecognizer) -> Bool {
        return true
    }
}

extension EPUBViewController: UserSettingsNavigationControllerDelegate {

    internal func getUserSettings() -> UserSettings {
        return epubNavigator.userSettings
    }
    
    internal func updateUserSettingsStyle() {
        DispatchQueue.main.async {
            self.epubNavigator.updateUserSettingStyle()
        }
    }
    
    /// Synchronyze the UI appearance to the UserSettings.Appearance.
    ///
    /// - Parameter appearance: The appearance.
    internal func setUIColor(for appearance: UserProperty) {
        self.appearanceChanged(appearance)
        let colors = AssociatedColors.getColors(for: appearance)
        
        navigator.view.backgroundColor = colors.mainColor
        view.backgroundColor = colors.mainColor
        //
        navigationController?.navigationBar.barTintColor = colors.mainColor
        navigationController?.navigationBar.tintColor = colors.textColor
        
        navigationController?.navigationBar.titleTextAttributes = [NSAttributedString.Key.foregroundColor: colors.textColor]
    }
}
