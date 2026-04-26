package com.genspark.sunmit2webprintdemo;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.Toast;

import com.genspark.sunmit2webprintdemo.printer.SunmiPrinterManager;
import com.genspark.sunmit2webprintdemo.web.PrintJsBridge;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private SunmiPrinterManager printerManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webView);
        Button btnReload = findViewById(R.id.btnReload);
        Button btnPrintTest = findViewById(R.id.btnPrintTest);

        printerManager = new SunmiPrinterManager(this);
        printerManager.bind();

        setupWebView();
        webView.loadUrl(AppConfig.DEFAULT_URL);

        btnReload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                webView.reload();
            }
        });

        btnPrintTest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean ok = printerManager.printTestReceipt("Sunmi T2 Demo", AppConfig.DEFAULT_URL);
                Toast.makeText(MainActivity.this, ok ? "已送出測試列印" : "印表機尚未連線", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @SuppressLint({"SetJavaScriptEnabled", "AddJavascriptInterface"})
    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setLoadsImagesAutomatically(true);
        settings.setAllowFileAccess(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);

        webView.addJavascriptInterface(new PrintJsBridge(this, printerManager, webView), AppConfig.JS_INTERFACE_NAME);
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                injectAssetScript("inject-bridge.js");
                injectAssetScript("site-autoprint-adapter.js");
            }
        });
    }

    private void injectAssetScript(String fileName) {
        String js = loadAssetText(fileName);
        if (js != null && !js.trim().isEmpty()) {
            webView.evaluateJavascript(js, null);
        }
    }

    private String loadAssetText(String fileName) {
        InputStream inputStream = null;
        BufferedReader reader = null;
        try {
            inputStream = getAssets().open(fileName);
            reader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder builder = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line).append('\n');
            }
            return builder.toString();
        } catch (IOException e) {
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException ignored) {
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {
                }
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (printerManager != null) {
            printerManager.unbind();
        }
        if (webView != null) {
            webView.removeJavascriptInterface(AppConfig.JS_INTERFACE_NAME);
            webView.destroy();
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        if (webView != null && webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}
