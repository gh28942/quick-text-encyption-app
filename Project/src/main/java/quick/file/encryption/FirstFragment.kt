package quick.file.encryption

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.tappx.sdk.android.Tappx
import kotlinx.android.synthetic.main.fragment_first.*
import kotlin.system.exitProcess

/**
 * First fragment where the user can input key, salt and text
 */
class FirstFragment : Fragment() {

    private var textResultStr = ""
    private val PREFS_FILENAME = "quick.file.encryption.prefs"

    //This happens before the view is shown to the user
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false)
    }

    //Add click listeners for button actions
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //perform encryption
        view.findViewById<Button>(R.id.button_encrypt).setOnClickListener {
            performAES(true)
        }

        //perform decryption
        view.findViewById<Button>(R.id.button_decrypt).setOnClickListener {
            performAES(false)
        }

        //load a text file
        fabLoad.setOnClickListener {
            val intent = Intent()
                .setType("text/plain")
                .setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), 4628)
        }

        //save txt file
        fabSave.setOnClickListener {
            val intent = Intent()
                .setType("text/plain")
                .setAction(Intent.ACTION_CREATE_DOCUMENT)
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), 2749)
        }

        //clear
        view.findViewById<Button>(R.id.button_clear).setOnClickListener {
            editTextContent.setText("")
        }

        //show Tappx Info (User Agreement)
        Tappx.getPrivacyManager(context).setAutoPrivacyDisclaimerEnabled(true)


        //get user decision (TOS & privacy)
        val sharedPreferences = context?.getSharedPreferences(PREFS_FILENAME, 0)
        val userAgreed = sharedPreferences?.getBoolean("user_agreed", false)

        //The user HAS to read & agree to the TOS and privacy agreement.
        if(!userAgreed!!) { //if the user didn't agree yet, show message box
            val builder = AlertDialog.Builder(context)
            builder.setTitle(getString(R.string.tos_title))
            builder.setMessage(
                getString(R.string.tos_question) +
                        "\n\nhttps://bit.ly/qfe-tos \n\nhttps://bit.ly/qfe-priv"
            )
            builder.setPositiveButton(getString(R.string.tos_agree)) { _, _ ->

                // Store user decision
                val editor = sharedPreferences.edit()
                editor.putBoolean("user_agreed", true)
                editor.apply()
                Toast.makeText(context, getString(R.string.tos_thanks), Toast.LENGTH_SHORT).show()
            }
            builder.setNegativeButton(getString(R.string.tos_no)) { _, _ ->
                exitProcess(0)
            }
            builder.setCancelable(false)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }
    }

    private fun performAES(isEncryption: Boolean) {

        //encrypt or decrypt
        textResultStr = ""
        val originalTextString = editTextContent.text.toString()
        val secretKey = editTextKey.text.toString()
        val salt = editTextSalt.text.toString()
        try {
            textResultStr = if(isEncryption)
                AES.encrypt(originalTextString, secretKey, salt)
            else
                AES.decrypt(originalTextString, secretKey, salt)
        }catch (e: Exception) {
            val builder = AlertDialog.Builder(activity)
            builder.setTitle(getString(R.string.encr_error))
            builder.setMessage(getString(R.string.encr_error_msg))//+e.localizedMessage)
            val dialog: AlertDialog = builder.create()
            dialog.show()
        }

        //Go to new window with result
        val bundle = bundleOf("textResultStr" to textResultStr)
        findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        //Read file
        if (requestCode == 4628 && resultCode == Activity.RESULT_OK) {
            val selectedFile = data?.data //uri with file location

            val inputStream = selectedFile?.let { context?.contentResolver?.openInputStream(it) }
            val fileContent = inputStream?.bufferedReader().use { it?.readText() }
            editTextContent.setText(fileContent)
        }
        //Write file
        else if (requestCode == 2749 && resultCode == Activity.RESULT_OK) {

            val selectedFile = data!!.data
            val inputStream = selectedFile?.let { context?.contentResolver?.openOutputStream(it) }
            inputStream?.bufferedWriter().use { it?.write(editTextContent.text.toString()) }
        }
    }
}
