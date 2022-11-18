//
//  OutlineViewModelLoader.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 19/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import Combine

// MARK: - Generic state management

protocol OutlineViewModelLoaderDelegate: AnyObject {
    associatedtype T
    
    var dataTask: AnyPublisher<[T], Error> { get }
    func setLoadedValues(_ values: [T])
}

// This loader contains a state enum which can be used for expressive UI (loading progress, error handling etc). For this, status overlay view can be used (see https://stackoverflow.com/a/61858358/2567725).
final class OutlineViewModelLoader<T, Delegate: OutlineViewModelLoaderDelegate> {
    weak var delegate: Delegate!
    private var state = State.ready
    
    enum State {
        case ready
        case loading(Combine.Cancellable)
        case loaded
        case error(Error)
    }
    
    init(delegate: Delegate) {
        self.delegate = delegate
    }
    
    func load() {
        assert(Thread.isMainThread)
        self.state = .loading(delegate.dataTask.sink(
            receiveCompletion: { completion in
                switch completion {
                case .finished:
                    break
                case let .failure(error):
                    self.state = .error(error)
                }
            },
            receiveValue: { value in
                self.state = .loaded
                self.delegate.setLoadedValues(value)
            }
        ))
    }

    func loadIfNeeded() {
        assert(Thread.isMainThread)
        guard case .ready = self.state else { return }
        self.load()
    }
}
