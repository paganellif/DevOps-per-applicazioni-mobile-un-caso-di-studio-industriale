//
//  ReaderViewController.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Combine
import SafariServices
import UIKit
import R2Navigator
import R2Shared
import SwiftSoup
import WebKit
import SwiftUI
import shared

/// This class is meant to be subclassed by each publication format view controller. It contains the shared behavior, eg. navigation bar toggling.
class ReaderViewController: UIViewController, Loggable {
    weak var moduleDelegate: ReaderFormatModuleDelegate?
    
    let navigator: UIViewController & Navigator
    let publication: Publication
    let isbn: String
    
    private(set) var stackView: UIStackView!
    private lazy var positionLabel = UILabel()
    private var subscriptions = Set<AnyCancellable>()
    
    private var searchViewModel: SearchViewModel?
    private var searchViewController: UIHostingController<SearchView>?
    
    private let getHighlightsByIsbnUseCase = GetBookHighlightsUseCase.init()
    private let getHighlightUseCase = GetHighlightUseCase.init()
    private let addHighlightUseCase = AddHighlightUseCase.init()
    private let removeHighlightUseCase = RemoveHighlightUseCase.init()
    
    private let getBookmarksByIsbnUseCase = GetBookBookmarksUseCase.init()
    private let addBookmarkUseCase = AddBookmarkForBookUseCase.init()
    private let removeBookmarkUseCase = RemoveBookmarkForBookUseCase.init()
    
    private let addBookProgression = AddBookProgressionUseCase.init()
    private let getBookProgression = GetBookProgressionUseCase.init()
    
    /// This regex matches any string with at least 2 consecutive letters (not limited to ASCII).
    /// It's used when evaluating whether to display the body of a noteref referrer as the note's title.
    /// I.e. a `*` or `1` would not be used as a title, but `on` or `好書` would.
    private static var noterefTitleRegex: NSRegularExpression = {
        return try! NSRegularExpression(pattern: "[\\p{Ll}\\p{Lu}\\p{Lt}\\p{Lo}]{2}")
    }()
    
    private var highlightContextMenu: UIHostingController<HighlightContextMenu>?
    private let highlightDecorationGroup = "highlights"
    private var currentHighlightCancellable: AnyCancellable?
    
