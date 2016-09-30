package com.example.administrator.bignamemusic20;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.util.List;

/**
 * Created by Administrator on 2016/9/21.
 */
public class ListViewAdapter extends BaseAdapter {
    List<File> list ;
    Context context;
    LayoutInflater inflater;

    public ListViewAdapter(Context context, List<File> list) {
        this.context = context;
        this.list = list;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder = null;
        if (convertView == null) {
            viewHolder = new ViewHolder();
            convertView = inflater.inflate(R.layout.listview_item, parent, false);
            viewHolder.image = (ImageView) convertView.findViewById(R.id.musicimage);
            viewHolder.name = ((TextView) convertView.findViewById(R.id.musicname));
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }
        viewHolder.name.setText(list.get(position).getName());
        return convertView;
    }
    private class ViewHolder{
        ImageView image ;
        TextView name ;
    }
}
