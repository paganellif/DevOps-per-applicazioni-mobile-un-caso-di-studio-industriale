//
//  Extensions.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 02/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation
import shared

///
///
///
extension Libro: Identifiable {
    public var id: String {
        isbn!
    }
}


extension Data {
    static func fromKotlinByteArray(byteArray: KotlinByteArray?) -> Data {
        guard byteArray != nil else { return Data() }
        
        print("SIZE BYTEARRAY: \(byteArray!.size)")
        
        var tmp = [UInt8]()
        
        let byteArrayIterator = byteArray!.iterator()
        
        while (byteArrayIterator.hasNext()) {
            let nextByte: Int8 = byteArrayIterator.nextByte()
            
            if nextByte.signum() < 0 {
                tmp.append(UInt8.max)
            } else {
                tmp.append(UInt8(nextByte))
            }
        }
        
        print("CONVERTED DATA SIZE: \(tmp.count)")
        
        return Data(tmp)
    }
}
