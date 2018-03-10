package cn.mw.ethwallet.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceManager
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import cn.mw.ethwallet.R
import cn.mw.ethwallet.utils.qr.AddressEncoder
import cn.mw.ethwallet.utils.qr.Contents
import cn.mw.ethwallet.utils.qr.QREncoder
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 22:14
 * @description
 */
class FragmentDetailShare : Fragment() {

    private var ethaddress: String? = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_detail_share, container, false)

        ethaddress = arguments!!.getString("ADDRESS")

        val clipboard = rootView.findViewById(R.id.copytoclip) as Button
        clipboard.setOnClickListener {
            val i = Intent(Intent.ACTION_SEND)
            i.type = "text/plain"
            i.putExtra(Intent.EXTRA_TEXT, ethaddress)
            startActivity(Intent.createChooser(i, "Share via"))
        }

        val scale = context!!.resources.displayMetrics.density
        val qrCodeDimention = (310 * scale + 0.5f).toInt()

        val qrcode = rootView.findViewById(R.id.qrcode) as ImageView

        qrcode.setOnClickListener {
            val clipboard = activity!!.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("", ethaddress)
            clipboard.primaryClip = clip
            Toast.makeText(activity, R.string.wallet_menu_action_copied_to_clipboard, Toast.LENGTH_SHORT).show()
        }

        val prefs = PreferenceManager.getDefaultSharedPreferences(activity)
        val qrCodeEncoder = QREncoder(if (prefs.getBoolean("qr_encoding_erc", true)) AddressEncoder.encodeERC(AddressEncoder(ethaddress)) else ethaddress, null,
                Contents.Type.TEXT, BarcodeFormat.QR_CODE.toString(), qrCodeDimention)
        try {
            val bitmap = qrCodeEncoder.encodeAsBitmap()
            qrcode.setImageBitmap(bitmap)
        } catch (e: WriterException) {
            e.printStackTrace()
        }

        return rootView
    }

}