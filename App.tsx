import React, { Component } from 'react';
import {
  StyleSheet,
  Text,
  View,
  Image,
  Dimensions,
  TouchableOpacity,
  ActivityIndicator,
  ImageBackground,
} from 'react-native';
import RNFetchBlob from 'rn-fetch-blob'


import imageService from './app/services/imageService';

//The multiple size images is here:https://zh.wikipedia.org/wiki/File:Allosaurus_AMNH_White_Background.jpg
const imageURI: string = 'https://upload.wikimedia.org/wikipedia/commons/thumb/1/1a/Allosaurus_AMNH_White_Background.jpg/800px-Allosaurus_AMNH_White_Background.jpg';

type Props = {};
type States = {
  width: number,
  height: number,
  originalImageStr: string,
  maskedImageStr: string,
  masking: boolean,
  fetching: boolean,
}
export default class App extends Component<Props, States> {
  converting: boolean = false;

  state: any = {
    width: Dimensions.get('window').width,
    height:Dimensions.get('window').height * 0.4,
    originalImageStr: '',
    maskedImageStr: '',
    masking: false,
    fetching: true,
  }

  componentWillMount() {
    Image.getSize(imageURI, (width: number, height: number) => {
      this.setState({
        height: height > this.state.height ? this.state.height : height,
        width: width > this.state.width ? this.state.width : width,
      });
    }, () => {});

    RNFetchBlob.fetch('GET', imageURI, {})
      .then((res: any) => {
      let status = res.info().status;
    
      if(status == 200) {
        // the conversion is done in native code
        this.setState({ 
          originalImageStr: res.base64(),
          fetching: false
        });
      } 
    });

    imageService.init((maskedImageStr: string) => {
      this.setState({ maskedImageStr });
    })
  }

  componentWillUnmount() {
    imageService.shutdown();
  }

  _onPress = () => {
    if (this.converting) return;
    this.converting = true;
    this.setState({ 
      maskedImageStr: '',
      masking: true,
     });

    imageService.maskImage(this.state.originalImageStr).then((maskedImageStr: string) => {
      this.setState({
        maskedImageStr,
        masking: false,
      });
      this.converting = false;
    }).catch((e: any) => {
      console.log('The error of image converter is ', e);
    })
  };

  render() {
    return (
      <View style={styles.container}>
        <ImageBackground
          source={{uri: `data:image/jpg;base64, ${this.state.originalImageStr}`}}
          style={{width: this.state.width, height: this.state.height, justifyContent: 'center'}}>
          {this.state.fetching && <ActivityIndicator size="large" color={'#D9155D'} /> }
        </ImageBackground>
        <TouchableOpacity
          style={styles.button}
          onPress={this._onPress}
          disabled={this.state.fetching}
       >
         <Text style={styles.text}> {this.state.masking ? 'Converting' : 'Convert'}</Text>
         {this.state.masking && <ActivityIndicator size="large" color={'#D9155D'} style={styles.indicator} /> }
       </TouchableOpacity>
      <Image
        source={{uri: `data:image/png;base64, ${this.state.maskedImageStr}`}}
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
