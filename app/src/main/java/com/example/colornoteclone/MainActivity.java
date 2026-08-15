package com.example.colornoteclone;

import android.app.*;
import android.os.Bundle;
import android.content.*;
import android.graphics.Color;
import android.view.*;
import android.widget.*;
import androidx.recyclerview.widget.*;
import org.json.*;
import java.util.*;

public class MainActivity extends Activity {
    ArrayList<Note> notes = new ArrayList<>();
    NoteAdapter adapter;
    android.content.SharedPreferences prefs;

    @Override public void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_main);
        prefs = getSharedPreferences("notes", MODE_PRIVATE);
        load();
        RecyclerView list = findViewById(R.id.list);
        list.setLayoutManager(new LinearLayoutManager(this));
        adapter = new NoteAdapter();
        list.setAdapter(adapter);

        EditText search = findViewById(R.id.search);
        search.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s,int a,int c,int d){}
            public void onTextChanged(CharSequence s,int a,int b,int c){ adapter.filter(s.toString()); }
            public void afterTextChanged(android.text.Editable e){}
        });
        findViewById(R.id.add).setOnClickListener(v -> editor(null));
        updateCount();
    }

    void editor(Note existing) {
        LinearLayout box = new LinearLayout(this);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setPadding(24, 4, 24, 4);
        EditText title = new EditText(this); title.setHint("Title");
        EditText body = new EditText(this); body.setHint("Write your note..."); body.setMinLines(6);
        box.addView(title); box.addView(body);
        if(existing != null){ title.setText(existing.title); body.setText(existing.body); }
        new AlertDialog.Builder(this).setTitle(existing==null?"New note":"Edit note")
            .setView(box).setPositiveButton("Save",(d,w)->{
                if(existing==null) notes.add(new Note(title.getText().toString(),body.getText().toString()));
                else { existing.title=title.getText().toString(); existing.body=body.getText().toString(); }
                save(); adapter.notifyDataSetChanged(); updateCount();
            }).setNegativeButton("Cancel",null).show();
    }

    void delete(Note n){
        new AlertDialog.Builder(this).setTitle("Delete note?").setMessage(n.title)
            .setPositiveButton("Delete",(d,w)->{notes.remove(n);save();adapter.notifyDataSetChanged();updateCount();})
            .setNegativeButton("Cancel",null).show();
    }

    void updateCount(){ ((TextView)findViewById(R.id.count)).setText(notes.size()+" notes"); }

    void save(){
        JSONArray a=new JSONArray();
        try { for(Note n:notes){JSONObject o=new JSONObject();o.put("title",n.title);o.put("body",n.body);a.put(o);}
        }catch(Exception ignored){}
        prefs.edit().putString("data",a.toString()).apply();
    }
    void load(){
        String s=prefs.getString("data","[]");
        try{JSONArray a=new JSONArray(s);for(int i=0;i<a.length();i++){JSONObject o=a.getJSONObject(i);notes.add(new Note(o.optString("title"),o.optString("body")));}}catch(Exception ignored){}
    }

    class Note { String title,body; Note(String t,String b){title=t;body=b;} }

    class NoteAdapter extends RecyclerView.Adapter<NoteVH>{
        ArrayList<Note> shown=new ArrayList<>(notes);
        void filter(String q){shown.clear();q=q.toLowerCase();for(Note n:notes)if((n.title+" "+n.body).toLowerCase().contains(q))shown.add(n);notifyDataSetChanged();}
        public NoteVH onCreateViewHolder(android.view.ViewGroup p,int v){
            TextView t=new TextView(MainActivity.this);t.setPadding(18,18,18,18);t.setTextSize(16);return new NoteVH(t);
        }
        public void onBindViewHolder(NoteVH h,int pos){
            Note n=shown.get(pos);h.t.setText((n.title.isEmpty()?"Untitled":n.title)+"\n"+n.body);
            h.t.setBackgroundColor(Color.rgb(255,249,196));
            h.t.setOnClickListener(v->editor(n));
            h.t.setOnLongClickListener(v->{delete(n);return true;});
        }
        public int getItemCount(){return shown.size();}
    }
    class NoteVH extends RecyclerView.ViewHolder { TextView t; NoteVH(View v){super(v);t=(TextView)v;} }
}
