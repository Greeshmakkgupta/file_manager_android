package net.dimk.simplefilemanager;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;import android.Manifest;
import android.content.DialogInterface;
import android.os.Environment;
import android.os.StrictMode;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
//import android.support.v7.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener {
    View view;
    Menu menu;
    ListView listView;

    ArrayAdapter<String>adapter;
    private long back_pressed_Time;
    private Toast backToast;
    String[] spnAction={"Settings", "New Folder", "Cut", "Copy", "Paste", "Rename", "Delete","BACKGROUND_COLOR", "  • lightyellow","  • mediumseagreen","  • lightcyan","  • palegoldenrod","  • white"};

    public ListView lst_Folder;
    public String dirPath="";
    public String ParentdirPath="";
    public ArrayList<String> theNamesOfFiles;
    public ArrayList<Integer> intImages;
    public TextView txtPath;
    public Spinner spin;
    public CustomList customList;
    public File dir;
    public ArrayList<Integer> intSelected;
    public ArrayList<String> strSelected;
    public Integer intCutORCopy=0;


    public final String[] EXTERNAL_PERMS = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE};
    public final int EXTERNAL_REQUEST = 138;

    public boolean requestForExternalStoragePermission() {

        boolean isPermissionOn = true;
        final int version = Build.VERSION.SDK_INT;
        if (version >= 23) {
            if (!canAccessExternalSd()) {
                isPermissionOn = false;
                requestPermissions(EXTERNAL_PERMS, EXTERNAL_REQUEST);
            }
        }

        return isPermissionOn;
    }

    public boolean canAccessExternalSd() {
        return (hasPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }

    private boolean hasPermission(String perm) {
        return (PackageManager.PERMISSION_GRANTED == ContextCompat.checkSelfPermission(this, perm));

    }

    @Override

    public void onBackPressed()
    {
      if(back_pressed_Time+2000>System.currentTimeMillis())
      {
          backToast.cancel();
          super.onBackPressed();
          return;
      }
      else
      {
          backToast=Toast.makeText(getBaseContext(),"PRESS BACK AGAIN TO EXIT",Toast.LENGTH_SHORT);
          backToast.show();
      }
      back_pressed_Time=System.currentTimeMillis();
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        view=this.getWindow().getDecorView();
        view.setBackgroundResource(R.color.lightyellow);


        listView=(ListView)findViewById(R.id.mylistview);
        adapter=new ArrayAdapter<>(this,android.R.layout.simple_expandable_list_item_1);
        listView.setAdapter(adapter);


        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);


        requestForExternalStoragePermission();


        theNamesOfFiles = new ArrayList<String >();
        intImages = new ArrayList<Integer>();
        strSelected = new ArrayList<String>();
        intSelected = new ArrayList<Integer>();


        lst_Folder=(ListView)findViewById(R.id.lstFolder);


        txtPath=(TextView) findViewById(R.id.txtvPath);


        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {
            dirPath = String.valueOf(android.os.Environment.getExternalStorageDirectory());
        }

        RefreshListView();
        set_Adapter();
        setPath();

        spin = (Spinner) findViewById(R.id.spinner);
        spin.setOnItemSelectedListener(this);


        ArrayAdapter aa = new ArrayAdapter(this,android.R.layout.simple_spinner_item,spnAction);
        aa.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spin.setAdapter(aa);


        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());

        lst_Folder.setOnItemClickListener(new android.widget.AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try{
                    ParentdirPath = dirPath+"/..";
                    dirPath = dirPath+"/"+theNamesOfFiles.get(position);

                    File f = new File(dirPath);
                    if (f.isDirectory()){
                        RefreshListView();
                        RefreshAdapter();
                        setPath();
                    }else{
                        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                        intent.setAction(android.content.Intent.ACTION_VIEW);
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        File file = new File(dirPath);

                        MimeTypeMap mime = MimeTypeMap.getSingleton();
                        String ext = file.getName().substring(file.getName().indexOf(".") + 1);
                        String type = mime.getMimeTypeFromExtension(ext);
                        intent.setDataAndType(Uri.fromFile(file), type);
                        startActivity(intent);
                    }

                }catch (Exception e){
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }

            }
        });

        lst_Folder.setChoiceMode(lst_Folder.CHOICE_MODE_MULTIPLE);


        lst_Folder.setOnItemLongClickListener(new android.widget.AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int i, long l) {


                if(intSelected.contains(i)){

                    intSelected.remove(intSelected.indexOf(i));
                    strSelected.remove(strSelected.indexOf(dirPath+"/"+theNamesOfFiles.get(i)));

                    lst_Folder.getChildAt(i).setBackgroundColor(Color.WHITE);

                }else {

                    strSelected.add(dirPath+"/"+theNamesOfFiles.get(i));
                    intSelected.add(i);

                    lst_Folder.getChildAt(i).setBackgroundColor(Color.BLUE);
                }

                return true;
            }
        });

    }

    public void ClearSelected(){
        strSelected.clear();
        intSelected.clear();
    }

    public void onParentDir_Click(View view){
        if (dirPath!="" && dirPath!="/"){
            String[] folders = dirPath.split("\\/");
            String[] folders2={};
            folders2 = Arrays.copyOf(folders, folders.length-1);
            dirPath = TextUtils.join("/", folders2);
        }

        if (dirPath==""){
            dirPath="/";
        }
        RefreshListView();
        RefreshAdapter();
        setPath();
    }

    public void setPath(){
        txtPath.setText(dirPath);
    }

    public void RefreshListView() {
    try{

        dir = new File(dirPath);
        File[] filelist = dir.listFiles();
 

        theNamesOfFiles.clear();
        intImages.clear();

        for (int i = 0; i < filelist.length; i++) {

            theNamesOfFiles.add(filelist[i].getName());

            if(filelist[i].isDirectory()==true){
                intImages.add(R.drawable.plus_circles);
            }else if(filelist[i].isFile()==true){
                intImages.add(R.drawable.plus_circles);
            }else{
                intImages.add(R.drawable.plus_circles);
            }
        }
    }catch (Exception e){

    }

    }

    public void set_Adapter() {
        customList = new CustomList();
        lst_Folder.setAdapter(customList);
    }

    public void RefreshAdapter(){
        customList.notifyDataSetChanged();
    }


    @Override

    public void onItemSelected(AdapterView<?> arg0, View arg1, int position, long id) {


        if (spnAction[position]=="Cut"){

            Cut();
        }
        if (spnAction[position]=="Copy"){

            Copy();
        }
        if (spnAction[position]=="Paste"){

            Paste();
        }
        if (spnAction[position]=="New Folder"){

            NewFolder();
        }
        if (spnAction[position]=="Delete"){

            Delete();
        }
        if (spnAction[position]=="Rename"){

            Rename();

        }
        if (spnAction[position]=="  • lightyellow"){

            view=this.getWindow().getDecorView();
            view.setBackgroundResource(R.color.lightyellow);
        }
        if (spnAction[position]=="  • lightcyan"){

            view=this.getWindow().getDecorView();
            view.setBackgroundResource(R.color.lightcyan);
        }
        if (spnAction[position]=="  • mediumseagreen"){

            view=this.getWindow().getDecorView();
            view.setBackgroundResource(R.color.mediumseagreen);
        }
        if (spnAction[position]=="  • palegoldenrod"){

            view=this.getWindow().getDecorView();
            view.setBackgroundResource(R.color.palegoldenrod);
        }
        if (spnAction[position]=="  • white"){

            view=this.getWindow().getDecorView();
            view.setBackgroundResource(R.color.WHITE);
        }


    }

    @Override
    public void onNothingSelected(AdapterView<?> arg0) {


    }

    public void Cut(){
        intCutORCopy=1;
        spin.setSelection(0);
    }

    public void Copy(){
        intCutORCopy=2;
        spin.setSelection(0);
    }

    public void Paste(){
        if (intSelected.size()>0){

            if (intCutORCopy==1){
                List<String> command = new ArrayList<String>();
                try {
                    for (int i=0; i<intSelected.size(); i++){
                    command.clear();
                    command.add("/system/bin/mv");
                    command.add(strSelected.get(i));
                    command.add(dirPath);


                        ProcessBuilder pb = new ProcessBuilder(command);
                        Process process = pb.start();
                        process.waitFor();
                        RefreshListView();
                        RefreshAdapter();
                        setPath();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
                ResetValues();
            }

            if(intCutORCopy==2){
                List<String> command = new ArrayList<String>();
                try {
                    for (int i=0; i<intSelected.size(); i++){
                        command.clear();
                        command.add("/system/bin/cp");
                        command.add("-rf");
                        command.add(strSelected.get(i));
                        command.add(dirPath);


                        ProcessBuilder pb = new ProcessBuilder(command);
                        Process process = pb.start();
                        process.waitFor();

                        RefreshListView();
                        RefreshAdapter();
                        setPath();
                    }
                } catch (Exception e) {
                    Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
                }
                ResetValues();
            }
        }

    }

    public void NewFolder(){
        try{

            AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
            LayoutInflater inflater = this.getLayoutInflater();
            final View dialogView = inflater.inflate(R.layout.newfolder, null);
            dialogBuilder.setView(dialogView);

            final EditText txtNewFolder = (EditText) dialogView.findViewById(R.id.newfolder);

            dialogBuilder.setTitle("New Folder");
            dialogBuilder.setMessage("Enter name of new folder");
            dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String sNewFolderName = txtNewFolder.getText().toString();
                    File fNewFolder = new File(dirPath+"/"+sNewFolderName);

                    Boolean bIsNewFolderCreated = fNewFolder.mkdir();

                    RefreshListView();
                    RefreshAdapter();
                    setPath();
                }
            });
            dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {

                }
            });
            AlertDialog b = dialogBuilder.create();
            b.show();


        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        spin.setSelection(0);
    }

    public void Delete(){
        List<String> command = new ArrayList<String>();
        try {
            for (int i=0; i<intSelected.size(); i++){
                command.clear();
                command.add("/system/bin/rm");
                command.add("-rf");
                command.add(strSelected.get(i).toString());


                ProcessBuilder pb = new ProcessBuilder(command);
                Process process = pb.start();
                process.waitFor();

                RefreshListView();
                RefreshAdapter();
                setPath();
            }
        } catch (Exception e) {
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        ResetValues();
    }

    public void Rename(){
        try{
            if(strSelected.size()==1){

                AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
                LayoutInflater inflater = this.getLayoutInflater();
                final View dialogView = inflater.inflate(R.layout.newfolder, null);
                dialogBuilder.setView(dialogView);

                final EditText txtNewFolder = (EditText) dialogView.findViewById(R.id.newfolder);

                dialogBuilder.setTitle("Rename Folder");
                dialogBuilder.setMessage("Enter name of new folder");
                dialogBuilder.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        File f = new File(strSelected.get(0).toString());
                        File fRename = new File(dirPath+"/"+txtNewFolder.getText().toString());
                        f.renameTo(fRename);

                        RefreshListView();
                        RefreshAdapter();
                        setPath();

                        ResetValues();
                    }
                });
                dialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {

                    }
                });
                AlertDialog b = dialogBuilder.create();
                b.show();

            }
        }catch (Exception e){
            Toast.makeText(getApplicationContext(), e.toString(), Toast.LENGTH_LONG).show();
        }
        spin.setSelection(0);
    }


    public void ResetValues(){

        spin.setSelection(0);
        intCutORCopy=0;
        intSelected.clear();
        strSelected.clear();
    }

    public void onMainStorage_Click(View view){
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {

            dirPath = String.valueOf(android.os.Environment.getRootDirectory());

            RefreshListView();
            RefreshAdapter();
            setPath();
        }
    }

    public  void onSDCARD_Click(View view){
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED))
        {

            dirPath = String.valueOf(android.os.Environment.getExternalStorageDirectory());

            RefreshListView();
            RefreshAdapter();
            setPath();
        }
    }

    public void onDownloads_Click(View view){
        dirPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS));

        RefreshListView();
        RefreshAdapter();
        setPath();
    }

    public void onImages_Click(View view){
        dirPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));

        RefreshListView();
        RefreshAdapter();
        setPath();
    }

    public void onAudio_Click(View view){
        dirPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC));

        RefreshListView();
        RefreshAdapter();
        setPath();
    }

    public void onVideo_Click(View view){
        dirPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES));

        RefreshListView();
        RefreshAdapter();
        setPath();
    }

    public void onDCIM_Click(View view){
        dirPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM));

        RefreshListView();
        RefreshAdapter();
        setPath();
    }

    public void onDocuments_Click(View view){
        dirPath = String.valueOf(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS));

        RefreshListView();
        RefreshAdapter();
        setPath();
    }

    public void onApps_Click(View view){
        dirPath = "/data/app/";

        RefreshListView();
        RefreshAdapter();
        setPath();
    }

    public class CustomList extends BaseAdapter {

        @Override
        public int getCount() {
            return intImages.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return i;
        };

        public boolean onCreateOptionsMenu(Menu menu)
        {
            MenuInflater inflater=getMenuInflater();
            inflater.inflate(R.menu.menu,menu);
            return true;
        }
             
        public View getView(int i, View view, ViewGroup viewGroup) {

            View view1 = getLayoutInflater().inflate(R.layout.custom_list, null);

            ImageView imageView = (ImageView) view1.findViewById(R.id.ItemIcon);
            TextView txtPath = (TextView) view1.findViewById(R.id.ItemName);

            imageView.setImageResource(intImages.get(i));
            txtPath.setText(theNamesOfFiles.get(i));

            return view1;


        }

    }
}
















































