package fr.ralala.ministock.ui.adapters;

import android.annotation.SuppressLint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

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
public class AdapterCartEntries extends RecyclerView.Adapter<AdapterCartEntries.ViewHolder> implements Filterable {
  private static final int RES_ID = R.layout.view_cart_entry;
  private final List<CartEntry> mItemsReal;
  private final List<Integer> mItems;
  private List<Integer> mItemsSearch;
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
    mItemsReal = objects;
    mItems = new ArrayList<>();
    mItemsSearch = new ArrayList<>();
    for (int i = 0; i < mItemsReal.size(); i++) {
      mItems.add(i);
      mItemsSearch.add(i);
    }
  }

  private CartEntry getCartEntry(int i) {
    int j = mItemsSearch.get(i);
    return mItemsReal.size() <= j ? null : mItemsReal.get(j);
  }

  /**
   * Returns an item.
   *
   * @param position Item position.
   * @return T
   */
  public CartEntry getItem(int position) {
    return getCartEntry(position);
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
    if (mItemsSearch.isEmpty()) return;
    if (i > mItemsSearch.size())
      i = 0;
    final CartEntry t = getCartEntry(i);
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
    return mItemsSearch.size();
  }

  /**
   * Adds an item.
   *
   * @param item The item to add.
   */
  public void addItem(CartEntry item) {
    mItemsReal.add(item);
    mItems.add(mItemsReal.size());
    safeNotifyDataSetChanged();
  }

  /**
   * Removes an item.
   *
   * @param item The item to remove.
   */
  public void removeItem(CartEntry item) {
    int idx = mItemsReal.indexOf(item);
    if (idx != -1)
      mItems.remove(idx);
    mItemsReal.add(item);
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

  public void setFilteredList(List<Integer> filteredList) {
    mItemsSearch = filteredList;
  }

  public List<Integer> apply(CharSequence constraint) {
    String charString = constraint == null ? "" : constraint.toString().toLowerCase(Locale.ROOT);
    final List<Integer> filteredList = new ArrayList<>();
    if (charString.isEmpty())
      filteredList.addAll(mItems);
    else {
      mItemsReal.stream().filter(item -> item.getTitle().toLowerCase(Locale.ROOT).contains(constraint))
        .forEach(item -> filteredList.add(mItemsReal.indexOf(item)));
    }
    return filteredList;
  }

  /**
   * Get custom filter
   *
   * @return filter
   */
  @Override
  public Filter getFilter() {
    return new Filter() {
      @Override
      protected FilterResults performFiltering(CharSequence constraint) {
        final List<Integer> filteredList = apply(constraint);
        FilterResults fr = new FilterResults();
        fr.count = filteredList.size();
        fr.values = filteredList;
        return fr;
      }

      /**
       * Notify about filtered list to ui
       *
       * @param constraint text
       * @param results    filtered result
       */
      @SuppressWarnings("unchecked")
      @Override
      protected void publishResults(CharSequence constraint, FilterResults results) {
        setFilteredList((List<Integer>) results.values);
        safeNotifyDataSetChanged();
      }
    };
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