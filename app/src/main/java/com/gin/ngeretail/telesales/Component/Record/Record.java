package com.gin.ngeretail.telesales.Component.Record;

import android.media.MediaRecorder;
import android.os.Environment;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class Record {
    private File f = null;
    private static String mFileName = null;
    private static String extStorageDirectory = null;
    private MediaRecorder recorder;
    public boolean isRecorder = false;

    public Record() {
    }

    public void createFolderToRecorder(){
        mFileName = Environment.getExternalStorageDirectory()+"/"+"RecordTelesalesVoice";
        f = new File(mFileName);
        if(!f.exists()){
            f.mkdir();
        }
    }

    public void startRecoder(String fileName)  {
        createFolderToRecorder();
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AAC_ADTS);
        extStorageDirectory = f.toString();
        extStorageDirectory += "/Voice_"+fileName+".mp3";
        recorder.setOutputFile(extStorageDirectory);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
        try{
            recorder.prepare();
            recorder.start();

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        try{
            recorder.stop();
            recorder.release();
            recorder = null;
        }catch (IllegalStateException e){
            e.printStackTrace();
        }
    }

    public void deleteFileRecords(File fileOrDirectory){
        if(fileOrDirectory.isDirectory()){
            for(File child : fileOrDirectory.listFiles()){
                deleteFileRecords(child);
            }
        }
        fileOrDirectory.delete();
    }

    public byte[] fileToBytes(String fileName){
        File F = new File(Environment.getExternalStorageDirectory() + "/" + "RecordTelesalesVoice"+"/Voice_"+fileName+".mp3");
        extStorageDirectory = f.toString();
        extStorageDirectory += "/Voice_"+fileName+".mp3";
        byte[] bytes = new byte[0];
        try(FileInputStream inputStream = new FileInputStream(F)) {
            bytes = new byte[inputStream.available()];
            //noinspection ResultOfMethodCallIgnored
            inputStream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public String FreeExtMemory()
    {
        long availableSpace = -1L;
        try {StatFs stat = new StatFs(Environment.getDataDirectory()
                .getPath());
            stat.restat(Environment.getDataDirectory().getPath());
            availableSpace = (long) stat.getAvailableBlocks() * (long) stat.getBlockSize() / 1000000;
        } catch (Exception e) {
            e.printStackTrace();
        }
        String strI = Long.toString(availableSpace);
        return strI;
    }

}
