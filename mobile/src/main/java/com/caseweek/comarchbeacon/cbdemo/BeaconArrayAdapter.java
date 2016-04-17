package com.caseweek.comarchbeacon.cbdemo;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

class BeaconArrayAdapter extends ArrayAdapter<CBeacon> {

    private static final int BLACK = Color.rgb(0, 0, 0);
    private static final int GREEN  = Color.rgb(0, 142, 9);
    private static final int ORANGE = Color.rgb(255, 165, 0);
    private static final int RED = Color.rgb(255, 5, 5);
    private static final int GREY = Color.rgb(150, 150, 150);

    public BeaconArrayAdapter(Context context, int resource, List<CBeacon> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final CBeacon beacon = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.cbeacon_list_item, parent, false);
        }
        //ImageView registrationStatus = (ImageView) convertView.findViewById(R.id.registrationStatus);
        TextView beaconUuid = (TextView) convertView.findViewById(R.id.cbeacon_uuid_value);
        beaconUuid.setText(beacon.getUuid());
        TextView beaconMajor = (TextView) convertView.findViewById(R.id.cbeacon_major_value);
        beaconMajor.setText(""+beacon.getMajor());
        TextView beaconMinor = (TextView) convertView.findViewById(R.id.cbeacon_minor_value);
        beaconMinor.setText(""+beacon.getMinor());
        TextView beaconAccuracy = (TextView) convertView.findViewById(R.id.cbeacon_accuracy_value);
        beaconAccuracy.setText(""+beacon.getAccuracy());
        TextView beaconDistance = (TextView) convertView.findViewById(R.id.cbeacon_distance_value);
        beaconDistance.setText(beacon.getDistance());
        TextView beaconRssi = (TextView) convertView.findViewById(R.id.cbeacon_rssi_value);
        beaconRssi.setText(""+beacon.getRssi());
        return convertView;
    }
}