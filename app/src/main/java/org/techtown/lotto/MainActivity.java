package org.techtown.lotto;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import org.tensorflow.lite.Interpreter;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ArrayList<String[]> rows = new ArrayList<String[]>();
    private static final int REQUEST_EXTERNAL_STORAGE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        verifyStoragePermissions(this);
        List<int[]> numbers = new GenerateNumbers().listNumbers;
        for(int i=0;i<numbers.size();i++){
            Log.d("onCreate", Arrays.toString(numbers.get(i)));
        }
    }

    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(activity, android.Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    class GenerateNumbers{
        private List<int[]> listNumbers;
        public GenerateNumbers() {
            try {
                Interpreter.Options options = new Interpreter.Options();
                Interpreter interpreter = new Interpreter(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "model.tflite"), options);

                InputStream inputStream = new FileInputStream(new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "lotto.txt"));
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                String line = "";
                while ((line = reader.readLine()) != null){
                    String[] list = line.split(",");
                    rows.add(list);
                }
                reader.close();
                inputStream.close();

                // 배열 슬라이싱을 통해 숫자만 추출
                int[][] numbers1 = new int[rows.size()][6];
                for (int i = 0; i < rows.size(); i++) {
                    for (int j = 1; j < 7; j++) {
                        numbers1[i][j-1] = Integer.parseInt(rows.get(i)[j]);
                    }
                }

                // numbers2ohbin 함수를 이용해 ohbins 리스트 생성
                ArrayList<int[]> ohbins = new ArrayList<int[]>();
                for (int i = 0; i < numbers1.length; i++) {
                    ohbins.add(numbers2ohbin(numbers1[i]));
                }

                int[][] lastSample = {ohbins.get(ohbins.size()-1)};
                float[][][] xs = new float[1][1][45];
                for(int i=0; i<45; i++){
                    xs[0][0][i] = lastSample[0][i];
                }

                float[][] ysPred = new float[1][45];
                interpreter.run(xs, ysPred);

                List<Float> ysPredList = new ArrayList<>();
                for(int i=0; i<45; i++){
                    ysPredList.add(ysPred[0][i]);
                }
                double[] ysPredArray = new double[0];
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    ysPredArray = ysPredList.stream().mapToDouble(i->i).toArray();
                }

                listNumbers = new ArrayList<>();
                for(int n=0; n<5; n++) {
                    float[] numbers = genNumbersFromProbability(ysPredArray);
                    int[] numbers_int = new int[numbers.length];
                    Arrays.sort(numbers);
                    for (int i = 0; i < numbers.length; i++) {
                        numbers_int[i] = (int) numbers[i];
                    }
                    System.out.format("%d : %s\n", (n + 1), Arrays.toString(numbers_int));
                    listNumbers.add(numbers_int);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        List<int[]> numbers_list(){
            return listNumbers;
        }
    }

    public float[] genNumbersFromProbability(double[] numsProb) {
        List<Integer> ballBox = new ArrayList<>();

        for (int n = 0; n < 45; ++n) {
            int ballCount = (int) (numsProb[n] * 100 + 1);
            for (int i = 0; i < ballCount; ++i) {
                ballBox.add(n + 1); // 1부터 시작
            }
        }

        List<Integer> selectedBalls = new ArrayList<>();

        while (selectedBalls.size() < 6) {
            int ballIndex = (int) (Math.random() * ballBox.size());
            int ball = ballBox.get(ballIndex);

            if (!selectedBalls.contains(ball)) {
                selectedBalls.add(ball);
            }
        }

        float[] result = new float[6];
        for (int i = 0; i < 6; ++i) {
            result[i] = selectedBalls.get(i);
        }
        return result;
    }

    // Convert winning number to one-hot encoding vector (ohbin)
    public static int[] numbers2ohbin(int[] numbers) {
        int[] ohbin = new int[45]; //creates 45 empty cells
        for (int i = 0; i < 6; i++) { //repeat for 6 winning numbers
            ohbin[numbers[i]-1] = 1; //Lotto numbers start from 1, but array index starts from 0, so subtract 1
        }
        return ohbin;
    }

    // Convert one-hot encoding vector (ohbin) to number
    public static int[] ohbin2numbers(int[] ohbin) {
        int[] numbers = new int[6];
        int index = 0;
        for (int i = 0; i < ohbin.length; i++) {
            if (ohbin[i] == 1) { // If set to 1, add that number to the return value.
                numbers[index] = i+1;
                index++;
            }
        }
        return Arrays.copyOf(numbers, index);
    }
}
