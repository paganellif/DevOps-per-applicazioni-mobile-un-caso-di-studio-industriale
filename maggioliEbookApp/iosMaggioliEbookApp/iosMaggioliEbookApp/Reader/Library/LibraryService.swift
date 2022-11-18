//
//  LibraryService.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Combine
import Foundation
import UIKit
import R2Shared
import R2Streamer
import shared

protocol LibraryServiceDelegate: AnyObject {
    func confirmImportingDuplicatePublication(withTitle title: String) -> AnyPublisher<Bool, Never>
}

/// The Library service is used to:
///
/// - Import new publications (`Book` in the database).
/// - Remove existing publications from the bookshelf.
/// - Open publications for presentation in a navigator.
final class LibraryService: Loggable {
    
    weak var delegate: LibraryServiceDelegate?
    private let streamer: Streamer
    private let publicationServer: PublicationServer
    private let httpClient: HTTPClient
    private var drmLibraryServices = [DRMLibraryService]()
    
    private let convertPdf2Epub = ConvertPdf2EpubUseCase.init()
    private let convertByteArray = ByteArrayConverter.init()
    private let getBookProgression = GetBookProgressionUseCase.init()
    
    init(publicationServer: PublicationServer, httpClient: HTTPClient) {
        self.publicationServer = publicationServer
        self.httpClient = httpClient
        
#if LCP
        drmLibraryServices.append(LCPLibraryService())
#endif
        
        streamer = Streamer(
            contentProtections: drmLibraryServices.compactMap { $0.contentProtection }
        )
    }
    
    // MARK: Opening
    
    /// Opens the Readium 2 Publication for the given `book`.
    ///
    /// If the `Publication` is intended to be presented in a navigator, set `forPresentation`.
    func openBook(_ book: Libro, forPresentation prepareForPresentation: Bool, sender: UIViewController) -> AnyPublisher<Publication, LibraryError> {
        Paths.makeDocumentURL(title: book.name!, mediaType: MediaType.epub)
            .flatMap{ self._openBook($0, book: book, forPresentation: prepareForPresentation, sender: sender) }
            .eraseToAnyPublisher()
    }
    
    private func _openBook(_ bookPath: URL, book: Libro, forPresentation prepareForPresentation: Bool, sender: UIViewController) -> AnyPublisher<Publication, LibraryError> {
        Future(on: DispatchQueue.main) { promise in
            // Path relative to Documents/.
            let files = FileManager.default
            
            print("EPUB FILE LOCATION: \(bookPath.relativePath)")
            
            // TODO: skip download if file already exist
            if !Paths.checkIfFileExists(title: book.name, mediaType: MediaType.epub) {
                self.convertPdf2Epub.invokeNative(isbn: book.isbn!) { result in
                    if result != nil {
                        let convertedEpub = self.convertByteArray.toData(byteArray: result!)
                        print("CONVERTING BOOK")
                        do {
                            try convertedEpub.write(to: bookPath)
                            print("FILE \(bookPath) SAVED")
                            print("FILE SIZE: \(files.contents(atPath: bookPath.relativePath)!.count)")
                            
                            promise(.success(bookPath))
                        } catch {
                            print("ERROR SAVING CONVERTED EPUB")
                            print(LibraryError.openFailed(error))
                            
                            promise(.failure(LibraryError.openFailed(error)))
                        }
                    }
                } onError: { throwable in
                    print(throwable.getStackTrace())
                    print(LibraryError.openFailed(throwable.asError()))
                }
            } else {
                print("EPUB FILE ALREADY EXIST")
                //print("FILE SIZE: \(files.contents(atPath: bookPath.relativePath)!.count)")
                promise(.success(bookPath))
            }
        }
        .flatMap { self.openPublication(at: $0, allowUserInteraction: true, sender: sender) }
        .flatMap { (pub, _) in self.checkIsReadable(publication: pub) }
        .handleEvents(receiveOutput: { self.preparePresentation(of: $0, book: book) })
        .eraseToAnyPublisher()
    }
    
    /// Opens the Readium 2 Publication at the given `url`.
    private func openPublication(at url: URL, allowUserInteraction: Bool, sender: UIViewController?) -> AnyPublisher<(Publication, MediaType), LibraryError> {
        Future(on: .global()) { promise in
            let mediaType = MediaType.epub // asset.mediaType()
            let asset = FileAsset(url: url, mediaType: mediaType.type)
            
            guard let mediaType = asset.mediaType() else {
                promise(.failure(.openFailed(Publication.OpeningError.unsupportedFormat)))
                return
            }
            
            print("STREAMER OPENING")
            self.streamer.open(asset: asset, allowUserInteraction: allowUserInteraction, sender: sender) { result in
                switch result {
                case .success(let publication):
                    promise(.success((publication, mediaType)))
                case .failure(let error):
                    print(error.errorDescription)
                    promise(.failure(.openFailed(error)))
                case .cancelled:
                    promise(.failure(.cancelled))
                }
            }
        }.eraseToAnyPublisher()
    }
    
