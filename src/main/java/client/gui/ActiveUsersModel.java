package client.gui;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class ActiveUsersModel extends AbstractListModel<String> {

    private List<String> list = new ArrayList<>();

    @Override
    public int getSize() {
        return list.size();
    }

    @Override
    public String getElementAt(int i) {
        return list.get(i);
    }

    public void add(String elem){
        list.add(elem);
        list.sort(null);
        fireContentsChanged(this, 0, list.size());
    }

    public void remove(String elem){
        int i = list.indexOf(elem);
        list.remove(elem);
        fireContentsChanged(this, i, i);

    }
}