    init(navigator: UIViewController & Navigator, publication: Publication, isbn: String) {
        self.navigator = navigator
        self.publication = publication
        self.isbn = isbn
        
        super.init(nibName: nil, bundle: nil)
        
        addHighlightDecorationsObserverOnce()
        updateHighlightDecorations()
    
        NotificationCenter.default.addObserver(self, selector: #selector(voiceOverStatusDidChange), name: UIAccessibility.voiceOverStatusDidChangeNotification, object: nil)
    }
    
    @available(*, unavailable)
    required init?(coder aDecoder: NSCoder) {
        fatalError("init(coder:) has not been implemented")
    }
    
    deinit {
        NotificationCenter.default.removeObserver(self)
    }

    override func viewDidLoad() {
        super.viewDidLoad()
        
        view.backgroundColor = .white
      
        navigationItem.rightBarButtonItems = makeNavigationBarButtons()
        updateNavigationBar(animated: false)
        
        stackView = UIStackView(frame: view.bounds)
        stackView.distribution = .fill
        stackView.axis = .vertical
        view.addSubview(stackView)
        stackView.translatesAutoresizingMaskIntoConstraints = false
        let topConstraint = stackView.topAnchor.constraint(equalTo: view.topAnchor)
        // `accessibilityTopMargin` takes precedence when VoiceOver is enabled.
        topConstraint.priority = .defaultHigh
        NSLayoutConstraint.activate([
            topConstraint,
            stackView.rightAnchor.constraint(equalTo: view.rightAnchor),
            stackView.bottomAnchor.constraint(equalTo: view.bottomAnchor),
            stackView.leftAnchor.constraint(equalTo: view.leftAnchor)
        ])

        addChild(navigator)
        stackView.addArrangedSubview(navigator.view)
        navigator.didMove(toParent: self)
        
        stackView.addArrangedSubview(accessibilityToolbar)
        
        positionLabel.translatesAutoresizingMaskIntoConstraints = false
        positionLabel.font = .systemFont(ofSize: 12)
        positionLabel.textColor = .darkGray
        view.addSubview(positionLabel)
        NSLayoutConstraint.activate([
            positionLabel.centerXAnchor.constraint(equalTo: view.centerXAnchor),
            positionLabel.bottomAnchor.constraint(equalTo: navigator.view.bottomAnchor, constant: -20)
        ])
        
        // Always adopt a light interface style.
        self.overrideUserInterfaceStyle = .light
    }
    
    override func willMove(toParent parent: UIViewController?) {
        // Restore library's default UI colors
        navigationController?.navigationBar.tintColor = .black
        navigationController?.navigationBar.barTintColor = .white
    }
    
    // MARK: - Navigation bar
    
    private var navigationBarHidden: Bool = true {
        didSet {
            updateNavigationBar()
        }
    }
    
    func makeNavigationBarButtons() -> [UIBarButtonItem] {
        var buttons: [UIBarButtonItem] = []
        // Table of Contents
        buttons.append(UIBarButtonItem(image: #imageLiteral(resourceName: "menuIcon"), style: .plain, target: self, action: #selector(presentOutline)))
        // DRM management
        if publication.isProtected {
            buttons.append(UIBarButtonItem(image: #imageLiteral(resourceName: "drm"), style: .plain, target: self, action: #selector(presentDRMManagement)))
        }
        // Bookmarks
        buttons.append(UIBarButtonItem(image: #imageLiteral(resourceName: "bookmark"), style: .plain, target: self, action: #selector(bookmarkCurrentPosition)))
        
        // Search
        if publication._isSearchable {
            buttons.append(UIBarButtonItem(image: UIImage(systemName: "magnifyingglass"), style: .plain, target: self, action: #selector(showSearchUI)))
        }
        
        return buttons
    }
    
    func toggleNavigationBar() {
        navigationBarHidden = !navigationBarHidden
    }
    
    func updateNavigationBar(animated: Bool = true) {
        let hidden = navigationBarHidden && !UIAccessibility.isVoiceOverRunning
        navigationController?.setNavigationBarHidden(hidden, animated: animated)
        setNeedsStatusBarAppearanceUpdate()
    }
    
    override var preferredStatusBarUpdateAnimation: UIStatusBarAnimation {
        return .slide
    }
    
    override var prefersStatusBarHidden: Bool {
        return navigationBarHidden && !UIAccessibility.isVoiceOverRunning
    }
    
    // MARK: - Locations
    /// FIXME: This should be implemented in a shared Navigator interface, using Locators.
    
    var currentBookmark: Bookmark? {
        fatalError("Not implemented")
    }
    
    // MARK: - Outlines

    @objc func presentOutline() {
        guard let locatorPublisher = moduleDelegate?.presentOutline(of: publication, isbn: isbn, from: self) else {
             return
        }
            
        locatorPublisher
            .sink(receiveValue: { locator in
                self.navigator.go(to: locator, animated: false) {
                    self.dismiss(animated: true)
                }
            })
            .store(in: &subscriptions)
    }
    
    private var colorScheme = ColorScheme()
    func appearanceChanged(_ appearance: UserProperty) {
        colorScheme.update(with: appearance)
    }
    
    // MARK: - Bookmarks
    
    @objc func bookmarkCurrentPosition() {
        guard let bookmark = currentBookmark else {
            return
        }
        
        addBookmarkUseCase.invokeNative(id: bookmark.id, createdDate: bookmark.createdDate, isbn: bookmark.isbn, publicationId: bookmark.publicationId, resourceIndex: bookmark.resourceIndex, resourceHref: bookmark.resourceHref, resourceType: bookmark.resourceType, resourceTitle: bookmark.resourceTitle, location: bookmark.location, locatorText: bookmark.locatorText) { _ in
            DispatchQueue.main.async {
                print("Added Bookmark for book \(bookmark.isbn)")
                toast(NSLocalizedString("reader_bookmark_success_message", comment: "Success message when adding a bookmark"), on: self.view, duration: 1)
            }
        } onError: { throwable in
            print(throwable.getStackTrace())
            toast(NSLocalizedString("reader_bookmark_failure_message", comment: "Error message when adding a new bookmark failed"), on: self.view, duration: 2)
        }
    }
    
    // MARK: - Search
    @objc func showSearchUI() {
        if searchViewModel == nil {
            searchViewModel = SearchViewModel(publication: publication)
            searchViewModel?.$selectedLocator.sink(receiveValue: { locator in
                self.searchViewController?.dismiss(animated: true, completion: nil)
                if let locator = locator {
                    self.navigator.go(to: locator, animated: true) {
                        if let decorator = self.navigator as? DecorableNavigator {
                            let decoration = Decoration(id: "selectedSearchResult", locator: locator, style: Decoration.Style.highlight(tint: .yellow, isActive: false))
                            decorator.apply(decorations: [decoration], in: "search")
                        }
                    }
                }
            }).store(in: &subscriptions)
        }
        
        let searchView = SearchView(viewModel: searchViewModel!)
        let vc = UIHostingController(rootView: searchView)
        vc.modalPresentationStyle = .pageSheet
        vc.overrideUserInterfaceStyle = .light
        present(vc, animated: true, completion: nil)
        searchViewController = vc
    }
    
    // MARK: - Highlights
    
    private func addHighlightDecorationsObserverOnce() {
        if let decorator = self.navigator as? DecorableNavigator {
            decorator.observeDecorationInteractions(inGroup: highlightDecorationGroup) { event in
                self.activateDecoration(event)
            }
        }
    }
    
    private func updateHighlightDecorations() {
        Future<[Highlight], Error>(on: DispatchQueue.main) { promise in
            self.getHighlightsByIsbnUseCase.invokeNative(isbn: self.isbn) { results in
                print("UPDATE HIGHLIGHTS: \(results)")
                promise(.success(results))
            } onError: { throwable in
                print(throwable.getStackTrace())
                promise(.failure(throwable.asError()))
            }
        }
        .assertNoFailure()
        .sink { highlights in
             if let decorator = self.navigator as? DecorableNavigator {
                 let decorations = highlights.map {
                     Decoration(id: $0.id!.stringValue, locator: $0.toLocator(), style: .highlight(tint: $0.tintToUIColor(), isActive: false))
                 }
                 decorator.apply(decorations: decorations, in: self.highlightDecorationGroup)
             }
        }
        .store(in: &subscriptions)
    }

    private func activateDecoration(_ event: OnDecorationActivatedEvent) {
        currentHighlightCancellable = Future<Highlight, Error>(on: DispatchQueue.main) { promise in
            self.getHighlightUseCase.invokeNative(id: Int64(event.decoration.id)!) { result in
                    let highlight = result[0]
                    print("HIGHLIGHT: \(result)")
                    promise(.success(highlight))
            } onError: { throwable in
                print(throwable.getStackTrace())
                promise(.failure(throwable.asError()))
            }
        }.sink { completion in } receiveValue: { [weak self] highlight in
            guard let self = self else { return }
            self.activateDecoration(for: highlight, on: event)
        }
        
        
    }
    
    private func activateDecoration(for highlight: Highlight, on event: OnDecorationActivatedEvent) {
        if highlightContextMenu != nil {
            highlightContextMenu?.removeFromParent()
        }
        
        let menuView = HighlightContextMenu(colors: [.red, .green, .blue, .yellow],
                                            systemFontSize: 20,
                                            colorScheme: colorScheme)
        
        menuView.selectedColorPublisher.sink { color in
            self.currentHighlightCancellable?.cancel()
            self.updateHighlight(Int64(event.decoration.id)!, withColor: color)
            self.highlightContextMenu?.dismiss(animated: true, completion: nil)
        }
        .store(in: &subscriptions)
        
        menuView.selectedDeletePublisher.sink { _ in
            self.currentHighlightCancellable?.cancel()
            self.deleteHighlight(Int64(event.decoration.id)!)
            self.highlightContextMenu?.dismiss(animated: true, completion: nil)
        }
        .store(in: &subscriptions)
        
        self.highlightContextMenu = UIHostingController(rootView: menuView)
        
        highlightContextMenu!.preferredContentSize = menuView.preferredSize
        highlightContextMenu!.modalPresentationStyle = .popover
        highlightContextMenu!.view.backgroundColor = UIColor(colorScheme.mainColor)
        
        if let popoverController = highlightContextMenu!.popoverPresentationController {
            popoverController.permittedArrowDirections = .down
            popoverController.sourceRect = event.rect ?? .zero
            popoverController.sourceView = self.view
            popoverController.backgroundColor = .cyan
            popoverController.delegate = self
            present(highlightContextMenu!, animated: true, completion: nil)
        }
    }
    
    // MARK: - DRM
    
    @objc func presentDRMManagement() {
        guard publication.isProtected else {
            return
        }
        moduleDelegate?.presentDRM(for: publication, from: self)
    }
    
    // MARK: - Accessibility
    
    /// Constraint used to shift the content under the navigation bar, since it is always visible when VoiceOver is running.
    private lazy var accessibilityTopMargin: NSLayoutConstraint = {
        let topAnchor: NSLayoutYAxisAnchor = {
            if #available(iOS 11.0, *) {
                return self.view.safeAreaLayoutGuide.topAnchor
            } else {
                return self.topLayoutGuide.bottomAnchor
            }
        }()
        return self.stackView.topAnchor.constraint(equalTo: topAnchor)
    }()
    
    private lazy var accessibilityToolbar: UIToolbar = {
        func makeItem(_ item: UIBarButtonItem.SystemItem, label: String? = nil, action: UIKit.Selector? = nil) -> UIBarButtonItem {
            let button = UIBarButtonItem(barButtonSystemItem: item, target: (action != nil) ? self : nil, action: action)
            button.accessibilityLabel = label
            return button
        }
        
        let toolbar = UIToolbar(frame: .zero)
        toolbar.items = [
            makeItem(.flexibleSpace),
            makeItem(.rewind, label: NSLocalizedString("reader_backward_a11y_label", comment: "Accessibility label to go backward in the publication"), action: #selector(goBackward)),
            makeItem(.flexibleSpace),
            makeItem(.fastForward, label: NSLocalizedString("reader_forward_a11y_label", comment: "Accessibility label to go forward in the publication"), action: #selector(goForward)),
            makeItem(.flexibleSpace),
        ]
        toolbar.isHidden = !UIAccessibility.isVoiceOverRunning
        toolbar.tintColor = UIColor.black
        return toolbar
    }()
    
    private var isVoiceOverRunning = UIAccessibility.isVoiceOverRunning
    
    @objc private func voiceOverStatusDidChange() {
        let isRunning = UIAccessibility.isVoiceOverRunning
        // Avoids excessive settings refresh when the status didn't change.
        guard isVoiceOverRunning != isRunning else {
            return
        }
        isVoiceOverRunning = isRunning
        accessibilityTopMargin.isActive = isRunning
        accessibilityToolbar.isHidden = !isRunning
        updateNavigationBar()
    }
    
    @objc private func goBackward() {
        navigator.goBackward()
    }
    
    @objc private func goForward() {
        navigator.goForward()
    }
    
}

extension ReaderViewController: NavigatorDelegate {

    func navigator(_ navigator: Navigator, locationDidChange locator: Locator) {
        addBookProgression.invokeNative(isbn: isbn, progression: locator.jsonString ?? "{}") { _ in
            print("Saved book progression")
        } onError: { throwable in
            print(throwable.getStackTrace())
        }

        positionLabel.text = {
            if let position = locator.locations.position {
                return "\(position) / \(publication.positions.count)"
            } else if let progression = locator.locations.totalProgression {
                return "\(progression)%"
            } else {
                return nil
            }
        }()
    }
    
    func navigator(_ navigator: Navigator, presentExternalURL url: URL) {
        // SFSafariViewController crashes when given an URL without an HTTP scheme.
        guard ["http", "https"].contains(url.scheme?.lowercased() ?? "") else {
            return
        }
        present(SFSafariViewController(url: url), animated: true)
    }
    
    func navigator(_ navigator: Navigator, presentError error: NavigatorError) {
        moduleDelegate?.presentError(error, from: self)
    }
    
    func navigator(_ navigator: Navigator, shouldNavigateToNoteAt link: R2Shared.Link, content: String, referrer: String?) -> Bool {
    
        var title = referrer
        if let t = title {
            title = try? clean(t, .none())
        }
        if !suitableTitle(title) {
            title = nil
        }
        
        let content = (try? clean(content, .none())) ?? ""
        let page =
        """
        <html>
            <head>
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body>
                \(content)
            </body>
        </html>
        """
        
        let wk = WKWebView()
        wk.loadHTMLString(page, baseURL: nil)
        
        let vc = UIViewController()
        vc.view = wk
        vc.navigationItem.title = title
        vc.navigationItem.leftBarButtonItem = BarButtonItem(barButtonSystemItem: .done, actionHandler: { (item) in
            vc.dismiss(animated: true, completion: nil)
        })
        
        let nav = UINavigationController(rootViewController: vc)
        nav.modalPresentationStyle = .formSheet
        self.present(nav, animated: true, completion: nil)
        
        return false
    }
    
    /// Checks to ensure the title is non-nil and contains at least 2 letters.
    func suitableTitle(_ title: String?) -> Bool {
        guard let title = title else { return false }
        let range = NSRange(location: 0, length: title.utf16.count)
        let match = ReaderViewController.noterefTitleRegex.firstMatch(in: title, range: range)
        return match != nil
    }
    
}

extension ReaderViewController: VisualNavigatorDelegate {
    
    func navigator(_ navigator: VisualNavigator, didTapAt point: CGPoint) {
        // clear a current search highlight
        if let decorator = self.navigator as? DecorableNavigator {
            decorator.apply(decorations: [], in: "search")
        }
        
        let viewport = navigator.view.bounds
        // Skips to previous/next pages if the tap is on the content edges.
        let thresholdRange = 0...(0.2 * viewport.width)
        var moved = false
        if thresholdRange ~= point.x {
            moved = navigator.goLeft(animated: false)
        } else if thresholdRange ~= (viewport.maxX - point.x) {
            moved = navigator.goRight(animated: false)
        }
        
        if !moved {
            toggleNavigationBar()
        }
    }
    
}

// MARK: - Highlights management

extension ReaderViewController {
    func saveHighlight(_ highlight: Highlight) {
        
        Future<Any?, Error>(on: DispatchQueue.main) { promise in
            self.addHighlightUseCase.invokeNative(id: highlight.id, isbn: highlight.isbn, location: highlight.location, style: highlight.style, tint: highlight.tint, href: highlight.href, type: highlight.type, title: highlight.title, text: highlight.text, annotation: highlight.annotation) { _ in
                promise(.success(highlight.id))
            } onError: { throwable in
                print(throwable.getStackTrace())
                toast(NSLocalizedString("reader_highlight_failure_message", comment: "Error message when adding a new bookmark failed"), on: self.view, duration: 2)
                promise(.failure(throwable.asError()))
            }
        }
        .assertNoFailure()
        .sink { id in
            print("Added Highlight \(id) for book \(highlight.isbn)")
            toast(NSLocalizedString("reader_highlight_success_message", comment: "Success message when adding a bookmark"), on: self.view, duration: 1)
        }
        .store(in: &subscriptions)
    }

    func updateHighlight(_ highlightID: Int64, withColor color: HighlightColor) {
        
        Future<Highlight, Error>(on: DispatchQueue.main) { promise in
            self.getHighlightUseCase.invokeNative(id: highlightID) { result in
                
                if !result.isEmpty && result[0].id != nil {
                    promise(.success(result[0]))
                }
                
            } onError: { throwable in
                print(throwable.getStackTrace())
                toast(NSLocalizedString("reader_highlight_failure_message", comment: "Error message when updating a bookmark failed"), on: self.view, duration: 2)
                promise(.failure(throwable.asError()))
            }
        }.sink { completion in } receiveValue: { highlight in
            self.addHighlightUseCase.invokeNative(id: highlight.id, isbn: highlight.isbn, location: highlight.location, style: highlight.style, tint: color.rawValue, href: highlight.href, type: highlight.type, title: highlight.title, text: highlight.text, annotation: highlight.annotation) { _ in
                DispatchQueue.main.async {
                    print("Updated Highlight \(highlight) for book \(highlight.isbn)")
                    toast(NSLocalizedString("reader_highlight_success_message", comment: "Success message when updatind a bookmark"), on: self.view, duration: 1)
                }
            } onError: { throwable in
                print(throwable.getStackTrace())
                toast(NSLocalizedString("reader_highlight_failure_message", comment: "Error message when updating a bookmark failed"), on: self.view, duration: 2)
            }
        }
        .store(in: &subscriptions)
    }

    func deleteHighlight(_ highlightID: Int64)  {
        removeHighlightUseCase.invokeNative(id: highlightID) { _ in
            DispatchQueue.main.async {
                print("Deleted Highlight \(highlightID)")
                toast(NSLocalizedString("reader_highlight_success_message", comment: "Success message when deleting a bookmark"), on: self.view, duration: 1)
            }
        } onError: { throwable in
            print(throwable.getStackTrace())
            toast(NSLocalizedString("reader_highlight_failure_message", comment: "Error message when deleting a bookmark failed"), on: self.view, duration: 2)
        }
    }
}

extension ReaderViewController: UIPopoverPresentationControllerDelegate {
    // Prevent the popOver to be presented fullscreen on iPhones.
    func adaptivePresentationStyle(for controller: UIPresentationController, traitCollection: UITraitCollection) -> UIModalPresentationStyle {
        return .none
    }
}
