package fr.raraph84.raraboxpanel;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.webkit.*;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class MainActivity extends AppCompatActivity {

    private WebView webview;
    private SwipeRefreshLayout swipeRefreshLayout;
    private ProgressBar loader;
    private TextView infoText;
    private Button refreshButton;

    @SuppressLint("SetJavaScriptEnabled")
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        webview = findViewById(R.id.webview);
        swipeRefreshLayout = findViewById(R.id.swipe);
        loader = findViewById(R.id.loader);
        infoText = findViewById(R.id.info);
        refreshButton = findViewById(R.id.refresh);

        swipeRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.dark_blue, null));
        swipeRefreshLayout.setOnRefreshListener(() -> {
            swipeRefreshLayout.setRefreshing(true);
            new Handler(getMainLooper()).postDelayed(() -> {
                swipeRefreshLayout.setRefreshing(false);
                webview.reload();
            }, 500);
        });

        webview.getSettings().setJavaScriptEnabled(true);
        webview.getSettings().setDomStorageEnabled(true);
        webview.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.d("WebView", consoleMessage.message());
                return true;
            }
        });
        webview.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, WebResourceRequest request) {
                if (request.getUrl().toString().startsWith(view.getUrl())) view.loadUrl(request.getUrl().toString());
                else startActivity(new Intent(Intent.ACTION_VIEW, request.getUrl()));
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                swipeRefreshLayout.setVisibility(WebView.VISIBLE);
            }
        });

        refreshButton.setOnClickListener((view) -> search());

        search();
    }

    @Override
    public void onBackPressed() {
        if (webview.canGoBack()) webview.goBack();
        else super.onBackPressed();
    }

    public void search() {

        infoText.setText(R.string.searching);
        refreshButton.setVisibility(View.INVISIBLE);
        loader.setVisibility(View.VISIBLE);

        new Thread(() -> {

            List<NetworkInterface> networkInterfaces;
            try {
                networkInterfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            } catch (SocketException exception) {
                throw new RuntimeException(exception);
            }

            List<String> ips = new ArrayList<>();
            ips.add("panel.lan");
            for (NetworkInterface networkInterface : networkInterfaces)
                for (InterfaceAddress address : networkInterface.getInterfaceAddresses())
                    if (!address.getAddress().isLoopbackAddress() && !Objects.requireNonNull(address.getAddress().getHostAddress()).contains(":"))
                        for (int i = 0; i < 255; i++)
                            ips.add(address.getAddress().getHostAddress().substring(0, address.getAddress().getHostAddress().lastIndexOf(".") + 1) + i);

            ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(25);
            AtomicBoolean found = new AtomicBoolean(false);
            for (String ip : ips) {
                pool.submit(() -> {
                    if (found.get()) return;
                    try {
                        InetAddress address = InetAddress.getByName(ip);
                        if (!address.isReachable(1000)) return;
                    } catch (Exception exception) {
                        throw new RuntimeException(exception);
                    }
                    HttpRequest request = new HttpRequest("http://" + ip);
                    try {
                        request.send();
                    } catch (Exception exception) {
                        return;
                    }
                    if (found.get() || request.getResponseCode() != 200 || !request.getResponse().contains("Raspberry Pi Hotspot"))
                        return;
                    found.set(true);
                    new Handler(getMainLooper()).post(() -> webview.loadUrl("http://" + ip));
                });
            }

            pool.shutdown();

            while (true) {
                try {
                    if (pool.awaitTermination(Long.MAX_VALUE, TimeUnit.MICROSECONDS)) break;
                } catch (InterruptedException exception) {
                    throw new RuntimeException(exception);
                }
            }

            if (!found.get()) {
                new Handler(getMainLooper()).post(() -> {
                    infoText.setText(R.string.not_found);
                    refreshButton.setVisibility(View.VISIBLE);
                    loader.setVisibility(View.INVISIBLE);
                });
            }

        }).start();
    }
}
