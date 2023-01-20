//package com.secuyou.android_v22_pin_app.dfu;
//
//import android.Manifest;
//import android.app.ActivityManager;
//import android.app.AlertDialog;
//import android.app.NotificationManager;
//import android.bluetooth.BluetoothAdapter;
//import android.bluetooth.BluetoothDevice;
//import android.bluetooth.BluetoothManager;
//import android.bluetooth.le.BluetoothLeScanner;
//import android.bluetooth.le.ScanCallback;
//import android.bluetooth.le.ScanResult;
//import android.bluetooth.le.ScanSettings;
//import android.content.Context;
//import android.content.DialogInterface;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.content.pm.PackageManager;
//import android.database.Cursor;
//import android.net.Uri;
//import android.os.Bundle;
//import android.os.Handler;
//import android.preference.PreferenceManager;
//import android.text.TextUtils;
//import android.view.Menu;
//import android.view.MenuItem;
//import android.view.View;
//import android.webkit.MimeTypeMap;
//import android.widget.Button;
//import android.widget.ProgressBar;
//import android.widget.RadioButton;
//import android.widget.TextView;
//import android.widget.Toast;
//
//import androidx.appcompat.widget.ActivityChooserView;
//import androidx.core.app.ActivityCompat;
//import androidx.loader.app.LoaderManager;
//import androidx.loader.content.CursorLoader;
//import androidx.loader.content.Loader;
//import androidx.localbroadcastmanager.content.LocalBroadcastManager;
//
//import com.google.android.youtube.player.YouTubeBaseActivity;
//import com.google.android.youtube.player.YouTubeInitializationResult;
//import com.google.android.youtube.player.YouTubePlayer;
//import com.google.android.youtube.player.YouTubePlayerView;
//import com.secuyou.android_v22_pin_app.PlayerConfig;
//import no.joymyr.secuyou_reverse.R;
//
//import java.io.File;
//import java.util.ArrayList;
//import java.util.List;
//
//import no.nordicsemi.android.dfu.BuildConfig;
//import no.nordicsemi.android.dfu.DfuBaseService;
//import no.nordicsemi.android.dfu.DfuProgressListener;
//import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
//import no.nordicsemi.android.dfu.DfuServiceInitiator;
//import no.nordicsemi.android.dfu.DfuServiceListenerHelper;
//
///* loaded from: classes.dex */
//public class DfuActivity extends YouTubeBaseActivity implements LoaderManager.LoaderCallbacks<Cursor>, ScannerFragment.OnDeviceSelectedListener, UploadCancelFragment.CancelFragmentListener {
//    private static final String DATA_DEVICE = "device";
//    private static final String DATA_DFU_COMPLETED = "dfu_completed";
//    private static final String DATA_DFU_ERROR = "dfu_error";
//    private static final String DATA_FILE_PATH = "file_path";
//    private static final String DATA_FILE_STREAM = "file_stream";
//    private static final String DATA_FILE_TYPE = "file_type";
//    private static final String DATA_FILE_TYPE_TMP = "file_type_tmp";
//    private static final String DATA_INIT_FILE_PATH = "init_file_path";
//    private static final String DATA_INIT_FILE_STREAM = "init_file_stream";
//    private static final String DATA_SCOPE = "scope";
//    private static final String DATA_STATUS = "status";
//    private static final int ENABLE_BT_REQ = 0;
//    private static final String EXTRA_URI = "uri";
//    private static final int PERMISSION_REQ = 25;
//    private static final String PREFS_DEVICE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_DEVICE_NAME";
//    private static final String PREFS_FILE_NAME = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_NAME";
//    private static final String PREFS_FILE_SCOPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SCOPE";
//    private static final String PREFS_FILE_SIZE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_SIZE";
//    private static final String PREFS_FILE_TYPE = "no.nordicsemi.android.nrftoolbox.dfu.PREFS_FILE_TYPE";
//    private static final long SCAN_DURATION = 5000;
//    private static final int SELECT_FILE_REQ = 1;
//    private static final int SELECT_INIT_FILE_REQ = 2;
//    private static final String TAG = "DfuActivity";
//    private BluetoothLeScanner BluetoothLeScannerCompat;
//    int fileSelected;
//    private Button mConnectButton;
//    private TextView mDeviceNameView;
//    private boolean mDfuCompleted;
//    private String mDfuError;
//    private String mFilePath;
//    private TextView mFileScopeView;
//    private TextView mFileSizeView;
//    private TextView mFileStatusView;
//    private Uri mFileStreamUri;
//    private int mFileType;
//    private int mFileTypeTmp;
//    private TextView mFileTypeView;
//    private Handler mHandler;
//    private TextView mInfoView;
//    private String mInitFilePath;
//    private Uri mInitFileStreamUri;
//    private ProgressBar mProgressBar;
//    private boolean mResumed;
//    private Integer mScope;
//    private Button mSelectFileButton;
//    private BluetoothDevice mSelectedDevice;
//    private boolean mStatusOk;
//    private TextView mTextPercentage;
//    private TextView mTextUploading;
//    private Button mUploadButton;
//    private YouTubePlayerView mVideoView;
//    private View.OnClickListener onClickListener;
//    private YouTubePlayer.OnInitializedListener onInitializedListener;
//    private boolean mIsScanning = false;
//    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() { // from class: com.secuyou.android_v22_pin_app.dfu.DfuActivity.1
//        @Override
//        // no.nordicsemi.android.dfu.DfuProgressListenerAdapter, no.nordicsemi.android.dfu.DfuProgressListener
//        public void onDeviceConnecting(String str) {
//        }
//
//        @Override
//        // no.nordicsemi.android.dfu.DfuProgressListenerAdapter, no.nordicsemi.android.dfu.DfuProgressListener
//        public void onDfuProcessStarting(String str) {
//            DfuActivity.this.mDeviceNameView.setVisibility(View.GONE);
//            DfuActivity.this.mTextPercentage.setText(R.string.dfu_status_starting);
//        }
//
//        @Override
//        // no.nordicsemi.android.dfu.DfuProgressListenerAdapter, no.nordicsemi.android.dfu.DfuProgressListener
//        public void onEnablingDfuMode(String str) {
//            DfuActivity.this.mTextPercentage.setText(R.string.dfu_status_switching_to_dfu);
//        }
//
//        @Override
//        // no.nordicsemi.android.dfu.DfuProgressListenerAdapter, no.nordicsemi.android.dfu.DfuProgressListener
//        public void onFirmwareValidating(String str) {
//            DfuActivity.this.mTextPercentage.setText(R.string.dfu_status_validating);
//        }
//
//        @Override
//        // no.nordicsemi.android.dfu.DfuProgressListenerAdapter, no.nordicsemi.android.dfu.DfuProgressListener
//        public void onDeviceDisconnecting(String str) {
//            DfuActivity.this.mTextPercentage.setText(R.string.dfu_status_disconnecting);
//        }
//
//        @Override
//        // no.nordicsemi.android.dfu.DfuProgressListenerAdapter, no.nordicsemi.android.dfu.DfuProgressListener
//        public void onDfuCompleted(String str) {
//            DfuActivity.this.mTextPercentage.setText(R.string.dfu_status_completed);
//            if (DfuActivity.this.mResumed) {
//                DfuActivity.this.onTransferCompleted();
//                ((NotificationManager) DfuActivity.this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(DfuBaseService.NOTIFICATION_ID);
//                return;
//            }
//            DfuActivity.this.mDfuCompleted = true;
//        }
//
//        @Override
//        // no.nordicsemi.android.dfu.DfuProgressListenerAdapter, no.nordicsemi.android.dfu.DfuProgressListener
//        public void onDfuAborted(String str) {
//            DfuActivity.this.mTextPercentage.setText(R.string.dfu_status_aborted);
//            DfuActivity.this.onUploadCanceled();
//            ((NotificationManager) DfuActivity.this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(DfuBaseService.NOTIFICATION_ID);
//        }
//
//        @Override
//        // no.nordicsemi.android.dfu.DfuProgressListenerAdapter, no.nordicsemi.android.dfu.DfuProgressListener
//        public void onProgressChanged(String str, int i, float f, float f2, int i2, int i3) {
//            DfuActivity.this.mTextPercentage.setText(DfuActivity.this.getString(R.string.dfu_uploading_percentage, new Object[]{Integer.valueOf(i)}));
//            if (i3 > 1) {
//                DfuActivity.this.mTextUploading.setText(DfuActivity.this.getString(R.string.dfu_status_uploading_part, new Object[]{Integer.valueOf(i2), Integer.valueOf(i3)}));
//            } else {
//                DfuActivity.this.mTextUploading.setText(R.string.dfu_status_uploading);
//            }
//        }
//
//        @Override
//        // no.nordicsemi.android.dfu.DfuProgressListenerAdapter, no.nordicsemi.android.dfu.DfuProgressListener
//        public void onError(String str, int i, int i2, String str2) {
//            if (DfuActivity.this.mResumed) {
//                DfuActivity.this.showErrorMessage(str2);
//                ((NotificationManager) DfuActivity.this.getSystemService(Context.NOTIFICATION_SERVICE)).cancel(DfuBaseService.NOTIFICATION_ID);
//                return;
//            }
//            DfuActivity.this.mDfuError = str2;
//        }
//    };
//    Runnable myRunnable = new Runnable() { // from class: com.secuyou.android_v22_pin_app.dfu.DfuActivity.4
//        @Override // java.lang.Runnable
//        public void run() {
//            DfuActivity.this.mIsScanning = false;
//            if (DfuActivity.this.mSelectedDevice == null) {
//                DfuActivity.this.mDeviceNameView.setVisibility(View.VISIBLE);
//                DfuActivity.this.mDeviceNameView.setText("No lock found!");
//                DfuActivity.this.mInfoView.setVisibility(View.VISIBLE);
//                DfuActivity.this.mProgressBar.setVisibility(View.GONE);
//            }
//            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
//                DfuActivity.this.BluetoothLeScannerCompat.stopScan(DfuActivity.this.mScanCallback);
//            }
//        }
//    };
//    private ScanCallback mScanCallback = new ScanCallback() { // from class: com.secuyou.android_v22_pin_app.dfu.DfuActivity.5
//        @Override // android.bluetooth.le.ScanCallback
//        public void onBatchScanResults(List<ScanResult> list) {
//        }
//
//        @Override // android.bluetooth.le.ScanCallback
//        public void onScanFailed(int i) {
//        }
//
//        @Override // android.bluetooth.le.ScanCallback
//        public void onScanResult(int i, ScanResult scanResult) {
//            if (ActivityCompat.checkSelfPermission(getBaseContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//                String name = scanResult.getDevice().getName();
//                BluetoothDevice device = scanResult.getDevice();
//                if (name == null || !name.contains("DfuTarg")) {
//                    return;
//                }
//                ExtendedBluetoothDevice extendedBluetoothDevice = new ExtendedBluetoothDevice(device);
//                DfuActivity.this.onDeviceSelected(extendedBluetoothDevice.device, extendedBluetoothDevice.name);
//                DfuActivity.this.stopScan();
//            }
//        }
//    };
//
//    @Override // android.app.Activity
//    public boolean onCreateOptionsMenu(Menu menu) {
//        return true;
//    }
//
//    @Override // com.secuyou.android_v22_pin_app.dfu.ScannerFragment.OnDeviceSelectedListener
//    public void onDialogCanceled() {
//    }
//
//    /* JADX INFO: Access modifiers changed from: protected */
//    @Override // com.google.android.youtube.player.YouTubeBaseActivity, android.app.Activity
//    public void onCreate(Bundle bundle) {
//        super.onCreate(bundle);
//        setContentView(R.layout.activity_feature_dfu);
//        isBLESupported();
//        if (!isBLEEnabled()) {
//            showBLEDialog();
//        }
//        setGUI();
//        this.mHandler = new Handler();
//        if (FileHelper.newSamplesAvailable(this)) {
//            FileHelper.createSamples(this);
//        }
//        boolean z = false;
//        this.mFileType = 0;
//        if (bundle != null) {
//            this.mFileType = bundle.getInt(DATA_FILE_TYPE);
//            this.mFileTypeTmp = bundle.getInt(DATA_FILE_TYPE_TMP);
//            this.mFilePath = bundle.getString(DATA_FILE_PATH);
//            this.mFileStreamUri = (Uri) bundle.getParcelable(DATA_FILE_STREAM);
//            this.mInitFilePath = bundle.getString(DATA_INIT_FILE_PATH);
//            this.mInitFileStreamUri = (Uri) bundle.getParcelable(DATA_INIT_FILE_STREAM);
//            this.mSelectedDevice = (BluetoothDevice) bundle.getParcelable(DATA_DEVICE);
//            this.mStatusOk = true;
//            this.mScope = bundle.containsKey(DATA_SCOPE) ? Integer.valueOf(bundle.getInt(DATA_SCOPE)) : null;
//            Button button = this.mUploadButton;
//            if (this.mSelectedDevice != null && this.mStatusOk) {
//                z = true;
//            }
//            button.setEnabled(z);
//            this.mDfuCompleted = bundle.getBoolean(DATA_DFU_COMPLETED);
//            this.mDfuError = bundle.getString(DATA_DFU_ERROR);
//        }
//        DfuServiceListenerHelper.registerProgressListener(this, this.mDfuProgressListener);
//        this.mVideoView = (YouTubePlayerView) findViewById(R.id.videoView);
//        YouTubePlayer.OnInitializedListener onInitializedListener = new YouTubePlayer.OnInitializedListener() { // from class: com.secuyou.android_v22_pin_app.dfu.DfuActivity.2
//            @Override // com.google.android.youtube.player.YouTubePlayer.OnInitializedListener
//            public void onInitializationFailure(YouTubePlayer.Provider provider, YouTubeInitializationResult youTubeInitializationResult) {
//            }
//
//            @Override // com.google.android.youtube.player.YouTubePlayer.OnInitializedListener
//            public void onInitializationSuccess(YouTubePlayer.Provider provider, YouTubePlayer youTubePlayer, boolean z2) {
//                youTubePlayer.loadVideo("v8JhsVRrCPM");
//            }
//        };
//        this.onInitializedListener = onInitializedListener;
//        this.mVideoView.initialize(PlayerConfig.API_KEY, onInitializedListener);
//        ((TextView) findViewById(R.id.actionbartitle)).setOnClickListener(new View.OnClickListener() { // from class: com.secuyou.android_v22_pin_app.dfu.DfuActivity.3
//            @Override // android.view.View.OnClickListener
//            public void onClick(View view) {
//                DfuActivity.super.onBackPressed();
//            }
//        });
//        this.fileSelected = R.raw.bl_sdk13_patch_pkg;
//    }
//
//    public void onRadioButtonClicked(View view) {
//        boolean isChecked = ((RadioButton) view).isChecked();
//        int id = view.getId();
//        if (id == R.id.app_os) {
//            if (isChecked) {
//                this.fileSelected = R.raw.bl_app_sd_v22_v6;
//                Toast.makeText(getApplicationContext(), "File App selected", Toast.LENGTH_LONG).show();
//            }
//        } else if (id == R.id.patch && isChecked) {
//            this.fileSelected = R.raw.bl_sdk13_patch_pkg;
//            Toast.makeText(getApplicationContext(), "File Patch selected ", Toast.LENGTH_LONG).show();
//        }
//    }
//
//    /* JADX INFO: Access modifiers changed from: protected */
//    @Override // com.google.android.youtube.player.YouTubeBaseActivity, android.app.Activity
//    public void onDestroy() {
//        super.onDestroy();
//        DfuServiceListenerHelper.unregisterProgressListener(this, this.mDfuProgressListener);
//    }
//
//    /* JADX INFO: Access modifiers changed from: protected */
//    @Override // com.google.android.youtube.player.YouTubeBaseActivity, android.app.Activity
//    public void onSaveInstanceState(Bundle bundle) {
//        super.onSaveInstanceState(bundle);
//        bundle.putInt(DATA_FILE_TYPE, this.mFileType);
//        bundle.putInt(DATA_FILE_TYPE_TMP, this.mFileTypeTmp);
//        bundle.putString(DATA_FILE_PATH, this.mFilePath);
//        bundle.putParcelable(DATA_FILE_STREAM, this.mFileStreamUri);
//        bundle.putString(DATA_INIT_FILE_PATH, this.mInitFilePath);
//        bundle.putParcelable(DATA_INIT_FILE_STREAM, this.mInitFileStreamUri);
//        bundle.putParcelable(DATA_DEVICE, this.mSelectedDevice);
//        bundle.putBoolean("status", this.mStatusOk);
//        Integer num = this.mScope;
//        if (num != null) {
//            bundle.putInt(DATA_SCOPE, num.intValue());
//        }
//        bundle.putBoolean(DATA_DFU_COMPLETED, this.mDfuCompleted);
//        bundle.putString(DATA_DFU_ERROR, this.mDfuError);
//    }
//
//    private void setGUI() {
//        this.mDeviceNameView = (TextView) findViewById(R.id.device_name);
//        this.mInfoView = (TextView) findViewById(R.id.info_name);
//        this.mUploadButton = (Button) findViewById(R.id.action_upload);
//        this.mConnectButton = (Button) findViewById(R.id.action_connect);
//        this.mTextPercentage = (TextView) findViewById(R.id.textviewProgress);
//        this.mTextUploading = (TextView) findViewById(R.id.textviewUploading);
//        this.mProgressBar = (ProgressBar) findViewById(R.id.progressBar2);
//        this.mVideoView = (YouTubePlayerView) findViewById(R.id.videoView);
//        showStartInfo();
//        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
//        if (isDfuServiceRunning()) {
//            this.mDeviceNameView.setText(defaultSharedPreferences.getString(PREFS_DEVICE_NAME, BuildConfig.FLAVOR));
//            this.mInfoView.setText(defaultSharedPreferences.getString(PREFS_FILE_NAME, BuildConfig.FLAVOR));
//            this.mStatusOk = true;
//            showProgressBar();
//        }
//    }
//
//    /* JADX INFO: Access modifiers changed from: protected */
//    @Override // com.google.android.youtube.player.YouTubeBaseActivity, android.app.Activity
//    public void onResume() {
//        super.onResume();
//        this.mResumed = true;
//        if (this.mDfuCompleted) {
//            onTransferCompleted();
//        }
//        String str = this.mDfuError;
//        if (str != null) {
//            showErrorMessage(str);
//        }
//        if (this.mDfuCompleted || this.mDfuError != null) {
//            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(DfuBaseService.NOTIFICATION_ID);
//            this.mDfuCompleted = false;
//            this.mDfuError = null;
//        }
//    }
//
//    /* JADX INFO: Access modifiers changed from: protected */
//    @Override // com.google.android.youtube.player.YouTubeBaseActivity, android.app.Activity
//    public void onPause() {
//        super.onPause();
//        this.mResumed = false;
//    }
//
//    private void isBLESupported() {
//        if (getPackageManager().hasSystemFeature("android.hardware.bluetooth_le")) {
//            return;
//        }
//        showToast(R.string.no_ble);
//        finish();
//    }
//
//    private boolean isBLEEnabled() {
//        BluetoothAdapter adapter = ((BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
//        return adapter != null && adapter.isEnabled();
//    }
//
//    private void showBLEDialog() {
//        startActivityForResult(new Intent("android.bluetooth.adapter.action.REQUEST_ENABLE"), 0);
//    }
//
//    private void showDeviceScanningDialog() {
//        ScannerFragment.getInstance(null);
//    }
//
//    @Override // android.app.Activity
//    public boolean onOptionsItemSelected(MenuItem menuItem) {
//        if (menuItem.getItemId() != 16908332) {
//            return true;
//        }
//        onBackPressed();
//        return true;
//    }
//
//    @Override // android.app.Activity
//    protected void onActivityResult(int i, int i2, Intent intent) {
//        if (i2 != -1) {
//            return;
//        }
//        if (i != 1) {
//            if (i != 2) {
//                return;
//            }
//            this.mInitFilePath = null;
//            this.mInitFileStreamUri = null;
//            Uri data = intent.getData();
//            if (data.getScheme().equals("file")) {
//                this.mInitFilePath = data.getPath();
//                this.mFileStatusView.setText(R.string.dfu_file_status_ok_with_init);
//                return;
//            } else if (data.getScheme().equals("content")) {
//                this.mInitFileStreamUri = data;
//                Bundle extras = intent.getExtras();
//                if (extras != null && extras.containsKey("android.intent.extra.STREAM")) {
//                    this.mInitFileStreamUri = (Uri) extras.getParcelable("android.intent.extra.STREAM");
//                }
//                this.mFileStatusView.setText(R.string.dfu_file_status_ok_with_init);
//                return;
//            } else {
//                return;
//            }
//        }
//        this.mFileType = this.mFileTypeTmp;
//        this.mFilePath = null;
//        this.mFileStreamUri = null;
//        Uri data2 = intent.getData();
//        if (data2.getScheme().equals("file")) {
//            String path = data2.getPath();
//            File file = new File(path);
//            this.mFilePath = path;
//            updateFileInfo(file.getName(), file.length(), this.mFileType);
//        } else if (data2.getScheme().equals("content")) {
//            this.mFileStreamUri = data2;
//            Bundle extras2 = intent.getExtras();
//            if (extras2 != null && extras2.containsKey("android.intent.extra.STREAM")) {
//                this.mFileStreamUri = (Uri) extras2.getParcelable("android.intent.extra.STREAM");
//            }
//            new Bundle().putParcelable(EXTRA_URI, data2);
//        }
//    }
//
//    @Override // androidx.loader.app.LoaderManager.LoaderCallbacks
//    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
//        return new CursorLoader(this, (Uri) bundle.getParcelable(EXTRA_URI), null, null, null, null);
//    }
//
//    @Override // androidx.loader.app.LoaderManager.LoaderCallbacks
//    public void onLoaderReset(Loader<Cursor> loader) {
//        this.mInfoView.setText((CharSequence) null);
//        this.mFileTypeView.setText((CharSequence) null);
//        this.mFileSizeView.setText((CharSequence) null);
//        this.mFilePath = null;
//        this.mFileStreamUri = null;
//        this.mStatusOk = false;
//    }
//
//    @Override // androidx.loader.app.LoaderManager.LoaderCallbacks
//    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
//        if (cursor != null && cursor.moveToNext()) {
//            String string = cursor.getString(Math.abs(cursor.getColumnIndex("_display_name")));
//            int i = cursor.getInt(Math.abs(cursor.getColumnIndex("_size")));
//            int columnIndex = cursor.getColumnIndex("_data");
//            String string2 = columnIndex != -1 ? cursor.getString(columnIndex) : null;
//            if (!TextUtils.isEmpty(string2)) {
//                this.mFilePath = string2;
//            }
//            updateFileInfo(string, i, this.mFileType);
//            return;
//        }
//        this.mInfoView.setText((CharSequence) null);
//        this.mFileTypeView.setText((CharSequence) null);
//        this.mFileSizeView.setText((CharSequence) null);
//        this.mFilePath = null;
//        this.mFileStreamUri = null;
//        this.mFileStatusView.setText(R.string.dfu_file_status_error);
//        this.mStatusOk = false;
//    }
//
//    private void updateFileInfo(String str, long j, int i) {
//        this.mInfoView.setText(str);
//        boolean z = false;
//        if (i == 0) {
//            this.mFileTypeView.setText(getResources().getStringArray(R.array.dfu_file_type)[0]);
//        } else if (i == 1) {
//            this.mFileTypeView.setText(getResources().getStringArray(R.array.dfu_file_type)[1]);
//        } else if (i == 2) {
//            this.mFileTypeView.setText(getResources().getStringArray(R.array.dfu_file_type)[2]);
//        } else if (i == 4) {
//            this.mFileTypeView.setText(getResources().getStringArray(R.array.dfu_file_type)[3]);
//        }
//        this.mFileScopeView.setText(getString(R.string.not_available));
//        boolean matches = MimeTypeMap.getFileExtensionFromUrl(str).matches(this.mFileType == 0 ? "(?i)ZIP" : "(?i)HEX|BIN");
//        this.mStatusOk = matches;
//        this.mFileStatusView.setText(matches ? R.string.dfu_file_status_ok : R.string.dfu_file_status_invalid);
//        Button button = this.mUploadButton;
//        if (this.mSelectedDevice != null && matches) {
//            z = true;
//        }
//        button.setEnabled(z);
//    }
//
//    public void onSelectFileHelpClicked(View view) {
//        new AlertDialog.Builder(this).setTitle(R.string.dfu_help_title).setMessage(R.string.dfu_help_message).setPositiveButton(R.string.ok, (DialogInterface.OnClickListener) null).show();
//    }
//
//    public void onSelectFileClicked(View view) {
//        this.mFileTypeTmp = this.mFileType;
//    }
//
//    private void openFileChooser() {
//        Intent intent = new Intent("android.intent.action.GET_CONTENT");
//        intent.setType(this.mFileTypeTmp == 0 ? DfuBaseService.MIME_TYPE_ZIP : DfuBaseService.MIME_TYPE_OCTET_STREAM);
//        intent.addCategory("android.intent.category.OPENABLE");
//        if (intent.resolveActivity(getPackageManager()) != null) {
//            startActivityForResult(intent, 1);
//        }
//    }
//
//    public void onUploadClicked(View view) {
//        if (isDfuServiceRunning()) {
//            showUploadCancelDialog();
//            return;
//        }
//        this.mStatusOk = true;
//        SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(this).edit();
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
//            edit.putString(PREFS_DEVICE_NAME, this.mSelectedDevice.getName());
//        }
//        edit.putString(PREFS_FILE_NAME, this.mInfoView.getText().toString());
//        edit.apply();
//        showProgressBar();
//        DfuServiceInitiator.createDfuNotificationChannel(this);
//        DfuServiceInitiator unsafeExperimentalButtonlessServiceInSecureDfuEnabled = new DfuServiceInitiator(this.mSelectedDevice.getAddress()).setDeviceName(this.mSelectedDevice.getName()).setKeepBond(false).setForceDfu(false).setPacketsReceiptNotificationsEnabled(true).setPacketsReceiptNotificationsValue(12).setUnsafeExperimentalButtonlessServiceInSecureDfuEnabled(false);
//        int i = this.mFileType;
//        if (i == 0) {
//            unsafeExperimentalButtonlessServiceInSecureDfuEnabled.setZip(this.fileSelected);
//            Integer num = this.mScope;
//            if (num != null) {
//                unsafeExperimentalButtonlessServiceInSecureDfuEnabled.setScope(num.intValue());
//            }
//        } else {
//            unsafeExperimentalButtonlessServiceInSecureDfuEnabled.setBinOrHex(i, this.mFileStreamUri, this.mFilePath).setInitFile(this.mInitFileStreamUri, this.mInitFilePath);
//        }
//        unsafeExperimentalButtonlessServiceInSecureDfuEnabled.start(this, DfuService.class);
//    }
//
//    private void showUploadCancelDialog() {
//        LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
//        Intent intent = new Intent(DfuBaseService.BROADCAST_ACTION);
//        intent.putExtra(DfuBaseService.EXTRA_ACTION, 0);
//        localBroadcastManager.sendBroadcast(intent);
//        UploadCancelFragment.getInstance();
//    }
//
//    public void onConnectClicked(View view) {
//        if (isBLEEnabled()) {
//            startScan();
//            connectStartInfo();
//            return;
//        }
//        showBLEDialog();
//    }
//
//    @Override // com.secuyou.android_v22_pin_app.dfu.ScannerFragment.OnDeviceSelectedListener
//    public void onDeviceSelected(BluetoothDevice bluetoothDevice, String str) {
//        this.mSelectedDevice = bluetoothDevice;
//        this.mUploadButton.setEnabled(true);
//        this.mDeviceNameView.setText("Device found! Ready for action - push UPLOAD");
//        this.mDeviceNameView.setVisibility(View.VISIBLE);
//        this.mProgressBar.setVisibility(View.GONE);
//    }
//
//    private void showProgressBar() {
//        this.mInfoView.setVisibility(View.GONE);
//        this.mDeviceNameView.setVisibility(View.GONE);
//        this.mTextPercentage.setVisibility(View.VISIBLE);
//        this.mTextPercentage.setText((CharSequence) null);
//        this.mTextUploading.setText(R.string.dfu_status_uploading);
//        this.mTextUploading.setVisibility(View.VISIBLE);
//        this.mConnectButton.setEnabled(false);
//        this.mUploadButton.setEnabled(true);
//        this.mUploadButton.setText(R.string.dfu_action_upload_cancel);
//        this.mProgressBar.setVisibility(View.GONE);
//    }
//
//    private void showStartInfo() {
//        this.mInfoView.setVisibility(View.VISIBLE);
//        this.mTextPercentage.setVisibility(View.GONE);
//        this.mTextUploading.setVisibility(View.GONE);
//        this.mDeviceNameView.setVisibility(View.GONE);
//        this.mConnectButton.setVisibility(View.VISIBLE);
//        this.mConnectButton.setEnabled(true);
//        this.mUploadButton.setEnabled(false);
//        this.mProgressBar.setVisibility(View.GONE);
//    }
//
//    private void connectStartInfo() {
//        this.mProgressBar.setVisibility(View.VISIBLE);
//        this.mTextPercentage.setText(R.string.dfu_status_connecting);
//        this.mDeviceNameView.setVisibility(View.GONE);
//        this.mDeviceNameView.setText(BuildConfig.FLAVOR);
//    }
//
//    /* JADX INFO: Access modifiers changed from: private */
//    public void onTransferCompleted() {
//        clearUI(true);
//        showToast(R.string.dfu_success);
//    }
//
//    public void onUploadCanceled() {
//        clearUI(false);
//        showToast(R.string.dfu_aborted);
//    }
//
//    @Override // com.secuyou.android_v22_pin_app.dfu.UploadCancelFragment.CancelFragmentListener
//    public void onCancelUpload() {
//        this.mTextUploading.setText(R.string.dfu_status_aborting);
//        this.mTextPercentage.setText((CharSequence) null);
//    }
//
//    /* JADX INFO: Access modifiers changed from: private */
//    public void showErrorMessage(String str) {
//        clearUI(false);
//        showToast("Upload failed: " + str);
//    }
//
//    private void clearUI(boolean z) {
//        this.mTextPercentage.setVisibility(View.INVISIBLE);
//        this.mTextUploading.setVisibility(View.INVISIBLE);
//        this.mConnectButton.setEnabled(true);
//        this.mUploadButton.setEnabled(false);
//        this.mUploadButton.setText(R.string.dfu_action_upload);
//        showStartInfo();
//        if (z) {
//            this.mSelectedDevice = null;
//            this.mDeviceNameView.setText(R.string.dfu_default_name);
//        }
//        this.mFilePath = null;
//        this.mFileStreamUri = null;
//        this.mInitFilePath = null;
//        this.mInitFileStreamUri = null;
//        this.mStatusOk = false;
//    }
//
//    private void showToast(int i) {
//        Toast.makeText(this, i, Toast.LENGTH_SHORT).show();
//    }
//
//    private void showToast(String str) {
//        Toast.makeText(this, str, Toast.LENGTH_SHORT).show();
//    }
//
//    private boolean isDfuServiceRunning() {
//        for (ActivityManager.RunningServiceInfo runningServiceInfo : ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE)).getRunningServices(Integer.MAX_VALUE)) {
//            if (DfuService.class.getName().equals(runningServiceInfo.service.getClassName())) {
//                return true;
//            }
//        }
//        return false;
//    }
//
//    public void startScan() {
//        this.BluetoothLeScannerCompat = BluetoothAdapter.getDefaultAdapter().getBluetoothLeScanner();
//        new ArrayList();
//        new ScanSettings.Builder().setScanMode(2).setReportDelay(1000L).build();
//        if (this.mIsScanning) {
//            return;
//        }
//        this.mHandler.postDelayed(this.myRunnable, 5000L);
//        this.mIsScanning = true;
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
//            this.BluetoothLeScannerCompat.startScan(this.mScanCallback);
//        }
//    }
//
//    /* JADX INFO: Access modifiers changed from: private */
//    public void stopScan() {
//        if (this.mIsScanning) {
//            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED) {
//                this.BluetoothLeScannerCompat.stopScan(this.mScanCallback);
//            }
//            this.mIsScanning = false;
//        }
//    }
//}
