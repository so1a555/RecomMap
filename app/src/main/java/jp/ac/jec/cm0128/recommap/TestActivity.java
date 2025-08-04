package jp.ac.jec.cm0128.recommap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.chip.ChipGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 地図上に保存済みのスポットを表示するアクティビティ。
 * 追加・更新・削除は {@link AddSpotActivity} を利用する。
 */
public class TestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private ChipGroup chipGroup;
    private final Map<Marker, Item> markerMap = new HashMap<>();
    private final Executor executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        findViewById(R.id.btn_add).setOnClickListener(v ->
                startActivity(new Intent(this, AddSpotActivity.class)));

        findViewById(R.id.btn_delete_all).setOnClickListener(v -> {
            executor.execute(() -> {
                AppDatabase.getInstance(this).itemDao().deleteAll();
                runOnUiThread(() -> {
                    loadMarkers();
                    Toast.makeText(this, "追加したマーカーを全件削除しました", Toast.LENGTH_SHORT).show();
                });
            });
        });

        chipGroup = findViewById(R.id.category_chip_group);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> filterMarkers());

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (googleMap != null) {
            loadMarkers();
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(TestData.JEC_LATLNG, 16f));

        googleMap.setOnMarkerClickListener(marker -> {
            marker.showInfoWindow();
            return true;
        });

        googleMap.setOnInfoWindowClickListener(marker -> {
            Item item = markerMap.get(marker);
            if (item != null) {
                Intent intent = new Intent(this, AddSpotActivity.class);
                intent.putExtra("item_id", item.id);
                startActivity(intent);
            }
        });

        loadMarkers();
    }

    private void loadMarkers() {
        if (googleMap == null) return;

        executor.execute(() -> {
            List<Item> items = AppDatabase.getInstance(this).itemDao().getAll();
            runOnUiThread(() -> {
                googleMap.clear();
                markerMap.clear();

                // デフォルトのマーカーを配置
                for (TestData.TestItem t : TestData.TEST_ITEMS) {
                    String category = ItemCategory.RAMEN.name();
                    for (ItemCategory c : ItemCategory.values()) {
                        if (c.getDisplayName().equals(t.category())) {
                            category = c.name();
                            break;
                        }
                    }
                    Item item = new Item(t.name(), t.comment(), 0f, t.latitude(), t.longitude(), category);
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(item.latitude, item.longitude))
                            .title(item.name)
                            .snippet(item.comment + " 評価: " + item.rating)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                    markerMap.put(marker, item);
                }

                for (Item item : items) {
                    Marker marker = googleMap.addMarker(new MarkerOptions()
                            .position(new LatLng(item.latitude, item.longitude))
                            .title(item.name)
                            .snippet(item.comment + " 評価: " + item.rating)
                            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
                    markerMap.put(marker, item);
                }

                filterMarkers();
            });
        });
    }

    private void filterMarkers() {
        if (chipGroup == null) return;

        int checkedId = chipGroup.getCheckedChipId();
        String category = null;
        if (checkedId == R.id.chip_ramen) {
            category = ItemCategory.RAMEN.name();
        } else if (checkedId == R.id.chip_conveni) {
            category = ItemCategory.CONVENI.name();
        } else if (checkedId == R.id.chip_jec) {
            category = ItemCategory.JEC.name();
        }

        for (Map.Entry<Marker, Item> entry : markerMap.entrySet()) {
            boolean visible = category == null || category.equals(entry.getValue().category);
            entry.getKey().setVisible(visible);
        }
    }
}

