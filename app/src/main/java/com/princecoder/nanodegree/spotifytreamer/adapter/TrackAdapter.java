package com.princecoder.nanodegree.spotifytreamer.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.princecoder.nanodegree.spotifytreamer.R;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by prinzlyngotoum on 7/5/15.
 */
public class TrackAdapter extends ArrayAdapter<IElement> {

    // Context
    Context mContext;

    /**
     * List of elements
     * I call elements (Artists or Tracks).
     *
     */
    ArrayList<IElement> mElements;

    public TrackAdapter(Context context, int resource,
                         int textViewResourceId, ArrayList<IElement> objects) {
        super(context, resource, textViewResourceId, objects);
        this.mContext = context;
        this.mElements = objects;
        // TODO Auto-generated constructor stub
    }

    @Override
    public IElement getItem(int position) {
        // TODO Auto-generated method stub
        return mElements.get(position);
    }

    @Override
    public int getCount() {
        // TODO Auto-generated method stub
        return mElements.size();
    }

    public ArrayList<IElement> getElements() {
        return mElements;
    }

    public void setElements(ArrayList<IElement> mElements) {
        this.mElements = mElements;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // TODO Auto-generated method stub

        View row = convertView;
        ViewHolder holder = null;
        if (row == null) {
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(R.layout.track_row_item, parent, false);
            holder = new ViewHolder(row);
            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }

        // It is time to set values
            holder.getTopTxt().setText(mElements.get(position).getBaseInfo());
            holder.getSubTxt().setText(mElements.get(position).getSubInfo());

        if (mElements.get(position).hasThumb()){
            Picasso.with(mContext)
                    .load(mElements.get(position).getThumb())
                    .resize(200, 200)
                    .centerCrop()
                    .into(holder.getImage());
        }

        return row;
    }


    /**
     *
     * View Holder for the adapter
     */
    private class ViewHolder {

        //TextView to display the artist name or track name
        private TextView mTopTxt;

        // ImageView to display the Thumbnail
        private ImageView mPicture;

        private TextView mSubText;

        public ViewHolder(View v){

            mTopTxt=(TextView)v.findViewById(R.id.topTxt);
            mSubText=(TextView)v.findViewById(R.id.subTxt);
            mPicture=(ImageView)v.findViewById(R.id.thumb);

        }

        public TextView getTopTxt() {
            return mTopTxt;
        }

        public void setTopTxt(TextView mTopTxt) {
            this.mTopTxt = mTopTxt;
        }

        public TextView getSubTxt() {
            return mSubText;
        }

        public void setSubTxt(TextView mSubTxt) {
            this.mSubText = mSubTxt;
        }

        public ImageView getImage() {
            return mPicture;
        }

        public void setImage(ImageView mPicture) {
            this.mPicture = mPicture;
        }
    }

}