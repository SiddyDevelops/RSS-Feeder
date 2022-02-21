package com.siddydevelops.rssfeeder

import android.content.Context
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*
import java.net.URL
import kotlin.properties.Delegates

class FeedEntry {
    var name: String = ""
    var artist: String = ""
    var releaseDate: String = ""
    var summary: String = ""
    var imageURL: String = ""

    override fun toString(): String {
        return """
            Name = $name
            Artist = $artist
            ReleaseDate = $releaseDate
            Summary = $summary
            ImageURL = $imageURL
        """.trimIndent()
    }
}

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"

    private var feedURl: String = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
    private var feedLimit = 10

    private var feedCacheURL = "INVALIDATED"
    private val STATE_URL = "feedURL"
    private val STATE_LIMIT = "feedLimit"

    private var downloadData: DownloadData? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if(savedInstanceState != null) {
            feedURl = savedInstanceState.getString(STATE_URL).toString()
            feedLimit = savedInstanceState.getInt(STATE_LIMIT)
        }
        downloadURL(feedURl.format(feedLimit))

    }

    private fun downloadURL(feedURL: String)
    {
        if(feedURL != feedCacheURL) {
            downloadData = DownloadData(this, xmlListView)
            downloadData?.execute(feedURL)
            feedCacheURL = feedURL
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.feeds_menu, menu)
        if(feedLimit == 10) {
            menu?.findItem(R.id.mnu10)?.isChecked = true
        } else {
            menu?.findItem(R.id.mnu25)?.isChecked = true
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.mnuFree -> feedURl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topfreeapplications/limit=%d/xml"
            R.id.mnuPaid -> feedURl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/toppaidapplications/limit=%d/xml"
            R.id.mnuSongs-> feedURl = "http://ax.itunes.apple.com/WebObjects/MZStoreServices.woa/ws/RSS/topsongs/limit=%d/xml"
            R.id.mnu10, R.id.mnu25-> {
                if(!item.isChecked) {
                    item.isChecked = true
                    feedLimit = 35 - feedLimit
                }
            }
            R.id.mnuRefresh -> feedCacheURL = "INVALIDATED"
            else -> return super.onOptionsItemSelected(item)
        }
        downloadURL(feedURl.format(feedLimit))
        return true
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(STATE_URL, feedURl)
        outState.putInt(STATE_LIMIT, feedLimit)
    }

    override fun onDestroy() {
        super.onDestroy()
        downloadData?.cancel(true)
    }

    companion object {
        private class DownloadData(context: Context, listView: ListView): AsyncTask<String, Void, String>()
        {
            private val TAG = "DownloadData"
            var propContext: Context by Delegates.notNull()
            var propListView: ListView by Delegates.notNull()

            init {
                propContext = context
                propListView = listView
            }

            override fun doInBackground(vararg url: String?): String {
                //Log.d(TAG,"doInBackground: starts with ${url[0]}")
                val rssFeed = downloadXML(url[0])
                if(rssFeed.isEmpty())
                {
                    Log.e(TAG, "doInBackground: Error downloading")
                }
                return rssFeed
            }

            override fun onPostExecute(result: String) {
                super.onPostExecute(result)
                //Log.d(TAG,"onPostExecute: parameter is $result")
                val parseApplications = ParseApplications()
                parseApplications.parse(result)

//                val arrayAdapter = ArrayAdapter<FeedEntry>(propContext, R.layout.list_item, parseApplications.applications)
//                propListView.adapter = arrayAdapter
                val feedAdapter = FeedAdapter(propContext,R.layout.list_record, parseApplications.applications)
                propListView.adapter = feedAdapter
            }

            private fun downloadXML(urlPath: String?): String
            {
                return URL(urlPath).readText()
            }

//            private fun downloadXML(urlPath: String?): String
//            {
//                val xmlResult = StringBuilder()
//                try {
//                    val url = URL(urlPath)
//                    val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
//                    val response = connection.responseCode
//                    Log.d(TAG, "downloadXML: The response code was $response")
//
////            val inputStream = connection.inputStream
////            val inputStreamReader = InputStreamReader(inputStream)
////            val reader = BufferedReader(inputStreamReader)
////                    val reader = BufferedReader(InputStreamReader(connection.inputStream))
////                    val inputBuffer = CharArray(500)
////                    var charsRead = 0
////                    while (charsRead >= 0) {
////                        charsRead = reader.read(inputBuffer)
////                        if (charsRead > 0)
////                            xmlResult.append(String(inputBuffer, 0, charsRead))
////                    }
////                    reader.close()
//
//                    //val stream = connection.inputStream
//                    connection.inputStream.buffered().reader().use {xmlResult.append(it.readText())}
//
//                    Log.d(TAG, "Received ${xmlResult.length} bytes")
//                    return xmlResult.toString()
//
////                } catch(e: MalformedURLException) {
////                    Log.e(TAG, "downloadXML: Invalid URL ${e.message}")
////                } catch(e: IOException) {
////                    Log.e(TAG, "downloadXML: IO Exception reading data ${e.message}")
////                } catch(e: SecurityException){
////                    Log.e(TAG, "downloadXML: Security Exception. Needs permission? ${e.message}")
////                } catch(e: Exception) {
////                    Log.e(TAG, "downloadXML: Unknown Exception ${e.message}")
////                }
//                } catch(e: Exception) {
//                    val errorMessage: String = when(e) {
//                        is MalformedURLException -> "downloadXML: Invalid URL ${e.message}"
//                        is IOException -> "downloadXML: IO Exception reading data ${e.message}"
//                        is SecurityException -> "downloadXML: Security Exception. Needs permission? ${e.message}"
//                        else -> "downloadXML: Unknown Exception ${e.message}"
//                    }
//                    Log.e(TAG, errorMessage)
//                }
//                return ""       // Error Occurred
//            }
        }
    }
}