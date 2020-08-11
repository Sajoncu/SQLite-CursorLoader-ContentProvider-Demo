package com.example.petsappcontentproviderexample.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log


class PetProvider : ContentProvider() {

    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)

    /** Database helper object  */
    private lateinit var mDbHelper: PetDbHelper

    // Static initializer. This is run the first time anything is called from this class.
    init {

        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        // The content URI of the form "content://com.example.android.pets/pets" will map to the
        // integer code {@link #PETS}. This URI is used to provide access to MULTIPLE rows
        // of the pets table.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS, PETS)

        // The content URI of the form "content://com.example.android.pets/pets/#" will map to the
        // integer code {@link #PET_ID}. This URI is used to provide access to ONE single row
        // of the pets table.
        //
        // In this case, the "#" wildcard is used where "#" can be substituted for an integer.
        // For example, "content://com.example.android.pets/pets/3" matches, but
        // "content://com.example.android.pets/pets" (without a number at the end) doesn't match.
        sUriMatcher.addURI(PetContract.CONTENT_AUTHORITY, PetContract.PATH_PETS + "/#", PET_ID)


//        URI_MATCHER.addURI(LentItemsContract.AUTHORITY,
//            "items",
//            ITEM_LIST);
//        URI_MATCHER.addURI(LentItemsContract.AUTHORITY,
//            "items/#",
//            ITEM_ID);
//        URI_MATCHER.addURI(LentItemsContract.AUTHORITY,
//            "photos",
//            PHOTO_LIST);
//        URI_MATCHER.addURI(LentItemsContract.AUTHORITY,
//            "photos/#",
//            PHOTO_ID);
//        URI_MATCHER.addURI(LentItemsContract.AUTHORITY,
//            "entities",
//            ENTITY_LIST);
//        URI_MATCHER.addURI(LentItemsContract.AUTHORITY,
//            "entities/#",
//            ENTITY_ID);
    }

    override fun onCreate(): Boolean {
        mDbHelper = PetDbHelper(context)
        return true
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
        val database = mDbHelper.readableDatabase
        val cursor: Cursor
        val match = sUriMatcher.match(uri)
        cursor = when(match) {
            PETS -> {
                database.query(
                    PetContract.PetEntry.TABLE_NAME, projection, selection, selectionArgs,
                    null, null, sortOrder)
            }
            PET_ID -> {
                val new_selection = PetContract.PetEntry._ID + "=?"
                val newSelectionAars = arrayOf((ContentUris.parseId(uri)).toString())
                database.query(PetContract.PetEntry.TABLE_NAME, projection, new_selection, newSelectionAars,
                    null, null, sortOrder);
            }
            else -> {
                throw IllegalArgumentException("Cannot query unknown URI " + uri)
            }
        }

        // Set notification URI on the Cursor,
        // so we know what content URI the Cursor was created for.
        // If the data at this URI changes, then we know we need to update the Cursor.
        cursor.setNotificationUri(context!!.contentResolver, uri);
        return cursor
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
        val match = sUriMatcher.match(uri)
        when(match) {
            PETS -> {
                return insertPets(uri, values)
            }
            else -> {
                throw IllegalArgumentException("Insertion is not supported for $uri")
            }
        }
    }

    /**
     * Insert a pet into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private fun insertPets(uri: Uri, values: ContentValues?) : Uri?{

        // Check that the name is not null
        val name = values!!.getAsString(PetContract.PetEntry.COLUMN_PET_NAME)
            ?: throw IllegalArgumentException("Pet requires a name")
//        if(name == null) {
//            throw IllegalArgumentException("Pet requires a name")
//        }

        // Check that the gender in valid
        val gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER)
        if(gender == null || !PetContract.PetEntry.isValidGender(gender)) {
            throw IllegalArgumentException("Pet requires valid gender")
        }

        // If the weight is provided, check that it's greater than or equal to 0 kg
        val weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT)
        if (weight != null && weight < 0) {
            throw IllegalArgumentException("Pet requires valid weight")
        }

        val db = mDbHelper.writableDatabase
        val id = db.insert(PetContract.PetEntry.TABLE_NAME, null, values)

        if(id == -1L) {
            Log.e(LOG_TAG, "Failed to insert row for $uri");
            return null
        }

        // Notify all listeners that the data has changed for the pet content URI
        context!!.contentResolver.notifyChange(uri, null);
        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        val match = sUriMatcher.match(uri)
        Log.d("CONTENT_PROVIDER", "Matched URI $match")
        return when(match) {
            PETS -> {
                Log.d("CONTENT_PROVIDER", "Inside update->PETS")
                updatePet(uri, values, selection, selectionArgs)
            }
            PET_ID -> {
                val new_selection = PetContract.PetEntry._ID + "=?"
                val newSelectionAars = arrayOf((ContentUris.parseId(uri)).toString())
                Log.d("CONTENT_PROVIDER", "Inside update->PET_ID and selectionArgs: $newSelectionAars")
                updatePet(uri, values, new_selection, newSelectionAars)
            }
            else -> {
                throw IllegalArgumentException("Insertion is not supported for $uri")
            }
        }
    }

    private fun updatePet(uri: Uri, values: ContentValues?, selection: String?, selectionArgs: Array<out String>?): Int {
        // If the {@link PetEntry#COLUMN_PET_NAME} key is present,
        // check that the name value is not null.
        if (values!!.containsKey(PetContract.PetEntry.COLUMN_PET_NAME)) {
//            val name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME)
//            if(name == null) {
//                throw IllegalArgumentException("Pet requires a name")
//            }

            val name = values.getAsString(PetContract.PetEntry.COLUMN_PET_NAME)
                ?: throw IllegalArgumentException("Pet requires a name");
        }

        // If the {@link PetEntry#COLUMN_PET_GENDER} key is present,
        // check that the gender value is valid.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_GENDER)) {
            val gender = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetContract.PetEntry.isValidGender(gender)) {
                throw IllegalArgumentException("Pet requires valid gender");
            }
        }

        // If the {@link PetEntry#COLUMN_PET_WEIGHT} key is present,
        // check that the weight value is valid.
        if (values.containsKey(PetContract.PetEntry.COLUMN_PET_WEIGHT)) {
            // Check that the weight is greater than or equal to 0 kg
            val weight = values.getAsInteger(PetContract.PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw IllegalArgumentException("Pet requires valid weight");
            }
        }

        // No need to check the breed, any value is valid (including null).

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        // Otherwise, get writeable database to update the data
        val database = mDbHelper.writableDatabase
        // Perform the update on the database and get the number of rows affected
         val rowsUpdated = database.update(PetContract.PetEntry.TABLE_NAME, values, selection, selectionArgs)

        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            context!!.contentResolver.notifyChange(uri, null)
        }

        return rowsUpdated
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        val match = sUriMatcher.match(uri)
        val db = mDbHelper.writableDatabase
        val rowsDeleted: Int

        when(match) {
            PETS-> {
                rowsDeleted = db.delete(PetContract.PetEntry.TABLE_NAME, selection, selectionArgs)
            }
            PET_ID -> {
                val newSelection = PetContract.PetEntry._ID + "=?";
                val newSelectionArgs = arrayOf(ContentUris.parseId(uri).toString())
                rowsDeleted = db.delete(PetContract.PetEntry.TABLE_NAME, newSelection, newSelectionArgs)
            }
            else -> {throw IllegalArgumentException("Deletion did not completed")}
        }
        context!!.contentResolver.notifyChange(uri, null)
        return rowsDeleted
    }

    override fun getType(uri: Uri): String? {
        val match = sUriMatcher.match(uri)
        when(match) {
            PETS -> {
                return PetContract.PetEntry.CONTENT_LIST_TYPE
            }
            PET_ID -> {
                return PetContract.PetEntry.CONTENT_ITEM_TYPE
            }
            else -> {
                throw IllegalStateException("Unknown URI " + uri + " with match " + match)
            }
        }
    }

    companion object {
        /** Tag for the log messages  */
        val LOG_TAG = PetProvider::class.java.simpleName

        /** URI matcher code for the content URI for the pets table  */
        private const val PETS = 100

        /** URI matcher code for the content URI for a single pet in the pets table  */
        private const val PET_ID = 101

        /**
         * UriMatcher object to match a content URI to a corresponding code.
         * The input passed into the constructor represents the code to return for the root URI.
         * It's common to use NO_MATCH as the input for this case.
         */
        private val sUriMatcher = UriMatcher(UriMatcher.NO_MATCH)
    }
}