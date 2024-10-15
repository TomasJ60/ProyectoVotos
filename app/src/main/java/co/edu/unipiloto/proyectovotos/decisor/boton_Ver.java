package co.edu.unipiloto.proyectovotos.decisor;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;

import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.votos.Proyecto;

public class boton_Ver extends AppCompatActivity {
    private TextView tvNombrePoryecto, tvNombrePlaneador, tvDireccionProyecto;
    private TextView tvTotalVotos, tvVotosFavor, tvVotosContra, tvVotoBlanco;
    FirebaseAuth fAuth;
    FirebaseFirestore fStore;
    String proyectoId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_boton_ver);

        // Inicializar TextViews
        tvNombrePoryecto = findViewById(R.id.tvNombrePoryecto);
        tvNombrePlaneador = findViewById(R.id.tvNombrePlaneador);
        tvDireccionProyecto = findViewById(R.id.tvDireccionProyecto);
        tvTotalVotos = findViewById(R.id.tvTotalVotos);
        tvVotosFavor = findViewById(R.id.tvVotosFavor);
        tvVotosContra = findViewById(R.id.tvVotosContra);
        tvVotoBlanco = findViewById(R.id.tvVotoBlanco);

        // Inicializar Firebase Auth y Firestore
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();

        String nombreProyecto = getIntent().getStringExtra("nombreProyecto");
        Log.d("ProyectoNombre", "Nombre del Proyecto: " + nombreProyecto);


        // Recupera el ID del proyecto
        String idProyecto = getIntent().getStringExtra("idProyecto");
        Log.d("ProyectoIDVer", "ID del Proyecto en Intent: " + idProyecto);  // Verificar si el ID se está recibiendo correctamente
        cargarDetallesProyecto(idProyecto);

        // Obtener el ID del proyecto del Intent y verificar
        proyectoId = getIntent().getStringExtra("idProyecto");
        Log.d("ProyectoID", "ID del Proyecto en ProyectoID: " + proyectoId);

        // Cargar datos del proyecto
        cargarDatosProyecto(proyectoId);
        cargarDatosVotacion(nombreProyecto);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarDetallesProyecto(String idProyecto) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("registroPropuesta").document(idProyecto).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Proyecto proyecto = document.toObject(Proyecto.class);

                        } else {

                        }
                    } else {

                    }
                });
    }

    private void cargarDatosProyecto(String proyectoId) {
        DocumentReference proyectoRef = fStore.collection("registroPropuesta").document(proyectoId);
        proyectoRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                tvNombrePoryecto.setText(documentSnapshot.getString("titulo"));
                tvDireccionProyecto.setText(documentSnapshot.getString("barrio"));
                tvNombrePlaneador.setText(documentSnapshot.getString("fname"));

                Log.d("Firestore", "Documento encontrado: " + documentSnapshot.getData());
                String planeadorId = documentSnapshot.getString("idPlaneador");
            } else {
                Log.d("Firestore", "El documento no existe.");
            }
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error al obtener los datos", e);
        });
    }


    private void cargarDatosVotacion(String nombreProyecto) {
        CollectionReference votacionRef = fStore.collection("registroVotacion");
        votacionRef.whereEqualTo("ProyectoVoto", nombreProyecto).get().addOnSuccessListener(queryDocumentSnapshots -> {
            int totalVotos = queryDocumentSnapshots.size();
            int votosFavor = 0;
            int votosContra = 0;
            int votosBlanco = 0;

            if (queryDocumentSnapshots.isEmpty()) {
                Log.d("Votacion", "No se encontraron documentos para el proyecto: " + nombreProyecto);
            }

            for (DocumentSnapshot document : queryDocumentSnapshots) {
                String voto = document.getString("voto");
                Log.d("Votacion", "Voto encontrado: " + voto); // Log para cada voto encontrado

                if (voto != null) {
                    if ("si".equalsIgnoreCase(voto)) {
                        votosFavor++;
                    } else if ("no".equalsIgnoreCase(voto)) {
                        votosContra++;
                    } else if ("blanco".equalsIgnoreCase(voto)) {
                        votosBlanco++;
                    }
                } else {
                    Log.d("Votacion", "El documento no contiene el campo 'voto': " + document.getId());
                }
            }

            // Actualizar los TextViews con los resultados
            tvTotalVotos.setText(String.valueOf(totalVotos));
            tvVotosFavor.setText(String.valueOf(votosFavor));
            tvVotosContra.setText(String.valueOf(votosContra));
            tvVotoBlanco.setText(String.valueOf(votosBlanco));

            // Log para verificar que los datos son correctos
            Log.d("Votacion", "Total Votos: " + totalVotos);
            Log.d("Votacion", "Votos a favor: " + votosFavor);
            Log.d("Votacion", "Votos en contra: " + votosContra);
            Log.d("Votacion", "Votos en blanco: " + votosBlanco);
        }).addOnFailureListener(e -> {
            Log.e("Firestore", "Error al obtener los datos de votación", e);
        });
    }


}

