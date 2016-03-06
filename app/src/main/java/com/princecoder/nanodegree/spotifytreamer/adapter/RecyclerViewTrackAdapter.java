package com.princecoder.nanodegree.spotifytreamer.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.princecoder.nanodegree.spotifytreamer.R;
import com.princecoder.nanodegree.spotifytreamer.model.IElement;
import com.princecoder.nanodegree.spotifytreamer.utils.L;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * Created by Prinzly Ngotoum on 2/25/16.
 */
public class RecyclerViewTrackAdapter extends RecyclerView.Adapter<RecyclerViewTrackAdapter.ViewHolder> {
    // Context
    Context mContext;

    /**
     * List of elements
     * I call elements (Artists or Tracks).
     *
     */
    ArrayList<IElement> mElements;
    final ViewHolderOnClickHandler mCallback;

    public RecyclerViewTrackAdapter(Context c, ViewHolderOnClickHandler vh) {
        mContext = c;
        mCallback=vh;
    }


    //View holder class which help to recycle row view elements
    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView mPicture;
        public TextView mTopTxt;
        public TextView mSubText;

        public ViewHolder(View view) {
            super(view);
            mTopTxt=(TextView)view.findViewById(R.id.topTxt);
            mSubText=(TextView)view.findViewById(R.id.subTxt);
            mPicture=(ImageView)view.findViewById(R.id.thumb);
            view.setClickable(true);

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
    public IElement getItem(int position) {
        // TODO Auto-generated method stub
        return mElements.get(position);
    }

    public ArrayList<IElement> getElements() {
        return mElements;
    }

    public void setElements(ArrayList<IElement> mElements) {
        this.mElements = mElements;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.track_row_item, parent, false);
            view.setFocusable(true);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mCallback.onClick(vh.getAdapterPosition(), vh);
                }
            });
            return vh;
        }
        else {
            throw new RuntimeException(mContext.getString(R.string.data_binding_error));
        }
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        // It is time to set values
        holder.getTopTxt().setText(mElements.get(position).getBaseInfo());
        holder.getSubTxt().setText(mElements.get(position).getSubInfo());

        if (mElements.get(position).hasThumb()){
            try{

                Picasso.with(mContext)
                        .load(mElements.get(position).getThumb())
                        .resize(200, 200)
                        .centerCrop()
                        .into(holder.getImage());
            } catch (Exception e) {
                L.m(getClass().getSimpleName().toString(), mContext.getString(R.string.picasso_error) + ": " + e.getMessage());
            }
        }
    }


    @Override
    public int getItemCount() {
        return mElements!=null?mElements.size():0;
    }

    public void swapElements(ArrayList<IElement> list) {
        mElements = list;
        notifyDataSetChanged();
    }

    public interface ViewHolderOnClickHandler{
        void onClick(int id, ViewHolder vh);
    }
}
