package me.addi.test;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;

import com.sea_monster.resource.Resource;
import com.sea_monster.widget.AsyncImageView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends ActionBarActivity {

    GridView mGridView;
    AsyncImageView imageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        mGridView = (GridView) findViewById(android.R.id.list);
        imageView = (AsyncImageView)findViewById(android.R.id.icon);
        List<Uri> data = getThumb();

        mGridView.setAdapter( new ImageAdapter(this, data));

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ImageAdapter.ViewHolder holder =(ImageAdapter.ViewHolder) view.getTag();

                imageView.setImageDrawable(holder.icon.getDrawable());

                Log.d("View", "index:" + position + "data:" + holder.icon.getDrawable());
            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    class ImageAdapter extends BaseAdapter {

        LayoutInflater mInflator;

        List<Uri> uris;

        class ViewHolder{
            AsyncImageView icon;
        }

        ImageAdapter(Context context, List<Uri> uris) {
            this.uris = uris;
            this.mInflator = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return uris.size();
        }

        @Override
        public Uri getItem(int position) {
            return uris.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            Log.d("POSITION", "index:"+position+" "+getItem(position).toString());


            ViewHolder holder;
            if (convertView == null||convertView.getTag() == null){
                convertView = mInflator.inflate(R.layout.item, null);

                holder = new ViewHolder();
                holder.icon = (AsyncImageView)convertView.findViewById(android.R.id.icon);

                convertView.setTag(holder);
            }else {
                holder =(ViewHolder) convertView.getTag();
            }

                holder.icon.setResource(new Resource(getItem(position)));

            return convertView;
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            getThumb();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private List<Uri> getThumb() {

        List<Uri> uris = new ArrayList<>();

//        Cursor cursor = MediaStore.Images.Thumbnails.queryMiniThumbnails(getContentResolver(), MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI, MediaStore.Images.Thumbnails.MINI_KIND, null);
//
//        cursor.moveToFirst();
//        int index = cursor.getColumnIndex(MediaStore.Images.Thumbnails.DATA);
//        do {
//            uris.add(Uri.parse("file://" + cursor.getString(index)));
//        } while (cursor.moveToNext());

        uris.add(Uri.parse("http://img1.bdstatic.com/static/home/widget/search_box_home/logo/home_white_logo_0ddf152.png"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/262df807fd2ee449df30cd9ba293f9a599b326b012649-izcZxf_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/09dfabe1258c38e53268ab5628b64e5f479fa00abd8e-OCQ1qR_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/fe1a537d5d5571b75a902546fad55c962d9631c341f5c-Hrk6b0_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/a7a66b158cdfda91c0dc367aa78d19efdeb69da41fbd9-yHvGuy_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/0ccb11e60b8e69e997beb8af57a616cc850f4e6c24172-FLFoLm_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/ddda6b6c9473367a75caa10cfc69fe2ccb8435ff4119d-LzILGo_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/083038976b865dca00c68330519eefbb7074f459d1f6-b1dcCL_fw658"));
        return uris;
    }
}
