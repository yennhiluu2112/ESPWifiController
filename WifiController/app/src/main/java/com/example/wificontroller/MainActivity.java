package com.example.wificontroller;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
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
    String SERVER_NODE = "http://172.20.10.4:5000";
    boolean isStart = false;
    int first_interval = 120;
    int normal_interval = 120;
    boolean onRight, onLeft, onUp, onDown = false;
    double max_height = 13;
    double ratio = 288.75;

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

//        binding.btnRight.setOnTouchListener(new RepeatListener(first_interval, normal_interval, v -> {
//            socket.emit("direct", "right");
//        }));

        binding.btnRight.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    onRight = true;

                    socket.emit("direct", "right");

                    return true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    onRight =false;

                    if(onUp){
                        socket.emit("direct", "forward");
                    }else if(onDown){
                        socket.emit("direct", "backward");
                    }else {
                        socket.emit("direct", "stop");
                    }
                    return true;
                }
                return false;
            }
        });

        binding.btnLeft.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    onLeft = true;
                    socket.emit("direct", "left");
                    return true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    onLeft = false;
                    if(onUp){
                        socket.emit("direct", "forward");
                    }else if(onDown){
                        socket.emit("direct", "backward");
                    }else {
                        socket.emit("direct", "stop");
                    }
                    return true;
                }
                return false;
            }
        });

        binding.btnUp.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    onUp = true;
                    socket.emit("direct", "forward");
                    return true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    onUp = false;
                    if(onRight){
                        socket.emit("direct", "right");
                    }else if(onLeft){
                        socket.emit("direct", "left");
                    }else {
                        socket.emit("direct", "stop");
                    }
                    return true;
                }
                return false;
            }
        });

        binding.btnDown.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN){
                    onDown = true;
                    socket.emit("direct", "backward");
                    return true;
                }
                else if(event.getAction() == MotionEvent.ACTION_UP){
                    onDown = false;
                    if(onRight){
                        socket.emit("direct", "right");
                    }else if(onLeft){
                        socket.emit("direct", "left");
                    }else {
                        socket.emit("direct", "stop");
                    }
                    return true;
                }
                return false;
            }
        });

//        binding.btnLeft.setOnTouchListener(new RepeatListener(first_interval, normal_interval, v -> {
//            socket.emit("direct", "left");
//        }));
//
//        binding.btnDown.setOnTouchListener(new RepeatListener(first_interval, normal_interval, v -> {
//            socket.emit("direct", "backward");
//        }));
//
//        binding.btnUp.setOnTouchListener(new RepeatListener(first_interval, normal_interval, v -> {
//            socket.emit("direct", "forward");
//        }));

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
                    double val = Double.parseDouble(String.valueOf(args[0]));

                    if(val > max_height) {
                        binding.tvPercent.setText("Thể tích còn lại: 100%");
                    } else if(val <= max_height && val >= 1){
                        double remain_volume = val * ratio;
                        double max_volume = max_height * ratio;

                        double percent = (remain_volume/max_volume)*100;
                        binding.tvPercent.setText("Thể tích còn lại: " + Math.round(percent) +"%");
                    }

                }
            });
        }
    };
}