package jp.ac.jec.cm0128.recommap;

import android.content.Context;
import android.location.Geocoder;
import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.CheckResult;
import androidx.annotation.NonNull;
import androidx.annotation.WorkerThread;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public final class TestData {
    /**
     * Log出力用のタグ
     */
    private static final String TAG = TestData.class.getSimpleName();
    /**
     * 中野駅 の位置情報.,
     */
    public static final LatLng NAKANO_STATION_LATLNG = new LatLng(35.6967775, 139.66508);

    /**
     * 日本電子専門学校　本館の位置情報.
     */
    public static final LatLng JEC_LATLNG = new LatLng(35.698528899254484, 139.69809016114817);

    /**
     * TestActivity.javaでのみ利用: デバッグで使用するお店情報リスト
     */
    public static final TestItem[] TEST_ITEMS = {
            new TestItem("ラーメン二郎", "こってこてのラーメン。完食は難しい！", 35.696346, 139.698336, "ラーメン"),
            new TestItem("らーめん　風来居", "スープにとろみのある塩とんこつ", 35.695339, 139.696587, "ラーメン"),
            new TestItem("麺屋 翔 本店", "客でにぎわうシンプルなラーメン店。鳥ベースのスープのボリュームのあるラーメンを提供。", 35.69673973325111, 139.6966075860729, "ラーメン"),
            new TestItem("セブンイレブン 北新宿１丁目店", "接客が素晴らしい。", 35.697932218462974, 139.69649148583713, "コンビニ"),
            new TestItem("ファミリーマート 北新宿店", "商品が素晴らしい。", 35.6985418063144, 139.69697861864998, "コンビニ"),
            new TestItem("ファミリーマート 大久保駅南口店", "学校から近くて便利。", 35.699811703627226, 139.6976939570079, "コンビニ"),
            new TestItem("日本電子専門学校 本館", "本館", 35.698528899254484, 139.69809016114817, "日本電子 建物"),
            new TestItem("日本電子専門学校 7号館", "7号館", 35.6989031546359, 139.69661038522688, "日本電子 建物"),
            new TestItem("日本電子専門学校 13号館", "13号館", 35.70000417613386, 139.69817780964502, "日本電子 建物"),
    };

    private TestData() {
    }

    /**
     * 中野駅から日本電子本館までの範囲でランダムな位置情報を作成し、テストデータのリストを作成する。(処理に時間がかかるので、メインスレッド上で実行しないこと)
     * <pre>
     * Activityからの呼び出しを想定した使用例
     *                     Executors.newSingleThreadExecutor().execute(() -> {
     *                         // 別スレッド (WorkerThread)で実行
     *                         var testItems = TestData.createTestItems(this, 50);
     *                         runOnUiThread(() -> {
     *                             // UIの処理をここで行う
     *                         });
     *                     });
     * </pre>
     *
     * @param context コンテキスト
     * @param size    作成するアイテム数
     * @return {@link TestItem}のリスト
     */
    @NonNull
    @CheckResult
    @WorkerThread
    public static List<TestItem> createTestItems(@NonNull final Context context, final int size) {
        Log.d(TAG, "createTestItems: IN");
        var testItems = new ArrayList<TestItem>();
        for (int i = 0; i < size; i++) {
            var longitude = new Random().doubles(1, NAKANO_STATION_LATLNG.longitude, JEC_LATLNG.longitude).toArray()[0];
            var latitude = new Random().doubles(1, NAKANO_STATION_LATLNG.latitude, JEC_LATLNG.latitude).toArray()[0];
            var addressLine = getAddressLine(context, latitude, longitude);
            var testItem = new TestItem("ランダムな指定", addressLine, latitude, longitude, "ランダム");
            Log.d(TAG, testItem.toString());
            testItems.add(testItem);
        }
        Log.d(TAG, "createTestItems: OUT");
        return testItems;
    }

    /**
     * 指定した緯度 経度の情報から住所を取得する.(処理に時間がかかるので、メインスレッド上で実行しないこと)
     * 注意事項：　より正確な現在地情報を取得したい場合はGoogle Place APIなどを使う必要がある.
     *
     * @param context   コンテキスト
     * @param latitude  緯度
     * @param longitude 経度
     * @return 住所 ex) 日本、〒160-0023 東京都新宿区西新宿８丁目２−２５
     */
    @WorkerThread
    @NonNull
    @CheckResult
    private static String getAddressLine(@NonNull final Context context, final double latitude, final double longitude) {
        var errorMessage = "不明";
        var geocoder = new Geocoder(context, Locale.getDefault());
        try {
            // getFromLocationメソッドの内部で通信処理が行われるため、ネットワーク状況が悪いとIOExceptionがthrowされる.
            var addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && !addressList.isEmpty()) {
                var address = addressList.get(0);
                Log.d(TAG, address.toString());
                var addressLine = address.getAddressLine(0); // "日本、〒160-0023 東京都新宿区西新宿８丁目２−２５"  (nullの可能性あり)
                return (!TextUtils.isEmpty(addressLine)) ? addressLine : errorMessage;
            } else {
                return errorMessage;
            }
        } catch (IllegalArgumentException | IOException e) {
            Log.e(TAG, "error: ", e);
            return errorMessage;
        }
    }

    /**
     * 指定した緯度 経度の情報から地域名を取得する.(処理に時間がかかるので、メインスレッド上で実行しないこと)
     * 注意事項：　より正確な現在地情報を取得したい場合はGoogle Place APIなどを使う必要がある.
     *
     * @param context   コンテキスト
     * @param latitude  緯度
     * @param longitude 経度
     * @return 地域名 ex)新宿
     */
    @WorkerThread
    @NonNull
    @CheckResult
    private static String getLocalityName(@NonNull final Context context, final double latitude, final double longitude) {
        var errorMessage = "不明";
        var geocoder = new Geocoder(context, Locale.getDefault());
        try {
            // getFromLocationメソッドの内部で通信処理が行われるため、ネットワーク状況が悪いとIOExceptionがthrowされる.
            var addressList = geocoder.getFromLocation(latitude, longitude, 1);
            if (addressList != null && !addressList.isEmpty()) {
                var address = addressList.get(0);
                Log.d(TAG, address.toString());
                var locality = address.getLocality(); // 新宿  (nullの可能性あり)
                return (!TextUtils.isEmpty(locality)) ? locality : errorMessage;
            } else {
                return errorMessage;
            }
        } catch (IllegalArgumentException | IOException e) {
            Log.e(TAG, "error: ", e);
            return errorMessage;
        }
    }

    public record TestItem(@NonNull String name, @NonNull String comment,
                           double latitude, double longitude, @NonNull String category) {
    }
}
