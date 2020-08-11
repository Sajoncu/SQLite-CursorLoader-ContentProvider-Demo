package com.example.petsappcontentproviderexample

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.DialogInterface
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.petsappcontentproviderexample.data.PetContract
import com.example.petsappcontentproviderexample.data.PetContract.PetEntry


class EditorActivity : AppCompatActivity() {

    /** Identifier for the pet data loader  */
    private val EXISTING_PET_LOADER = 0

    /** Content URI for the existing pet (null if it's a new pet)  */
    private var mCurrentPetUri: Uri? = null

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



    /**
     * OnTouchListener that listens for any user touches on a View, implying that they are modifying
     * the view, and we change the mPetHasChanged boolean to true.
     */
    private val mTouchListener = object: View.OnTouchListener {
        @SuppressLint("ClickableViewAccessibility")
        override fun onTouch(v: View?, event: MotionEvent?): Boolean {
            mPetHasChanged = true
            return false
        }
    }

    private val cursorCallback = object: LoaderManager.LoaderCallbacks<Cursor>{
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            val projection = arrayOf(
                PetContract.PetEntry._ID,
                PetContract.PetEntry.COLUMN_PET_NAME,
                PetContract.PetEntry.COLUMN_PET_BREED,
                PetContract.PetEntry.COLUMN_PET_GENDER,
                PetContract.PetEntry.COLUMN_PET_WEIGHT
            )

            return CursorLoader(applicationContext,
                mCurrentPetUri!!,
                projection,
                null,
                null,
                null
                )
        }

