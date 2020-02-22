package com.trustedoffers.vivoipl2020;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.webkit.DownloadListener;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebIconDatabase;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.ads.MobileAds;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {
    private InterstitialAd interstitialAd;
    private AdView adView;
    private WebView browser;
    public String baseurl = "vivoiplschedule.com";
    public String homepage="https://vivoiplschedule.com/";

    private SwipeRefreshLayout myswiperfreshlayout;
    private ViewTreeObserver.OnScrollChangedListener mOnScrollchangedListener;
    private RelativeLayout errorlayout;
    private TextView errtext;
    private int loadnum = 0;
    private String appname;
    int launchcount=0;
    Bitmap favbit;
    //for file loaders

    private static final String TAG = MainActivity.class.getSimpleName();
    private String mCM;
    private ValueCallback<Uri> mUM;
    private ValueCallback<Uri[]> mUMA;
    private final static int FCR=1;
    private boolean multiple_files = false;

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){

            if(Build.VERSION.SDK_INT >= 21){
                Uri[] results = null;
                //checking if response is positive
                if(resultCode== Activity.RESULT_OK){
                    if(requestCode == FCR){
                        if(null == mUMA){
                            return;
                        }
                        if(intent == null || intent.getData() == null){
                            if(mCM != null){
                                results = new Uri[]{Uri.parse(mCM)};
                            }
                        }else{
                            String dataString = intent.getDataString();
                            if(dataString != null){
                                results = new Uri[]{Uri.parse(dataString)};
                            } else {
                                if(multiple_files) {
                                    if (intent.getClipData() != null) {
                                        final int numSelectedFiles = intent.getClipData().getItemCount();
                                        results = new Uri[numSelectedFiles];
                                        for (int i = 0; i < numSelectedFiles; i++) {
                                            results[i] = intent.getClipData().getItemAt(i).getUri();
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                mUMA.onReceiveValue(results);
                mUMA = null;
            }else {
                if (requestCode == FCR) {
                    if (null == mUM) return;
                    Uri result2 = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                    mUM.onReceiveValue(result2);
                    mUM = null;
                }
            }

        super.onActivityResult(requestCode, resultCode, intent);
    }//----



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appname = getResources().getString(R.string.app_name);
        errorlayout = (RelativeLayout)findViewById(R.id.errorlayout);
        errtext = (TextView)findViewById(R.id.errortext);

        int trial_limit = 10;
        if(launchcount<=trial_limit){
            //  tst("Trial remaining: "+(trial_limit-launchcount)+" times",1);
        }else {
            //   homepage = "https://fiverr.com/dhirrr/convert-your-website-into-responsive-app";  //TODO: update this link
            //   tst("trial expired. App launched "+launchcount + " times", 1);
        }

        //------------

        //Button Section Button
        buttonButtons();

        //swiperefresh start
        myswiperfreshlayout = (SwipeRefreshLayout)findViewById(R.id.swiperefreshlayout);
        myswiperfreshlayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                browser.reload();
            }
        });




        //browser functionality
        browser = findViewById(R.id.browserview);
        adView=findViewById(R.id.avBannerHomeId);
        setSharedPref(0);
        //Banner Ad
        MobileAds.initialize(this, String.valueOf(R.string.admob_ad_id));
        AdRequest adRequest=new AdRequest.Builder().build();
        adView.loadAd(adRequest);
        //InterstitialAd Implimentation
        interstitialAd=new InterstitialAd(this);
        interstitialAd.setAdUnitId(getString(R.string.industrial_ad_id));
        interstitialAd.loadAd(new AdRequest.Builder().build());
        WebSettings ezwebset = browser.getSettings();
        //ezwebset.setSupportZoom(true);
        //ezwebset.setBuiltInZoomControls(true);
        //ezwebset.setDisplayZoomControls(false);
        ezwebset.setUseWideViewPort(true);
        ezwebset.setJavaScriptEnabled(true);
        ezwebset.setDomStorageEnabled(true);
        ezwebset.setAllowContentAccess(true);
        ezwebset.enableSmoothTransition();
        ezwebset.setAllowFileAccess(true);
        ezwebset.setLoadsImagesAutomatically(true);
        WebIconDatabase.getInstance().open(getDir("icons",MODE_PRIVATE).getPath());
        favbit = BitmapFactory.decodeResource(getBaseContext().getResources(), R.drawable.round_logo);

        browser.setWebChromeClient(new WebChromeClient(){

            @Override
            public void onReceivedIcon(WebView view, Bitmap icon) {
                favbit = icon;
                super.onReceivedIcon(view, icon);
                // tst("icon tcvd",1);
            }

            @Override
            public void onReceivedTitle(WebView view, String title) {
                super.onReceivedTitle(view, title);
            }

            /*
             * openFileChooser is not a public Android API and has never been part of the SDK.
             */
            //handling input[type="file"] requests for android API 16+
            @SuppressLint("ObsoleteSdkInt")
            @SuppressWarnings("unused")
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUM = uploadMsg;
                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("*/*");
                if (multiple_files && Build.VERSION.SDK_INT >= 18) {
                    i.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                startActivityForResult(Intent.createChooser(i, "File Chooser"), FCR);
            }

            //handling input[type="file"] requests for android API 21+
            @SuppressLint("InlinedApi")
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                if (file_permission()) {
                    String[] perms = {Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.CAMERA};

                    //checking for storage permission to write images for upload
                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED && ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, perms, FCR);

                        //checking for WRITE_EXTERNAL_STORAGE permission
                    } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, FCR);

                        //checking for CAMERA permissions
                    } else if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, FCR);
                    }
                    if (mUMA != null) {
                        mUMA.onReceiveValue(null);
                    }
                    mUMA = filePathCallback;
                    Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    if (takePictureIntent.resolveActivity(MainActivity.this.getPackageManager()) != null) {
                        File photoFile = null;
                        try {
                            photoFile = createImageFile();
                            takePictureIntent.putExtra("PhotoPath", mCM);
                        } catch (IOException ex) {
                            Log.e(TAG, "Image file creation failed", ex);
                        }
                        if (photoFile != null) {
                            mCM = "file:" + photoFile.getAbsolutePath();
                            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));
                        } else {
                            takePictureIntent = null;
                        }
                    }
                    Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
                    contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
                    contentSelectionIntent.setType("*/*");
                    if (multiple_files) {
                        contentSelectionIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    }
                    Intent[] intentArray;
                    if (takePictureIntent != null) {
                        intentArray = new Intent[]{takePictureIntent};
                    } else {
                        intentArray = new Intent[0];
                    }

                    Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
                    chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
                    chooserIntent.putExtra(Intent.EXTRA_TITLE, "File Chooser");
                    chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);
                    startActivityForResult(chooserIntent, FCR);
                    return true;
                }else{
                    return false;
                }
            }//file uploader end

            // for fullscreen


            private View mCustomView;
            private CustomViewCallback mCustomViewCallback;
            private int mOriginalOrientation;
            private int mOriginalSystemUiVisibility;


            public Bitmap getDefaultVideoPoster()
            {
                if (mCustomView == null) {
                    return null;
                }
                return BitmapFactory.decodeResource(getApplicationContext().getResources(), 2130837573);
            }

            public void onHideCustomView()
            {
                ((FrameLayout)getWindow().getDecorView()).removeView(this.mCustomView);
                this.mCustomView = null;
                getWindow().getDecorView().setSystemUiVisibility(this.mOriginalSystemUiVisibility);
                setRequestedOrientation(this.mOriginalOrientation);
                this.mCustomViewCallback.onCustomViewHidden();
                this.mCustomViewCallback = null;
            }

            public void onShowCustomView(View paramView, CustomViewCallback paramCustomViewCallback)
            {
                if (this.mCustomView != null)
                {
                    onHideCustomView();
                    return;
                }
                this.mCustomView = paramView;
                this.mOriginalSystemUiVisibility = getWindow().getDecorView().getSystemUiVisibility();
                this.mOriginalOrientation = getRequestedOrientation();
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
                this.mCustomViewCallback = paramCustomViewCallback;
                mCustomView.setBackgroundColor(Color.parseColor("#000000"));
                ((FrameLayout)getWindow().getDecorView()).addView(this.mCustomView, new FrameLayout.LayoutParams(-1, -1));
                getWindow().getDecorView().setSystemUiVisibility(3846);
            }

            //---


        });


        browser.setWebViewClient(new ezwebviewclient(){
            public void onReceivedError(WebView view, int errcode, String desc, String failurl){
                myswiperfreshlayout.setRefreshing(false);
                errorlayout.setVisibility(View.VISIBLE);
                errtext.setText("error "+errcode+". " +desc);
                loadnum++;

            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                loadnum = 0;
                myswiperfreshlayout.setRefreshing(true);
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if(loadnum==0) {
                    errorlayout.setVisibility(View.GONE);
                }

                //  refreshNavList(true);
                myswiperfreshlayout.setRefreshing(false);
                super.onPageFinished(view, url);
            }



        }); //webchromeclient ends

        browser.setDownloadListener(new DownloadListener() {
            public void onDownloadStart(String durl, String userAgent,
                                        String contentDisposition, String mimetype,
                                        long contentLength) {

                Uri uri = Uri.parse(durl);
                Intent intent = new Intent(Intent.ACTION_VIEW,uri);
                startActivity(intent);
            }
        });//download listener end

        browser.loadUrl(homepage);
    }

    /**
     * Buttom Menu Buttons
     */
    private void buttonButtons() {

        ImageButton backbutton,homebutton,forwardbutton,sharebutton,refreshbutton;

        //find id
        backbutton = findViewById(R.id.back_button_id);
        homebutton = findViewById(R.id.home_button_id);
        forwardbutton = findViewById(R.id.forward_button_id);
        sharebutton=findViewById(R.id.share_button_id);
        refreshbutton=findViewById(R.id.refresh_button_id);



        //back button
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(browser.canGoBack()){
                    SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.AppPra, Context.MODE_PRIVATE);
                    int clickCount = sharedPreferences.getInt(String.valueOf(SharedPref.count), 0);
                    clickCount=clickCount+1;
                    int constantClick= ConstantVariable.AdPerClick;
                    if (clickCount==constantClick) {
                        if (interstitialAd.isLoaded())
                        {
                            setSharedPref(0);
                            interstitialAd.show();

                            final int finalClickCount = clickCount;
                            interstitialAd.setAdListener(new AdListener() {
                                @Override
                                public void onAdClosed() {
                                    interstitialAd.loadAd(new AdRequest.Builder().build());
                                    browser.goBack();

                                }
                            });
                        }
                        else {
                            setSharedPref(0);
                        }


                    } else {
                        setSharedPref(clickCount);

                    }

                }
                else {
                    exitConfirmation();
                }
            }
        });
        //forwardbutton
        forwardbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.AppPra, Context.MODE_PRIVATE);
                int clickCount = sharedPreferences.getInt(String.valueOf(SharedPref.count), 0);
                clickCount=clickCount+1;
                int constantClick= ConstantVariable.AdPerClick;
                if (clickCount==constantClick) {
                    if (interstitialAd.isLoaded())
                    {
                        setSharedPref(0);
                        interstitialAd.show();

                        final int finalClickCount = clickCount;
                        interstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                interstitialAd.loadAd(new AdRequest.Builder().build());
                                if(browser.canGoForward()){
                                    browser.goForward();
                                }
                                else {
                                    Toast.makeText(getApplicationContext(),"No Page Available", Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                    else {
                        setSharedPref(0);
                    }


                } else {
                    setSharedPref(clickCount);
                    if(browser.canGoForward()){
                        browser.goForward();
                    }
                    else {
                        Toast.makeText(getApplicationContext(),"No Page Available", Toast.LENGTH_SHORT).show();
                    }
                }

            }
        });
        //home button
        homebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.AppPra, Context.MODE_PRIVATE);
                int clickCount = sharedPreferences.getInt(String.valueOf(SharedPref.count), 0);
                clickCount=clickCount+1;
                int constantClick= ConstantVariable.AdPerClick;
                if (clickCount==constantClick) {
                    if (interstitialAd.isLoaded())
                    {
                        setSharedPref(0);
                        interstitialAd.show();

                        final int finalClickCount = clickCount;
                        interstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                interstitialAd.loadAd(new AdRequest.Builder().build());
                                browser.loadUrl(homepage);
                            }
                        });
                    }
                    else {
                        setSharedPref(0);
                    }


                } else {
                    setSharedPref(clickCount);
                    browser.loadUrl(homepage);
                }
            }
        });

        //sharebutton
        sharebutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.AppPra, Context.MODE_PRIVATE);
                int clickCount = sharedPreferences.getInt(String.valueOf(SharedPref.count), 0);
                clickCount=clickCount+1;
                int constantClick= ConstantVariable.AdPerClick;
                if (clickCount==constantClick) {
                    if (interstitialAd.isLoaded())
                    {
                        setSharedPref(0);
                        interstitialAd.show();

                        final int finalClickCount = clickCount;
                        interstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                interstitialAd.loadAd(new AdRequest.Builder().build());
                                shareOperation();
                            }
                        });
                    }
                    else {
                        setSharedPref(0);
                    }


                } else {
                    setSharedPref(clickCount);
                    shareOperation();
                }


            }
        });
        //refreshbutton
        refreshbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.AppPra, Context.MODE_PRIVATE);
                int clickCount = sharedPreferences.getInt(String.valueOf(SharedPref.count), 0);
                clickCount=clickCount+1;
                int constantClick= ConstantVariable.AdPerClick;
                if (clickCount==constantClick) {
                    if (interstitialAd.isLoaded())
                    {
                        setSharedPref(0);
                        interstitialAd.show();

                        final int finalClickCount = clickCount;
                        interstitialAd.setAdListener(new AdListener() {
                            @Override
                            public void onAdClosed() {
                                interstitialAd.loadAd(new AdRequest.Builder().build());
                                browser.reload();
                            }
                        });
                    }
                    else {
                        setSharedPref(0);
                    }


                } else {
                    setSharedPref(clickCount);
                    browser.reload();
                }

            }
        });

    }

    private void shareOperation() {
        Intent a = new Intent(Intent.ACTION_SEND);

        //this is to get the app link in the playstore without launching your app.
        final String appPackageName = getApplicationContext().getPackageName();
        String strAppLink = "";

        try
        {
            strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
        }
        catch (android.content.ActivityNotFoundException anfe)
        {
            strAppLink = "https://play.google.com/store/apps/details?id=" + appPackageName;
        }
        // this is the sharing part
        a.setType("text/link");
        String shareBody = "Hey! Download The App Free And Enjoy Live Matches" +
                "\n"+""+strAppLink;
        String shareSub = "IPL20";
        a.putExtra(Intent.EXTRA_SUBJECT, shareSub);
        a.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(a, "Share Using"));
    }

    private void setSharedPref(int clickCount) {
        SharedPreferences sharedPreferences = getSharedPreferences(SharedPref.AppPra, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(String.valueOf(SharedPref.count),clickCount);
        editor.apply();
    }
    @Override
    protected void onStart() {
        myswiperfreshlayout.getViewTreeObserver().addOnScrollChangedListener(mOnScrollchangedListener =
                new ViewTreeObserver.OnScrollChangedListener(){
                    @Override
                    public void onScrollChanged(){
                        if ( !myswiperfreshlayout.isRefreshing()){
                            if (browser.getScrollY() == 0) {
                                myswiperfreshlayout.setEnabled(true);
                            } else if (browser.getScrollY() > 10) {
                                myswiperfreshlayout.setEnabled(false);
                            }
                        }
                    }
                });


        super.onStart();
    }

    @Override
    protected void onStop() {
        myswiperfreshlayout.getViewTreeObserver().removeOnScrollChangedListener( mOnScrollchangedListener);
        super.onStop();
    }

//    @Override
//    protected void onNewIntent(Intent intent) {
//        Intent brint= intent;
//        //'''
//        if (brint.getAction()=="deeplink"){
//            String burl = brint.getStringExtra("burl");
//            if (burl.contains(baseurl)){
//                if (!burl.contains(":")){
//                    browser.loadUrl("https://"+burl);
//                }else {
//                    browser.loadUrl(burl);
//                }
//            }else{
//                String eurl = burl;
//                if(!burl.contains(":")){
//                    eurl = "https://"+burl;
//                }
//                Intent di = new Intent(Intent.ACTION_VIEW);
//                di.setData(Uri.parse(eurl));
//                startActivity(di);
//            }
//        }else {
//            browser.loadUrl(homepage);
//        }
//
//        super.onNewIntent(intent);
//    }

    @Override
    public void onBackPressed() {

        if (browser.canGoBack()) {
            browser.goBack();
        } else {
            exitConfirmation();
        }

    }

    /**
     * Exit Confirmation
     */
    private void exitConfirmation(){
        new AlertDialog.Builder(this)
                .setTitle("Exit " + appname)
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        MainActivity.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //canceled
                    }
                })
                .show();
    }


    public void tryagain(View v){
        browser.reload();
    }

    /**
     *
     */
    private class ezwebviewclient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String link){

            String url;
            url = link;
            Log.wtf("balbal",link);
            if(url.contains(baseurl)) {
                return false;
            }else{
                //Toast.makeText(getBaseContext(), "opening external link:"+url, Toast.LENGTH_LONG).show();
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                startActivity(i);
                return false;
            }
        }



    }

    @Override
    protected void onDestroy() {

        // appDataBaseAdapter.close();
        super.onDestroy();
    }
    
    public void tst(String t, int dur){

        if (dur==1){
            Toast.makeText(getBaseContext(),t, Toast.LENGTH_LONG).show();}else{
            Toast.makeText(getBaseContext(),t, Toast.LENGTH_SHORT).show();}
    }


    //for uploader
    public boolean file_permission(){
        if(Build.VERSION.SDK_INT >=23 && (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA}, 1);
            return false;
        }else{
            return true;
        }
    }

    //creating new image file here
    //creating new image file here
    private File createImageFile() throws IOException {
        @SuppressLint("SimpleDateFormat") String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "img_"+timeStamp+"_";
        File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        return File.createTempFile(imageFileName,".jpg",storageDir);
    }
}
