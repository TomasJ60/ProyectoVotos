package co.edu.unipiloto.proyectovotos.propuestas;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import co.edu.unipiloto.proyectovotos.Homes.HomeProyectos;
import co.edu.unipiloto.proyectovotos.R;
import co.edu.unipiloto.proyectovotos.votos.votacion;

public class registrodepropuestas extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth fAuth;
    private StorageReference storageReference;
    private String imageUrl;

    private EditText editTextTitulo, editTextDescripcion;
    private Button buttonImage, buttonPublicar;
    private String fname, barrio, localidad, entidad;

    private ActivityResultLauncher<Intent> imagePickerLauncher;

    //Poner el tiempo
    private EditText editTextTiempoMinutos;
    private DatePicker datePicker;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrodepropuestas);

        //Conexion con firebase
        db = FirebaseFirestore.getInstance();
        fAuth = FirebaseAuth.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        editTextTitulo = findViewById(R.id.editTextTitulo);
        editTextDescripcion = findViewById(R.id.editTextDescripcion);
        buttonImage = findViewById(R.id.buttonImage);
        buttonPublicar = findViewById(R.id.buttonPublicar);

        //tiempo de el proyecto
        editTextTiempoMinutos = findViewById(R.id.editTextTiempoMinutos);
        datePicker = findViewById(R.id.datePicker);


        cargarDatosRegistroProyectos();

        imagePickerLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                Uri imageUri = result.getData().getData();
                subirImagenFirebase(imageUri);
            }
        });

        buttonImage.setOnClickListener(view -> {
            // Abrir la galería para seleccionar imagen
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            imagePickerLauncher.launch(intent);
        });

        buttonPublicar.setOnClickListener(view -> {
            String titulo = editTextTitulo.getText().toString();
            String descripcion = editTextDescripcion.getText().toString();

            //tiempo de votacion
            int day = datePicker.getDayOfMonth();
            int month = datePicker.getMonth(); // Recuerda que el mes es 0-indexado
            int year = datePicker.getYear();
            int duracionMin = Integer.parseInt(editTextTiempoMinutos.getText().toString());

            if (titulo.isEmpty()) {
                Toast.makeText(registrodepropuestas.this, "Se requiere llenar el campo del título", Toast.LENGTH_LONG).show();
                return;
            }

            if (descripcion.isEmpty()) {
                Toast.makeText(registrodepropuestas.this, "Se requiere llenar el campo de descripción", Toast.LENGTH_LONG).show();
                return;
            }

            // Guardar los datos de la propuesta
            guardarDatosRegistroPropuesta(titulo, descripcion, duracionMin, year, month, day);

            Intent votacionIntent = new Intent(registrodepropuestas.this, votacion.class);
            votacionIntent.putExtra("proyectoTitulo", titulo);
            startActivity(votacionIntent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void cargarDatosRegistroProyectos() {
        String userID = fAuth.getCurrentUser().getUid();
        DocumentReference docRef = db.collection("registroProyectos").document(userID);
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                if (documentSnapshot.exists()) {
                    // Carga de datos, asegurándote de que estás obteniendo los nombres de los campos correctos
                    fname = documentSnapshot.getString("fName"); // Asegúrate de que el campo en la BD sea "fName"
                    barrio = documentSnapshot.getString("barrio");
                    localidad = documentSnapshot.getString("localidad");
                    entidad = documentSnapshot.getString("entidad");

                    Log.d("Firestore", "Datos cargados: " + fname + ", " + barrio + ", " + localidad + ", " + entidad);
                } else {
                    Log.d("Firestore", "No se encontró el documento");
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("Firestore", "Error al cargar los datos: " + e.getMessage());
            }
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1 && resultCode == RESULT_OK && data != null) {
            Uri imageUri = data.getData();
            subirImagenFirebase(imageUri);
        }
    }

    private void subirImagenFirebase(Uri imageUri) {
        if (imageUri != null) {
            StorageReference fileRef = storageReference.child("images/" + UUID.randomUUID().toString());
            fileRef.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUrl = uri.toString();
                                    Toast.makeText(registrodepropuestas.this, "Imagen subida exitosamente", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.e("Storage", "Error al subir la imagen: " + e.getMessage());
                            Toast.makeText(registrodepropuestas.this, "Error al subir la imagen", Toast.LENGTH_SHORT).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No se ha seleccionado ninguna imagen", Toast.LENGTH_SHORT).show();
        }
    }


    private void guardarDatosRegistroPropuesta(String titulo, String descripcion, int duracionMin, int year, int month, int day) {
        // Validar que la duración en minutos sea positiva
        if (duracionMin <= 0) {
            Toast.makeText(this, "La duración debe ser mayor a cero minutos", Toast.LENGTH_SHORT).show();
            return;
        }

        Map<String, Object> propuesta = new HashMap<>();
        propuesta.put("titulo", titulo);
        propuesta.put("descripcion", descripcion);
        propuesta.put("fname", fname);
        propuesta.put("barrio", barrio);
        propuesta.put("localidad", localidad);
        propuesta.put("entidad", entidad);
        propuesta.put("imagenUrl", imageUrl);

        // Calcular el tiempo límite para votar
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, day);
        Date fechaInicio = cal.getTime();
        propuesta.put("fechaInicio", fechaInicio);

        cal.add(Calendar.MINUTE, duracionMin);
        Date votingDeadline = cal.getTime();
        propuesta.put("votingDeadline", votingDeadline);

        db.collection("registroPropuesta").add(propuesta)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Toast.makeText(registrodepropuestas.this, "Propuesta guardada exitosamente", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(getApplicationContext(), HomeProyectos.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e("Firestore", "Error al guardar la propuesta: " + e.getMessage());
                    }
                });
    }

}