        override fun onLoadFinished(loader: Loader<Cursor>, cursor: Cursor?) {
            if(cursor == null || cursor.getCount() < 1) {
                return
            }

            // Proceed with moving to the first row of the cursor and reading data from it
            // (This should be the only row in the cursor)
            if(cursor.moveToFirst()) {
                // Find the columns of pet attributes that we're interested in
                val nameColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_NAME)
                val breedColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_BREED)
                val genderColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_GENDER)
                val weightColumnIndex = cursor.getColumnIndex(PetEntry.COLUMN_PET_WEIGHT)

                // Extract out the value from the Cursor for the given column index
                val name = cursor.getString(nameColumnIndex)
                val breed = cursor.getString(breedColumnIndex)
                val gender = cursor.getInt(genderColumnIndex)
                val weight = cursor.getInt(weightColumnIndex)

                // Update the views on the screen with the values from the database
                mNameEditText.setText(name)
                mBreedEditText.setText(breed)
                mWeightEditText.setText(weight.toString())

                Log.d("CONTENT_PROVIDER", "Gender: $gender")
                when(gender) {
                    PetEntry.GENDER_MALE -> {
                        mGenderSpinner.setSelection(1)
                        return
                    }
                    PetEntry.GENDER_FEMALE -> {
                        mGenderSpinner.setSelection(2)
                        return
                    }
                    else -> {
                        mGenderSpinner.setSelection(0)
                        return
                    }
                }
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            // If the loader is invalidated, clear out all the data from the input fields.
            mNameEditText.setText("")
            mBreedEditText.setText("")
            mWeightEditText.setText("")
            mGenderSpinner.setSelection(0) // Select "Unknown" gender
        }

    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)

        val intent = getIntent()
        if(intent.data != null) {
            mCurrentPetUri = intent.data!!
        }


        Log.d("CONTENT_PROVIDER", "URI for edit Pet: ${mCurrentPetUri.toString()}")

        if(mCurrentPetUri == null) {
            title = getString(R.string.editor_activity_title_new_pet)

            // Invalidate the options menu, so the "Delete" menu option can be hidden.
            // (It doesn't make sense to delete a pet that hasn't been created yet.)
            invalidateOptionsMenu()

        } else {
            title = getString(R.string.editor_activity_title_edit_pet)

            // Initialize a loader to read the pet data from the database
            // and display the current values in the editor
            supportLoaderManager.initLoader(EXISTING_PET_LOADER, null, cursorCallback)
        }




        // Find all relevant views that we will need to read user input from
        mNameEditText   = findViewById(R.id.edit_pet_name)
        mBreedEditText  = findViewById(R.id.edit_pet_breed)
        mWeightEditText = findViewById(R.id.edit_pet_weight)
        mGenderSpinner  = findViewById(R.id.spinner_gender)

        // Set OnTouchListener all the input fields so that we can keep track that the user
        // has touched or modified them
        mNameEditText.setOnTouchListener(mTouchListener)
        mBreedEditText.setOnTouchListener(mTouchListener)
        mWeightEditText.setOnTouchListener(mTouchListener)
        mGenderSpinner.setOnTouchListener(mTouchListener)


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
        mGenderSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onNothingSelected(parent: AdapterView<*>?) {
                mGender = PetEntry.GENDER_UNKNOWN
            }

            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selection = parent!!.getItemAtPosition(position).toString()
                Log.d("CONTETN_PROVIDER", "Gendr Selection: $selection")
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

        }
    }

    override fun onBackPressed() {
        if(!mPetHasChanged) {
            super.onBackPressed()
            return
        }

        // Notify the user that he changed pet
        // and make sure he want to save the changed or not
        val discardButtonClickableSpan =
            DialogInterface.OnClickListener{ dialogInterface: DialogInterface, i: Int -> finish()}
        // this will show a dialog to make sure that there are unsaved change
        showUnsaveChangedDialog(discardButtonClickableSpan)

    }

    private fun showUnsaveChangedDialog(discardButtonClickableSpan: DialogInterface.OnClickListener) {
        val dialog = AlertDialog.Builder(this)

        dialog.apply{
            setMessage(R.string.unsaved_changes_dialog_msg)
            setPositiveButton(R.string.discard, discardButtonClickableSpan)
            setNegativeButton(R.string.cancel) {dialog, id ->
                run {
                    dialog?.dismiss()
                }
            }
        }.create().show()

    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        super.onPrepareOptionsMenu(menu)
        if(mCurrentPetUri == null) {
            val menuItem = menu!!.findItem(R.id.action_delete)
            menuItem.isVisible = false
        }
        return true
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
                    finish()
                    return true
                } catch(ex: Exception) {
                    Log.d("CONTENT_PROVIDER", "Exception $ex")
                    //Toast.makeText(this, "Exception $ex", Toast.LENGTH_SHORT).show()
                }
            }

            R.id.action_delete -> {
                Log.d("CONTENT_PROVIDER", "Delete Menu item clicked")
                showDeleteConfirmationDialog()
                return true
                //Toast.makeText(this, "Delete Menu item clicked", Toast.LENGTH_SHORT).show()
            }

            android.R.id.home-> {
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    /**
     * Prompt the user to confirm that they want to delete this pet.
     */
    private fun showDeleteConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage(R.string.delete_dialog_msg)
        builder.setPositiveButton(R.string.delete) { dialog, which -> run {
            deletePet()
        }}
        builder.setNegativeButton(R.string.cancel) {dialog, id ->
            run {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                dialog?.dismiss()
            }
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }

    private fun deletePet() {
        if(mCurrentPetUri != null) {
            // Call the ContentResolver to delete the pet at the given content URI.
            // Pass in null for the selection and selection args because the mCurrentPetUri
            // content URI already identifies the pet that we want.
            val rowsDeleted = contentResolver.delete(mCurrentPetUri!!, null, null)

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_pet_failed),
                    Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_pet_successful),
                    Toast.LENGTH_SHORT).show();
            }
        }
        finish()
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
        if (mCurrentPetUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(breedString) && TextUtils.isEmpty(weightString) && mGender == PetEntry.GENDER_UNKNOWN) {
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

        if(mCurrentPetUri == null) {
            val newUri = contentResolver.insert(PetEntry.CONTENT_URI, values)
            // Show a toast message depending on whether or not the insertion was successful.
            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Log.d("CONTENT_PROVIDER", getString(R.string.editor_insert_pet_failed)+"URI: $newUri")
                Toast.makeText(this, getString(R.string.editor_insert_pet_failed),
                    Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Log.d("CONTENT_PROVIDER", getString(R.string.editor_insert_pet_successful)+"URI: $newUri")
                Toast.makeText(this, getString(R.string.editor_insert_pet_successful),
                    Toast.LENGTH_SHORT).show();
            }
        } else {
            val rowsAffected = contentResolver.update(mCurrentPetUri!!, values, null, null);

            // Show a toast message depending on whether or not the update was successful.
            if (rowsAffected == 0) {
                // If no rows were affected, then there was an error with the update.
                Toast.makeText(this, getString(R.string.editor_update_pet_failed),
                    Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the update was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_update_pet_successful),
                    Toast.LENGTH_SHORT).show();
            }
        }

    }

}
