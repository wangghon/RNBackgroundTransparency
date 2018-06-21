import React, { Component } from 'react';
import {
  Platform,
  StyleSheet,
  Text,
  View,
  Image,
  Dimensions,
  TouchableOpacity
} from 'react-native';

const imageURI: string = 'https://images.pexels.com/photos/768943/pexels-photo-768943.jpeg';

type Props = {};
export default class App extends Component<Props> {
  state: any = {
    width: Dimensions.get('window').width,
    height:Dimensions.get('window').height * 0.4,
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
