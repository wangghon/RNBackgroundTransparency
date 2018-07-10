import React, { Component } from 'react';
import {
  Platform,
  StyleSheet,
  Text,
  View,
  Image,
  Dimensions,
  TouchableOpacity,
  ActivityIndicator,
  ImageBackground,
} from 'react-native';

import imageService from './app/services/imageService';

//The multiple size images is here:https://zh.wikipedia.org/wiki/File:Allosaurus_AMNH_White_Background.jpg
const imageURI: string = 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Allosaurus_AMNH_White_Background.jpg/800px-Allosaurus_AMNH_White_Background.jpg';

type Props = {};
type States = {
  width: number,
  height: number,
  imageBase64: string,
  converting: boolean,
  loading: boolean,
  loaded: boolean,
}
export default class App extends Component<Props, States> {
  converting: boolean = false;

  state: any = {
    width: Dimensions.get('window').width,
    height:Dimensions.get('window').height * 0.4,
    imagebase64: '',
    converting: false,
    loading: false,
    loaded: false,
  }

  componentWillMount() {
    Image.prefetch(imageURI);
    Image.getSize(imageURI, (width: number, height: number) => {
      this.setState({
        height: height > this.state.height ? this.state.height : height,
        width: width > this.state.width ? this.state.width : width,
      });
    }, () => {});

    imageService.init((imageBase64: string) => {
      this.setState({ imageBase64 });
    })
  }

  componentWillUnmount() {
    imageService.shutdown();
  }

  _onPress = () => {
    if (this.converting) return;
    this.converting = true;
    this.setState({ 
      converting: true,
      imageBase64: '',
     });

    imageService.convertImage(imageURI).then((imageBase64: string) => {
      this.setState({
        imageBase64,
        converting: false,
      });
      this.converting = false;
    }).catch((e: any) => {
      console.log('The error of image converter is ', e);
    })
  };

  _onImageLoadStart = () => {
    this.setState({
      loading: true,
    })
  };

  _onImageLoadEnd = () => {
    this.setState({
      loading: false,
      loaded: true,
    })
  };

  render() {
    return (
      <View style={styles.container}>
        <ImageBackground
          source={{uri: imageURI}}
          style={{width: this.state.width, height: this.state.height, justifyContent: 'center'}}
          onLoadStart={this._onImageLoadStart}
          onLoadEnd= {this._onImageLoadEnd}>
          {this.state.loading && <ActivityIndicator size="large" color={'#D9155D'} /> }
        </ImageBackground>
        <TouchableOpacity
          style={styles.button}
          onPress={this._onPress}
          disabled={!this.state.loaded}
       >
         <Text style={styles.text}> {this.state.converting ? 'Converting' : 'Convert'}</Text>
         {this.state.converting && <ActivityIndicator size="large" color={'#D9155D'} style={styles.indicator} /> }
       </TouchableOpacity>
      <Image
        source={{uri: `data:image/png;base64, ${this.state.imageBase64}`}}
        style={{width: this.state.width, height: this.state.height}}
      />
      </View>
    );
  }
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    backgroundColor: 'yellow',
  },
  button: {
    margin: 10,
    width: 200,
    height: 50,
    backgroundColor: 'lightgray',
    borderRadius: 5,
    justifyContent: 'center',
    alignItems: 'center',
    flexDirection: 'row',
  },
  text: {
    fontSize: 20,
  },
  indicator: {
    marginLeft: 5,
  },
});
