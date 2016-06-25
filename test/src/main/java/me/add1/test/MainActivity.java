package me.add1.test;

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

import me.add1.resource.ImageResource;
import me.add1.widget.AsyncImageView;

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
        imageView = (AsyncImageView) findViewById(android.R.id.icon);
        List<Uri> data = getThumb();

        mGridView.setAdapter(new ImageAdapter(this, data));

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ImageAdapter.ViewHolder holder = (ImageAdapter.ViewHolder) view.getTag();
                holder = null;
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

        class ViewHolder {
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

            Log.d("POSITION", "index:" + position + " " + getItem(position).toString());


            ViewHolder holder;
            if (convertView == null || convertView.getTag() == null) {
                convertView = mInflator.inflate(R.layout.item, null);

                holder = new ViewHolder();
                holder.icon = (AsyncImageView) convertView.findViewById(android.R.id.icon);
                holder.icon.setShape(AsyncImageView.Shape.ROUNDED);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.icon.setResource(new ImageResource.Builder().uri(getItem(position)).supportThumb(true).limitHeight(150).limitWidth(150).build());

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

        uris.add(Uri.parse("http://img1.bdstatic.com/static/home/widget/search_box_home/logo/home_white_logo_0ddf152.png"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/262df807fd2ee449df30cd9ba293f9a599b326b012649-izcZxf_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/09dfabe1258c38e53268ab5628b64e5f479fa00abd8e-OCQ1qR_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/fe1a537d5d5571b75a902546fad55c962d9631c341f5c-Hrk6b0_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/a7a66b158cdfda91c0dc367aa78d19efdeb69da41fbd9-yHvGuy_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/0ccb11e60b8e69e997beb8af57a616cc850f4e6c24172-FLFoLm_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/ddda6b6c9473367a75caa10cfc69fe2ccb8435ff4119d-LzILGo_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/083038976b865dca00c68330519eefbb7074f459d1f6-b1dcCL_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/4363215304852bdcb5be16f71eb4b9239aa9738bfe76-IS4nMY_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/16fa5bf2b4adf7101c5e27e93bedf32f63e94a264a4c8-pFUnOX_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/35be2fb6bb56c2b210ded4cec496d4a3769e304c1234c-5ymsxK_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/2c9a7bf3dfc53c5da2073240a9a1d211b50270161d3de-I5UW4M_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/f986e495073d4f93c1e3a56a3a2f7295d2eec50e167d2-mYA0V1_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/b11c5771b4b073e5491447732a95abc1737821e917638-qoFOCN_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/48782909dfbb98e021ba20e3fe43f789953f13bb19205-u7qWGA_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/6cb5a47d04058c8196d1f44671939dcd2d0e00df17ea0-oFCgV7_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/746e7f6995a9ea1ba163a0d7c0fa564836e9bc3b9a9b-yyncSK_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/25cbf52f8f8c94861264b406dbd918b63ca5357d535a-pfn54Z_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/27216f0d9fa36fb682e44482bdcda5ef2140c9ac23501-3Jm7c7_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/794a7249de77ec2ed295f0df6cefa05f63983c2811eb2-7Mmsti_fw658"));
        uris.add(Uri.parse("http://img.hb.aicdn.com/785b40e0b3768085237ca8021520debbab777586230b7-ewTtfH_fw658"));
        return uris;
    }
}
