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
} from 'react-native';

import imageService from './app/services/imageService';

const imageURI: string = 'https://images.pexels.com/photos/768943/pexels-photo-768943.jpeg';

type Props = {};
type States = {
  width: number,
  height: number,
  imageBase64: string,
  converting: boolean,
}
export default class App extends Component<Props, States> {
  converting: boolean = false;
  setState: any;

  state: any = {
    width: Dimensions.get('window').width,
    height:Dimensions.get('window').height * 0.4,
    imagebase64: '',
    converting: false,
  }

  componentWillMount() {
    Image.prefetch(imageURI);
    Image.getSize(imageURI, (width: number, height: number) => {
      this.setState({
        height: height > this.state.height ? this.state.height : height,
        width: width > this.state.width ? this.state.width : width,
      });
    }, () => {});
  }

  _onPress = () => {
    if (this.converting) return;
    this.converting = true;
    this.setState({ converting: true });
    imageService.convertImage(imageURI).then((imageBase64: string) => {
      this.setState({
        imageBase64,
        converting: false,
      });
    }).catch((e: any) => {
      console.log('The error of image converter is ', e);
    })
  };

  render() {
    return (
      <View style={styles.container}>
        <Image
          source={{uri: imageURI}}
          style={{width: this.state.width, height: this.state.height}}
        />
        <TouchableOpacity
          style={styles.button}
          onPress={this._onPress}
       >
         <Text style={styles.text}> Convert </Text>
       </TouchableOpacity>
       {this.state.converting && <ActivityIndicator size="large" color={'#D9155D'} /> }
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
  },
  text: {
    fontSize: 20,
  }
});
