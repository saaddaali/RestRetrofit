import android.app.AlertDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.retrorest.R
import com.example.retrorest.modele.TypeCompte
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale




class CompteAdapter(
    comptes: List<Compte>,
    private var compteApi: CompteApi

) : RecyclerView.Adapter<CompteAdapter.CompteViewHolder>() {

    private val comptes = comptes.toMutableList()
    private val gson = Gson()


    fun updateComptes(newComptes: List<Compte>) {
        comptes.clear()
        comptes.addAll(newComptes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CompteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_compte, parent, false)
        return CompteViewHolder(view)
    }

    override fun onBindViewHolder(holder: CompteViewHolder, position: Int) {
        try {
            val compte = comptes[position]
            with(holder) {
                solde.text = "Solde: ${compte.solde}"
                dateCreation.text = "Date: ${compte.dateCreation}"
                typeCompte.text = "Type: ${compte.typeCompte}"

                // Setup Update button click listener
                btnUpdate?.setOnClickListener {
                    showUpdateDialog(compte, holder.itemView)
                }

                // Setup Delete button click listener
                btnDelete?.setOnClickListener {
                    showDeleteDialog(compte, holder.itemView, position)
                }
            }
        } catch (e: Exception) {
            Log.e("CompteAdapter", "Error in onBindViewHolder", e)
        }
    }

    private fun showUpdateDialog(compte: Compte, view: View) {
        val dialogView = LayoutInflater.from(view.context).inflate(R.layout.dialog_update_compte, null)

        // Initialize input fields
        val etSolde = dialogView.findViewById<EditText>(R.id.et_solde)
        val etDate = dialogView.findViewById<EditText>(R.id.et_date)
        val spinnerType = dialogView.findViewById<Spinner>(R.id.spinner_type)

        // Set current values
        etSolde.setText(compte.solde.toString())
        etDate.setText(compte.dateCreation)

        // Setup spinner for compte types (using enum values)
        val types = TypeCompte.values().map { it.name.capitalize() }.toTypedArray() // Convert enum values to capitalized strings
        val adapter = ArrayAdapter(view.context, android.R.layout.simple_spinner_item, types)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerType.adapter = adapter

        // Set current type
        val typeIndex = types.indexOf(compte.typeCompte.name.capitalize()) // Ensure enum name is capitalized
        if (typeIndex != -1) {
            spinnerType.setSelection(typeIndex)
        }

        val builder = AlertDialog.Builder(view.context)
            .setTitle("Update Compte")
            .setView(dialogView)
            .setPositiveButton("Update") { dialog, _ ->
                try {
                    // Get updated values
                    val newSolde = etSolde.text.toString().toDouble()
                    val newDate = etDate.text.toString()
                    val newType = spinnerType.selectedItem.toString()

                    // Convert the string to the corresponding enum
                    val typeCompteEnum = TypeCompte.valueOf(newType.toUpperCase())

                    // Create updated compte
                    val updatedCompte = Compte().apply {
                        id = compte.id
                        solde = newSolde
                        dateCreation = newDate
                        typeCompte = typeCompteEnum // Use enum type here
                    }

                    // Make API call
                    compte.id?.let {
                        compteApi.updateCompte(it, updatedCompte).enqueue(object : Callback<Compte> {
                            override fun onResponse(call: Call<Compte>, response: Response<Compte>) {
                                if (response.isSuccessful && response.body() != null) {
                                    val updatedIndex = comptes.indexOfFirst { it.id == compte.id }
                                    if (updatedIndex != -1) {
                                        comptes[updatedIndex] = response.body()!!
                                        notifyItemChanged(updatedIndex)
                                        Toast.makeText(view.context, "Compte updated successfully", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    Toast.makeText(view.context, "Failed to update compte", Toast.LENGTH_SHORT).show()
                                }
                            }

                            override fun onFailure(call: Call<Compte>, t: Throwable) {
                                Toast.makeText(view.context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                            }
                        })
                    }
                } catch (e: NumberFormatException) {
                    Toast.makeText(view.context, "Please enter valid numbers", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(view.context, "Error updating compte: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)

        builder.show()

        // Get the InputMethodManager from the context of the view
        val imm = view.context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        if (imm != null) {
            // Hide the keyboard before showing it again
            imm.hideSoftInputFromWindow(view.windowToken, 0)
            imm.showSoftInput(dialogView.findViewById<EditText>(R.id.et_solde), InputMethodManager.SHOW_IMPLICIT)
        } else {
            Toast.makeText(view.context, "Failed to get InputMethodManager", Toast.LENGTH_SHORT).show()
        }
    }

    fun showCreateDialog(context: Context) {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_create_compte, null)
        var dialog: AlertDialog? = null

        // Initialize fields
        val etSolde = dialogView.findViewById<EditText>(R.id.create_solde)
        val spinnerTypeCompte = dialogView.findViewById<Spinner>(R.id.create_type_compte)
        val formatSwitch = dialogView.findViewById<Switch>(R.id.format_toggle)

        // Setup spinner
        val types = TypeCompte.values().map { it.name.capitalize() }.toTypedArray()
        val spinnerAdapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, types)
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerTypeCompte.adapter = spinnerAdapter

        // Create dialog
        dialog = AlertDialog.Builder(context)
            .setTitle("Create New Compte")
            .setView(dialogView)
            .setPositiveButton("Create", null) // Set to null initially
            .setNegativeButton("Cancel") { dialog, _ ->
                hideKeyboard(context, dialogView)
                dialog.dismiss()
            }
            .create()

        // Show dialog and setup button after
        dialog.show()

        // Get the create button and set its click listener
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            try {
                val solde = etSolde.text.toString().toDouble()
                val typeCompteString = spinnerTypeCompte.selectedItem.toString()
                val useXml = formatSwitch.isChecked

                if (solde <= 0) {
                    etSolde.error = "Please enter a valid amount"
                    return@setOnClickListener
                }

                val newCompte = Compte(
                    id = null,
                    solde = solde,
                    dateCreation = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                        .format(Date()),
                    typeCompte = TypeCompte.fromString(typeCompteString.toUpperCase())
                )

                if (useXml) {
                    createCompteXml(newCompte, context)
                } else {
                    createCompteJson(newCompte, context)
                }

                hideKeyboard(context, dialogView)
                dialog.dismiss()

            } catch (e: NumberFormatException) {
                etSolde.error = "Please enter a valid number"
            } catch (e: Exception) {
                Toast.makeText(context, "Error creating compte: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        // Focus on solde input and show keyboard
        etSolde.requestFocus()
        showKeyboard(context, etSolde)
    }

    private fun createCompteXml(compte: Compte, context: Context) {
        compteApi = RetrofitClient.getXmlInstance().create(CompteApi::class.java)

        compteApi.createCompteXml(compte).enqueue(object : Callback<Compte> {
            override fun onResponse(call: Call<Compte>, response: Response<Compte>) {
                if (response.isSuccessful) {
                    response.body()?.let { createdCompte ->
                        comptes.add(createdCompte)
                        notifyItemInserted(comptes.size - 1)
                        Toast.makeText(context, "Compte created successfully (XML)", Toast.LENGTH_SHORT).show()
                    } ?: run {
                        Log.e("CompteAdapter", "Response body is null")
                        Toast.makeText(context, "Error: Empty response", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CompteAdapter", "Error response: $errorBody")
                    Toast.makeText(context, "Failed to create compte: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Compte>, t: Throwable) {
                Log.e("CompteAdapter", "Network error", t)
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun createCompteJson(compte: Compte, context: Context) {
        compteApi.createCompteJson(compte).enqueue(object : Callback<Compte> {
            override fun onResponse(call: Call<Compte>, response: Response<Compte>) {
                if (response.isSuccessful && response.body() != null) {
                    comptes.add(response.body()!!)
                    notifyItemInserted(comptes.size - 1)
                    Toast.makeText(context, "Compte created successfully (JSON)", Toast.LENGTH_SHORT).show()
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CompteAdapter", "Error response: $errorBody")
                    Toast.makeText(context, "Failed to create compte: ${response.code()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Compte>, t: Throwable) {
                Log.e("CompteAdapter", "Network error", t)
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // Helper methods for keyboard handling
    private fun showKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard(context: Context, view: View) {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        imm?.hideSoftInputFromWindow(view.windowToken, 0)
    }


    private fun showDeleteDialog(compte: Compte, view: View, position: Int) {
        AlertDialog.Builder(view.context)
            .setTitle("Delete Compte")
            .setMessage("Are you sure you want to delete this compte?")
            .setPositiveButton("Delete") { dialog, _ ->
                compte.id?.let {
                    compteApi.deleteCompte(it).enqueue(object : Callback<Void> {
                        override fun onResponse(call: Call<Void>, response: Response<Void>) {
                            if (response.isSuccessful) {
                                comptes.removeAt(position)
                                notifyItemRemoved(position)
                                notifyItemRangeChanged(position, comptes.size)
                                Toast.makeText(view.context, "Compte deleted successfully", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(view.context, "Failed to delete compte", Toast.LENGTH_SHORT).show()
                            }
                        }

                        override fun onFailure(call: Call<Void>, t: Throwable) {
                            Toast.makeText(view.context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                        }
                    })
                }
                dialog.dismiss()
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    override fun getItemCount(): Int = comptes.size

    class CompteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val solde: TextView = itemView.findViewById(R.id.solde)
        val dateCreation: TextView = itemView.findViewById(R.id.dateCreation)
        val typeCompte: TextView = itemView.findViewById(R.id.typeCompte)
        val btnUpdate: Button = itemView.findViewById(R.id.updateButton)
        val btnDelete: Button = itemView.findViewById(R.id.deleteButton)
    }
}