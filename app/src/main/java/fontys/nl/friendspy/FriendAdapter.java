package fontys.nl.friendspy;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by oriol on 6/3/2017.
 */

public class FriendAdapter extends BaseAdapter implements Filterable {

    public Context context;
    public ArrayList<Friend> friendArrayList;
    public ArrayList<Friend> orig;

    public FriendAdapter(Context context, ArrayList<Friend> friendArrayList) {
        super();
        this.context = context;
        this.friendArrayList = friendArrayList;
    }


    public class EmployeeHolder
    {
        TextView name;
        TextView email;
    }

    public Filter getFilter() {
        return new Filter() {

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                final FilterResults oReturn = new FilterResults();
                final ArrayList<Friend> results = new ArrayList<Friend>();
                if (orig == null)
                    orig = friendArrayList;
                if (constraint != null) {
                    if (orig != null && orig.size() > 0) {
                        for (final Friend g : orig) {
                            if (g.getName().toLowerCase()
                                    .contains(constraint.toString()))
                                results.add(g);
                        }
                    }
                    oReturn.values = results;
                }
                return oReturn;
            }

            @SuppressWarnings("unchecked")
            @Override
            protected void publishResults(CharSequence constraint,
                                          FilterResults results) {
                friendArrayList = (ArrayList<Friend>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
    }


    @Override
    public int getCount() {
        return friendArrayList.size();
    }

    @Override
    public Object getItem(int position) {
        return friendArrayList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final EmployeeHolder holder;
        if(convertView==null)
        {
            convertView= LayoutInflater.from(context).inflate(R.layout.row, parent, false);
            holder=new EmployeeHolder();
            holder.name=(TextView) convertView.findViewById(R.id.txtName);
            holder.email=(TextView) convertView.findViewById(R.id.txtEmail);
            final ImageButton mButton = (ImageButton) convertView.findViewById(R.id.mButton);
            final ImageButton mButtonDelete = (ImageButton) convertView.findViewById(R.id.mButtonDelete);
            ContextUser myUser = ContextUser.getInstance();

            Friend f = (Friend) getItem(position);

            if (myUser.isFriend(f.getEmail())) {
                mButton.setVisibility(View.GONE);
                mButtonDelete.setVisibility(View.VISIBLE);
            }
            else{
                mButton.setVisibility(View.VISIBLE);
                mButtonDelete.setVisibility(View.GONE);
            }

            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                        String friendEmail = (String) holder.email.getText();
                        ContextUser myUser = ContextUser.getInstance();
                        myUser.addFriend((String) holder.email.getText());
                        mButton.setVisibility(View.GONE);
                        mButtonDelete.setVisibility(View.VISIBLE);

                }
            });

            mButtonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    ContextUser myUser = ContextUser.getInstance();
                    myUser.deleteFriend((String) holder.email.getText());
                    mButton.setVisibility(View.VISIBLE);
                    mButtonDelete.setVisibility(View.GONE);

                }
            });

            convertView.setTag(holder);
        }
        else
        {
            holder=(EmployeeHolder) convertView.getTag();
        }

        holder.name.setText(friendArrayList.get(position).getName());
        holder.email.setText(String.valueOf(friendArrayList.get(position).getEmail()));
        return convertView;

    }




}
