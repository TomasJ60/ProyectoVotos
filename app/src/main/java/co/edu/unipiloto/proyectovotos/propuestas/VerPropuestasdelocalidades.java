package co.edu.unipiloto.proyectovotos.propuestas;

import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import java.util.ArrayList;
import java.util.List;

import android.widget.Button;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.iniciodesesion.Login;
import co.edu.unipiloto.proyectovotos.votos.votar;

public class VerPropuestasdelocalidades extends AppCompatActivity {

    private Spinner spinnerLocalidades;
    private Spinner spinnerTitulos;
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;

    private Button btnVotar;

    // Google Maps
    private GoogleMap mMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_ver_propuestasdelocalidades);

        // Inicializar Firebase
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        // Configurar el Spinner
        spinnerLocalidades = findViewById(R.id.spinner2);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.localidades, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerLocalidades.setAdapter(adapter);

        spinnerTitulos = findViewById(R.id.spinnerTitulos);

        btnVotar = findViewById(R.id.btn_votar);

        mostrarTitulosPropuestas();

        FirebaseUser currentUser = fAuth.getCurrentUser();
        if (currentUser == null) {
            // Si no hay un usuario autenticado, redirige al Login
            startActivity(new Intent(VerPropuestasdelocalidades.this, Login.class));
            finish(); // Para que no vuelva a esta actividad si el usuario no está autenticado
        }



        // Listener para el Spinner
        spinnerLocalidades.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                String localidadSeleccionada = parentView.getItemAtPosition(position).toString();
                Log.d("VerPropuestas", "Localidad seleccionada: " + localidadSeleccionada);
                Toast.makeText(VerPropuestasdelocalidades.this, "Localidad seleccionada: " + localidadSeleccionada, Toast.LENGTH_SHORT).show();
                mostrarProyectosPorLocalidad(localidadSeleccionada);
            }


            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // No hacer nada si no se selecciona nada
            }
        });

        Button btnVerProyecto = findViewById(R.id.btn_ver_proyecto);
        btnVerProyecto.setOnClickListener(v -> {
            String tituloSeleccionado = spinnerTitulos.getSelectedItem().toString();

            // Verifica que se haya seleccionado un título
            if (tituloSeleccionado != null && !tituloSeleccionado.isEmpty()) {
                // Redirigir a la nueva actividad y pasar el título seleccionado
                Intent intent = new Intent(VerPropuestasdelocalidades.this, VerProyectoActivity.class);
                intent.putExtra("tituloProyecto", tituloSeleccionado);
                startActivity(intent);
            } else {
                Toast.makeText(VerPropuestasdelocalidades.this, "Selecciona un proyecto", Toast.LENGTH_SHORT).show();
            }
        });


        // Manejar insets del sistema (si es necesario para adaptaciones UI)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Inicializar el mapa (suponiendo que Google Maps ya está configurado)
        configurarMapa();


        btnVotar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(VerPropuestasdelocalidades.this, votar.class);
            }
        });

    }

    // Método para configurar el mapa
    private void configurarMapa() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                mMap.setOnMarkerClickListener(marker -> {
                    String tituloProyecto = marker.getTitle();
                    Toast.makeText(VerPropuestasdelocalidades.this, "Proyecto: " + tituloProyecto, Toast.LENGTH_SHORT).show();
                    return false;
                });
            });
        }
    }

    // Método para obtener proyectos por localidad desde Firebase Firestore
    private void mostrarProyectosPorLocalidad(String localidadSeleccionada) {
        // Limpiar el mapa antes de agregar nuevos marcadores
        if (mMap != null) {
            mMap.clear();
        }

        // Consulta a Firebase para obtener todas las propuestas con la localidad seleccionada
        fStore.collection("registroPropuesta")
                .whereEqualTo("localidad", localidadSeleccionada)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Verificar si existen documentos (propuestas) en la localidad seleccionada
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Obtener los datos necesarios de cada propuesta
                                String barrio = document.getString("barrio");
                                String titulo = document.getString("titulo");

                                // Mostrar la ubicación en el mapa para cada propuesta
                                mostrarUbicacionEnMapa(barrio, titulo);
                            }
                        } else {
                            Toast.makeText(VerPropuestasdelocalidades.this, "No hay propuestas en esta localidad", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Firebase", "Error al obtener documentos: ", task.getException());
                        Toast.makeText(VerPropuestasdelocalidades.this, "Error al obtener las propuestas", Toast.LENGTH_SHORT).show();
                    }
                });
    }


    // Método para mostrar la ubicación en el mapa usando Geocoder
    private void mostrarUbicacionEnMapa(String direccion, String tituloProyecto) {
        if (mMap == null) {
            Log.e("VerPropuestas", "GoogleMap is not initialized");
            return;
        }

        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        List<Address> addressList;

        try {
            // Buscar la ubicación de la dirección proporcionada
            addressList = geocoder.getFromLocationName(direccion, 1);
            if (addressList != null && !addressList.isEmpty()) {
                Address address = addressList.get(0);
                LatLng latLng = new LatLng(address.getLatitude(), address.getLongitude());

                // Agregar un marcador por cada proyecto
                mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .title(tituloProyecto));

                // Mover la cámara para ver la ubicación
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12)); // Ajustar el nivel de zoom
            } else {
                Toast.makeText(this, "No se encontró la dirección: " + direccion, Toast.LENGTH_SHORT).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al obtener la dirección", Toast.LENGTH_SHORT).show();
        }
    }
    // Método para obtener solo los títulos de los proyectos y mostrarlos en el Spinner
    private void mostrarTitulosPropuestas() {
        // Crear una lista para los títulos
        List<String> listaTitulos = new ArrayList<>();  // Inicializar la lista

        // Consulta a Firebase para obtener los títulos de las propuestas
        fStore.collection("registroPropuesta")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Verificar si existen documentos (propuestas)
                        if (task.getResult() != null && !task.getResult().isEmpty()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                // Obtener el título de cada propuesta
                                String titulo = document.getString("titulo");

                                // Asegúrate de que el título no es null antes de agregarlo
                                if (titulo != null) {
                                    listaTitulos.add(titulo);
                                }
                            }

                            // Actualizar el Spinner con los títulos
                            ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, listaTitulos);
                            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                            spinnerTitulos.setAdapter(adapter);  // Usar el Spinner correcto
                        } else {
                            Toast.makeText(VerPropuestasdelocalidades.this, "No hay propuestas disponibles", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.d("Firebase", "Error al obtener documentos: ", task.getException());
                        Toast.makeText(VerPropuestasdelocalidades.this, "Error al obtener las propuestas", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}