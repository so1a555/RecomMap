package jp.ac.jec.cm0128.recommap;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.HashMap;
import java.util.List;

public class TestActivity extends AppCompatActivity implements OnMapReadyCallback {

    private GoogleMap googleMap;
    private EditText edtName, edtComment;
    private RatingBar ratingBar;
    private Spinner spCategory;
    private TextView txtId;
    private Marker selectedMarker;
    private final HashMap<Marker, Item> markerMap = new HashMap<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test); // ← 正しいレイアウト名

        findViewById(R.id.btn_add).setOnClickListener(v -> {
            Intent intent = new Intent(TestActivity.this, AddSpotActivity.class);
            startActivity(intent);
        });

        edtName = findViewById(R.id.edt_name);
        edtComment = findViewById(R.id.edt_comment);
        ratingBar = findViewById(R.id.rating_bar);
        spCategory = findViewById(R.id.sp_category);
        txtId = findViewById(R.id.txt_id);

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                new String[]{"ラーメン", "コンビニ", "学校", "その他"});
        spCategory.setAdapter(adapter);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    @Override
    public void onMapReady(GoogleMap map) {
        this.googleMap = map;
        LatLng start = TestData.JEC_LATLNG;
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(start, 16f));

        // DBからマーカー読み込みして地図に表示
        List<Item> savedItems = AppDatabase.getInstance(this).itemDao().getAll();
        for (Item item : savedItems) {
            LatLng pos = new LatLng(item.latitude, item.longitude);
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(pos)
                    .title(item.name)
                    .snippet(item.comment + " 評価: " + item.rating)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE)));
            markerMap.put(marker, item);
        }

        // マーカー追加（ロングタップ）
        googleMap.setOnMapLongClickListener(latLng -> {
            String name = edtName.getText().toString().trim();
            String comment = edtComment.getText().toString().trim();
            float rating = ratingBar.getRating();
            String category = spCategory.getSelectedItem().toString();

            if (name.isEmpty()) {
                edtName.setError("場所名が必要です");
                return;
            }

            String snippet = comment + " 評価: " + rating;
            Marker marker = googleMap.addMarker(new MarkerOptions()
                    .position(latLng)
                    .title(name)
                    .snippet(snippet)
                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));

            Item item = new Item(name, comment, rating, latLng.latitude, latLng.longitude, category);
            markerMap.put(marker, item);
            txtId.setText("ID: " + marker.hashCode());
        });

        // マーカークリック時：情報を入力欄に表示
        googleMap.setOnMarkerClickListener(marker -> {
            selectedMarker = marker;
            Item item = markerMap.get(marker);
            if (item != null) {
                edtName.setText(item.name);
                edtComment.setText(item.comment);
                ratingBar.setRating(item.rating);
                spCategory.setSelection(getSpinnerIndex(spCategory, item.category));
                txtId.setText("ID: " + marker.hashCode());
            }
            return false;
        });
    }

    private int getSpinnerIndex(Spinner spinner, String value) {
        for (int i = 0; i < spinner.getCount(); i++) {
            if (spinner.getItemAtPosition(i).toString().equalsIgnoreCase(value)) {
                return i;
            }
        }
        return 0;
    }
}
