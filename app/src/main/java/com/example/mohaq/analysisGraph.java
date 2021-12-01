package com.example.mohaq;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

public class analysisGraph extends AppCompatActivity {

    Spinner aType;
    LineChart aGraph;
    TextView tv_type;
    DatabaseReference dRef;
    XAxis xAxis;
    FirebaseAuth mAuth;
    FirebaseUser user;
    String uid, device, spinnerValue, currentDate, firstDateOfWeek, firstDateOfMonth, AnalysisType, lastDateOfMonth;
    private static final String TAG = "QueryActivity";
    LineDataSet lineDataSet = new LineDataSet(null, "Sensor reading");
    ArrayList<ILineDataSet> iLineDataSets = new ArrayList<>();
    LineData lineData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analysis_graph);

        aType = findViewById(R.id.spinner_type);
        tv_type = findViewById(R.id.tv_analysisType);
        tv_type.setText(getIntent().getStringExtra("Type"));
        aGraph = findViewById(R.id.aGraph);
        xAxis = aGraph.getXAxis();

        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        uid = user.getUid();
        searchDevice();
    }

    private String getAnalysisType() {
        String type = null;
        switch (tv_type.getText().toString()) {
            case "Temperature Reading":
                type = "Temperature";
                break;
            case "Humidity Reading":
                type = "Humidity";
                break;
            case "NH3 Reading":
                type = "nh3Value";
                break;
            case "CO Reading":
                type = "coValue";
                break;
        }
        return type;
    }

    private void searchDevice(){
        dRef = FirebaseDatabase.getInstance().getReference();
        dRef.child("User").child(uid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                device = snapshot.child("device").getValue(String.class);
                dRef = FirebaseDatabase.getInstance().getReference("Device/"+device+"/SensorReading");
                AnalysisType = getAnalysisType();
                spinnerSet();
                getSpinnerValue();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void spinnerSet() {
        String analysisType[] = getResources().getStringArray(R.array.analysis_type);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, R.layout.support_simple_spinner_dropdown_item, analysisType);
        spinnerAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        aType.setAdapter(spinnerAdapter);
    }

    private void getSpinnerValue() {
        aType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerValue = parent.getItemAtPosition(position).toString();
                graphDisplay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    private void graphDisplay() {
        switch (spinnerValue) {
            case "Day": dayGraph();
                break;
            case "Week": weekGraph();
                break;
            case "Month": monthGraph();
                break;
            case "no selection":{
                aGraph.clear();
                aGraph.invalidate();
            }
        }
    }

    private void dayGraph() {
        currentDate = getDate();
        dRef.child(currentDate).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int x = 0;
                ArrayList<Entry> dataValue = new ArrayList<>();
                ArrayList<String> date = new ArrayList<>();
                if (snapshot.hasChildren()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        Float data = ds.child(AnalysisType).getValue(Float.class);
                        date.add(ds.getKey());
                        dataValue.add(new Entry(x, data));
                        x++;
                    }
                    showChart(dataValue);
                    formatXaxis(date);
                } else {
                    aGraph.clear();
                    aGraph.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void weekGraph() {
        firstDateOfWeek = firstDayOfThisWeek();
        dRef.orderByKey().startAt(firstDateOfWeek).limitToFirst(7).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int x = 0;
                ArrayList<Entry> dataValue = new ArrayList<>();
                if (snapshot.hasChildren()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        for (DataSnapshot dsChild : ds.getChildren()) {
                            Float data = dsChild.child(AnalysisType).getValue(Float.class);
                            dataValue.add(new Entry(x, data));
                            x++;
                        }
                        showChart(dataValue);
                        XAxishide();
                    }
                } else {
                    aGraph.clear();
                    aGraph.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void monthGraph() {
        firstDateOfMonth = firstDayOfMonth();
        lastDateOfMonth = lastDayOfMonth();
        dRef.orderByKey().startAt(firstDateOfMonth).endAt(lastDateOfMonth).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                int x = 0;
                ArrayList<Entry> dataValue = new ArrayList<>();
                if (snapshot.hasChildren()) {
                    for (DataSnapshot ds : snapshot.getChildren()) {
                        for (DataSnapshot dsChild : ds.getChildren()) {
                            Float data = dsChild.child(AnalysisType).getValue(Float.class);
                            dataValue.add(new Entry(x, data));
                            x++;
                        }
                        showChart(dataValue);
                        XAxishide();
                    }
                } else {
                    aGraph.clear();
                    aGraph.invalidate();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void formatXaxis(ArrayList<String> date) {
        xAxis.setValueFormatter(new IndexAxisValueFormatter(date));
    }

    private void showChart(ArrayList<Entry> dataValue) {
        lineDataSet.setValues(dataValue);
        lineDataSet.setColor(Color.BLACK);
        lineDataSet.setCircleColor(Color.BLACK);
        lineDataSet.setValueTextSize(10);
        lineDataSet.setDrawValues(false);
        iLineDataSets.clear();
        iLineDataSets.add(lineDataSet);
        lineData = new LineData(iLineDataSets);
        aGraph.clear();
        aGraph.setBackgroundColor(Color.WHITE);
        aGraph.setData(lineData);
        aGraph.invalidate();
        aGraph.getDescription().setEnabled(false);
        aGraph.getAxisRight().setEnabled(false);
        aGraph.getAxisRight().setDrawGridLines(false);
        aGraph.getXAxis().setAxisLineWidth(2F);
        aGraph.getAxisLeft().setAxisLineWidth(2F);
        aGraph.getXAxis().setGridColor(Color.BLACK);
        aGraph.getAxisLeft().setGridColor(Color.BLACK);
        aGraph.getXAxis().setDrawGridLines(false);
        aGraph.getAxisLeft().setDrawGridLines(false);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        Legend legend = aGraph.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setXEntrySpace(15f);
        legend.setTextSize(15f);
    }

    public String getDate() {
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
        Date todayDate = new Date();
        return date.format(todayDate);
    }

    public String firstDayOfThisWeek() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        calendar.setFirstDayOfWeek(Calendar.MONDAY);
        Date startOfWeek = calendar.getTime();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
        return date.format(startOfWeek);
    }

    public String firstDayOfMonth() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        Date startOfMonth = calendar.getTime();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
        return date.format(startOfMonth);
    }

    public String lastDayOfMonth(){
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH));
        Date startOfMonth = calendar.getTime();
        SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd");
        return date.format(startOfMonth);
    }

    public void XAxishide(){
        aGraph.getXAxis().setDrawLabels(false);
    }
}