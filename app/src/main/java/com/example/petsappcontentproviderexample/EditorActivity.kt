package com.example.petsappcontentproviderexample

import android.content.ContentValues
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.petsappcontentproviderexample.data.PetContract.PetEntry


class EditorActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    /** Identifier for the pet data loader  */
    private val EXISTING_PET_LOADER = 0

    /** Content URI for the existing pet (null if it's a new pet)  */
    private lateinit var mCurrentPetUri: Uri

    /** EditText field to enter the pet's name  */
    private lateinit var mNameEditText: EditText

    /** EditText field to enter the pet's breed  */
    private lateinit var mBreedEditText: EditText

    /** EditText field to enter the pet's weight  */
    private lateinit var mWeightEditText: EditText

    /** EditText field to enter the pet's gender  */
    private lateinit var mGenderSpinner: Spinner

    /**
     * Gender of the pet. The possible valid values are in the PetContract.java file:
     * [PetEntry.GENDER_UNKNOWN], [PetEntry.GENDER_MALE], or
     * [PetEntry.GENDER_FEMALE].
     */
    private var mGender = PetEntry.GENDER_UNKNOWN

    /** Boolean flag that keeps track of whether the pet has been edited (true) or not (false)  */
    private var mPetHasChanged = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        // Find all relevant views that we will need to read user input from
        mNameEditText = findViewById(R.id.edit_pet_name)
        mBreedEditText = findViewById(R.id.edit_pet_breed)
        mWeightEditText = findViewById(R.id.edit_pet_weight)
        mGenderSpinner = findViewById(R.id.spinner_gender)

        setupSpinner()
    }

    /**
     * Setup the dropdown spinner that allows the user to select the gender of the pet.
     */
    private fun setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        val genderSpinnerAdapter: ArrayAdapter<*> = ArrayAdapter.createFromResource(
            this,
            R.array.array_gender_options,
            android.R.layout.simple_spinner_item
        )

        // Specify dropdown layout style - simple list view with 1 item per line
        // Specify dropdown layout style - simple list view with 1 item per line
        genderSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line)

        // Apply the adapter to the spinner
        // Apply the adapter to the spinner
        mGenderSpinner.adapter = genderSpinnerAdapter
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_editor, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.action_save -> {
                try{
                    savePet()
                } catch(ex: Exception) {
                    Log.d("CONTENT_PROVIDER", "Exception $ex")
                    //Toast.makeText(this, "Exception $ex", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.action_delete -> {
                Log.d("CONTENT_PROVIDER", "Delete Menu item clicked")
                //Toast.makeText(this, "Delete Menu item clicked", Toast.LENGTH_SHORT).show()
            }

            android.R.id.home-> {

            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Get user input from editor and save pet into database.
     */
    private fun savePet() {
        // Read from input fields
        // Use trim to eliminate leading or trailing white space
        val nameString = mNameEditText.text.toString().trim()
        val breedString = mBreedEditText.text.toString().trim()
        val weightString = mWeightEditText.text.toString().trim()

        // Check if this is supposed to be a new pet
        // and check if all the fields in the editor are blank
        if (TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) && TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN) {
            return
        }

        // Create a ContentValues object where column names are the keys,
        // and pet attributes from the editor are the values.
        val values = ContentValues()
        values.put(PetEntry.COLUMN_PET_NAME, nameString)
        values.put(PetEntry.COLUMN_PET_BREED, breedString)
        values.put(PetEntry.COLUMN_PET_GENDER, mGender)

        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        // If the weight is not provided by the user, don't try to parse the string into an
        // integer value. Use 0 by default.
        var weight = 0
        if (!TextUtils.isEmpty(weightString)) {
            weight = weightString.toInt()
        }
        values.put(PetEntry.COLUMN_PET_WEIGHT, weight)

        val newUri = contentResolver.insert(PetEntry.CONTENT_URI, values)

        newUri.let {
            Toast.makeText(this, "Item Inserted into this ${it.toString()}", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        val selection = parent!!.getItemAtPosition(position).toString()
        if(!TextUtils.isEmpty(selection)) {
            if(selection == getString(R.string.gender_male)) {
                mGender = PetEntry.GENDER_MALE
            } else if(selection == getString(R.string.gender_female)) {
                mGender = PetEntry.GENDER_FEMALE
            } else {
                mGender = PetEntry.GENDER_UNKNOWN
            }
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        mGender = PetEntry.GENDER_UNKNOWN
    }
}
