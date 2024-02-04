package com.example.aplikasi2;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class MainActivity6 extends AppCompatActivity {
    private TextView tbPredTextView;
    private TextView bbPredTextView;
    private ImageView bmiStatusImageView;
    private Button backButton;
    private Button exitButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main6);

        tbPredTextView = findViewById(R.id.tbPredTextView);
        bbPredTextView = findViewById(R.id.bbPredTextView);
        bmiStatusImageView = findViewById(R.id.bmiStatusImageView);
        backButton = findViewById(R.id.backButton);
        exitButton = findViewById(R.id.exitButton);

        // Mendapatkan nilai-nilai yang dikirim melalui intent
        double tb_asli = getIntent().getDoubleExtra("tb_asli", 0.0);
        double BBpred = getIntent().getDoubleExtra("BBpred", 0.0);
        String bmiStatus = getIntent().getStringExtra("bmiStatus");

        // Menampilkan nilai-nilai dalam TextView yang sesuai
        tbPredTextView.setText("TINGGI BADAN : " + String.format("%.0f", tb_asli));
        bbPredTextView.setText("BERAT BADAN : " + String.format("%.0f", BBpred));

        // Menampilkan gambar berdasarkan bmiStatus
        if (bmiStatus.equals("KURUS")) {
            bmiStatusImageView.setImageResource(R.drawable.kurus);
        } else if (bmiStatus.equals("NORMAL")) {
            bmiStatusImageView.setImageResource(R.drawable.normal);
        } else if (bmiStatus.equals("GEMUK")) {
            bmiStatusImageView.setImageResource(R.drawable.gemuk);
        } else if (bmiStatus.equals("OBESITAS")) {
            bmiStatusImageView.setImageResource(R.drawable.obesity);
        }

        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        exitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finishAffinity();
            }
        });
    }
}
