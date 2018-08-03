package com.josfloy.magicmirror.activity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.josfloy.magicmirror.R;

/**
 * Created by Jos on 2018/8/2 0002.
 * You can copy it anywhere you want
 */
public class PhotoFrameActivity extends AppCompatActivity
        implements View.OnClickListener, AdapterView.OnItemClickListener {
    private GridView gridView;            //镜框网格
    private TextView textView;            //返回键
    private int[] photo_styles;            //图片的数组
    private String[] photo_name;        //图片的名称数组
    private Bitmap[] bitmaps;            //镜框的集合

    /**
     * 初始化数据，利用数组的下标让两者一一对应，这种设置方式很不好，应该用map或者HashMap来做
     */
    private void initDatas() {
        //图片样式
        photo_styles = new int[]{R.drawable.mag_0001, R.drawable.mag_0003, R.drawable.mag_0005,
                R.drawable.mag_0006, R.drawable.mag_0007, R.drawable.mag_0008, R.drawable.mag_0009,
                R.drawable.mag_0011, R.drawable.mag_0012, R.drawable.mag_0014};
        //图片名称
        photo_name = new String[]{"Beautiful", "Special", "Wishes", "Forever",
                "Journey", "Love", "River", "Wonderful", " Birthday", "Nice"};
        bitmaps = new Bitmap[photo_styles.length];    //新建图片对象
        for (int i = 0; i < photo_styles.length; i++) {
            //获取图片
            Bitmap bitmap = BitmapFactory.decodeResource(getResources(), photo_styles[i]);
            bitmaps[i] = bitmap;
        }
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_frame);

        textView = findViewById(R.id.back_to_main);
        gridView = findViewById(R.id.photo_frame_list);

        initDatas();
        textView.setOnClickListener(this);

        PhotoFrameAdapter adapter = new PhotoFrameAdapter();
        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.back_to_main:
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent();
        intent.putExtra("POSITION", position);
        setResult(RESULT_OK, intent);
        finish();
    }

    class PhotoFrameAdapter extends BaseAdapter {
        /**
         * 获取item数量
         */
        @Override
        public int getCount() {
            return photo_name.length;
        }

        /**
         * 获取item
         */
        @Override
        public Object getItem(int position) {
            return photo_name[position];            //返回position位置的图片对象
        }

        /**
         * 获取图片ID
         */
        @Override
        public long getItemId(int position) {
            return position;                //返回图片位置position
        }

        /**
         * 获取item对象
         */
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();            //新建holder对象
                //将布局填充成控件
                convertView = getLayoutInflater().inflate(R.layout.item_gridview, null);
                //获取展示图片的控件对象
                holder.image = (ImageView) convertView.findViewById(R.id.item_pic);
                //获取展示文本的控件对象
                holder.txt = (TextView) convertView.findViewById(R.id.item_txt);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();    //根据tag获取holder对象
            }
            setData(holder, position);            //设置控件显示的内容
            return convertView;
        }

        /**
         * 设置数据
         */
        private void setData(ViewHolder holder, int position) {
            holder.image.setImageBitmap(bitmaps[position]);        //设置对象
            holder.txt.setText((photo_name[position]));        //设置名称
        }

        /**
         * 复用类
         */
        class ViewHolder {
            ImageView image;                    //声明图片控件
            TextView txt;
        }
    }
}
