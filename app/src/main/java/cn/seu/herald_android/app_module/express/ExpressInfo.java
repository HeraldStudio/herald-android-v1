package cn.seu.herald_android.app_module.express;

/**
 * Created by corvo on 8/6/16.
 */
public class ExpressInfo {

    private String username;    // 用户姓名
    private String userphone;   // 联系电话
    private String smsInfo;     // 短信内容

    private String dest;        // 取回至
    private String arrival;     // 取回时间
    private String locate;      // 快件所在地
    private String weight;      // 快件质量
    private Long submitTime;    // 提交时间

    private boolean isFetched;  // 是否取回
    private boolean isReceived; // 是否接单

    public ExpressInfo() {

    }

    public ExpressInfo(String username, String userphone, String smsInfo, String dest,
                       String arrival, String locate, String weight, Long submitTime, boolean isFetched, boolean isReceived) {
        this.username = username;
        this.userphone = userphone;
        this.smsInfo = smsInfo;
        this.dest = dest;
        this.arrival = arrival;
        this.locate = locate;
        this.weight = weight;
        this.submitTime = submitTime;
        this.isFetched = isFetched;
        this.isReceived = isReceived;
    }

    public String getArrival() {
        return arrival;
    }

    public String getDest() {
        return dest;
    }

    public String getLocate() {
        return locate;
    }

    public String getSmsInfo() {
        return smsInfo;
    }

    public String getUsername() {
        return username;
    }

    public String getUserphone() {
        return userphone;
    }

    public String getWeight() {
        return weight;
    }

    public Long getSubmitTime() {
        return submitTime;
    }

    public boolean isFetched() {
        return isFetched;
    }

    public boolean isReceived() {
        return isReceived;
    }

    public void setArrival(String arrival) {
        this.arrival = arrival;
    }

    public void setDest(String dest) {
        this.dest = dest;
    }

    public void setLocate(String locate) {
        this.locate = locate;
    }

    public void setSmsInfo(String smsInfo) {
        this.smsInfo = smsInfo;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setUserphone(String userphone) {
        this.userphone = userphone;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public void setSubmitTime(Long submitTime) {
        this.submitTime = submitTime;
    }

    public void setFetched(boolean fetched) {
        isFetched = fetched;
    }

    public void setReceived(boolean received) {
        isReceived = received;
    }
}
