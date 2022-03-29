package client.gui;

import client.UserInfo;

import javax.swing.*;
import java.awt.*;

public class ActiveUserListRenderer extends DefaultListCellRenderer {
    @Override
    public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        UserInfo userInfo = (UserInfo) value;
        setText(userInfo.username);
        if (userInfo.isOnline()){
            setBackground(Color.GREEN);
        } else {
            setBackground(Color.RED);
        }

        if (isSelected){
            setBorder(BorderFactory.createLineBorder(Color.BLACK));
        }
        else {
            setBorder(BorderFactory.createEmptyBorder());
        }

        return this;
//        return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
    }
}
