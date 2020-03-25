package quick.file.encryption

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.tappx.sdk.android.TappxAdError
import com.tappx.sdk.android.TappxInterstitial
import com.tappx.sdk.android.TappxInterstitialListener
import kotlinx.android.synthetic.main.fragment_second.*

/**
 * Fragment where the result (encrypted or decrypted text) is shown
 */
class SecondFragment : Fragment() {

    private var tappxInterstitial: TappxInterstitial? = null
    private var randomNumberShowAd = true

    //This happens before the view is shown to the user
    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        //load ad (50:50 chance)
        randomNumberShowAd = Math.random() > 0.5
        if(randomNumberShowAd) {
            tappxInterstitial = TappxInterstitial(context, "YOUR_TAPPX_KEY_HERE")
            tappxInterstitial!!.setAutoShowWhenReady(false)
            tappxInterstitial!!.setListener(object : TappxInterstitialListener {
                override fun onInterstitialLoaded(tappxInterstitial: TappxInterstitial) {
                }
                override fun onInterstitialLoadFailed(tappxInterstitial: TappxInterstitial,tappxAdError: TappxAdError) {
                    val intent = Intent(context, default_ad::class.java)
                    startActivity(intent)
                }
                override fun onInterstitialShown(tappxInterstitial: TappxInterstitial) {
                }
                override fun onInterstitialClicked(tappxInterstitial: TappxInterstitial) {
                }
                override fun onInterstitialDismissed(tappxInterstitial: TappxInterstitial) {
                }
            })
            tappxInterstitial!!.loadAd()
        }

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_second, container, false)
    }

    //This happens when the view gets destroyed - e.g. when the user presses the 'back' button (in app or on phone)
    override fun onDestroy() {
        super.onDestroy()

        //show ad
        if(randomNumberShowAd) {
            if (tappxInterstitial?.isReady!!) {
                tappxInterstitial?.show()
            }
            else {
                if (tappxInterstitial != null)
                    tappxInterstitial!!.destroy()
                val intent = Intent(context, default_ad::class.java)
                startActivity(intent)
            }
        }
    }

    //Add click listeners for button actions
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //get en/decrypted text
        val textResultStr = arguments?.getString("textResultStr")
        editTextResult.setText(textResultStr)

        //back to first view
        view.findViewById<Button>(R.id.buttonBack).setOnClickListener {
            activity?.onBackPressed()
        }

        //copy
        view.findViewById<Button>(R.id.buttonCopy).setOnClickListener {

            val clipboard = context!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText(getString(R.string.copied_text), editTextResult.text)
            clipboard.setPrimaryClip(clip)

            Toast.makeText(context, getString(R.string.text_copied), Toast.LENGTH_LONG).show()
        }

        //save txt file
        fabSaveResult.setOnClickListener {
            val intent = Intent()
                .setType("text/plain")
                .setAction(Intent.ACTION_CREATE_DOCUMENT)
            startActivityForResult(Intent.createChooser(intent, getString(R.string.select_file)), 2712)
        }
    }

    //Catch Intents, e.g. if the user chose a destination for a file to save
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 2712 && resultCode == Activity.RESULT_OK) {

            val selectedFile = data!!.data
            val inputStream = selectedFile?.let { context?.contentResolver?.openOutputStream(it) }
            inputStream?.bufferedWriter().use { it?.write(editTextResult.text.toString()) }
        }
    }
}
