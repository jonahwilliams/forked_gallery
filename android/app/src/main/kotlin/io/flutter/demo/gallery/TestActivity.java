// Copyright 2013 The Flutter Authors. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

package io.flutter.demo.gallery;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import io.flutter.Log;
import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.loader.FlutterLoader;
import io.flutter.plugin.common.BasicMessageChannel;
import io.flutter.plugin.common.BinaryCodec;
import io.flutter.plugin.common.JSONMethodCodec;
import io.flutter.plugin.common.MethodChannel;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class TestActivity extends FlutterActivity {
  static final String TAG = "Gallery";

  @Override
  protected void onCreate(@Nullable Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    final Intent launchIntent = getIntent();
    if ("com.google.intent.action.TEST_LOOP".equals(launchIntent.getAction())) {
      if (Build.VERSION.SDK_INT > 22) {
        requestPermissions(
            new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
      }
      final Uri logFileUri = launchIntent.getData();
      new Handler().postDelayed(new Runnable() {
        @Override
        public void run() {
          writeTimelineData(logFileUri);
        }
      }, 20000);
    }
  }

  protected void writeTimelineData(@Nullable Uri logFile) {
    if (logFile == null) {
      throw new IllegalArgumentException();
    }
    if (getFlutterEngine() == null) {
      Log.e(TAG, "Could not write timeline data - no engine.");
      return;
    }
    final BasicMessageChannel<ByteBuffer> channel =
        new BasicMessageChannel<>(getFlutterEngine().getDartExecutor(),
                                  "write_timeline", BinaryCodec.INSTANCE);
    channel.send(null, (ByteBuffer reply) -> {
      try {
        final FileDescriptor fd = getContentResolver()
                                      .openAssetFileDescriptor(logFile, "w")
                                      .getFileDescriptor();
        final FileOutputStream outputStream = new FileOutputStream(fd);
        outputStream.write(reply.array());
        outputStream.close();
      } catch (IOException ex) {
        Log.e(TAG, "Could not write timeline file", ex);
      }
      finish();
    });
  }
}
