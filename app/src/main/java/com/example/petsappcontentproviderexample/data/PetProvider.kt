package com.example.petsappcontentproviderexample.data

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.util.Log
import java.lang.IllegalArgumentException


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
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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

        // Return the new URI with the ID (of the newly inserted row) appended at the end
        return ContentUris.withAppendedId(uri, id)
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getType(uri: Uri): String? {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
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