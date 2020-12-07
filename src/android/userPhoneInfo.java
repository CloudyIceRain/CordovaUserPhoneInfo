package com.cordova.plugin.userPhoneInfo;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class userPhoneInfo extends CordovaPlugin {
    /////---------------------------------------------------------------------------
    //获取联系人列表
    //index 为第几页，一次只返回100个，多的在下一页，避免信息过长，导致String放不下
    public static String getAllContacts_JSONStr(Activity activity, int index){
        JSONArray arr = getAllContacts(activity, index);
        String json_str = arr.toString();
        return json_str;
    }
    public static JSONArray getAllContacts(Activity activity, int index) {
        JSONArray json_arr = new JSONArray();
        try {
            Cursor cursor = activity.getContentResolver().query(
                    ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
            while (cursor!= null && cursor.moveToNext()) {
                JSONObject json_obj = new JSONObject();
                String contactId = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                json_obj.put("name", name);
                //通过contactId找电话
                int phoneid = 0;
                Cursor phoneCursor = activity.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID + "=" + contactId, null, null);
                while (phoneCursor != null && phoneCursor.moveToNext()) {
                    String phone = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    if (null != phone) {
                        phone = phone.replace("-", "");
                        phone = phone.replace(" ", "");
                        if (phone != "") {
                            if (phoneid == 0) {
                                json_obj.put("phone", phone);
                            }else{
                                json_obj.put("phone_"+phoneid, phone);
                            }
                            phoneid++;
                        }
                    }
                }
                //获取联系人备注信息
                int noteid = 0;
                Cursor noteCursor = activity.getContentResolver().query(
                        ContactsContract.Data.CONTENT_URI,
                        new String[]{ContactsContract.Data._ID, ContactsContract.CommonDataKinds.Nickname.NAME},
                        ContactsContract.Data.CONTACT_ID + "=?" + " AND " + ContactsContract.Data.MIMETYPE + "='"
                                + ContactsContract.CommonDataKinds.Nickname.CONTENT_ITEM_TYPE + "'",
                        new String[]{contactId}, null);
                if (noteCursor!=null && noteCursor.moveToFirst()) {
                    do {
                        String note = noteCursor.getString(noteCursor
                                .getColumnIndex(ContactsContract.CommonDataKinds.Nickname.NAME));
                        if (note != "") {
                            if (noteid == 0) {
                                json_obj.put("note", note);
                            }else{
                                json_obj.put("note_"+noteid, note);
                            }
                            noteid++;
                        }
                    } while (noteCursor != null && noteCursor.moveToNext());
                }
                json_arr.put(json_obj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return json_arr;
    }
    ////----------------------------------------------------------------------
    //获取App列表
    //index 为第几页，一次只返回100个，多的在下一页，避免信息过长，导致String放不下
    public static String getAppInstallList_JSONstr(Activity activity, int index) {
        JSONArray arr = getAppInstallList(activity, index);
        String json_str = arr.toString();
        return json_str;
    }
    public static JSONArray getAppInstallList(Activity activity, int index){
        JSONArray json_arr = new JSONArray();
        try {
            List<PackageInfo> packageInfos = activity.getPackageManager().getInstalledPackages(0);
            for (int i = 0; i < packageInfos.size(); i++) {
                PackageInfo packageInfo = packageInfos.get(i);
                if (null != packageInfo) {
                    JSONObject json_obj = new JSONObject();
                    json_obj.put("appName", packageInfo.applicationInfo.name);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                        json_obj.put("appVersionCode", packageInfo.getLongVersionCode());
                    }
                    json_obj.put("appVersionName", packageInfo.versionName);
                    json_obj.put("appPackageName", packageInfo.packageName);
                    json_obj.put("installTime", packageInfo.firstInstallTime);
                    json_obj.put("updateTime", packageInfo.lastUpdateTime);
                    json_arr.put(json_obj);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return json_arr;
    }

    private static Uri SMS_INBOX = Uri.parse("content://sms/");
    private static String getALLSMS_JSONStr(Activity activity, int index){
        JSONArray arr = getAllSMS(activity, index);
        String json_str = arr.toString();
        return json_str;
    }
    private static JSONArray getAllSMS(Activity activity, int index) {
        JSONArray json_arr = new JSONArray();
        try {
            ContentResolver cr = activity.getContentResolver();
            String[] projection = new String[]{"_id", "address", "person", "body", "date", "type"};
            Cursor cur = cr.query(SMS_INBOX, projection, null, null, "date desc");
            if (null == cur) {
                Log.i("ooc", "getAllSMS cur == null");
                return json_arr;
            }
            while (cur!=null && cur.moveToNext()) {
                JSONObject json_obj = new JSONObject();
                String phone = cur.getString(cur.getColumnIndex("address"));//手机号
                String name = cur.getString(cur.getColumnIndex("person"));//联系人姓名列表
                String msg = cur.getString(cur.getColumnIndex("body"));//短信内容
                String _type = cur.getString(cur.getColumnIndex("type"));//短信类型
                String time = cur.getString(cur.getColumnIndex("date"));//短信时间
                //至此就获得了短信的相关的内容, 以下是把短信加入map中，构建listview,非必要。
                json_obj.put("phone", phone);
                json_obj.put("name", name);
                json_obj.put("msg", msg);
                json_obj.put("type", _type);
                json_obj.put("time", time);
                json_arr.put(json_obj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return json_arr;
    }

    public static String getAllPhotoInfos_JSONStr(Activity activity, int index){
        JSONArray arr = getAllPhotoInfos(activity, index);
        String json_str = arr.toString();
        return json_str;
    }
    public static JSONArray getAllPhotoInfos(Activity activity, int index)
    {
        JSONArray json_arr = new JSONArray();
        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        try {
            ContentResolver contentResolver = activity.getContentResolver();
            Cursor cursor = contentResolver.query(uri, null, null, null, null);
            if (cursor == null || cursor.getCount() <= 0) return null; // 没有图片
            while (cursor != null && cursor.moveToNext()) {
                JSONObject json_obj = new JSONObject();
                int _idx = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String path = cursor.getString(_idx); // 文件地址
                String fileName = path.substring(path.lastIndexOf("/")+1);
                //Log.i("pluginUtils", "getAllPhotoInfos path="+path);
                ExifInterface exif = new ExifInterface(path);
                String date = exif.getAttribute(ExifInterface.TAG_DATETIME_ORIGINAL);//获得拍摄时间
                String model = exif.getAttribute(ExifInterface.TAG_MODEL);//获得拍摄机器
                String width = exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH);//宽度
                String height = exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH);//高度
                String lat = exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE);//高度
                String lon = exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);//高度

                json_obj.put("name", fileName);
                json_obj.put("path", path);
                json_obj.put("date", date);
                json_obj.put("model", model);
                json_obj.put("width", width);
                json_obj.put("height", height);
                json_obj.put("lat", lat);
                json_obj.put("lon", lon);

                json_arr.put(json_obj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return json_arr;
    }

    private static Uri callUri = CallLog.Calls.CONTENT_URI;
    private static String[] columns = {CallLog.Calls.CACHED_NAME// 通话记录的联系人
            , CallLog.Calls.NUMBER// 通话记录的电话号码
            , CallLog.Calls.DATE// 通话记录的日期
            , CallLog.Calls.DURATION// 通话时长
            , CallLog.Calls.TYPE};// 通话类型}
    public static String getCallLog_JSONStr(Activity activity, int index){
        JSONArray arr = getCallLog(activity, index);
        String json_str = arr.toString();
        return json_str;
    }
    public static JSONArray getCallLog(Activity activity, int index) {
        JSONArray json_arr = new JSONArray();
        try {
            Cursor cursor = activity.getContentResolver().query(callUri, // 查询通话记录的URI
                    columns, null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
            );
            while (cursor!=null && cursor.moveToNext()) {
                JSONObject json_obj = new JSONObject();
                String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));  //姓名
                String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));  //号码
                long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)); //获取通话日期
                String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
                //String time = new SimpleDateFormat("HH:mm").format(new Date(dateLong));
                int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));//获取通话时长，值为多少秒
                int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)); //获取通话类型：1.呼入2.呼出3.未接
//                String dayCurrent = new SimpleDateFormat("dd").format(new Date());
//                String dayRecord = new SimpleDateFormat("dd").format(new Date(dateLong));

                json_obj.put("name", name);
                json_obj.put("phone", number);
                json_obj.put("date", date);
                json_obj.put("duration", duration);
                json_obj.put("type", type);

                json_arr.put(json_obj);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return json_arr;
    }

    @Override
    public boolean execute(String action, CordovaArgs args, CallbackContext callbackContext) throws JSONException {
        Activity activity = this.cordova.getActivity();
        Context context = this.cordova.getContext();

        if ("getAllContacts_JSONStr".equals(action)
                || "getAllContacts".equals(action)){
            int index = args.getInt(0);
            String ret = getAllContacts_JSONStr(activity, index);
            callbackContext.success(ret);
            return true;
        }else if ("getAppInstallList_JSONstr".equals(action)
                || "getAppInstallList".equals(action)){
            int index = args.getInt(0);
            String ret = getAppInstallList_JSONstr(activity, index);
            callbackContext.success(ret);
            return true;
        }else if ("getALLSMS_JSONStr".equals(action)
                || "getALLSMS".equals(action)){
            int index = args.getInt(0);
            String ret = getALLSMS_JSONStr(activity, index);
            callbackContext.success(ret);
            return true;
        }else if ("getAllPhotoInfos_JSONStr".equals(action)
                || "getAllPhotoInfos".equals(action)){
            int index = args.getInt(0);
            String ret = getAllPhotoInfos_JSONStr(activity, index);
            callbackContext.success(ret);
            return true;
        }else if ("getCallLog_JSONStr".equals(action)
                || "getCallLog".equals(action)){
            int index = args.getInt(0);
            String ret = getCallLog_JSONStr(activity, index);
            callbackContext.success(ret);
            return true;
        }

        callbackContext.error(action + " is not a supported action");
        return false;
    }
}
