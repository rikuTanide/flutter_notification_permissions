package com.vanethos.notification_permissions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.NotificationManagerCompat;
import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.PluginRegistry.Registrar;

public class NotificationPermissionsPlugin implements MethodChannel.MethodCallHandler {
  public static void registerWith(Registrar registrar) {
    final MethodChannel channel =
        new MethodChannel(registrar.messenger(), "notification_permissions");
    channel.setMethodCallHandler(new NotificationPermissionsPlugin(registrar));
  }

  private static final String PERMISSION_GRANTED = "granted";
  private static final String PERMISSION_DENIED = "denied";

  private final Context context;

  private NotificationPermissionsPlugin(Registrar registrar) {
    this.context = registrar.activity();
  }

  @Override
  public void onMethodCall(MethodCall call, MethodChannel.Result result) {
    if ("getNotificationPermissionStatus".equalsIgnoreCase(call.method)) {
      result.success(getNotificationPermissionStatus());
    } else if ("requestNotificationPermissions".equalsIgnoreCase(call.method)) {
      if (PERMISSION_DENIED.equalsIgnoreCase(getNotificationPermissionStatus())) {
        if (context instanceof Activity) {
          Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
          // カテゴリは設定しなくてもいいかも
          intent.addCategory(Intent.CATEGORY_DEFAULT);
          // Flagは好みで設定
          intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
          intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
          intent.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);

          // for Android 5-7
          intent.putExtra("app_package", BuildConfig.APPLICATION_ID);
          intent.putExtra("app_uid", context.getApplicationInfo().uid);

          // for Android O
          intent.putExtra("android.provider.extra.APP_PACKAGE", BuildConfig.APPLICATION_ID);

          context.startActivity(intent);

          result.success(null);
        } else {
          result.error(call.method, "context is not instance of Activity", null);
        }
      } else {
        result.success(null);
      }
    } else {
      result.notImplemented();
    }
  }

  private String getNotificationPermissionStatus() {
    return (NotificationManagerCompat.from(context).areNotificationsEnabled())
        ? PERMISSION_GRANTED
        : PERMISSION_DENIED;
  }
}
