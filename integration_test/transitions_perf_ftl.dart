// Copyright 2019 The Flutter team. All rights reserved.
// Use of this source code is governed by a BSD-style license that can be
// found in the LICENSE file.

import 'dart:async';
import 'dart:convert';
import 'dart:typed_data';
import 'dart:ui';

import 'package:flutter/material.dart';
import 'package:flutter_driver/flutter_driver.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:gallery/main.dart' show GalleryApp;
import 'package:integration_test/integration_test.dart';
import 'package:vm_service/vm_service.dart' as vm;

Completer<Object> pendingTimelineData = Completer<Object>();

Future<void> _handleWriteTimelineMessage(ByteData? data, PlatformMessageResponseCallback? callback) async {
  await pendingTimelineData.future.then((data) {
    callback!((utf8.encode(json.encode(data)) as Uint8List).buffer.asByteData());
  });
}

void main([List<String> args = const <String>[]]) {
  final IntegrationTestWidgetsFlutterBinding binding = IntegrationTestWidgetsFlutterBinding.ensureInitialized();
  binding.framePolicy = LiveTestWidgetsFlutterBindingFramePolicy.fullyLive;
  channelBuffers.setListener('write_timeline', _handleWriteTimelineMessage);

  testWidgets('Gallery Performance test', (tester) async {
    final vm.Timeline result = await binding.traceTimeline(() async {
      runApp(const GalleryApp(isTestMode: true));

      // Trigger a frame.
      await tester.pumpAndSettle();
    });

    var timelineJson = result.toJson();
    var summary = TimelineSummary.summarize(Timeline.fromJson(timelineJson));

    pendingTimelineData.complete(summary.summaryJson);
  });
}
