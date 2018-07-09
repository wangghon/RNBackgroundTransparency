//
//  ImageConverterBridge.m
//  RNBackgroundTransparency
//
//  Created by wanghongbo on 4/7/18.
//  Copyright © 2018 Facebook. All rights reserved.
//

#import <Foundation/Foundation.h>

#import <React/RCTBridgeModule.h>

@interface RCT_EXTERN_MODULE(ImageConverter, NSObject)
RCT_EXTERN_METHOD(convertImage: (NSString *)imageURL threshold: (NSInteger)threshold resolver: (RCTPromiseResolveBlock *)resolve rejecter: (RCTPromiseRejectBlock *)reject)
@end
