package com.example.petsappcontentproviderexample

import android.content.ContentUris
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView.OnItemClickListener
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import com.example.petsappcontentproviderexample.adapter.PetCursorAdapter
import com.example.petsappcontentproviderexample.data.PetContract
import com.example.petsappcontentproviderexample.data.PetContract.PetEntry
import com.google.android.material.floatingactionbutton.FloatingActionButton


class CatalogActivity : AppCompatActivity() {
    lateinit var mPetsListView: ListView
    lateinit var mCursorAdapter:PetCursorAdapter
    lateinit var fab: FloatingActionButton
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalog)

        // Initialize pet ListView
        mPetsListView = findViewById(R.id.list)

        // Add an empty view to the ListView
        val emptyView:View = findViewById(R.id.empty_view)
        mPetsListView.emptyView = emptyView

        // Initialize floating action button
        fab = findViewById(R.id.fab)
        fab.setOnClickListener{
            val intent = Intent(this, EditorActivity::class.java)
            startActivity(intent)
        }

        // Initialize PetCursorAdapter
        initializeAdapter()

        // Set click listener in a list item
        mPetsListView.setOnItemClickListener{ adapterView, view, position, id ->

            val intent = Intent(this@CatalogActivity, EditorActivity::class.java)

            val currentPetUri: Uri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id)
            intent.data = currentPetUri

            // Launch the {@link EditorActivity} to display the data for the current pet.
            startActivity(intent)
        }

        //supportLoaderManager.initLoader(PET_LOADER, null, this)
        supportLoaderManager.initLoader(PET_LOADER, null, callback)
        //LoaderManager.getInstance(this).initLoader(PET_LOADER, null, this)
    }

    private val callback = object: LoaderManager.LoaderCallbacks<Cursor>{
        override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
            // Define a projection that specifies the columns from the table we care about.
            val projection = arrayOf(
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED
            )

            return CursorLoader(applicationContext,           // Parent activity context
                PetContract.PetEntry.CONTENT_URI,      // Provider content URI to query
                projection,                           // Columns to include in the resulting Cursor
                null,                       // No selection clause
                null,                   // No selection arguments
                null                       // Default sort order
            )
            Log.d("CONTENT_PROVIDER", "onCreateLoader has been called")
        }

        override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
            Log.d("CONTENT_PROVIDER", "onLoadFinished has been called DATA: ${data.toString()} Loader: $loader")
            mCursorAdapter.swapCursor(data)
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            Log.d("CONTENT_PROVIDER", "onLoaderReset has been called")
            mCursorAdapter.swapCursor(null)
        }
    }
    /**
     * Initialize Adapter
     */
    private fun initializeAdapter() {
        mCursorAdapter = PetCursorAdapter(this, null)
        mPetsListView.setAdapter(mCursorAdapter)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        //return super.onCreateOptionsMenu(menu)
        menuInflater.inflate(R.menu.menu_catalog, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        supportLoaderManager.restartLoader(0, null, callback)
        //LoaderManager.getInstance(this).restartLoader(PET_LOADER, null, this)
    }

//    override fun onCreateLoader(id: Int, args: Bundle?): Loader<Cursor> {
//        // Define a projection that specifies the columns from the table we care about.
//        val projection = arrayOf(
//            PetEntry._ID,
//            PetEntry.COLUMN_PET_NAME,
//            PetEntry.COLUMN_PET_BREED
//        )
//
//        return CursorLoader(this,           // Parent activity context
//            PetContract.PetEntry.CONTENT_URI,      // Provider content URI to query
//            projection,                           // Columns to include in the resulting Cursor
//            null,                       // No selection clause
//            null,                   // No selection arguments
//            null                       // Default sort order
//        )
//        Log.d("CONTENT_PROVIDER", "onCreateLoader has been called")
//    }
//
//    override fun onLoadFinished(loader: Loader<Cursor>, data: Cursor?) {
//        Log.d("CONTENT_PROVIDER", "onLoadFinished has been called DATA: ${data.toString()} Loader: $loader")
//        mCursorAdapter.swapCursor(data)
//    }
//
//    override fun onLoaderReset(loader: Loader<Cursor>) {
//        Log.d("CONTENT_PROVIDER", "onLoaderReset has been called")
//        mCursorAdapter.swapCursor(null)
//    }

    companion object {
        /** Identifier for the pet data loader  */
        private val PET_LOADER = 0
    }
}
