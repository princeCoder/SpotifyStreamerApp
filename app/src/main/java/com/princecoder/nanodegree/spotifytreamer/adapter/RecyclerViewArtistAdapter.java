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
public class RecyclerViewArtistAdapter extends RecyclerView.Adapter<RecyclerViewArtistAdapter.ViewHolder> {
    // Context
    Context mContext;

    /**
     * List of elements
     * I call elements (Artists or Tracks).
     *
     */
    ArrayList<IElement> mElements;
    final ViewHolderOnClickHandler mCallback;

    //Current element selected
    private int selectedItem=-1;

    public RecyclerViewArtistAdapter(Context c, ViewHolderOnClickHandler vh) {
        mContext = c;
        mCallback=vh;
    }


    //View holder class which help to recycle row view elements
    public class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView mPicture;
        public TextView mTopTxt;

        //The row index
        public int position;

        public ViewHolder(View view) {
            super(view);
            mPicture = (ImageView) view.findViewById(R.id.thumb);
            mTopTxt = (TextView) view.findViewById(R.id.topTxt);
            view.setClickable(true);

        }

    }
    public IElement getItem(int position) {
        // TODO Auto-generated method stub
        return mElements.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if(parent instanceof RecyclerView){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.artist_row_item, parent, false);
            view.setFocusable(true);
            final ViewHolder vh = new ViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    //Handle the click on an element
                    if(selectedItem!=-1){
                        notifyItemChanged(selectedItem);
                    }
                    setSelectedItem(vh.getAdapterPosition());
                    mCallback.onClick(vh.getAdapterPosition(), vh);
                    notifyItemChanged(getSelectedItem());
                }
            });
            return vh;
        }
        else {
            throw new RuntimeException(mContext.getString(R.string.data_binding_error));
        }
    }

    /**
     * Get the selected Item Index
     * @return
     */
    public int getSelectedItem() {
        return selectedItem;
    }


    public void setSelectedItem(int selectedItem) {
        this.selectedItem = selectedItem;
    }



    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {

        holder.mTopTxt.setText(mElements.get(position).getBaseInfo());
        holder.position=position;
        if (mElements.get(position).hasThumb()){
            try {
                Picasso.with(mContext)
                        .load(mElements.get(position).getThumb())
                        .resize(200, 200)
                        .centerCrop()
                        .into(holder.mPicture);

                holder.itemView.setSelected(getSelectedItem() == position);
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
