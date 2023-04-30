package fr.ralala.ministock.ui.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.ralala.ministock.R;
import fr.ralala.ministock.models.CartEntry;
import fr.ralala.ministock.models.CartItem;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Adapter used for the RecyclerView
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class AdapterCartEntries extends RecyclerView.Adapter<AdapterCartEntries.ViewHolder> {
  private static final int RES_ID = R.layout.view_cart_entry;
  private final List<CartEntry> mItems;
  private final RecyclerView mRecyclerView;

  /**
   * Creates the array adapter.
   *
   * @param recyclerView The owner object.
   * @param objects      The objects list.
   */
  public AdapterCartEntries(RecyclerView recyclerView,
                            final List<CartEntry> objects) {
    mRecyclerView = recyclerView;
    mItems = objects;
  }

  /**
   * Returns an item.
   *
   * @param position Item position.
   * @return T
   */
  public CartEntry getItem(int position) {
    return mItems.get(position);
  }

  /**
   * Called when the view is created.
   *
   * @param viewGroup The view group.
   * @param i         The position
   * @return ViewHolder
   */
  @Override
  public @NonNull AdapterCartEntries.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
    View view = LayoutInflater.from(viewGroup.getContext()).inflate(RES_ID, viewGroup, false);
    return new ViewHolder(view);
  }

  /**
   * Called on Binding the view holder.
   *
   * @param viewHolder The view holder.
   * @param i          The position.
   */
  @Override
  public void onBindViewHolder(@NonNull AdapterCartEntries.ViewHolder viewHolder, int i) {
    if (mItems.isEmpty()) return;
    if (i > mItems.size())
      i = 0;
    final CartEntry t = mItems.get(i);
    if (t != null) {
      t.sortItems();
      List<CartItem> items = t.getItems();
      viewHolder.tvLogo.setText(t.getTitle());
      viewHolder.ivLogo.setImageBitmap(t.getImage());
      viewHolder.tvCount.setText(String.valueOf(items.size()));
      if (!items.isEmpty())
        viewHolder.tvDate.setText(items.get(0).getDate());
    }
  }

  /**
   * Returns the items count/
   *
   * @return int
   */
  @Override
  public int getItemCount() {
    return mItems.size();
  }

  /**
   * Adds an item.
   *
   * @param item The item to add.
   */
  public void addItem(CartEntry item) {
    mItems.add(item);
    safeNotifyDataSetChanged();
  }

  /**
   * Removes an item.
   *
   * @param item The item to remove.
   */
  public void removeItem(CartEntry item) {
    mItems.remove(item);
    safeNotifyDataSetChanged();
  }

  /**
   * This method call mRecyclerView.getRecycledViewPool().clear() and notifyDataSetChanged().
   */
  @SuppressLint("NotifyDataSetChanged")
  public void safeNotifyDataSetChanged() {
    mRecyclerView.getRecycledViewPool().clear();
    try {
      notifyDataSetChanged();
    } catch (Exception e) {
      Log.e(getClass().getSimpleName(), "Exception: " + e.getMessage(), e);
    }
  }

  protected static class ViewHolder extends RecyclerView.ViewHolder {
    AppCompatTextView tvLogo;
    AppCompatImageView ivLogo;
    AppCompatTextView tvCount;
    AppCompatTextView tvDate;

    ViewHolder(View view) {
      super(view);
      tvLogo = view.findViewById(R.id.tvLogo);
      ivLogo = view.findViewById(R.id.ivLogo);
      tvCount = view.findViewById(R.id.tvCount);
      tvDate = view.findViewById(R.id.tvDate);
    }
  }
}