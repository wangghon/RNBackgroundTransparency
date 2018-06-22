import { 
  NativeModules, 
  DeviceEventEmitter,
} from 'react-native';

const COLOR_DISTANCE_THREHOLD: number = 32;

const ImageConverter = NativeModules.ImageConverter;

const init = (eventCallback: any) => {
  DeviceEventEmitter.addListener('onImageConvert', (e: any): void => {
    if (eventCallback) eventCallback(e.image);
  });
}

const convertImage = (imageURI: string): Promise<string> => new Promise((resolve: any, reject: any) => {

  ImageConverter.convertImage(imageURI, COLOR_DISTANCE_THREHOLD)
  .then((imageBase64: string) => { resolve(imageBase64) })
  .catch((e: any) => {
    reject(e)
  });
});

export default { 
  init, 
  convertImage 
};