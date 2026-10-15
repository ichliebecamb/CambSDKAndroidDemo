package com.camb.shell;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends Activity {
    static {
        // 暂时注释camb_media，UI调试
        // System.loadLibrary("camb_media");
        System.loadLibrary("camb_shell");
    }

    private View mPlayOverlayView;
    private TextureView mTextureView;
    private String mH264FilePath; // 最终给到SDK的真实文件路径

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button btnTest = findViewById(R.id.btn_test_h264);
        btnTest.setOnClickListener(v -> openPlayView());

        // 把assets里的video.h264复制到app私有files目录，只拷贝一次
        mH264FilePath = copyAssetToFilesDir("video.h264");
        Log.i("SHELL", "H264真实路径：" + mH264FilePath);
    }

    // 将assets内文件拷贝到应用私有files目录，返回绝对路径
    private String copyAssetToFilesDir(String assetFileName) {
        File destFile = new File(getFilesDir(), assetFileName);
        if (destFile.exists()) {
            // 文件已经拷贝过，直接返回路径，不用重复拷贝
            return destFile.getAbsolutePath();
        }
        try (InputStream is = getAssets().open(assetFileName);
             FileOutputStream os = new FileOutputStream(destFile)) {
            byte[] buf = new byte[8192];
            int len;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            Log.i("SHELL", "拷贝成功，生成文件：" + destFile.getAbsolutePath());
            return destFile.getAbsolutePath();
        } catch (Exception e) {
            Log.e("SHELL", "拷贝asset失败", e);
            return null;
        }
    }

    // 点击按钮，弹出全屏播放View
    private void openPlayView() {
        if(mH264FilePath == null) {
            Log.e("SHELL", "h264文件路径无效");
            return;
        }
        LayoutInflater inflater = LayoutInflater.from(this);
        mPlayOverlayView = inflater.inflate(R.layout.play_view, null);
        FrameLayout root = findViewById(android.R.id.content);
        root.addView(mPlayOverlayView);

        mTextureView = mPlayOverlayView.findViewById(R.id.texture_view);
        Button closeBtn = mPlayOverlayView.findViewById(R.id.btn_close);
        closeBtn.setOnClickListener(v -> closePlayView());

        mTextureView.setSurfaceTextureListener(new TextureView.SurfaceTextureListener() {
            @Override
            public void onSurfaceTextureAvailable(android.graphics.SurfaceTexture surfaceTexture, int width, int height) {
                Log.i("SHELL", "Surface ready, pass path and surface to native SDK");
                android.view.Surface surface = new android.view.Surface(surfaceTexture);
                // 【重点：把文件路径字符串 + Surface一起下发给SDK】
                nativeSetSurfaceAndFilePath(surface, mH264FilePath);
            }
            @Override
            public void onSurfaceTextureSizeChanged(android.graphics.SurfaceTexture surface, int w, int h) {}
            @Override
            public boolean onSurfaceTextureDestroyed(android.graphics.SurfaceTexture surface) {
                nativeStopPlayer();
                return true;
            }
            @Override
            public void onSurfaceTextureUpdated(android.graphics.SurfaceTexture surface) {}
        });
    }

    // 关闭播放弹窗
    private void closePlayView() {
        if(mPlayOverlayView != null) {
            FrameLayout root = findViewById(android.R.id.content);
            root.removeView(mPlayOverlayView);
            mPlayOverlayView = null;
            mTextureView = null;
        }
    }

    // JNI方法：增加文件路径字符串参数
    public native void nativeSetSurfaceAndFilePath(android.view.Surface surface, String filePath);
    public native void nativeStopPlayer();
}
