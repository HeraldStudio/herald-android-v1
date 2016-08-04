package cn.seu.herald_android.helper;

import org.json.JSONException;
import org.json.JSONObject;

public class User {

    public static User trialUser = new User(new JSONObject().toString());

    public String userName;

    public String password;

    public String uuid;

    public String schoolNum;

    // 加密后的密码，$get() 将自动对明文密码加密返回；$set() 将自动解密密文密码赋给 password
    // 需要注意调用此 get/set 之前须先设置正确的 userName
    public String getEncryptedPassword() {
        return new EncryptHelper(userName).encrypt(password);
    }

    public void setPasswordFromEncrypted(String encrypted) {
        password = new EncryptHelper(userName).decrypt(encrypted);
    }

    public User(String userName, String password, String uuid, String schoolNum) {
        this.userName = userName;
        this.password = password;
        this.uuid = uuid;
        this.schoolNum = schoolNum;
    }

    public User(String jsonString) {
        try {
            JSONObject json = new JSONObject(jsonString);
            this.userName = json.getString("userName");
            this.setPasswordFromEncrypted(json.getString("password"));
            this.uuid = json.getString("uuid");
            this.schoolNum = json.getString("schoolNum");
        } catch (JSONException e) {
            this.userName = "000000000";
            this.password = "";
            this.uuid = "0000000000000000000000000000000000000000";
            this.schoolNum = "00000000";
        }
    }

    public String toJsonString() {
        try {
            JSONObject object = new JSONObject();
            object.put("userName", userName);
            object.put("password", getEncryptedPassword());
            object.put("uuid", uuid);
            object.put("schoolNum", schoolNum);
            return object.toString();
        } catch (JSONException e) {
            return new JSONObject().toString();
        }
    }
}
