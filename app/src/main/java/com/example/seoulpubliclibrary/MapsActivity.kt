package com.example.seoulpubliclibrary

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.example.seoulpubliclibrary.data.Library
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        val mapFragment = supportFragmentManager
                .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        loadLibraries()
    }

    fun loadLibraries(){
        val retrofit = Retrofit.Builder()
                .baseUrl(SeoulOpenApi.DOMAIN)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

        val seoulOpenService = retrofit.create(SeoulOpenService::class.java)

        seoulOpenService
                .getLibrary(SeoulOpenApi.API_KEY)
                .enqueue(object: Callback<Library>{
                    override fun onResponse(call: Call<Library>, response: Response<Library>) {
                        showLibraries(response.body() as Library)
                    }

                    override fun onFailure(call: Call<Library>, t: Throwable) {
                        Toast.makeText(baseContext,
                                "서버에서 데이터를 가져올 수 없습니다",
                                Toast.LENGTH_LONG).show()
                    }
                })
    }

    fun showLibraries(libraries: Library){
        val latLngBounds = LatLngBounds.Builder()
        for(lib in libraries.SeoulPublicLibraryInfo.row){
            val position = LatLng(lib.XCNTS.toDouble(), lib.YDNTS.toDouble())
            val marker = MarkerOptions().position(position).title(lib.LBRRY_NAME)
            mMap.addMarker(marker)
            latLngBounds.include(marker.position)
        }

        val bounds = latLngBounds.build()
        val padding = 1

        val updated = CameraUpdateFactory.newLatLngBounds(bounds, padding)
        mMap.moveCamera(updated)
    }
}