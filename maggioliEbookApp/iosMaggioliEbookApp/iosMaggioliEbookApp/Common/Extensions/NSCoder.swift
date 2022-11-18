//
//  NSCoder.swift
//  iosMaggioliEbookApp
//
//  Created by Filippo Paganelli on 18/10/22.
//  Copyright © 2022 orgName. All rights reserved.
//

import Foundation

extension NSCoder {
  class func empty() -> NSCoder {
    let data = NSMutableData()
      let archiver = NSKeyedArchiver(forWritingWith: data)
    archiver.finishEncoding()
      return NSKeyedUnarchiver(forReadingWith: data as Data)
  }
}
