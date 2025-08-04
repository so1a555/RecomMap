package jp.ac.jec.cm0128.recommap;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.snackbar.Snackbar;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class AddSpotActivity extends AppCompatActivity implements OnMapReadyCallback {

    private EditText edtName, edtComment;
    private RatingBar ratingBar;
    private Spinner spCategory;
    private Button btnAdd, btnUpdate, btnDelete;

    private GoogleMap googleMap;
    private Marker marker;

    @NonNull
    private LatLng currentLatLng = TestData.JEC_LATLNG;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private int editingItemId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);

        // UI
        edtName = findViewById(R.id.edt_name);
        edtComment = findViewById(R.id.edt_comment);
        ratingBar = findViewById(R.id.rating_bar);
        spCategory = findViewById(R.id.sp_category);
        btnAdd = findViewById(R.id.btn_add);
        btnUpdate = findViewById(R.id.btn_update);
        btnDelete = findViewById(R.id.btn_delete);

        List<String> categoryList = Arrays.stream(ItemCategory.values())
                .map(ItemCategory::getDisplayName)
                .collect(Collectors.toList());
        spCategory.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categoryList));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) mapFragment.getMapAsync(this);

        editingItemId = getIntent().getIntExtra("item_id", -1);
        if (editingItemId != -1) {
            btnAdd.setVisibility(View.GONE);
            btnUpdate.setVisibility(View.VISIBLE);
            btnDelete.setVisibility(View.VISIBLE);

            executor.execute(() -> {
                Item item = AppDatabase.getInstance(getApplicationContext()).itemDao().findById(editingItemId);
                if (item != null) {
                    currentLatLng = new LatLng(item.latitude, item.longitude);
                    runOnUiThread(() -> {
                        edtName.setText(item.name);
                        edtComment.setText(item.comment);
                        ratingBar.setRating(item.rating);
                        spCategory.setSelection(ItemCategory.valueOf(item.category).ordinal());
                        if (googleMap != null && marker != null) {
                            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                                    new CameraPosition.Builder().target(currentLatLng).zoom(16f).build()));
                            marker.setPosition(currentLatLng);
                        }
                    });
                }
            });
        } else {
            btnUpdate.setVisibility(View.GONE);
            btnDelete.setVisibility(View.GONE);
        }

        btnAdd.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String comment = edtComment.getText().toString().trim();
            float rating = ratingBar.getRating();
            ItemCategory category = ItemCategory.values()[spCategory.getSelectedItemPosition()];
            String categoryStr = category.name();

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(comment)) {
                Snackbar.make(edtName, "入力が不足しています", Snackbar.LENGTH_SHORT).show();
                return;
            }

            Item item = new Item(name, comment, rating, currentLatLng.latitude, currentLatLng.longitude, categoryStr);

            executor.execute(() -> {
                AppDatabase.getInstance(getApplicationContext()).itemDao().upsert(item);
                runOnUiThread(() -> {
                    Toast.makeText(this, "登録しました", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });

        btnUpdate.setOnClickListener(v -> {
            String name = edtName.getText().toString().trim();
            String comment = edtComment.getText().toString().trim();
            float rating = ratingBar.getRating();
            ItemCategory category = ItemCategory.values()[spCategory.getSelectedItemPosition()];

            if (TextUtils.isEmpty(name) || TextUtils.isEmpty(comment)) {
                Snackbar.make(edtName, "入力が不足しています", Snackbar.LENGTH_SHORT).show();
                return;
            }

            Item item = new Item(name, comment, rating, currentLatLng.latitude, currentLatLng.longitude, category.name());
            item.id = editingItemId;

            executor.execute(() -> {
                AppDatabase.getInstance(getApplicationContext()).itemDao().upsert(item);
                runOnUiThread(() -> {
                    Toast.makeText(this, "更新しました", Toast.LENGTH_SHORT).show();
                    finish();
                });
            });
        });

        btnDelete.setOnClickListener(v -> executor.execute(() -> {
            AppDatabase.getInstance(getApplicationContext()).itemDao().deleteById(editingItemId);
            runOnUiThread(() -> {
                Toast.makeText(this, "削除しました", Toast.LENGTH_SHORT).show();
                finish();
            });
        }));
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        this.googleMap = map;

        googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(
                new CameraPosition.Builder()
                        .target(currentLatLng)
                        .zoom(16f)
                        .build()));

        marker = googleMap.addMarker(new MarkerOptions()
                .position(currentLatLng)
                .draggable(true)
                .title("ドラッグして場所を調整"));

        googleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
            @Override public void onMarkerDragStart(@NonNull Marker marker) {}
            @Override public void onMarkerDrag(@NonNull Marker marker) {}
            @Override public void onMarkerDragEnd(@NonNull Marker m) {
                currentLatLng = m.getPosition();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
