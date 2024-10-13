package co.edu.unipiloto.proyectovotos.votos;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.storage.FirebaseStorage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import co.edu.unipiloto.proyectovotos.R;

    public class votacion extends AppCompatActivity {

        private FirebaseFirestore db;
        private FirebaseAuth mAuth;
        private TextView tvPuesto, tvDireccionC, tvLocalidadC, tvTituloProyecto;
        private RadioButton rdSi, rdNo, rdBlanco;
        private Button btnVotar;
        private String nombreUsuario, puestoVotacion, direccionCiudadano, localidadCiudadano, proyectoTitulo, voto;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);
            setContentView(R.layout.activity_votacion);

            // Inicializar Firebase Firestore y Auth
            db = FirebaseFirestore.getInstance();
            mAuth = FirebaseAuth.getInstance();

            // Referencias a los TextView y RadioButton
            Intent intent = getIntent();
            puestoVotacion = intent.getStringExtra("puestoVotacion");
            proyectoTitulo = intent.getStringExtra("proyectoTitulo");

            tvPuesto = findViewById(R.id.tvPuesto);
            tvPuesto.setText(puestoVotacion);

            tvTituloProyecto = findViewById(R.id.tvTituloProyecto);
            if (proyectoTitulo != null) {
                tvTituloProyecto.setText(proyectoTitulo); // Muestra el título del proyecto
            } else {
                Toast.makeText(this, "Error al obtener el título del proyecto", Toast.LENGTH_SHORT).show();
            }


            tvPuesto = findViewById(R.id.tvPuesto);
            tvPuesto.setText(puestoVotacion);

            tvDireccionC = findViewById(R.id.tvDireccionC);
            tvLocalidadC = findViewById(R.id.tvLocalidadC);
            tvTituloProyecto = findViewById(R.id.tvTituloProyecto);

            // En onCreate() de la clase votacion
            intent = getIntent();
            proyectoTitulo = intent.getStringExtra("proyectoTitulo");
            tvTituloProyecto = findViewById(R.id.tvTituloProyecto);

            tvPuesto = findViewById(R.id.tvPuesto);
            tvPuesto.setText(puestoVotacion);

            tvDireccionC = findViewById(R.id.tvDireccionC);
            tvLocalidadC = findViewById(R.id.tvLocalidadC);
            tvTituloProyecto = findViewById(R.id.tvTituloProyecto);


            rdSi = findViewById(R.id.rdSi);
            rdNo = findViewById(R.id.rdNo);
            rdBlanco = findViewById(R.id.rdBlanco);
            btnVotar = findViewById(R.id.btnVotar);


            cargarDatosUsuario();

            btnVotar.setOnClickListener(view -> registrarVoto());
        }

        private void cargarDatosUsuario() {
            // Obtener el usuario actualmente autenticado
            FirebaseUser currentUser = mAuth.getCurrentUser();
            if (currentUser != null) {
                String userId = currentUser.getUid();

                // Obtener los datos del usuario desde la tabla "users"
                db.collection("users").document(userId).get().addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        nombreUsuario = documentSnapshot.getString("fName");
                        direccionCiudadano = documentSnapshot.getString("barrio");
                        localidadCiudadano = documentSnapshot.getString("localidad");

                        // Colocar los valores en los TextViews
                        tvDireccionC.setText(direccionCiudadano);
                        tvLocalidadC.setText(localidadCiudadano);

                        // Log para depuración
                        Log.d("FirestoreData", "Direccion: " + direccionCiudadano);

                        // Obtener el puesto de votación seleccionado por el usuario desde la tabla "puestosdeVotacion"
                        db.collection("puestosdeVotacion").document(userId).get().addOnSuccessListener(puestoSnapshot -> {
                            if (puestoSnapshot.exists()) {
                                puestoVotacion = puestoSnapshot.getString("direccion");
                                tvPuesto.setText(puestoVotacion);
                            } else {
                                Toast.makeText(votacion.this, "No se encontró el puesto de votación.", Toast.LENGTH_SHORT).show();
                            }
                        }).addOnFailureListener(e -> {
                            Toast.makeText(votacion.this, "Error al obtener el puesto de votación.", Toast.LENGTH_SHORT).show();
                        });

                        // Obtener el primer proyecto por el que está votando desde la tabla "registroPropuesta"
                        db.collection("registroPropuesta").get().addOnSuccessListener(queryDocumentSnapshots -> {
                            List<String> proyectos = new ArrayList<>();
                            for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                String tituloProyecto = document.getString("titulo");
                                if (tituloProyecto != null) {
                                    proyectos.add(tituloProyecto);
                                }
                            }

                            // Mostrar un dialogo para que el usuario elija un proyecto
                        });
                    } else {
                        Toast.makeText(votacion.this, "No se encontraron datos del usuario.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(votacion.this, "Error al obtener los datos del usuario.", Toast.LENGTH_SHORT).show();
                });
            }
        }


        private void registrarVoto() {

            if (rdSi.isChecked()) {
                voto = "si";
            } else if (rdNo.isChecked()) {
                voto = "no";
            } else if (rdBlanco.isChecked()) {
                voto = "en blanco";
            }




            // Comprobar si el usuario ya ha votado
            String userId = mAuth.getCurrentUser().getUid();
            db.collection("registroVotacion").whereEqualTo("userId", userId).get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            // No ha votado aún, registrar el voto
                            Map<String, Object> votacionData = new HashMap<>();
                            votacionData.put("nombre", nombreUsuario);
                            votacionData.put("LVotacion", puestoVotacion);
                            votacionData.put("DireccionCiudadano", direccionCiudadano);
                            votacionData.put("LocalidadCuidadano", localidadCiudadano);
                            votacionData.put("ProyectoVoto", proyectoTitulo);
                            votacionData.put("voto", voto);
                            votacionData.put("userId", userId); // Asegúrate de incluir el userId

                            db.collection("registroVotacion").add(votacionData)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(votacion.this, "Voto registrado con éxito", Toast.LENGTH_SHORT).show();
                                        finish(); // Cerrar la actividad o hacer lo que sea necesario
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(votacion.this, "Error al registrar el voto", Toast.LENGTH_SHORT).show();
                                    });
                        } else {
                            Toast.makeText(votacion.this, "Ya has votado anteriormente.", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }
