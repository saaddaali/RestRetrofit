package com.example.retrorest

import Compte
import CompteAdapter
import CompteApi
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorest.ui.theme.RetroRestTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var compteAdapter: CompteAdapter
    private lateinit var compteApi: CompteApi
    private var currentDataType = "json"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Using regular Button
        val btnCreateCompte = findViewById<Button>(R.id.btn_create_compte)
        btnCreateCompte.setOnClickListener {
            compteAdapter.showCreateDialog(this)
        }

        // Setup Spinner
        setupSpinner()

        // Initialize API
        val dataType = "json" // or "xml"
        compteApi = RetrofitClient.getJsonInstance().create(CompteApi::class.java)


        loadComptes()
    }


    private fun setupSpinner() {
        val spinner = findViewById<Spinner>(R.id.spinner_data_type)
        val dataTypes = arrayOf("JSON", "XML")

        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, dataTypes)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter
        spinner.setSelection(0) // Default to JSON

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val newDataType = dataTypes[position].lowercase()
                if (newDataType != currentDataType) {
                    currentDataType = newDataType
                    updateApiClient()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun updateApiClient() {
        // Initialize Retrofit and API with current data type
        compteApi = RetrofitClient.getJsonInstance().create(CompteApi::class.java)

        // Initialize or update adapter
        if (!::compteAdapter.isInitialized) {
            compteAdapter = CompteAdapter(emptyList(), compteApi)
            recyclerView.adapter = compteAdapter
        } else {
            compteAdapter = CompteAdapter(emptyList(), compteApi)
            recyclerView.adapter = compteAdapter
        }

        // Load data with new client
        loadComptes()
    }
    private fun loadComptes() {
        val call: Call<List<Compte>> = compteApi.getAllComptes()
        call.enqueue(object : Callback<List<Compte>> {
            override fun onResponse(call: Call<List<Compte>>, response: Response<List<Compte>>) {
                if (response.isSuccessful) {
                    val comptes = response.body() ?: emptyList()
                    compteAdapter = CompteAdapter(comptes, compteApi)
                    recyclerView.adapter = compteAdapter
                } else {
                    Toast.makeText(this@MainActivity, "Erreur : ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<Compte>>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Échec : ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }


}