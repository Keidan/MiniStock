package fr.ralala.ministock.ui.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatTextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import fr.ralala.ministock.R;
import fr.ralala.ministock.db.models.CartItem;

/**
 * ******************************************************************************
 * <p><b>Project MiniStock</b><br/>
 * Adapter used for the ListView
 * </p>
 *
 * @author Keidan
 * ******************************************************************************
 */
public class AdapterCartItems extends ArrayAdapter<CartItem> {
  private static final int ID = R.layout.view_cart_item;
  private final List<CartItem> mItems;
  private final OnClick mListener;

  public AdapterCartItems(final Context context,
                          final List<CartItem> objects,
                          final OnClick listener) {
    super(context, ID, objects);
    mListener = listener;
    mItems = new ArrayList<>(objects);
  }

  /**
   * Returns the items.
   *
   * @return List<CartItem>
   */
  public List<CartItem> getItems() {
    return mItems;
  }

  /**
   * How many items are in the data set represented by this Adapter.
   *
   * @return Count of items.
   */
  @Override
  public int getCount() {
    return mItems.size();
  }

  /**
   * Get the row id associated with the specified position in the list.
   *
   * @param position The position of the item within the adapter's data set whose row id we want.
   * @return The id of the item at the specified position.
   */
  @Override
  public long getItemId(int position) {
    return mItems.get(position).hashCode();
  }

  /**
   * Remove all elements from the list.
   */
  @Override
  public void clear() {
    mItems.clear();
    notifyDataSetChanged();
  }

  /**
   * Remove an elements from the list.
   */
  public void remove(int index) {
    mItems.remove(index);
    notifyDataSetChanged();
  }

  /**
   * Adds a list of new items to the list.
   *
   * @param collection The items to be added.
   */
  @Override
  public void addAll(@NonNull Collection<? extends CartItem> collection) {
    mItems.addAll(collection);
    notifyDataSetChanged();
  }

  /**
   * Adds a new item to the list.
   */
  @Override
  public void add(CartItem item) {
    mItems.add(item);
    notifyDataSetChanged();
  }

  /**
   * Get a View that displays the data at the specified position in the data set.
   *
   * @param position    The position of the item within the adapter's data set of the item whose view we want.
   * @param convertView This value may be null.
   * @param parent      This value cannot be null.
   * @return This value cannot be null.
   */
  @Override
  public @NonNull View getView(final int position, final View convertView,
                               @NonNull final ViewGroup parent) {
    View v = convertView;
    if (v == null) {
      final LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
      if (inflater != null) {
        v = inflater.inflate(ID, null);
        ViewHolder vh = new ViewHolder();
        vh.ivPhoto = v.findViewById(R.id.ivPhoto);
        vh.tvCodeLabel = v.findViewById(R.id.tvCodeLabel);
        vh.tvCode = v.findViewById(R.id.tvCode);
        vh.tvCreation = v.findViewById(R.id.tvCreation);
        v.setTag(vh);
      }
    }
    if (v != null && v.getTag() != null) {
      ViewHolder vh = (ViewHolder) v.getTag();
      CartItem ci = mItems.get(position);
      vh.ivPhoto.setOnClickListener(unused -> mListener.onClick(ci, position));
      if (ci.getQrCodeId() == null || ci.getQrCodeId().trim().isEmpty()) {
        vh.tvCodeLabel.setVisibility(View.GONE);
        vh.tvCode.setVisibility(View.GONE);
      } else {
        vh.tvCodeLabel.setVisibility(View.VISIBLE);
        vh.tvCode.setVisibility(View.VISIBLE);
        vh.tvCode.setText(ci.getQrCodeId());
      }
      vh.tvCreation.setText(ci.getCreationDate());
    }
    return v == null ? new View(getContext()) : v;
  }

  public interface OnClick {
    void onClick(CartItem ci, int position);
  }

  private static class ViewHolder {
    ImageView ivPhoto;
    AppCompatTextView tvCodeLabel;
    AppCompatTextView tvCode;
    AppCompatTextView tvCreation;
  }
}
