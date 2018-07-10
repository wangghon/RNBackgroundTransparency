//
//  ImageConverter.swift
//  RNBackgroundTransparency
//
//  Created by wanghongbo on 4/7/18.
//  Copyright © 2018 Facebook. All rights reserved.
//

import Foundation
import UIKit

@objc(ImageConverter)
class ImageConverter: NSObject {
  
  static func requiresMainQueueSetup() -> Bool {
    return true
  }
  
  func getImageFromWeb(_ urlString: String, closure: @escaping (UIImage?) -> ()) {
    guard let url = URL(string: urlString) else {
      return closure(nil)
    }
    
    let task = URLSession(configuration: .default).dataTask(with: url) { (data, response, error) in
      guard error == nil else {
        print("error: \(String(describing: error))")
        return closure(nil)
      }
      guard response != nil else {
        print("no response")
        return closure(nil)
      }
      guard data != nil else {
        print("no data")
        return closure(nil)
      }
      DispatchQueue.global().async {
        closure(UIImage(data: data!))
      }
    }
    
    task.resume()
  }
  
  func transparencyImageBG(_ image: UIImage) -> UIImage? {
    return transparencyImageBGByMask(image);
    }
  
  func transparencyImageBGByMask(_ image: UIImage) -> UIImage? {
    let colorMasking: [CGFloat] = [240, 255, 240, 255, 240, 255]
    let sz = image.size
    
    if let rawImageRef = image.cgImage {
      UIGraphicsBeginImageContext(sz)
      if let maskedImageRef = rawImageRef.copy(maskingColorComponents: colorMasking) {
        let context: CGContext = UIGraphicsGetCurrentContext()!
        context.translateBy(x: 0.0, y: sz.height)
        context.scaleBy(x: 1.0, y: -1.0)
        context.draw(maskedImageRef, in: CGRect(x:0, y:0, width:sz.width,
                                                height:sz.height))
        let result = UIGraphicsGetImageFromCurrentImageContext()
        UIGraphicsEndImageContext()
        return result
      }
    }
    return nil
  }
  
  @objc(convertImage:resolver:rejecter:)
  func convertImage(_ imageURL: String,
                    resolver resolve: @escaping RCTPromiseResolveBlock,
                    rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
    
    getImageFromWeb(imageURL) { (image) in
       //change the background to transparency
       if let image = image {
        let resImage: UIImage? = self.transparencyImageBG(image)
        
        let imageData = UIImagePNGRepresentation(resImage!)!
        let imageStr = imageData.base64EncodedString()
        
        resolve(imageStr)

       } else {
        let error = NSError(domain: "", code: 0, userInfo: [NSLocalizedDescriptionKey : "image convert fail"])
        reject("", "", error)
      }
    }
  }
}
