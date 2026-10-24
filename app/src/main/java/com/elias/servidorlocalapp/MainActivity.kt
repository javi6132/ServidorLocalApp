
package com.elias.servidorlocalapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.elias.servidorlocalapp.Producto
import com.elias.servidorlocalapp.ProductoAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            PantallaProductos()
        }
    }
}

@Composable
fun PantallaProductos() {

    var productos by remember {
        mutableStateOf<List<Producto>>(emptyList())
    }

    var cargando by remember {
        mutableStateOf(false)
    }

    var error by remember {
        mutableStateOf<String?>(null)
    }

    val scope = rememberCoroutineScope()

    suspend fun cargarProductos() {
        cargando = true
        error = null

        try {
            productos = withContext(Dispatchers.IO) {
                val url = URL(
                    "http://10.0.2.2/catalogo_api/productos.php"
                )

                val conexion =
                    url.openConnection() as HttpURLConnection

                try {
                    conexion.requestMethod = "GET"
                    conexion.connectTimeout = 10000
                    conexion.readTimeout = 10000

                    if (conexion.responseCode !=
                        HttpURLConnection.HTTP_OK
                    ) {
                        throw Exception(
                            "Error HTTP: ${conexion.responseCode}"
                        )
                    }

                    val respuesta = conexion.inputStream
                        .bufferedReader(Charsets.UTF_8)
                        .use { it.readText() }

                    val jsonArray = JSONArray(respuesta)
                    val lista = mutableListOf<Producto>()

                    for (i in 0 until jsonArray.length()) {
                        val objeto = jsonArray.getJSONObject(i)

                        lista.add(
                            Producto(
                                id = objeto.getInt("id"),
                                nombre = objeto.getString("nombre"),
                                descripcion = objeto.getString(
                                    "descripcion"
                                ),
                                precio = objeto.getDouble("precio")
                            )
                        )
                    }

                    lista
                } finally {
                    conexion.disconnect()
                }
            }
        } catch (e: Exception) {
            error = "No se pudieron cargar los productos: " +
                    (e.message ?: "Error desconocido")
        } finally {
            cargando = false
        }
    }

    LaunchedEffect(Unit) {
        cargarProductos()
    }

    MaterialTheme {
        Scaffold(
            modifier = Modifier.fillMaxSize()
        ) { padding ->

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
            ) {

                Text(
                    text = "Catálogo de productos",
                    style = MaterialTheme.typography.headlineSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Datos obtenidos desde el servidor local"
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        scope.launch {
                            cargarProductos()
                        }
                    },
                    enabled = !cargando
                ) {
                    Text("Actualizar productos")
                }


                if (cargando) {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                if (error != null) {
                    Text(
                        text = error!!,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                if (!cargando && error == null) {
                    Text("Productos recibidos: ${productos.size}")
                }

                Spacer(modifier = Modifier.height(8.dp))

                AndroidView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),

                    factory = { context ->
                        RecyclerView(context).apply {
                            layoutManager =
                                LinearLayoutManager(context)

                            adapter = ProductoAdapter()
                        }
                    },

                    update = { recyclerView ->
                        val adaptador =
                            recyclerView.adapter as ProductoAdapter

                        adaptador.actualizar(productos)
                    }
                )
            }
        }
    }
}
