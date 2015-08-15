var RecordVoice = {
    startRecord: function(successCallback, errorCallback) {
        cordova.exec(
            successCallback, // success callback function
            errorCallback, // error callback function
            'RecordVoice', // mapped to our native Java class called "RecordVoicePlugin"
            'startrecording'); 
    }
}
module.exports = RecordVoice;