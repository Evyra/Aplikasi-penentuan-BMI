package com.example.aplikasi2;

import android.os.Bundle;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.graphics.Point;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MainActivity5 extends AppCompatActivity implements View.OnClickListener  {
    private Button frontPictureButton;
    private Button sidePictureButton;
    private Button processButton;
    private Bitmap frontPictureBitmap;
    private Bitmap sidePictureBitmap;
    private static final int REQUEST_CAMERA_PERMISSION = 1001;
    private static final int REQUEST_CAMERA_FRONT = 1;
    private static final int REQUEST_CAMERA_SIDE = 2;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main5);

        frontPictureButton = findViewById(R.id.frontPictureButton);
        sidePictureButton = findViewById(R.id.sidePictureButton);
        processButton = findViewById(R.id.processButton);
        frontPictureButton.setOnClickListener(this);
        sidePictureButton.setOnClickListener(this);
        processButton.setOnClickListener(this);
    }
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.frontPictureButton:
                // Memeriksa izin untuk menggunakan kamera
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Meminta izin jika belum diberikan
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
                } else {
                    // Jika izin sudah diberikan, lanjutkan untuk membuka kamera depan
                    openCamera(REQUEST_CAMERA_FRONT);
                }
                break;
            case R.id.sidePictureButton:
                // Memeriksa izin untuk menggunakan kamera
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    // Meminta izin jika belum diberikan
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                            REQUEST_CAMERA_PERMISSION);
                } else {
                    // Jika izin sudah diberikan, lanjutkan untuk membuka kamera samping
                    openCamera(REQUEST_CAMERA_SIDE);
                }
                break;
            case R.id.processButton:
                if (frontPictureBitmap != null && sidePictureBitmap != null) {
                    // Konversi gambar ke grayscale
                    Bitmap grayscaleFrontBitmap = convertToGrayscale(frontPictureBitmap);
                    Bitmap grayscaleSideBitmap = convertToGrayscale(sidePictureBitmap);

                    // Lakukan thresholding pada kedua gambar
                    Bitmap thresholdedFrontBitmap = applyThreshold(grayscaleFrontBitmap, 90, 255);
                    Bitmap thresholdedSideBitmap = applyThreshold(grayscaleSideBitmap, 90, 255);

                    // Lakukan dilasi pada kedua gambar
                    Bitmap dilatedFrontBitmap = dilate(thresholdedFrontBitmap, 2);
                    Bitmap dilatedSideBitmap = dilate(thresholdedSideBitmap, 2);

                    // Lakukan erosi pada kedua gambar
                    Bitmap erodedFrontBitmap = erode(dilatedFrontBitmap, 2);
                    Bitmap erodedSideBitmap = erode(dilatedSideBitmap, 2);

                    // Lakukan flood fill pada kedua gambar
                    Bitmap floodFilledFrontBitmap = floodFill(erodedFrontBitmap, 0, 0, Color.RED);
                    Bitmap floodFilledSideBitmap = floodFill(erodedSideBitmap, 0, 0, Color.GREEN);

                    // Lakukan labeling pada kedua gambar
                    Bitmap labeledFrontBitmap = labelRegions(floodFilledFrontBitmap);
                    Bitmap labeledSideBitmap = labelRegions(floodFilledSideBitmap);

                    // Proses kontur pada kedua gambar
                    processContour(labeledFrontBitmap, labeledSideBitmap);

                }else {
                    Toast.makeText(this, "Ambil gambar terlebih dahulu", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
    private void openCamera(int requestCode) {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (requestCode == REQUEST_CAMERA_FRONT) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA_FRONT);
        } else if (requestCode == REQUEST_CAMERA_SIDE) {
            startActivityForResult(cameraIntent, REQUEST_CAMERA_SIDE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Izin kamera diberikan, lanjutkan untuk membuka kamera depan
                openCamera(REQUEST_CAMERA_FRONT);
            } else {
                // Izin kamera ditolak
                Toast.makeText(this, "Izin kamera ditolak", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == REQUEST_CAMERA_FRONT || requestCode == REQUEST_CAMERA_SIDE) {
                // Gambar diambil dari kamera
                Bitmap capturedImageBitmap = (Bitmap) data.getExtras().get("data");

                // Simpan gambar pada variabel instance frontPictureBitmap atau sidePictureBitmap sesuai jenis kamera
                if (requestCode == REQUEST_CAMERA_FRONT) {
                    frontPictureBitmap = capturedImageBitmap;

                } else if (requestCode == REQUEST_CAMERA_SIDE) {
                    sidePictureBitmap = capturedImageBitmap;

                }
            }
        }
    }


    private Bitmap convertToGrayscale(Bitmap bitmap) {
        Bitmap grayscaleBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int pixel = bitmap.getPixel(x, y);

                int red = Color.red(pixel);
                int green = Color.green(pixel);
                int blue = Color.blue(pixel);

                int grayscaleValue = (int) (0.2989 * red + 0.587 * green + 0.114 * blue);
                int grayscalePixel = Color.rgb(grayscaleValue, grayscaleValue, grayscaleValue);

                grayscaleBitmap.setPixel(x, y, grayscalePixel);
            }
        }

        return grayscaleBitmap;
    }

    // Metode untuk menerapkan threshold pada gambar
    private Bitmap applyThreshold(Bitmap bitmap, int thresholdMin, int thresholdMax) {
        Bitmap thresholdedBitmap = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.RGB_565);

        for (int y = 0; y < bitmap.getHeight(); y++) {
            for (int x = 0; x < bitmap.getWidth(); x++) {
                int pixel = bitmap.getPixel(x, y);

                int grayscaleValue = Color.red(pixel);

                if (grayscaleValue >= thresholdMin && grayscaleValue <= thresholdMax) {
                    thresholdedBitmap.setPixel(x, y, Color.WHITE);
                } else {
                    thresholdedBitmap.setPixel(x, y, Color.BLACK);
                }
            }
        }

        return thresholdedBitmap;
    }

    // Metode untuk melaksanakan dilasi pada gambar
    private Bitmap dilate(Bitmap bitmap, int iterations) {
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        Bitmap dilatedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565);

        int[] pixels = new int[width * height];
        bitmap.getPixels(pixels, 0, width, 0, 0, width, height);

        int[] dilatedPixels = new int[width * height];
        System.arraycopy(pixels, 0, dilatedPixels, 0, pixels.length);

        for (int iteration = 0; iteration < iterations; iteration++) {
            System.arraycopy(pixels, 0, dilatedPixels, 0, pixels.length);

            for (int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int centerPixel = pixels[y * width + x];

                    if (centerPixel == Color.BLACK) {
                        if (x > 0 && pixels[y * width + x - 1] == Color.WHITE) {
                            dilatedPixels[y * width + x - 1] = Color.BLACK;
                        }
                        if (x < width - 1 && pixels[y * width + x + 1] == Color.WHITE) {
                            dilatedPixels[y * width + x + 1] = Color.BLACK;
                        }
                        if (y > 0 && pixels[(y - 1) * width + x] == Color.WHITE) {
                            dilatedPixels[(y - 1) * width + x] = Color.BLACK;
                        }
                        if (y < height - 1 && pixels[(y + 1) * width + x] == Color.WHITE) {
                            dilatedPixels[(y + 1) * width + x] = Color.BLACK;
                        }
                    }
                }
            }

            System.arraycopy(dilatedPixels, 0, pixels, 0, dilatedPixels.length);
        }

        dilatedBitmap.setPixels(dilatedPixels, 0, width, 0, 0, width, height);

        return dilatedBitmap;
    }
    private Bitmap erode(Bitmap bitmap, int iterations) {
        Bitmap erodedBitmap = bitmap.copy(bitmap.getConfig(), true);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int[][] structuringElement = {
                {0, 1, 0},
                {1, 1, 1},
                {0, 1, 0}
        };

        for (int i = 0; i < iterations; i++) {
            for (int y = 1; y < height - 1; y++) {
                for (int x = 1; x < width - 1; x++) {
                    if (bitmap.getPixel(x, y) == Color.BLACK) {
                        boolean shouldErode = true;

                        for (int j = -1; j <= 1; j++) {
                            for (int k = -1; k <= 1; k++) {
                                if (structuringElement[j + 1][k + 1] == 1 &&
                                        bitmap.getPixel(x + k, y + j) != Color.BLACK) {
                                    shouldErode = false;
                                    break;
                                }
                            }

                            if (!shouldErode) {
                                break;
                            }
                        }

                        if (shouldErode) {
                            erodedBitmap.setPixel(x, y, Color.WHITE);
                        }
                    }
                }
            }
        }

        return erodedBitmap;
    }

    private Bitmap floodFill(Bitmap bitmap, int startX, int startY, int targetColor) {
        Bitmap floodFilledBitmap = bitmap.copy(bitmap.getConfig(), true);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int sourceColor = Color.BLACK;

        if (sourceColor == targetColor) {
            return floodFilledBitmap;
        }

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(startX, startY));

        while (!queue.isEmpty()) {
            Point point = queue.poll();
            int x = point.x;
            int y = point.y;

            if (x < 0 || x >= width || y < 0 || y >= height) {
                continue;
            }

            if (floodFilledBitmap.getPixel(x, y) == sourceColor) {
                floodFilledBitmap.setPixel(x, y, targetColor);

                queue.add(new Point(x + 1, y));
                queue.add(new Point(x - 1, y));
                queue.add(new Point(x, y + 1));
                queue.add(new Point(x, y - 1));
            }
        }

        return floodFilledBitmap;
    }

    private Bitmap labelRegions(Bitmap bitmap) {
        Bitmap labeledBitmap = bitmap.copy(bitmap.getConfig(), true);
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();

        int currentLabel = 1;
        int[][] labels = new int[width][height];

        // Cari objek tengah berdasarkan flood fill
        int centerX = width / 2;
        int centerY = height / 2;
        int centerLabel = -1;

        Queue<Point> queue = new LinkedList<>();
        queue.add(new Point(centerX, centerY));

        while (!queue.isEmpty()) {
            Point point = queue.poll();
            int x = point.x;
            int y = point.y;

            if (x < 0 || x >= width || y < 0 || y >= height) {
                continue;
            }

            if (bitmap.getPixel(x, y) == Color.WHITE && labels[x][y] == 0) {
                labels[x][y] = currentLabel;

                if (centerLabel == -1) {
                    centerLabel = currentLabel;
                }

                queue.add(new Point(x + 1, y));
                queue.add(new Point(x - 1, y));
                queue.add(new Point(x, y + 1));
                queue.add(new Point(x, y - 1));
            }
        }

        // Menggunakan label untuk mendeteksi kontur pada objek yang berada di tengah
        List<Point> contour = new ArrayList<>();
        boolean isContour = false;

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                if (labels[x][y] == centerLabel) {
                    boolean isBoundary = isBoundaryPixel(labels, x, y);
                    if (isBoundary) {
                        contour.add(new Point(x, y));
                        isContour = true;
                    }
                }
            }
        }

        // Menandai kontur pada labeledBitmap
        if (isContour) {
            for (Point point : contour) {
                labeledBitmap.setPixel(point.x, point.y, Color.RED);
            }
        }

        return labeledBitmap;
    }

    private boolean isBoundaryPixel(int[][] labels, int x, int y) {
        int width = labels.length;
        int height = labels[0].length;

        // Cek apakah piksel (x, y) merupakan piksel boundary (piksel tetangga berbeda label)
        if (x > 0 && labels[x - 1][y] != labels[x][y]) {
            return true;
        }
        if (x < width - 1 && labels[x + 1][y] != labels[x][y]) {
            return true;
        }
        if (y > 0 && labels[x][y - 1] != labels[x][y]) {
            return true;
        }
        if (y < height - 1 && labels[x][y + 1] != labels[x][y]) {
            return true;
        }

        return false;
    }
    private void processContour(Bitmap frontPicture, Bitmap sidePicture) {
        int width1 = frontPicture.getWidth();
        int height1 = frontPicture.getHeight();
        int width2 = sidePicture.getWidth();
        int height2 = sidePicture.getHeight();

        int maxHorizontalHeight1 = Integer.MIN_VALUE;
        int maxHorizontalHeight2 = Integer.MIN_VALUE;
        int maxVerticalWidth1 = Integer.MIN_VALUE;
        int maxVerticalWidth2 = Integer.MIN_VALUE;

        // Cari lebar terbesar dalam arah horizontal dan vertikal pada frontPicture
        for (int y = 0; y < height1; y++) {
            for (int x = 0; x < width1; x++) {
                int pixel = frontPicture.getPixel(x, y);

                // Periksa apakah piksel merupakan bagian dari kontur (misalnya, piksel berwarna merah)
                if (pixel == Color.RED) {
                    // Perbarui lebar terbesar dalam arah horizontal pada frontPicture
                    if (x > maxHorizontalHeight1) {
                        maxHorizontalHeight1 = x;
                    }

                    // Perbarui lebar terbesar dalam arah vertikal pada frontPicture
                    if (y > maxVerticalWidth1) {
                        maxVerticalWidth1 = y;
                    }
                }
            }
        }

        // Cari lebar terbesar dalam arah horizontal dan vertikal pada sidePicture
        for (int y = 0; y < height2; y++) {
            for (int x = 0; x < width2; x++) {
                int pixel = sidePicture.getPixel(x, y);

                // Periksa apakah piksel merupakan bagian dari kontur (misalnya, piksel berwarna merah)
                if (pixel == Color.RED) {
                    // Perbarui lebar terbesar dalam arah horizontal pada sidePicture
                    if (x > maxHorizontalHeight2) {
                        maxHorizontalHeight2 = x;
                    }

                    // Perbarui lebar terbesar dalam arah vertikal pada sidePicture
                    if (y > maxVerticalWidth2) {
                        maxVerticalWidth2 = y;
                    }
                }
            }
        }

        // Simpan nilai lebar terbesar dalam variabel yang sesuai
        int w1 = maxHorizontalHeight1;
        int w2 = maxHorizontalHeight2;
        int h1 = maxVerticalWidth1;
        int h2 = maxVerticalWidth2;


        double t = (h1 + h2) / 2;
        double tb_asli = 0.0;

        // Menentukan nilai tb_asli berdasarkan t
        if (t >= 240 && t <= 244) {
            tb_asli = 165.0;
        } else {
            double diff = Math.abs(t - 244);
            double tb_increment = Math.floor(diff / 4);
            if (t > 244) {
                tb_asli = 165.0 + tb_increment;
            } else {
                tb_asli = 165.0 - tb_increment;
            }
        }

        double tpiksel = tb_asli / t;
        double k = 0.19;

        // Menghitung BSA prediksi dengan menggunakan nilai k terbaik
        double BSApred = (3.14 / 2) * (w1 * w2 + ((w1 + w2) * (t * 2))) * Math.pow(tpiksel, 2) * k * 0.0001;

        // Calculate BBpred
        double BBpred = ((BSApred * BSApred) * 3600) / (t * tpiksel);

        // Calculate BMI and determine BMI status
        double TBB = tb_asli / 100.0;
        double BMI = BBpred / Math.pow(TBB, 2);
        String bmiStatus;
        if (BMI <18.5){
            bmiStatus = "KURUS";
        }else if (BMI < 25.0) {
            bmiStatus = "NORMAL";
        } else if (BMI < 27.0) {
            bmiStatus = "GEMUK";
        } else {
            bmiStatus = "OBESITAS";
        }

        Intent intent = new Intent(MainActivity5.this, MainActivity6.class);
        intent.putExtra("tb_asli", tb_asli);
        intent.putExtra("BBpred", BBpred);
        intent.putExtra("bmiStatus", bmiStatus);
        startActivity(intent);
    }
}
