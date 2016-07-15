package cn.seu.herald_android.app_framework;

public abstract class UITableViewDataSource {

    public abstract int numberOfRowsInSection(int section);

    public abstract UITableViewCell cellForRowAtIndexPath(NSIndexPath indexPath);

    public int numberOfSectionsInTableView() {
        return 1;
    }

    public String titleForHeaderInSection(int section) {
        return null;
    }

    public String titleForFooterInSection(int section) {
        return null;
    }

    public void didSelectRowAtIndexPath(NSIndexPath indexPath) {
    }
}
