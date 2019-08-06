package fr.ralala.ministock.ui.adapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.List;

import fr.ralala.ministock.R;
import fr.ralala.ministock.models.ShoppingCartEntry;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Adapter used for the RecyclerView
 * </p>
 *
 * @author Keidan
 * <p>
 * ******************************************************************************
 */
public class AdapterCartItems extends RecyclerView.Adapter<AdapterCartItems.ViewHolder>{
  private static final int RES_ID = R.layout.view_cart_item;
  private List<ShoppingCartEntry> mItems;
  private RecyclerView mRecyclerView;

  /**
   * Creates the array adapter.
   * @param recyclerView The owner object.
   * @param objects The objects list.
   */
  public AdapterCartItems(RecyclerView recyclerView,
                          final List<ShoppingCartEntry> objects) {
    mRecyclerView = recyclerView;
    mItems = objects;
  }

  /**
   * Returns an item.
   * @param position Item position.
   * @return T
   */
  public ShoppingCartEntry getItem(int position) {
    return mItems.get(position);
  }

  /**
   * Called when the view is created.
   * @param viewGroup The view group.
   * @param i The position
   * @return ViewHolder
   */
  @Override
  public @NonNull AdapterCartItems.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(RES_ID, viewGroup, false);
    return new ViewHolder(view);
  }

  /**
   * Called on Binding the view holder.
   * @param viewHolder The view holder.
   * @param i The position.
   */
  @Override
  public void onBindViewHolder(@NonNull AdapterCartItems.ViewHolder viewHolder, int i) {
    if(mItems.isEmpty()) return;
    if(i > mItems.size())
      i = 0;
    final ShoppingCartEntry t = mItems.get(i);
    if (t != null) {
      viewHolder.tvLogo.setText(t.getTitle());
      viewHolder.ivLogo.setImageBitmap(t.getImage());
      viewHolder.tvCount.setText(String.valueOf(t.getCount()));

    }
  }

  /**
   * Returns the items count/
   * @return int
   */
  @Override
  public int getItemCount() {
    return mItems.size();
  }

  /**
   * Adds an item.
   * @param item The item to add.
   */
  public void addItem(ShoppingCartEntry item) {
    mItems.add(item);
    safeNotifyDataSetChanged();
  }

  /**
   * Removes an item.
   * @param item The item to remove.
   */
  public void removeItem(ShoppingCartEntry item) {
    mItems.remove(item);
    safeNotifyDataSetChanged();
  }

  /**
   * This method call mRecyclerView.getRecycledViewPool().clear() and notifyDataSetChanged().
   */
  public void safeNotifyDataSetChanged() {
    mRecyclerView.getRecycledViewPool().clear();
    try {
      notifyDataSetChanged();
    } catch(Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
    }
  }

  class ViewHolder extends RecyclerView.ViewHolder{
    AppCompatTextView tvLogo;
    AppCompatImageView ivLogo;
    AppCompatTextView tvCount;

    ViewHolder(View view) {
      super(view);
      tvLogo = view.findViewById(R.id.tvLogo);
      ivLogo = view.findViewById(R.id.ivLogo);
      tvCount = view.findViewById(R.id.tvCount);
    }
  }
}