    /// Checks if the publication is not still locked by a DRM.
    private func checkIsReadable(publication: Publication) -> AnyPublisher<Publication, LibraryError> {
        print("CHECK IS READABLE")
        guard !publication.isRestricted else {
            if let error = publication.protectionError {
                return .fail(.openFailed(error))
            } else {
                return .fail(.cancelled)
            }
        }
        return .just(publication)
    }
    
    private func preparePresentation(of publication: Publication, book: Libro) {
        publicationServer.removeAll()
        print("PREPARING PRESENTATION")
        do {
            try publicationServer.add(publication)
        } catch {
            log(.error, error)
        }
    }
    
    /// Downloads `sourceURL` if it locates a remote file.
    private func downloadIfNeeded(_ url: URL, progress: @escaping (Double) -> Void) -> AnyPublisher<URL, LibraryError> {
        guard !url.isFileURL, url.scheme != nil else {
            return .just(url)
        }
        
        return httpClient.download(url, progress: progress)
            .map { $0.file }
            .mapError { .downloadFailed($0) }
            .eraseToAnyPublisher()
    }
    
    /// Fulfills the given `url` if it's a DRM license file.
    private func fulfillIfNeeded(_ url: URL) -> AnyPublisher<URL, LibraryError> {
        guard let drmService = drmLibraryServices.first(where: { $0.canFulfill(url) }) else {
            return .just(url)
        }
        
        return drmService.fulfill(url)
            .mapError { LibraryError.downloadFailed($0) }
            .flatMap { pub -> AnyPublisher<URL, LibraryError> in
                guard let url = pub?.localURL else {
                    return .fail(.cancelled)
                }
                return .just(url)
            }
            .eraseToAnyPublisher()
    }
    
    /// Moves the given `sourceURL` to the user Documents/ directory.
    private func moveToDocuments(from source: URL, title: String, mediaType: MediaType) -> AnyPublisher<URL, LibraryError> {
        Paths.makeDocumentURL(title: title, mediaType: mediaType)
            .flatMap { destination in
                Future(on: .global()) { promise in
                    // Necessary to read URL exported from the Files app, for example.
                    let shouldRelinquishAccess = source.startAccessingSecurityScopedResource()
                    defer {
                        if shouldRelinquishAccess {
                            source.stopAccessingSecurityScopedResource()
                        }
                    }
                    
                    do {
                        // If the source file is part of the app folder, we can move it. Otherwise we make a
                        // copy, to avoid deleting files from iCloud, for example.
                        if Paths.isAppFile(at: source) {
                            try FileManager.default.moveItem(at: source, to: destination)
                        } else {
                            try FileManager.default.copyItem(at: source, to: destination)
                        }
                        promise(.success(destination))
                    } catch {
                        promise(.failure(LibraryError.importFailed(error)))
                    }
                }
            }
            .eraseToAnyPublisher()
    }
    
    /// Imports the publication cover and return its path relative to the Covers/ folder.
    private func importCover(of publication: Publication) -> AnyPublisher<String?, LibraryError> {
        Future(on: .global()) { promise in
            guard let cover = publication.cover?.pngData() else {
                promise(.success(nil))
                return
            }
            let coverURL = Paths.covers.appendingUniquePathComponent()
            
            do {
                try cover.write(to: coverURL)
                promise(.success(coverURL.lastPathComponent))
            } catch {
                print(coverURL)
                print(error)
                promise(.failure(.importFailed(error)))
            }
            
        }.eraseToAnyPublisher()
    }
    
    private func confirmImportingDuplicate(book: Libro) -> AnyPublisher<Void, LibraryError> {
        guard let delegate = delegate else {
            return .just(())
        }
        
        return delegate.confirmImportingDuplicatePublication(withTitle: book.name!)
            .setFailureType(to: LibraryError.self)
            .flatMap { confirmed -> AnyPublisher<Void, LibraryError> in
                if confirmed {
                    return .just(())
                } else {
                    return .fail(.cancelled)
                }
            }
            .eraseToAnyPublisher()
    }
}
