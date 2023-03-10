package org.techtown.lotto;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

public class FileDownloader {
    public FileDownloader(){
        String url1 = "https://github.com/Roista57/lotto/raw/master/data/lotto.csv";
        String fileName1 = "lotto.csv";
        String url2 = "https://github.com/Roista57/lotto/raw/master/data/model.tflite";
        String fileName2 = "model.tflite";
        System.out.println("테스트 1");
        new DownloadTask().execute(url1, fileName1, url2, fileName2);
    }

    private class DownloadTask extends AsyncTask<String, Void, Void> {
        @Override
        protected Void doInBackground(String... urls) {
            System.out.println("테스트 2");
            try {
                downloadFile(urls[0], urls[1]);
                downloadFile(urls[2], urls[3]);
                System.out.println("테스트 3");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        private void downloadFile(String url, String fileName) throws IOException {
            URL fileUrl = new URL(url);
            System.out.println("테스트 ## : ");
            InputStream inputStream = fileUrl.openStream();

            File outputFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName);
            FileOutputStream outputStream = new FileOutputStream(outputFile);

            byte[] buffer = new byte[2048];
            int length;

            while ((length = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, length);
            }
            System.out.println(fileName+" 다운로드 완료하였습니다.");

            inputStream.close();
            outputStream.flush();
            outputStream.close();

            // Set file permissions to make it readable and writable by the owner only
            outputFile.setReadable(true, false);
            outputFile.setWritable(true, false);
        }
    }
}
