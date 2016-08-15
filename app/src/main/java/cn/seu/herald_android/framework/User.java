package cn.seu.herald_android.framework;

import cn.seu.herald_android.framework.json.JObj;
import cn.seu.herald_android.helper.EncryptHelper;

public class User {

    public static User trialUser = new User("000000000", "", "0000000000000000000000000000000000000000", "00000000");

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

    public User(JObj json) {
        this.userName = json.$s("userName");
        this.setPasswordFromEncrypted(json.$s("password"));
        this.uuid = json.$s("uuid");
        this.schoolNum = json.$s("schoolNum");

        // 未登录状态
        if (userName.equals("") || uuid.equals("") || schoolNum.equals("")) {
            userName = trialUser.userName;
            password = trialUser.password;
            uuid = trialUser.uuid;
            schoolNum = trialUser.schoolNum;
        }
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof User
                && userName.equals(((User) o).userName)
                && password.equals(((User) o).password)
                && uuid.equals(((User) o).uuid)
                && schoolNum.equals(((User) o).schoolNum);
    }

    public JObj toJson() {
        JObj object = new JObj();
        object.put("userName", userName);
        object.put("password", getEncryptedPassword());
        object.put("uuid", uuid);
        object.put("schoolNum", schoolNum);
        return object;
    }
}
