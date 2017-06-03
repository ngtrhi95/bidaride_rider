package Modules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.trhie.bidariderider.R;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by trhie on 5/31/2017.
 */

public class TripAdapter extends BaseAdapter {
    private Context mContext;
    private LayoutInflater mInflater;
    private ArrayList<Trip> mDataSource;

    public TripAdapter(Context context, ArrayList<Trip> items) {
        mContext = context;
        mDataSource = items;
        mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    //1
    @Override
    public int getCount() {
        return mDataSource.size();
    }

    //2
    @Override
    public Object getItem(int position) {
        return mDataSource.get(position);
    }

    //3
    @Override
    public long getItemId(int position) {
        return position;
    }

    //4
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Trip trip = (Trip) getItem(position);
        // Get view for row item
        View rowView = mInflater.inflate(R.layout.log_item, parent, false);

        TextView driverFullName =
                (TextView) rowView.findViewById(R.id.driverFullName);
        TextView driverPhone =
                (TextView) rowView.findViewById(R.id.phonedriver);
        TextView from =
                (TextView) rowView.findViewById(R.id.tvfromLo);
        TextView to =
                (TextView) rowView.findViewById(R.id.tvtoLo);
        TextView time =
                (TextView) rowView.findViewById(R.id.tvHisTime);
        TextView price = (TextView) rowView.findViewById(R.id.tvHisCost);

        driverFullName.setText(trip.getDriverFullname());
        driverPhone.setText(trip.getDriverPhone());
        from.setText(trip.getTripFrom());
        to.setText(trip.getTripTo());
        DateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd");
        DateFormat outputFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String inputDateStr=trip.getCreatedDate();
        Date date = null;
        try {
            date = inputFormat.parse(inputDateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        String outputDateStr = outputFormat.format(date);
        time.setText(outputDateStr);
        price.setText(String.valueOf(trip.getPrice()) + " VNĐ");

        return rowView;
    }
}
