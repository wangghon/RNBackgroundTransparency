import { 
  NativeModules, 
  DeviceEventEmitter,
} from 'react-native';

const COLOR_DISTANCE_THREHOLD: number = 32;

console.log(NativeModules.Counter);

const ImageConverter = NativeModules.ImageConverter;
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

  ImageConverter.convertImage(imageURI)
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