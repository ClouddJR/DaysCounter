package com.arkadiusz.dayscounter.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.TextView;
import com.arkadiusz.dayscounter.database.Event;
import com.arkadiusz.dayscounter.utils.SharedPreferencesUtils;
import io.realm.OrderedRealmCollection;
import io.realm.RealmBaseAdapter;

/**
 * Created by Arkadiusz on 08.01.2017
 */

public class WidgetConfigureAdapter_old extends RealmBaseAdapter<Event> implements ListAdapter {

  private OrderedRealmCollection<Event> events;
  private Context mContext;

  private class ViewHolder {
    TextView eventitle;
  }

  public WidgetConfigureAdapter_old(Context context, OrderedRealmCollection<Event> realmResults) {
    super(context, realmResults);
    this.mContext = context;
    this.events = realmResults;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup parent) {
    ViewHolder viewHolder;
    if (convertView == null) {
      convertView = LayoutInflater.from(parent.getContext())
          .inflate(android.R.layout.simple_list_item_1, parent, false);
      viewHolder = new ViewHolder();
      viewHolder.eventitle = (TextView) convertView.findViewById(android.R.id.text1);
      convertView.setTag(viewHolder);
    } else {
      viewHolder = (ViewHolder) convertView.getTag();
    }

    Event item = events.get(position);
    if(SharedPreferencesUtils.isBlackTheme(context)) {
      viewHolder.eventitle.setTextColor(Color.WHITE);
    }
    viewHolder.eventitle.setText(item.getName());
    return convertView;
  }
}