package net.noratek.smartvoxxwear.adapter;

import java.util.ArrayList;

/**
 * Created by eloudsa on 24/08/15.
 */
public class SimpleRow {
    private ArrayList<SimplePage> mPagesRow = new ArrayList();

    public void addPages(SimplePage page) {
        mPagesRow.add(page);
    }

    public SimplePage getPages(int index) {
        return mPagesRow.get(index);
    }

    public int size(){
        return mPagesRow.size();
    }
}
