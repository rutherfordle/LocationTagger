package com.example.tagger;

import java.util.List;

import com.example.tagger.MainActivity.ListViewItem;

import android.app.Activity;  
import android.content.Context;
import android.view.LayoutInflater;  
import android.view.View;  
import android.view.ViewGroup;  
import android.widget.BaseAdapter;  
import android.widget.ImageView;  
import android.widget.TextView;  
  
public class CustomListViewAdapter extends BaseAdapter
{  
	
	LayoutInflater inflater;
	List<ListViewItem> items;
	
    public CustomListViewAdapter(Context context, List<ListViewItem> items) {  
        super();
		
        this.items = items;
        this.inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }
    
    @Override  
    public int getCount() {  
        // TODO Auto-generated method stub  
        return items.size();  
    }  
  
    @Override  
    public Object getItem(int position) {  
        // TODO Auto-generated method stub  
        return null;  
    }  
  
    @Override  
    public long getItemId(int position) {  
        // TODO Auto-generated method stub  
        return 0;  
    }
      
    @Override  
    public View getView(final int position, View convertView, ViewGroup parent) {  
        // TODO Auto-generated method stub  

    	ListViewItem item = items.get(position);
    	
    	View vi=convertView;
        
        if(convertView==null)
            vi = inflater.inflate(R.layout.item_row, null);
        
        TextView txtTitle = (TextView) vi.findViewById(R.id.txtTitle); 
        txtTitle.setText(item.Title);
        return vi;  
    }

	public void clear() {
		items.clear();
		
	}
}