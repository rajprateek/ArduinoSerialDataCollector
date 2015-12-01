package rajprateek.helloworld;

import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Calendar;

public class RecordActivity extends AppCompatActivity {
    public static boolean flag = false;
    public MediaRecorder recorder = new MediaRecorder();
    String mainPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/ExperimentData/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Intent intent = getIntent();
        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText("hello recording");
        setContentView(R.layout.activity_record);
    }
    public void helpButton(View view){

        Toast toast = Toast.makeText(getApplicationContext(), "Developed by Raj (rajprateek@gatech.edu) & Ishaan", Toast.LENGTH_LONG);
        toast.show();

    }
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    public void stopRecording(View view)
    {
        if(flag) {
            flag = false;
            recorder.stop();

            Toast toast = Toast.makeText(getApplicationContext(), "Success! Files have been saved to \n" + mainPath, Toast.LENGTH_LONG);
            toast.show();
        }
        else{
            Toast toast = Toast.makeText(getApplicationContext(), "Nothing to stop \nRecording was not in progress", Toast.LENGTH_LONG);
            toast.show();
        }
    }
    public void startRecordingProx(View view) {
        flag = true;
        Toast toast = Toast.makeText(getApplicationContext(), "Data is being recorded", Toast.LENGTH_LONG);
        toast.show();
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                startCapRecording();
                //
            }
        });
        //thread.start();

        Thread thread2 = new Thread(new Runnable() {
            @Override
            public void run() {

                startMicRecording();
                //
            }
        });
        thread2.start();



    }
    private void startMicRecording() {
        String status = Environment.getExternalStorageState();
        recorder = new MediaRecorder();

        try {
            File dir = new File(mainPath);
            dir.mkdirs();
            String postFix =  getPostFix();
            String path = mainPath+ "/MicRecording_" + postFix + ".3gp";
            recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
            recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            recorder.setOutputFile(path);
            recorder.prepare();
            recorder.start();
        }
        catch(Exception e){
            Log.d("myApp",e.getMessage().toString());
        }

    }
    private String getPostFix(){
        String postFix =  (Calendar.getInstance().get(Calendar.MONTH)+1)+"."+ Calendar.getInstance().get(Calendar.DAY_OF_MONTH)+"." +Calendar.getInstance().get(Calendar.HOUR_OF_DAY) +"_"+ System.currentTimeMillis();
        return postFix;
    }

    private void startCapRecording(){
        flag = true;
        try {
            Process p = Runtime.getRuntime().exec(new String[]{"su", "-c", "system/bin/sh"});
            DataOutputStream stdin = new DataOutputStream(p.getOutputStream());
//from here all commands are executed with su permissions
            stdin.writeBytes("cat ~/dev/usb/tty1-1:1.0\n"); // \n executes the command
            InputStream stdout = p.getInputStream();
            byte[] buffer = new byte[4096];
            int read;
            String out = new String();
            if (isExternalStorageWritable()) {
                File sdCard = Environment.getExternalStorageDirectory();
//                File dir = new File(sdCard.getAbsolutePath());
                File dir = new File(mainPath);
                dir.mkdirs();
                String postFix =  getPostFix();
                File file = new File(mainPath, "SensorDataRecording_"+postFix +".csv");
                FileOutputStream f = new FileOutputStream(file , true);
                while (flag) {
                    read = stdout.read(buffer);
                    Log.d("myApp", System.currentTimeMillis() + ", " + new String(buffer, 0, read));
                    out += new String(buffer, 0, read);
                    String string = System.currentTimeMillis() + ", " + new String(buffer, 0, read);
                    f.write(string.getBytes());
                    Log.d("myApp", ""+flag);
                }
                f.close();
            }
        }catch(Exception e){
            Log.d("myApp", "ENDED " + e.getMessage());
        }
    }

}
