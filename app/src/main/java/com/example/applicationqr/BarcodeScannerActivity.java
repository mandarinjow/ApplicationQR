package com.example.applicationqr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;
import android.widget.Toast;

import com.example.applicationqr.QRScanner.BarcodeScanningProcessor;
import com.example.applicationqr.QRScanner.OverlayView;
import com.example.applicationqr.QRScanner.common.CameraSource;
import com.example.applicationqr.QRScanner.common.CameraSourcePreview;
import com.example.applicationqr.QRScanner.common.FrameMetadata;
import com.example.applicationqr.QRScanner.common.GraphicOverlay;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;

import java.io.IOException;
import java.util.List;

public class BarcodeScannerActivity extends AppCompatActivity
{
    private static final String KEY_CAMERA_PERMISSION_GRANTED = "CAMERA_PERMISSION_GRANTED";
    private static final int PERMISSION_REQUEST_CAMERA = 1;
    private final String TAG = getClass().getName();

    BarcodeScanningProcessor barcodeScanningProcessor;

    private CameraSource mCameraSource = null;

    boolean isCalled;

    private Toast toast;

    boolean isAdded = false;

    private GraphicOverlay barcodeOverlay;
    private CameraSourcePreview preview;
    private OverlayView overlayView;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        if (getWindow() != null)
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        else
            Log.e(TAG, "Barcode scanner could not go into fullscreen mode!");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_barcode_scanner);
        InitUI();

        if (preview != null)
            if (preview.isPermissionGranted(true, mMessageSender))
                new Thread(mMessageSender).start();
    }

    private void InitUI()
    {
        preview = findViewById(R.id.preview);
        barcodeOverlay = findViewById(R.id.barcodeOverlay);
        overlayView = findViewById(R.id.overlayView);
    }


    private void createCameraSource()
    {

        // To initialise the detector

        FirebaseVisionBarcodeDetectorOptions options =
                new FirebaseVisionBarcodeDetectorOptions.Builder()
                        .setBarcodeFormats(
                                FirebaseVisionBarcode.FORMAT_QR_CODE)
                        .build();

        FirebaseVisionBarcodeDetector detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);


        // To connect the camera resource with the detector

        mCameraSource = new CameraSource(this, barcodeOverlay);
        mCameraSource.setFacing(CameraSource.CAMERA_FACING_BACK);

        barcodeScanningProcessor = new BarcodeScanningProcessor(detector);
        barcodeScanningProcessor.setBarcodeResultListener(getBarcodeResultListener());

        mCameraSource.setMachineLearningFrameProcessor(barcodeScanningProcessor);

        startCameraSource();
    }

    private void startCameraSource() {

        // check that the device has play services available.
        int code = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(
                getApplicationContext());

        Log.d(TAG, "startCameraSource: " + code);

        if (code != ConnectionResult.SUCCESS)
        {
            Dialog dlg = GoogleApiAvailability.getInstance().getErrorDialog(this, code, PERMISSION_REQUEST_CAMERA);
            dlg.show();
        }

        if (mCameraSource != null && preview != null && barcodeOverlay != null)
        {
            try
            {
                Log.d(TAG, "startCameraSource: ");
                preview.start(mCameraSource, barcodeOverlay);
            }
            catch (IOException e)
            {
                Log.d(TAG, "Unable to start camera source.", e);
                mCameraSource.release();
                mCameraSource = null;
            }
        } else
            Log.d(TAG, "startCameraSource: not started");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {

        Log.d(TAG, "onRequestPermissionsResult: " + requestCode);
        preview.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * Restarts the camera.
     */
    @Override
    protected void onResume()
    {
        super.onResume();
        startCameraSource();
    }

    /**
     * Stops the camera.
     */
    @Override
    protected void onPause()
    {
        super.onPause();
        if (preview != null)
            preview.stop();
    }

    @Override
    protected void onRestart()
    {
        super.onRestart();
        isCalled = true;
    }

    /**
     * Releases the resources associated with the camera source, the associated detector, and the
     * rest of the processing pipeline.
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        if (mCameraSource != null) {
            mCameraSource.release();
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {

            Log.d(TAG, "handleMessage: ");

            if (preview != null)
                createCameraSource();

        }
    };

    private final Runnable mMessageSender = () ->
    {
        Log.d(TAG, "mMessageSender: ");
        Message msg = mHandler.obtainMessage();
        Bundle bundle = new Bundle();
        bundle.putBoolean(KEY_CAMERA_PERMISSION_GRANTED, false);
        msg.setData(bundle);
        mHandler.sendMessage(msg);
    };

    public BarcodeScanningProcessor.BarcodeResultListener getBarcodeResultListener() {

        return new BarcodeScanningProcessor.BarcodeResultListener() {
            @Override
            public void onSuccess(@Nullable Bitmap originalCameraImage, @NonNull List<FirebaseVisionBarcode> barcodes, @NonNull FrameMetadata frameMetadata, @NonNull GraphicOverlay graphicOverlay) {
                Log.d(TAG, "onSuccess_size: " + barcodes.size());

                for (FirebaseVisionBarcode barCode : barcodes)
                {

                    Log.d(TAG, "onSuccess_raw: " + barCode.getRawValue());
                    Log.d(TAG, "onSuccess_format: " + barCode.getFormat());
                    Log.d(TAG, "onSuccess_val_type: " + barCode.getValueType());

                    String string = barCode.getDisplayValue();
                    Log.d(TAG, "onSuccess_value: " + string);
                    finish();

//                    ContactDetail contactDetail = new ContactDetail();
//                    if (contactInfo != null) {
//                        if (contactInfo.getName() != null && !TextUtils.isEmpty(contactInfo.getName().getFormattedName())) {
//                            Log.d(TAG, "onSuccess: getFormattedName" + contactInfo.getName().getFormattedName());
//                            contactDetail.setName(contactInfo.getName().getFormattedName());
//                        }
//
//                        if (contactInfo.getAddresses().size() > 0) {
//                            Log.d(TAG, "onSuccess: getAddresses" + contactInfo.getAddresses().get(0).getAddressLines()[0]);
//                            contactDetail.setAddress(contactInfo.getAddresses().get(0).getAddressLines()[0]);
//                        }
//
//                        if (contactInfo.getEmails().size() > 0) {
//                            Log.d(TAG, "onSuccess: getEmails" + contactInfo.getEmails().get(0).getAddress());
//                            contactDetail.setEmailID(contactInfo.getEmails().get(0).getAddress());
//                        }
//
//                        if (contactInfo.getPhones().size() > 0) {
//                            Log.d(TAG, "onSuccess: getPhones" + contactInfo.getPhones().get(0).getNumber());
//                            contactDetail.setPhoneNumber(contactInfo.getPhones().get(0).getNumber());
//
//                        }
//
//                        if (!TextUtils.isEmpty(contactInfo.getOrganization())) {
//                            Log.d(TAG, "onSuccess: getOrganization" + contactInfo.getOrganization());
//                            contactDetail.setOrgName(contactInfo.getOrganization());
//                        }
//
//                        if (contactInfo.getUrls() != null && contactInfo.getUrls().length > 0) {
//                            String[] urlList = contactInfo.getUrls();
//                            Log.d(TAG, "onSuccess: getUrls" + urlList[0]);
//                            contactDetail.setWebLink(urlList[0]);
//                        }
//
//                        if (!dbHandler.isAccountAlreadyExist(contactDetail.getName(), contactDetail.getPhoneNumber())) {
//                            dbHandler.insertAccountDetails(contactDetail);
//                            Log.d(TAG, "onSuccess: Added");
//                            isAdded = true;
//                            finish();
//                        } else
//                            showToast("This account is already exists");
//                    }
                }

                if (isAdded)
                    finish();
            }

            @Override
            public void onFailure(@NonNull Exception e) {

            }
        };
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (toast != null) {
            toast.cancel();
        }
    }

    public void showToast(String message) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();
    }
}
