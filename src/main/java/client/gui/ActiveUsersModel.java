package client.gui;

import client.UserInfo;

import javax.swing.*;
import java.util.*;

public class ActiveUsersModel extends AbstractListModel<UserInfo> {

    private List<UserInfo> list = new ArrayList<>();

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public UserInfo getElementAt(int i) {
        return list.get(i);
    }

    public void add(String username, boolean isOnline){
        UserInfo user = new UserInfo(username, isOnline);
        int i = list.indexOf(user);
        if (i == -1){
            list.add(user);
            list.sort(Comparator.comparing(userInfo -> userInfo.username));
            fireContentsChanged(this, 0, list.size());
        } else{
            list.get(i).setIsOnline(isOnline);
            fireContentsChanged(this, i, i);

        }
    }

    public void setAsInactive(UserInfo user){
        int i = list.indexOf(user);
//        list.remove(user);
        list.get(i).setIsOnline(false);
        fireContentsChanged(this, i, i);
    }

    public List<String> getProperUsers(){
        List<String> properUserList = new ArrayList<>();
        list.forEach((userInfo)->{
            if (!userInfo.username.startsWith("#")){
                properUserList.add(userInfo.username);
            }
        });
        return properUserList;
    }
}
