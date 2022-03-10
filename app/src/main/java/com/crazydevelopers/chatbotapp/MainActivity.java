package com.crazydevelopers.chatbotapp;

import android.app.Application;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.*;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import com.crazydevelopers.chatbotapp.Models.ChatModel;
import com.crazydevelopers.chatbotapp.Adapter.CustomAdapter;

import org.alicebot.ab.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
public class MainActivity extends AppCompatActivity {

    ListView listView;
    EditText editText;
    //    ImageView aphro;
    List <ChatModel> list_chat=new ArrayList<>();
    private TextToSpeech mTTS;
    FloatingActionButton btn_send_msg,talk;
    String myspeech="";
    public Bot bot;
    public static Chat chat;
    StringBuffer presentChats=new StringBuffer("");
    String Filename="AphroChatHistory.txt";
    Boolean ispresent=false,hasdel=false;
    private static final int REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.drawable.wallpaper);
        listView=(ListView)findViewById(R.id.list_of_message);
        editText=(EditText)findViewById(R.id.user_message);
        btn_send_msg=(FloatingActionButton)findViewById(R.id.fab);
        talk=(FloatingActionButton)findViewById(R.id.talk);
        /////////////////////////////////////////////////////////////////////////
//        if (ActivityCompat.checkSelfPermission(MainActivity.this,
//                Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED | ActivityCompat.checkSelfPermission(MainActivity.this,
//                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS, Manifest.permission.CALL_PHONE}, 0x12345);
//        }
//
//        if(Build.VERSION.SDK_INT>=23){
//            requestPermissions(new String[]
//                            {Manifest.permission.CAMERA,
//                                    Manifest.permission.WRITE_EXTERNAL_STORAGE},
//                    2);
//        }
        AssetManager assets = getResources().getAssets();
        File jayDir = new File(getCacheDir().toString()  + "/crazydevelopers/bots/aphro");
        Log.e("File jayDir: ",jayDir.getPath());
        boolean b = jayDir.mkdirs();
        Log.e("Exists: ",String.valueOf(jayDir.exists()));
        if (jayDir.exists()) {
            //Reading the file
            try {
                for (String dir : assets.list("aphro")) {
                    File subdir = new File(jayDir.getPath() + "/" + dir);
                    boolean subdir_check = subdir.mkdirs();
                    for (String file : assets.list("aphro/" + dir)) {
                        File f = new File(jayDir.getPath() + "/" + dir + "/" + file);
                        if (f.exists()) {
                            continue;
                        }
                        InputStream in = null;
                        OutputStream out = null;
                        in = assets.open("aphro/" + dir + "/" + file);
                        out = new FileOutputStream(jayDir.getPath() + "/" + dir + "/" + file);
                        Log.e("Path of File: ",jayDir.getPath() + "/" + dir + "/" + file);
                        //copy file from assets to the mobile's SD card or any secondary memory
                        copyFile(in, out);
                        in.close();
                        out.flush();
                        out.close();

                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        //get the working directory
        MagicStrings.root_path = getCacheDir().toString() + "/crazydevelopers";
        Log.e("rootpath: ",MagicStrings.root_path);
        System.out.println("Working Directory = " + MagicStrings.root_path);
        AIMLProcessor.extension =  new PCAIMLProcessorExtension();
        //Assign the AIML files to bot for processing
        bot = new Bot("aphro", MagicStrings.root_path, "chat");
        chat = new Chat(bot);
        /////////////////////////////////////////////////////////////////
        final String wel="Welcome !! Do you want to chat with me ?";
        mTTS=new TextToSpeech(MainActivity.this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status==TextToSpeech.SUCCESS){
                    int reult=mTTS.setLanguage(Locale.US);
                    if(reult==TextToSpeech.LANG_MISSING_DATA ||
                            reult==TextToSpeech.LANG_NOT_SUPPORTED)
                        Log.e("TTS","Language not supported");
                    else{
                        Log.e("TTS","Language is supported");
                        read();

                        // If file is now present, then do the following.
                        if(!ispresent) {
                            ChatModel chatModel = new ChatModel(wel, false);
                            presentChats.append(wel+",\n;");
                            list_chat.add(chatModel);
                            CustomAdapter adapter_orig = new CustomAdapter(list_chat, getApplicationContext());
                            listView.setAdapter(adapter_orig);
                            speak(wel,1.22f,1.0f);
                        }
                    }
                }else{
                    Log.e("TTS","Initialisation failed");
                }
            }
        });
//        aphro=(ImageView)findViewById(R.id.simsimi_img);
//        Toast.makeText(MainActivity.this,"aphro= "+aphro,Toast.LENGTH_SHORT).show();
        setOnClickButtonListener();
        // Read chat dump file here to set the previous chats.
//        aphro.setOnClickListener(this);
    }

    //copying the file
    private void copyFile(InputStream in, OutputStream out){
        byte[] buffer = new byte[1024];
        int read;
        try {
            while ((read = in.read(buffer)) != -1) {
                out.write(buffer, 0, read);
            }
        }catch (Exception e){
            e.printStackTrace();
        }
    }

