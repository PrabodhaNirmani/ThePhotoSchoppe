package com.example.acer.thephotoschoppe.fragment;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v4.app.Fragment;
import android.util.Log;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.acer.thephotoschoppe.R;
import com.example.acer.thephotoschoppe.activities.SignUpActivity;
import com.example.acer.thephotoschoppe.flickr.FlickrManager;
import com.example.acer.thephotoschoppe.models.Photo;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Text;

import java.util.ArrayList;

public class PortfolioFragment extends Fragment {


    static ArrayList<Photo> photosList;
    Context context;
    GridView gridView;

    private static final String TAG="PortfolioFragment";
    public PortfolioFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView=inflater.inflate(R.layout.fragment_portfolio, container, false);

        gridView=(GridView) rootView.findViewById(R.id.gridView);

        context=getContext();
        photosList=new ArrayList<>();

        new GetContacts().execute();
        return rootView;
    }


    private class GetContacts extends AsyncTask<Void, Void, Void> {

        ProgressDialog pDialog;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog=new ProgressDialog(context);
            pDialog.setMessage("Please wait");
            pDialog.setCancelable(false);
            pDialog.show();
        }


        @Override
        protected Void doInBackground(Void... voids) {
            FlickrManager handler=new FlickrManager();

            String imagesUrl=handler.getImageListUrl();
            String jsonStr=handler.makeServiceCall(imagesUrl);

            if(jsonStr!=null){
                try {
                    JSONObject jsonObject=new JSONObject(jsonStr);

                    JSONArray photos=jsonObject.getJSONArray("items");
                    int breakpoint;
                    if(photos.length()>10){
                        breakpoint=10;
                    }
                    else {
                        breakpoint=photos.length();
                    }

                    for (int i=0;i<breakpoint;i++){
                        JSONObject c=photos.getJSONObject(i);

                        String title=c.getString("title");
                        String webUrl=c.getString("link");
                        String dateTaken=c.getString("date_taken");
                        String datePublished=c.getString("published");

                        JSONObject media=c.getJSONObject("media");
                        String srcUrl=media.getString("m");

                        //create new photo object
                        Photo p=new Photo(title,webUrl,srcUrl,dateTaken,datePublished);

                        //add photo to the list
                        photosList.add(p);


                    }
                } catch (final JSONException e) {

                    e.printStackTrace();

                }
            }
            else {

                Log.d(TAG,"exception");
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);

            if(photosList.size()==0){
                Toast.makeText(context,"Please check your internet connection and relaunch the app.",Toast.LENGTH_LONG).show();
            }
            else {
                gridView.setAdapter(new BaseAdapter() {
                    @Override
                    public int getCount() {
                        return photosList.size();
                    }

                    @Override
                    public Object getItem(int i) {
                        return null;
                    }

                    @Override
                    public long getItemId(int i) {
                        return 0;
                    }

                    @Override
                    public View getView(int i, View convertView, ViewGroup viewGroup) {

                        Photo photo=photosList.get(i);
                        View cellPhoto=null;
                        if(convertView==null){
                            cellPhoto= LayoutInflater.from(context).inflate(R.layout.grid_view_cell,null);
                        }
                        else {
                            cellPhoto=convertView;
                        }

                        PlaceHolder ph=(PlaceHolder)cellPhoto.getTag();

                        ImageView image;
                        TextView date;

                        if(ph==null){
                            image=(ImageView)cellPhoto.findViewById(R.id.image_view);
                            date=(TextView)cellPhoto.findViewById(R.id.date_tv);
                            ph=new PlaceHolder();
                            ph.image=image;
                            ph.date=date;
                            cellPhoto.setTag(ph);
                        }
                        else {
                            image=ph.image;
                            date=ph.date;
                        }
                        Picasso.with(context)
                                .load(photo.getSrcUrl())
                                .resize(150,150)
                                .centerCrop()
                                .into(image);

                        date.setText(photo.getTakenDate().substring(0,10));

                        return cellPhoto;
                    }
                });

                gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    public void onItemClick(AdapterView<?> parent,
                                            View v, int position, long id){
                        //Send intent to SingleViewActivity
                        Intent i = new Intent(context, SingleViewActivity.class);
                        // Pass image index
                        i.putExtra("id", position);
                        i.putExtra("url",photosList.get(position).getSrcUrl());
                        startActivity(i);


                    }
                });
                gridView.setDrawSelectorOnTop(true);
            }

            if(pDialog.isShowing()){
                pDialog.dismiss();
            }
        }
        private class PlaceHolder{
            ImageView image;
            TextView date;
        }
    }

    public static ArrayList<Photo> getPhotos(){
        return photosList;
    }

}
