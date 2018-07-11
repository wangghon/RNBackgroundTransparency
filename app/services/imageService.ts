import { 
  NativeModules, 
  DeviceEventEmitter,
} from 'react-native';

const ImageConverter = NativeModules.ImageConverter;

const WHITE_COLOR_MASKING: Array<number> = [240.0, 255.0, 240.0, 255.0, 240.0, 255.0];

let subscription: any;

const init = (eventCallback: any) => {
  subscription = DeviceEventEmitter.addListener('onImageConvert', (e: any): void => {
    if (eventCallback) eventCallback(e.image);
  });
}

const shutdown = () => {
  if (subscription) subscription.remove();
}

const convertImage = (imageURI: string): Promise<string> => new Promise((resolve: any, reject: any) => {

  ImageConverter.convertImage(imageURI, WHITE_COLOR_MASKING)
  .then((imageBase64: string) => { resolve(imageBase64) })
  .catch((e: any) => {
    reject(e)
  });
});

export default { 
  init, 
  shutdown,
  convertImage 
};