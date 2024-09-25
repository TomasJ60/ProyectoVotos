package co.edu.unipiloto.proyectovotos.propuestas;

import android.os.Bundle;

import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import co.edu.unipiloto.proyectovotos.R;

public class VerProyectoActivity extends AppCompatActivity {

    private FirebaseFirestore fStore;
    private TextView txtTitulo, txtDescripcion, txtEntidad, txtBarrio;
    private ImageView imgProyecto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ver_proyecto);

        // Inicializar Firestore
        fStore = FirebaseFirestore.getInstance();

        // Inicializar las vistas
        txtTitulo = findViewById(R.id.txtTitulo);
        txtDescripcion = findViewById(R.id.txtDescripcion);
        txtEntidad = findViewById(R.id.txtEntidad);
        txtBarrio = findViewById(R.id.txtBarrio);
        imgProyecto = findViewById(R.id.imgProyecto);

        // Obtener el título del proyecto que fue seleccionado
        String tituloProyecto = getIntent().getStringExtra("tituloProyecto");

        // Mostrar el proyecto seleccionado
        mostrarProyecto(tituloProyecto);
    }

    private void mostrarProyecto(String titulo) {
        fStore.collection("registroPropuesta")
                .whereEqualTo("titulo", titulo)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && !task.getResult().isEmpty()) {
                        // Si task.getResult() no es null y contiene documentos
                        for (DocumentSnapshot document : task.getResult().getDocuments()) {
                            // Obtener los datos del proyecto
                            String descripcion = document.getString("descripcion");
                            String entidad = document.getString("entidad");
                            String barrio = document.getString("barrio");
                            String urlImagen = document.getString("imagenUrl");

                            // Mostrar los datos en las vistas
                            txtTitulo.setText(titulo);
                            txtDescripcion.setText(descripcion);
                            txtEntidad.setText(entidad);
                            txtBarrio.setText(barrio);

                            // Verifica que la URL de la imagen no sea null o vacía
                            if (urlImagen != null && !urlImagen.isEmpty()) {
                                // Usa Glide para cargar la imagen
                                Glide.with(this)
                                        .load(urlImagen)
                                        .into(imgProyecto);
                            } else {
                                // Si no hay imagen, muestra un placeholder o mensaje
                                imgProyecto.setImageResource(R.drawable.ic_launcher_background);
                            }
                        }
                    } else {
                        Log.d("VerProyectoActivity", "No se encontró el proyecto o hubo un error.");
                        Toast.makeText(VerProyectoActivity.this, "No se encontró el proyecto", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Log.d("VerProyectoActivity", "Error obteniendo el proyecto: " + e.getMessage());
                    Toast.makeText(VerProyectoActivity.this, "Error obteniendo el proyecto", Toast.LENGTH_SHORT).show();
                });
    }

}