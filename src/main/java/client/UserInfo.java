package client;

import java.util.Objects;

public class UserInfo {
    public final String username;
    private boolean isOnline;

    public UserInfo(String username, boolean isOnline) {
        this.username = username;
        this.isOnline = isOnline;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setIsOnline(boolean online) {
        isOnline = online;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UserInfo userInfo = (UserInfo) o;
        return username.equals(userInfo.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(username, isOnline);
    }
}
