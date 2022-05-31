package com.example.wificontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.example.wificontroller.databinding.ActivityMainBinding;
import com.example.wificontroller.databinding.ActivitySplashBinding;

import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class MainActivity extends AppCompatActivity {
    ActivityMainBinding binding;
    Socket socket;
    String SERVER_NODE = "http://192.168.1.14:3000";
    boolean isStart = false;
    int first_interval = 200;
    int normal_interval = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        InitSocketIO();

        SetupView();

    }

    private void SetupView() {

        binding.btnRight.setOnTouchListener(new RepeatListener(first_interval, normal_interval, v -> {
            socket.emit("direct", "right");
        }));

        binding.btnLeft.setOnTouchListener(new RepeatListener(first_interval, normal_interval, v -> {
            socket.emit("direct", "left");
        }));

        binding.btnDown.setOnTouchListener(new RepeatListener(first_interval, normal_interval, v -> {
            socket.emit("direct", "backward");
        }));

        binding.btnUp.setOnTouchListener(new RepeatListener(first_interval, normal_interval, v -> {
            socket.emit("direct", "forward");
        }));

        binding.btnStart.setOnClickListener(v -> {
            if(isStart){
                socket.emit("toggle", "stop");
                binding.ivStart.setVisibility(View.VISIBLE);
                binding.ivStop.setVisibility(View.GONE);
                isStart = false;
            }else{
                socket.emit("toggle", "start");
                binding.ivStart.setVisibility(View.GONE);
                binding.ivStop.setVisibility(View.VISIBLE);
                isStart = true;
            }
        });
    }

    private void InitSocketIO(){
        try {
            socket = IO.socket(SERVER_NODE);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        socket.on(Socket.EVENT_CONNECT_ERROR, onConnectError);
        socket.on("onPercentUpdate", onPercentUpdate);

        socket.connect();

    }

    private Emitter.Listener onConnectError = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    Toast.makeText(MainActivity.this, "Lỗi socket: " + args[0], Toast.LENGTH_SHORT).show();
                }
            });
        }
    };

    private Emitter.Listener onPercentUpdate = new Emitter.Listener() {
        @Override
        public void call(Object... args) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    String json = String.valueOf(args[0]);
                }
            });
        }
    };
}