import { NativeModules } from 'react-native';

const ImageConverter = NativeModules.ImageConverter;

const convertImage = (imageURI: string): any => new Promise((resolve, reject) => {

  ImageConverter.convertImage(imageURI)
  .then((imageBase64: string) => { resolve(imageBase64) })
  .catch(e => {
    reject(e)
  });
});

export default { convertImage };