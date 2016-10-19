package com.openup.covadonga.covadongaapp;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.openup.covadonga.covadongaapp.util.Order;

import java.util.ArrayList;

/**
 * Created by Emilino on 04/08/2015.
 */
public class OrderListAdapter extends BaseAdapter {

    private static ArrayList<Order> searchArrayList;

    private LayoutInflater mInflater;

    public OrderListAdapter(Context context, ArrayList<Order> results) {
        searchArrayList = results;
        mInflater = LayoutInflater.from(context);
    }

    public int getCount() {
        return searchArrayList.size();
    }

    public Object getItem(int position) {
        return searchArrayList.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.row_orders, null);
            holder = new ViewHolder();
            holder.txtDesc = (TextView) convertView.findViewById(R.id.textViewDesc);
            //holder.txtOrden = (TextView) convertView.findViewById(R.id.textViewOrden);
            holder.txtFact = (TextView) convertView.findViewById(R.id.textViewFact);
            holder.txtRecibido = (TextView) convertView.findViewById(R.id.textViewRecibido);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.txtDesc.setText(searchArrayList.get(position).getCodigoDesc());
        //holder.txtOrden.setText(Double.toString(searchArrayList.get(position).getCantOrdenada()));
        holder.txtFact.setText(Double.toString(searchArrayList.get(position).getCantFactura()));
        holder.txtRecibido.setText(Double.toString(searchArrayList.get(position).getCantRecibida()));

        return convertView;
    }

    static class ViewHolder {
        TextView txtDesc;
        //TextView txtOrden;
        TextView txtFact;
        TextView txtRecibido;
    }
}
