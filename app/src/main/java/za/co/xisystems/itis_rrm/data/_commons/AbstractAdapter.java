package za.co.xisystems.itis_rrm.data._commons;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import za.co.xisystems.itis_rrm.R;
import za.co.xisystems.itis_rrm.data.localDB.entities.ItemDTO;
import za.co.xisystems.itis_rrm.ui.mainview.create.new_job_utils.contracts.IAdapter;


public abstract class AbstractAdapter<T> extends RecyclerView.Adapter<AbstractAdapter.AbstractViewHolder> implements IAdapter<T> {
    protected List<T> data = new ArrayList<>();
    private List<T> originalData = new ArrayList<>();
    protected int layout;
    protected OnItemClickListener<T> listener;
    private boolean canSelectItem = true;

    public AbstractAdapter(List<T> data, int layout) {
        setData(data);
        this.layout = layout;
    }

    @Override public RecyclerView.Adapter getAdapter() {
        return this;
    }

    public AbstractAdapter(List<T> data, int layout, OnItemClickListener<T> listener) {
        this(data, layout);
        this.listener = listener;
    }

    public void enableItemClickListener() {
        canSelectItem = true;
    }

    public void disableItemClickListener() {
        canSelectItem = false;
    }

    public abstract class AbstractViewHolder extends RecyclerView.ViewHolder {
        Animation click;

        protected AbstractViewHolder(View view) {
            super(view);
            click = AnimationUtils.loadAnimation(view.getContext(), R.anim.click);
            init(view);
        }

        public abstract void init(View view);

        public void setData(final int position, final T item) {
            this.itemView.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) {
                    v.startAnimation(click);
                    if (listener != null && canSelectItem) {
                        listener.onItemClick(item);
                    }
                }
            });
        }
    }

    // Create new views (invoked by the layout manager)
    @NonNull @Override
    public AbstractViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext()).inflate(layout, parent, false);
        return createViewHolder(v);
    }

    public abstract AbstractViewHolder createViewHolder(View view);

    @Override
    public void onBindViewHolder(@NonNull AbstractAdapter.AbstractViewHolder holder, int position) {
        holder.setData(position, data.get(position));
    }

    public void setData(List<T> data) {
        updateData(data);
        setOriginalData();
        notifyDataSetChanged();
    }

    private void setOriginalData(){
        originalData = new ArrayList<>(data);
    }

    public void resetOriginalData(){
        updateData(originalData);
    }

    public void updateData(List<T> _data){
        this.data.clear();
        if (_data != null) this.data.addAll(_data);
        notifyDataSetChanged();
    }

    public List<T> getData() {
        return data;
    }

    protected List<T> getOriginalData(){
        return  originalData;
    }

    public void deleteItem(int position) {
        if (0 <= position && position < getItemCount()) {
            data.remove(position);
            setOriginalData();
            notifyDataSetChanged();
        }
    }

    public void deleteItem(ItemDTO item) {
        data.remove(item);
        setOriginalData();
        notifyDataSetChanged();
    }

    public T getItem(int position) {
        return data == null || position >= data.size() ? null : data.get(position);
    }

    public void addItem(T item) {
        if (data != null) this.data.add(item);
        setOriginalData();
        notifyDataSetChanged();
    }

    public void clearData() {
        data.clear();
        setOriginalData();
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public interface OnItemClickListener<T> {
        void onItemClick(T item);
    }
}