//    public static class MyCustomApplication extends Application {
//        @Override
//        public void onCreate() {
//            super.onCreate();
//            Smartlook.setupAndStartRecording(7140ac6c1b1b878874619ee6240954ec15f850d7);
//        }
//    }

    public static String mainFunction (String args) {
        MagicBooleans.trace_mode = false;
        System.out.println("trace mode = " + MagicBooleans.trace_mode);
        Graphmaster.enableShortCuts = true;
        String request = args;
        String response = chat.multisentenceRespond(request);

        return response;
    }

    public void setOnClickButtonListener() {
        btn_send_msg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                myspeech=editText.getText().toString();
                task();
            }
        });
        talk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listen();
            }
        });
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                alertDialogBuilder.setMessage("What do you want to do ?");
                alertDialogBuilder.setPositiveButton("Delete Chat",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {
                                ChatModel cm=list_chat.get(position);
                                cm.message="THE MESSAGE WAS DELETED";
                                list_chat.set(position,cm);
                                CustomAdapter adapter_del = new CustomAdapter(list_chat, getApplicationContext());
                                listView.setAdapter(adapter_del);
                                Toast.makeText(MainActivity.this,"Deleting chat \""+cm.message+"\""
                                        ,Toast.LENGTH_LONG).show();
                                hasdel=true;
                            }
                        });
                alertDialogBuilder.setNeutralButton("View Profile",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                showProfile(position);
                            }
                        });
                alertDialogBuilder.setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(MainActivity.this,"No option chosen.",Toast.LENGTH_LONG).show();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();
                return true;
            }
        });
    }
    public void task() {

        ChatModel model=new ChatModel(myspeech,true);
        presentChats.append(myspeech+",\n;");
        list_chat.add(model);
//        new SimsimiAPI().execute(list_chat);

        String reply=getResponse();
        reply=reply.equals("")?"Sorry! I could not get you.":reply;
        ChatModel chatModel=new ChatModel(reply,false);
        list_chat.add(chatModel);
        presentChats.append(reply+",\n;");
        speak(reply,1.22f,1.0f);
        CustomAdapter adapter=new CustomAdapter(list_chat,getApplicationContext());
        listView.setAdapter(adapter);

        editText.setText("");
    }
    public void speak(String content,float pitch, float speed) {
        mTTS.setPitch(pitch);
        mTTS.setSpeechRate(speed);
//        Toast.makeText(MainActivity.this,"pitch= "+pitch+" speed= "+speed,Toast.LENGTH_LONG).show();
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP)
            mTTS.speak(content,TextToSpeech.QUEUE_FLUSH,null,null);
        else
            mTTS.speak(content,TextToSpeech.QUEUE_FLUSH,null);
