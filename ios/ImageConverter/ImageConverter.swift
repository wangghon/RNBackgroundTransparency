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
  
  func transparencyImageBG(_ image: UIImage, threshold: Int) -> UIImage? {
    let rawImage: CGImage = image.cgImage!
    
    var resImage: UIImage?
    
    let colorMasking: [CGFloat] = [240, 255, 240, 255, 240, 255]

    if let imgRefCopy = rawImage.copy(maskingColorComponents: colorMasking) {
      
      resImage = UIImage(cgImage: imgRefCopy)
    }
    return resImage
  }
  
  @objc(convertImage:threshold:resolver:rejecter:)
  func convertImage(_ imageURL: String,
                    threshold: NSInteger,
                    resolver resolve: @escaping RCTPromiseResolveBlock,
                    rejecter reject: @escaping RCTPromiseRejectBlock) -> Void {
    
    getImageFromWeb(imageURL) { (image) in
       //change the background to transparency
       if let image = image {
        let resImage: UIImage? = self.transparencyImageBG(image, threshold: threshold)
        
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
