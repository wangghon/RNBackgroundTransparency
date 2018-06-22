import { NativeModules } from 'react-native';

const COLOR_DISTANCE_THREHOLD: number = 32;

const ImageConverter = NativeModules.ImageConverter;

const convertImage = (imageURI: string): Promise<string> => new Promise((resolve: any, reject: any) => {

  ImageConverter.convertImage(imageURI, COLOR_DISTANCE_THREHOLD)
  .then((imageBase64: string) => { resolve(imageBase64) })
  .catch((e: any) => {
    reject(e)
  });
});

export default { convertImage };