//        Toast.makeText(MainActivity.this,content,Toast.LENGTH_LONG).show();
    }
    private void listen() {
        // Many options are available here find it out.
        Intent intent=new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        // Set Language here.
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT,"Hi Speak Something");
        try{

            startActivityForResult(intent,REQUEST_CODE);
        }catch (Exception e)
        {
            Toast.makeText(MainActivity.this,"Error: "+e.getMessage(),Toast.LENGTH_SHORT).show();
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case  REQUEST_CODE:{
                if(resultCode==RESULT_OK && data!=null){
                    ArrayList<String> result=data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    myspeech=result.get(0);
                    task();
                    // Add the functions that is needed to be performed.
                }
                break;
            }
        }
    }
    @Override
    protected void onPause() {
        write();
        super.onPause();
    }
//    @Override
//    public void onBackPressed() {
//        write();
//        super.onBackPressed();
//    }
    @Override
    protected void onDestroy() {
        write();
        if(mTTS!=null) {
            mTTS.stop();
            mTTS.shutdown();
        }
        finish();
        System.exit(0);
        super.onDestroy();
    }
    public void read() {
        StringBuilder sb=new StringBuilder("");
        try {
            FileInputStream fin = openFileInput(Filename);
            int c;
            while( (c = fin.read()) != -1)
                sb.append((char)c);
            if(sb.length()>0)
                sb.delete((sb.length()) - 3,sb.length());
            String []strarr=sb.toString().split(",\n;");
            int token=1;
            Boolean isSendflag;
            ChatModel chathist;
            CustomAdapter adapter_hist;
            for(String str: strarr){
                isSendflag=(token==0);
                chathist=new ChatModel(str, isSendflag);
                list_chat.add(chathist);
                token^=1;
            }
            adapter_hist = new CustomAdapter(list_chat, getApplicationContext());
            listView.setAdapter(adapter_hist);
            ispresent=true;

            int iter=(strarr.length)-1;
            while(iter>=0 && strarr[iter].equals("THE MESSAGE WAS DELETED"))
                iter--;
            if(iter>=0)
                speak("The last conversation was "+ strarr[iter],1.22f,1.0f);
            else
                speak("You deleted all our previous conversations.",1.22f,1.0f);

        } catch (FileNotFoundException e) {
            Log.e("NOTE: ","FILE IS NOT FOUND");
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
        }
    }
    public void write(){
        try {
//            if(presentChats.toString().length()>0)
//                presentChats.deleteCharAt((presentChats.toString().length())-1);
            if(!ispresent&&list_chat.size()>0) {
                FileOutputStream writer = openFileOutput(Filename, MODE_PRIVATE);
                StringBuilder delbr=new StringBuilder();
                for(int i=0;i<list_chat.size();i++)
                    delbr.append(list_chat.get(i).message+",\n;");
                writer.write(delbr.toString().getBytes());
                writer.flush();
                writer.close();
            }
            else {
                if(!hasdel) {
                    FileOutputStream writer = openFileOutput(Filename, MODE_APPEND);
                    writer.write(presentChats.toString().getBytes());
                    writer.flush();
                    writer.close();
                }
                else{
                    FileOutputStream writer = openFileOutput(Filename, MODE_PRIVATE);
                    StringBuilder delbr=new StringBuilder();
                    for(int i=0;i<list_chat.size();i++)
                        delbr.append(list_chat.get(i).message+",\n;");
                    writer.write(delbr.toString().getBytes());
                    writer.flush();
                    writer.close();
                    hasdel=false;
                }
            }
            Log.e("NOTE: ","FILE WRITING IS DONE");
            if(presentChats.length()>0)
                presentChats.delete(0,presentChats.length());
        } catch (IOException e) {
            Log.e("ERROR: ", e.toString());
            e.printStackTrace();
        }
    }
    public String openApp(String appname){
        Intent intent=getPackageManager().getLaunchIntentForPackage("com."+appname);
        Log.e("NOTE: ",appname);
        if(intent!=null) {
            startActivity(intent);
            return "";
        }
        return ("Sorry the app cannot be opened. Maybe it doesn't exists.");
    }
    public  String getResponse() // working only for whatsapp check this
    {
        String speech=myspeech.toLowerCase();
        String reply="";
        if(speech.contains("open")){
            if(speech.contains("facebook")) {
                reply = "Opening facebook."+
                openApp("facebook.katana");
            }
            else if(speech.contains("whatsapp")) {
                reply = "Opening whatsapp. "+
                openApp("whatsapp");
            }
            else if(speech.contains("youtube")) {
                reply = "Opening youtube. "+
                openApp("google.android.youtube");
            }
            else if(speech.contains("instagram")) {
                reply = "Opening instagram. "+
                openApp("instagram.android");
            }
            else if(speech.contains("messenger")) {
                reply = "Opening messenger. "+
                openApp("facebook.orca");
            }
            else if(speech.contains("calendar")) {
                reply = "Opening calendar. "+
                openApp("android.calendar");
            }
            else if(speech.contains("camera")) {
                reply = "Opening camera. "+
                openApp("android.camera");
            }
            else if(speech.contains("chrome")) {
                reply = "Opening google chrome. "+
                openApp("android.chrome");
            }
            else if(speech.contains("xender")) {
                reply = "Opening xender. "+
                openApp_gen("cn.xender");
            }
            else if(speech.contains("insti")) {
                reply = "Opening insti. "+
                        openApp_gen("app.insti");
            }
            else if(speech.contains("office")) {
                reply = "Opening W P S office. "+
                        openApp_gen("cn.wps.moffice_eng");
            }
            else if(speech.contains("phonegap")) {
                reply = "Opening adobe phonegap. "+
                        openApp("adobe.phonegap.app");
            }
            else if(speech.contains("reader")) {
                reply = "Opening adobe reader. "+
                        openApp("adobe.reader");
            }
            else if(speech.contains("browser")) {
                reply = "Opening android browser. "+
                        openApp("android.browser");
            }
            else if(speech.contains("contacts")) {
                reply = "Opening contacts. "+
                        openApp("android.contacts");
            }
            else if(speech.contains("deskclock")) {
                reply = "Opening android deskclock. "+
                        openApp("android.deskclock");
            }
            else if(speech.contains("android email")) {
                reply = "Opening android email. "+
                        openApp("android.email");
            }
            else if(speech.contains("file explorer")) {
                reply = "Opening file explorer. "+
                        openApp("android.fileexplorer");
            }
            else if(speech.contains("settings")) {
                reply = "Opening settings. "+
                        openApp("android.settings");
            }
            else if(speech.contains("playstore")||speech.contains("play store")) {
                reply = "Opening google playstore. "+
                        openApp("android.vending");
            }
            else if(speech.contains("download manager")) {
                reply = "Opening download manager. "+
                        openApp("app.downloadmanager");
            }
            else if(speech.contains("book my show")) {
                reply = "Opening book my show. "+
                        openApp("bt.bms");
            }
            else if(speech.contains("gaana")) {
                reply = "Opening gaana. "+
                        openApp("gaana");
            }
            else if(speech.contains("drive")) {
                reply = "Opening google drive. "+
                        openApp("google.android.apps.docs");
            }
            else if(speech.contains("sheets")) {
                reply = "Opening google sheets. "+
                        openApp("google.android.apps.docs.editors.sheets");
            }
            else if(speech.contains("maps")) {
                reply = "Opening google maps. "+
                        openApp("google.android.apps.maps");
            }
            else if(speech.contains("google photos")) {
                reply = "Opening google photos. "+
                        openApp("google.android.apps.photos");
            }
            else if(speech.contains("gmail")) {
                reply = "Opening google mail. "+
                        openApp("google.android.gm");
            }
            else if(speech.contains("google")) {
                reply = "Opening google quick ssearch box. "+
                        openApp("google.android.googlequicksearchbox");
            }
            else if(speech.contains("google music")) {
                reply = "Opening google music. "+
                        openApp("google.android.music");
            }
            else if(speech.contains("google videos")) {
                reply = "Opening google videos. "+
                        openApp("google.android.videos");
            }
            else if(speech.contains("Jio tv")) {
                reply = "Opening JIO TV. "+
                        openApp("jio.jioplay.tv");
            }
            else if(speech.contains("My JIO")) {
                reply = "Opening My Jio app. "+
                        openApp("jio.myjio");
            }
            else if(speech.contains("share it")) {
                reply = "Opening shareit. "+
                        openApp("lenovo.anyshare.gps");
            }
            else if(speech.contains("linked in")) {
                reply = "Opening linked in. "+
                        openApp("linkedin.android");
            }
            else if(speech.contains("make my trip")) {
                reply = "Opening make my trip. "+
                        openApp("makemytrip");
            }
            else if(speech.contains("global file explorar")) {
                reply = "Opening global file explorar. "+
                        openApp("mi.android.globalFileexplorar");
            }
            else if(speech.contains("global personal assistant")) {
                reply = "Opening global personal assistant. "+
                        openApp("mi.android.globalpersonalassistant");
            }
            else if(speech.contains("calculator")) {
                reply = "Opening calculator. "+
                        openApp("miui.calculator");
            }
            else if(speech.contains("m i u i video player")) {
                reply = "Opening M I U I video player. "+
                        openApp("miui.videoplayer");
            }
            else if(speech.contains("video player")) {
                reply = "Opening video player. "+
                        openApp("mxtech.videoplayer.ad");
            }
            else if(speech.contains("ola")) {
                reply = "Opening ola cabs. "+
                        openApp("olacabs.customer");
            }
            else if(speech.contains("saavn")) {
                reply = "Opening saavn. "+
                        openApp("saavn.android");
            }
            else if(speech.contains("yono")) {
                reply = "Opening s b i yono. "+
                        openApp("sbi.lotusintouch");
            }
            else if(speech.contains("truecaller")) {
                reply = "Opening truecaller. "+
                        openApp("truecaller");
            }
            else if(speech.contains("uber")) {
                reply = "Opening ubercab. "+
                        openApp("ubercab");
            }
            else if(speech.contains("u c browser")) {
                reply = "Opening u c browser. "+
                        openApp("UCMobile.intl");
            }
            else if(speech.contains("where is my train")) {
                reply = "Opening where is my train. "+
                        openApp("whereismytrain.android");
            }
            else if(speech.contains("hotstar")) {
                reply = "Opening hotstar. "+
                        openApp_gen("in.startv.hotstar");
            }
            else if(speech.contains("paytm")) {
                reply = "Opening paytm. "+
                        openApp_gen("net.one97.paytm");
            }
            if(speech.contains("please")){
                reply="Yup sure. Why not \n"+reply;
            }
        }
        else
            reply=mainFunction(myspeech);
        return reply;
    }
    public String openApp_gen(String appname){
        Intent intent=getPackageManager().getLaunchIntentForPackage(appname);
        Log.e("NOTE: ",appname);
        if(intent!=null) {
            startActivity(intent);
            return "";
        }
        return ("Sorry the app cannot be opened. Maybe it doesn't exists.");
    }
    public void showProfile(int position){
        Intent intent;
        if(position%2 ==0) {
            intent = new Intent(MainActivity.this,LuffyProfile.class);
           speak("Awwww! You are stalking me", 1.5f, 0.75f);
        }
        else {
            intent = new Intent(MainActivity.this, ForProfile.class);
            speak("You can see who is the developer.",1.5f,1.0f);
        }
        startActivity(intent);
    }
//    public static Intent openFacebook(Context context)
}