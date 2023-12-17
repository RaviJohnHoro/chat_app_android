package com.example.chatapp

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.ComponentActivity
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.chatapp.ui.theme.ChatAppTheme
import io.flutter.embedding.android.FlutterActivity
import io.flutter.embedding.engine.FlutterEngine
import io.flutter.plugin.common.MethodChannel
import androidx.appcompat.app.AppCompatActivity
import android.app.AlertDialog
import android.content.DialogInterface

class MainActivity : ComponentActivity() {

    private val FLUTTER_CHAT_ACTIVITY_REQUEST_CODE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var rollButton =  findViewById<Button>(R.id.roll_button);
        rollButton.setOnClickListener {
            val intent = Intent(this, FlutterChatActivity::class.java)
            startActivityForResult(intent, FLUTTER_CHAT_ACTIVITY_REQUEST_CODE)
            //startActivity(intent) // Start the target activity
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == FLUTTER_CHAT_ACTIVITY_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                val productListString = data?.getStringExtra("productList")
                // Handle the productListString as needed
                showProductsDialog(productListString ?: "No products")
            } else {
                // Handle the result accordingly (canceled or ended with an error)
            }
        }
    }

    private fun showProductsDialog(productListText: String) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Product List")
        builder.setMessage(productListText)

        builder.setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
            dialogInterface.dismiss()
        }

        val alertDialog = builder.create()
        alertDialog.show()
    }
}

class FlutterChatActivity: FlutterActivity() {
    private val DATA_CHANNEL = "data_channel"
    private lateinit var dataChannel: MethodChannel

    override fun configureFlutterEngine(flutterEngine: FlutterEngine) {
        super.configureFlutterEngine(flutterEngine)

        dataChannel = MethodChannel(flutterEngine.dartExecutor.binaryMessenger, DATA_CHANNEL)

        dataChannel.setMethodCallHandler { call, result ->
            if (call.method == "sendDataToAndroid") {
                val productList = call.argument<List<Map<String, Any>>>("products")
                //finish()
                handleProductsInAndroid(productList)
                //result.success(null) // Respond to the Flutter side
            } else {
                result.notImplemented()
            }
        }
    }

    private fun handleProductsInAndroid(productList: List<Map<String, Any>>?) {
        productList?.let {
            val productsStringBuilder = StringBuilder()

            for (product in it) {
                productsStringBuilder.append(product)
            }

            println(productsStringBuilder)

            val resultIntent = Intent()
            resultIntent.putExtra("productList", productsStringBuilder.toString())
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }

    private fun showProductsDialog(productsText: String) {
        val alertDialogBuilder = AlertDialog.Builder(this)
        alertDialogBuilder.setTitle("Products List")
        alertDialogBuilder.setMessage(productsText)

        alertDialogBuilder.setPositiveButton("OK") { dialogInterface: DialogInterface, _: Int ->
            // Handle OK button click if needed
            dialogInterface.dismiss()
        }

        val alertDialog = alertDialogBuilder.create()
        alertDialog.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sendDataToFlutter("Hello from Kotlin!")
    }

    fun sendDataToFlutter(data: String) {
        dataChannel.invokeMethod("sendData", mapOf("data" to data))
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
            text = "Hello $name!",
            modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    ChatAppTheme {
        Greeting("Android")
    }
}