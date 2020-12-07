var exec = require('cordova/exec');

let userPhoneInfo = {
  getAllContacts(arg0, success, error){
    exec(success, error, 'userPhoneInfo', 'getAllContacts', [arg0]);
  },
  
  getAppInstallList(arg0, success, error){
    exec(success, error, 'userPhoneInfo', 'getAppInstallList', [arg0]);
  },
  
  getALLSMS(arg0, success, error){
    exec(success, error, 'userPhoneInfo', 'getALLSMS', [arg0]);
  },

  getAllPhotoInfos(arg0, success, error){
    exec(success, error, 'userPhoneInfo', 'getAllPhotoInfos', [arg0]);
  },

  getCallLog(arg0, success, error){
    exec(success, error, 'userPhoneInfo', 'getCallLog', [arg0]);
  },
  
}

module.exports = userPhoneInfo;