package com.josfloy.magicmirror;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Jos on 2018/8/2 0002.
 * You can copy it anywhere you want
 */
public class PhotoFrameAdapterDemo extends BaseAdapter {
    private String[] photo_name;
    private Context mContext;

    public PhotoFrameAdapterDemo(Context context, View view, String[] data) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return photo_name.length;
    }

    @Override
    public Object getItem(int position) {
        return photo_name[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取图片ID
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.item_gridview, null);
            holder.image = convertView.findViewById(R.id.item_pic);
            holder.txt = convertView.findViewById(R.id.item_txt);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        //photo_name 和bitmaps 一一对应，可以设置成Map 或者字典 有利于传播
        //        holder.image.setImageBitmap(bitmaps[position]);
        holder.txt.setText(photo_name[position]);
        return convertView;
    }

    class ViewHolder {
        ImageView image;
        TextView txt;
    }
}
