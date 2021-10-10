package com.example.keepwalking;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import static android.content.ContentValues.TAG;

import static android.speech.tts.TextToSpeech.ERROR;

public class MainActivity2 extends AppCompatActivity {

    public static Context context_main2;
    private LineChart chart;

    // Graph 그리기
    private LineGraphSeries<DataPoint> mSeriesAccelX, mSeriesAccelY, mSeriesAccelZ;
    private GraphView mGraphAccel;
    private double graphLastAccelXValue = 10d;
    private GraphView line_graph;
    TextView xValue, yValue, zValue;
    private TextView walkingTextView;

    private TextToSpeech tts;
    private Button btn;

    // 그래프 저장
    private String[] permissionList = {Manifest.permission.READ_EXTERNAL_STORAGE};
    private FirebaseStorage storage;
    private LineChart chartView;
    private Button btUpload, btDownload;

    private String result2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        walkingTextView = findViewById(R.id.tv_output);
        btn = findViewById(R.id.button3);

        context_main2 = this;
        storage = FirebaseStorage.getInstance();
        chartView = findViewById(R.id.chart);
        btUpload = findViewById(R.id.upload_btn);

        // 테스트!!!!
//        redirectSignupActivity();

        btUpload.setOnClickListener(view -> {
            upLoadFromMemory();
        });

        // 데이터 수신
        Intent intent = getIntent();
//        ArrayList<Float> data = (ArrayList<Float>) intent.getSerializableExtra("data");

        ArrayList<Float> accX = (ArrayList<Float>) intent.getSerializableExtra("accX");
        ArrayList<Float> accY = (ArrayList<Float>) intent.getSerializableExtra("accY");
        ArrayList<Float> accZ = (ArrayList<Float>) intent.getSerializableExtra("accZ");

        ArrayList<Float> gyroX = (ArrayList<Float>) intent.getSerializableExtra("gyroX");
        ArrayList<Float> gyroY = (ArrayList<Float>) intent.getSerializableExtra("gyroY");
        ArrayList<Float> gyroZ = (ArrayList<Float>) intent.getSerializableExtra("gyroZ");

        ArrayList<Float> lx = (ArrayList<Float>) intent.getSerializableExtra("lx");
        ArrayList<Float> ly = (ArrayList<Float>) intent.getSerializableExtra("ly");
        ArrayList<Float> lz = (ArrayList<Float>) intent.getSerializableExtra("lz");

        String result = intent.getStringExtra("result");
        result2 = intent.getStringExtra("result2");
        Log.e("정상/비정상 결과:", result);
        walkingTextView.setText(result);

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    tts.setLanguage(Locale.KOREA);
                }
            }
        });

        btn.setOnClickListener(view -> {
            tts.setPitch(0.5f);         // 음성 톤을 0.5배 내려준다.
            tts.setSpeechRate(1.0f);    // 읽는 속도는 기본 설정
            // editText에 있는 문장을 읽는다.
            tts.speak(result, TextToSpeech.QUEUE_FLUSH, null);
        });

        // 음성 텍스트
//        tts.speak(result,TextToSpeech.QUEUE_FLUSH, null);
//        Log.e("LOG", accX.size() + "," + accY.size() + "," + accZ.size());
        // here

        chart = findViewById(R.id.chart);
        ArrayList<Entry> entry1 = new ArrayList<>();
        ArrayList<Entry> entry2 = new ArrayList<>();

        for (int i = 0; i < accX.size(); i++) {
            float res = (float) Math.sqrt(Math.pow(accX.get(i), 2) + Math.pow(accY.get(i), 2) + Math.pow(accZ.get(i), 2));

            entry1.add(new Entry(i, res));
            entry2.add(new Entry(i, res + 10));
        }

        LineDataSet set1, set2;
        set1 = new LineDataSet(entry1, "사용자");
        set2 = new LineDataSet(entry2, "정상인");

        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1);
        dataSets.add(set2);

        LineData dat = new LineData(dataSets);

        // ******그래프 디자인*********
        // 사용자 측정 Graph
        set1.setColor(Color.rgb(153, 204, 255));
        set1.setDrawCircles(false);
//        set1.setCircleColor(Color.rgb(153, 204, 255));
//        set1.setCircleRadius(3f);
        set1.setLineWidth(2);
        set1.setDrawFilled(true); // 차트 아래 fill(채우기) 설정
        set1.setFillColor(Color.rgb(212, 248, 253));
        set1.setValueTextSize(10f);

        // 정상 Graph
        set2.setColor(Color.rgb(255, 51, 153));
        set2.setCircleColor(Color.rgb(255, 51, 153));
        set2.setCircleRadius(3f);
        set2.setLineWidth(2);
        set2.setValueTextSize(10f);

        // y축 오른쪽 Label remove
        YAxis yAxisRight = chart.getAxisRight(); //Y축의 오른쪽면 설정
        yAxisRight.setDrawLabels(false);
        yAxisRight.setDrawAxisLine(false);
        yAxisRight.setDrawGridLines(false);
        //*************************

        chart.getDescription().setEnabled(false); // 하단 regend remove
        chart.setData(dat);
//        Log.e("Log", String.valueOf(data));
    }

    // 메모리 데이터, 비트맵을 바이트코드로 compress 하여 추가하기
    //  Get the data from an ImageView as bytes
    private void upLoadFromMemory() {
        String kakaoid = ((GlobalApplication) getApplication()).getKakaoID();
//        Log.e("메인카카오: ",( (GlobalApplication) getApplication() ).getKakaoID());

        chartView.setDrawingCacheEnabled(true);

        Bitmap bitmap = chartView.getDrawingCache();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] data = baos.toByteArray();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd_HH시 mm분");
        Date time = new Date();
        String current_time = sdf.format(time);

        String[] file_name = current_time.split("_");

        StorageReference mountainsRef = storage.getReference().child(kakaoid + "/" + file_name[0] + "/" + file_name[1] + "_" + result2);
        UploadTask uploadTask = mountainsRef.putBytes(data);

        uploadTask.addOnProgressListener(taskSnapshot -> {
            double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
            Log.d(TAG, "Upload is " + progress + "% done");

        }).addOnPausedListener(taskSnapshot -> Log.d(TAG, "Upload is paused")).addOnFailureListener(exception -> {
            Toast.makeText(this.getApplicationContext(), "그래프가 정상적으로 저장되지 않았습니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "업로드 실패");

        }).addOnSuccessListener(taskSnapshot -> {
            Toast.makeText(this.getApplicationContext(), "그래프가 정상적으로 저장되었습니다.", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "업로드 성공");
        });
    }

    /*** 테스트위함 ***/
    public void redirectSignupActivity() {
        //로그인이 완료된 후 이동하는 액티비티 지정
        final Intent intent = new Intent(this, CalendarActivity.class);
        startActivity(intent);
        finish();
    }
}