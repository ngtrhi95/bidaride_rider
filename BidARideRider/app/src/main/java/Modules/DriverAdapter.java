package Modules;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.example.trhie.bidariderider.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by trhie on 5/2/2017.
 */

public class DriverAdapter extends BaseAdapter {

    public List<String> getListDriverName() {
        return listDriverName;
    }

    public void setListDriverName(List<String> listDriverName) {
        this.listDriverName = listDriverName;
    }

    public List<String> getLisyDriverPhone() {
        return listDriverPhone;
    }

    public void setLisyDriverPhone(List<String> lisyDriverPhone) {
        this.listDriverPhone = lisyDriverPhone;
    }

    public List<String> getListDriverDistance() {
        return listDriverDistance;
    }

    public void setListDriverDistance(List<String> listDriverDistance) {
        this.listDriverDistance = listDriverDistance;
    }

    class ViewHolder {
        TextView name;
        TextView phone;
        TextView distance;
    }

    private List<String> listDriverName;
    private List<String> listDriverPhone;
    private List<String> listDriverDistance;
    LayoutInflater infater = null;
    private Context mContext;

    public DriverAdapter (Context context, List<String> names, List<String> phones, List<String> distances) {
        infater = LayoutInflater.from(context);
        mContext = context;
        this.listDriverName = names;
        this.listDriverPhone = phones;
        this.listDriverDistance = distances;
    }


    @Override
    public int getCount() {
        return listDriverName.size();
    }

    @Override
    public Object getItem(int position) {
        List<String> result = new ArrayList<String>();
        result.add(listDriverName.get(position));
        result.add(listDriverPhone.get(position));
        result.add(listDriverDistance.get(position));
        return result;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        if (convertView == null){
            convertView = infater.inflate(R.layout.item_listview, null);
            holder = new ViewHolder();
            holder.name = (TextView)convertView.findViewById(R.id.drivername);
            holder.phone = (TextView)convertView.findViewById(R.id.driverphone);
            holder.distance = (TextView) convertView.findViewById(R.id.distance);

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }
        final List<String> item = (List<String>) getItem(position);
        if (item != null) {
            //   holder.appIcon.setImageResource(item.);
            final ViewHolder finalHolder = holder;

            finalHolder.name.setText(item.get(0));
            finalHolder.phone.setText(item.get(1));
            finalHolder.distance.setText(item.get(2));
            //imageLoader.displayImage(item.getIcon(), finalHolder.appIcon);
        }
        return convertView;
    }
}
