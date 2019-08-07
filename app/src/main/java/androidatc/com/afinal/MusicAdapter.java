package androidatc.com.afinal;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.ViewHolder> implements View.OnClickListener {

    private int selected;
    private List<music> musicList;
    private OnItemClickListener onItemClickListener;
    private OnLongItemClickListener onLongItemClickListener;
    private Context mContext;

    public interface OnLongItemClickListener{
        void onLongItemClick(View v,int position);
    }

    public interface OnItemClickListener {
        void onItemClick(View v, int position);
    }

    public void setOnLongItemClickListener(OnLongItemClickListener onLongItemClickListener) {
        this.onLongItemClickListener = onLongItemClickListener;
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public void onClick(View view) {
        if (onItemClickListener != null) {
            onItemClickListener.onItemClick(view, (Integer) view.getTag());
        }
    }


    class ViewHolder extends RecyclerView.ViewHolder {
        RelativeLayout content;

        ViewHolder(View itemView) {
            super(itemView);
            content=itemView.findViewById(R.id.content);
        }
    }

    @Override
    public MusicAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        mContext=parent.getContext();
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_music_content, parent, false);
        ViewHolder holder = new ViewHolder(view);
        view.setOnClickListener(this);
        view.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (onLongItemClickListener!=null)
                    onLongItemClickListener.onLongItemClick(v, (Integer) v.getTag());
                return true;
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(MusicAdapter.ViewHolder holder, int position) {
        music curMusic = musicList.get(position);
        holder.content.removeAllViews();
        if(position == selected){
            holder.content.addView(LayoutInflater.from(mContext).inflate(R.layout.item_music_focus,null,false));
            TextView textView = holder.content.findViewById(R.id.music_playing);
            textView.setText("Now playing: "+ curMusic.getMusicName());
        }else{
            holder.content.addView(LayoutInflater.from(mContext).inflate(R.layout.item_music_list,null,false));
            TextView musicName=holder.content.findViewById(R.id.music_name);
            TextView musicSinger=holder.content.findViewById(R.id.music_singer);

            musicName.setText(curMusic.getMusicName());
            musicSinger.setText(curMusic.getSinger());
        }
        holder.itemView.setTag(position);
    }

    @Override
    public int getItemCount() {
        return musicList.size();
    }


    public void setSelected(int selected) {
        this.selected = selected;
    }

    public MusicAdapter(List<music> musicList) {
        this.musicList = musicList;
    }



}
