package cn.mw.ethwallet.network

import android.content.Context
import android.util.Log
import cn.mw.ethwallet.domain.request.TokenDisplay
import cn.mw.ethwallet.domain.request.TransactionDisplay
import cn.mw.ethwallet.domain.request.WalletDisplay
import cn.mw.ethwallet.domain.request.WatchWallet
import cn.mw.ethwallet.interfaces.LastIconLoaded
import cn.mw.ethwallet.interfaces.StorableWallet
import cn.mw.ethwallet.utils.AddressNameConverter
import cn.mw.ethwallet.utils.Settings
import org.json.JSONException
import org.json.JSONObject
import java.math.BigDecimal
import java.math.BigInteger
import java.util.ArrayList

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 16:54
 * @description
 */
object ResponseParser {


    fun parseTransactions(response: String, walletname: String, address: String, type: Byte): ArrayList<TransactionDisplay> {
        try {
            val erg = ArrayList<TransactionDisplay>()

            val data = JSONObject(response).getJSONArray("result")
            for (i in 0 until data.length()) {
                var from = data.getJSONObject(i).getString("from")
                var to = data.getJSONObject(i).getString("to")
                var vorzeichen = "+"
                if (address.equals(data.getJSONObject(i).getString("from"), ignoreCase = true)) {
                    vorzeichen = "-"
                } else {
                    val temp = from
                    from = to
                    to = temp
                }
                if (data.getJSONObject(i).getString("value") == "0" && !Settings.showTransactionsWithZero)
                    continue // Skip contract calls or empty transactions
                erg.add(TransactionDisplay(
                        from,
                        to,
                        BigInteger(vorzeichen + data.getJSONObject(i).getString("value")),
                        if (data.getJSONObject(i).has("confirmations")) data.getJSONObject(i).getInt("confirmations") else 13,
                        data.getJSONObject(i).getLong("timeStamp") * 1000,
                        walletname,
                        type,
                        data.getJSONObject(i).getString("hash"),
                        if (data.getJSONObject(i).has("nonce")) data.getJSONObject(i).getString("nonce") else "0",
                        data.getJSONObject(i).getLong("blockNumber"),
                        data.getJSONObject(i).getInt("gasUsed"),
                        if (data.getJSONObject(i).has("gasPrice")) data.getJSONObject(i).getLong("gasPrice") else 0,
                        data.getJSONObject(i).has("isError") && data.getJSONObject(i).getInt("isError") == 1
                ))
            }


            return erg
        } catch (e: JSONException) {
            return ArrayList<TransactionDisplay>()
        }

    }

    @Throws(Exception::class)
    fun parseWallets(response: String, storedwallets: ArrayList<StorableWallet>, context: Context): ArrayList<WalletDisplay> {
        val display = ArrayList<WalletDisplay>()
        val data = JSONObject(response).getJSONArray("result")
        for (i in storedwallets.indices) {
            var balance = BigInteger("0")
            for (j in 0 until data.length()) {
                if (data.getJSONObject(j).getString("account").equals(storedwallets[i].pubKey, ignoreCase = true)) {
                    balance = BigInteger(data.getJSONObject(i).getString("balance"))
                    break
                }
            }
            val walletname = AddressNameConverter.getInstance(context).get(storedwallets[i].pubKey)
            display.add(WalletDisplay(
                    walletname ?: "New Wallet",
                    storedwallets[i].pubKey,
                    balance,
                    if (storedwallets[i] is WatchWallet) WalletDisplay.WATCH_ONLY else WalletDisplay.NORMAL
            ))
        }
        return display
    }

    @Throws(Exception::class)
    fun parseTokens(c: Context, response: String, callback: LastIconLoaded): ArrayList<TokenDisplay> {
        Log.d("tokentest", response)
        val display = ArrayList<TokenDisplay>()
        val data = JSONObject(response).getJSONArray("tokens")
        for (i in 0 until data.length()) {
            val currentToken = data.getJSONObject(i)
            try {
                display.add(TokenDisplay(
                        currentToken.getJSONObject("tokenInfo").getString("name"),
                        currentToken.getJSONObject("tokenInfo").getString("symbol"),
                        BigDecimal(currentToken.getString("balance")),
                        currentToken.getJSONObject("tokenInfo").getInt("decimals"),
                        currentToken.getJSONObject("tokenInfo").getJSONObject("price").getDouble("rate"),
                        currentToken.getJSONObject("tokenInfo").getString("address"),
                        currentToken.getJSONObject("tokenInfo").getString("totalSupply"),
                        currentToken.getJSONObject("tokenInfo").getLong("holdersCount").toDouble(),
                        0
                ))
            } catch (e: JSONException) {
                e.printStackTrace()
            }

            // Download icon and cache it
            EtherscanAPI.instance.loadTokenIcon(c, currentToken.getJSONObject("tokenInfo").getString("name"), i == data.length() - 1, callback)

        }
        return display
    }

    @Throws(JSONException::class)
    @JvmOverloads
    fun parseBalance(response: String, comma: Int = 7): String {
        val balance = JSONObject(response).getString("result")
        return if (balance == "0") "0" else BigDecimal(balance).divide(BigDecimal(1000000000000000000.0), comma, BigDecimal.ROUND_UP).toPlainString()
    }

    @Throws(Exception::class)
    fun parseGasPrice(response: String): BigInteger {
        val gasprice = JSONObject(response).getString("result")
        return BigInteger(gasprice.substring(2), 16)
    }

    // Only call for each address, not the combined one
    /*public static void saveNewestNoncesOfAddresses(Context c, ArrayList<TransactionDisplay> tx, String address){
        try{
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(c);
            long curNonce = -1;
            for(int i=tx.size()-1; i >= 0; i--){
                if(tx.get(i).getFromAddress().equals(address)) { // From address is always our address (thanks to @parseTransactions above for that)
                    curNonce = Long.parseLong(tx.get(tx.size() - 1).getNounce());
                    break;
                }
            }

            long oldNonce = preferences.getLong("NONCE"+address, 0);
            if(curNonce > oldNonce){
                SharedPreferences.Editor editor = preferences.edit();
                editor.putLong("NONCE"+address, curNonce);
                editor.commit();
            }
        }catch(Exception e){
        }
    }*/

    fun parsePriceConversionRate(response: String): Double {
        try {
            val jo = JSONObject(response).getJSONObject("rates")
            val key = jo.keys().next()
            return jo.getDouble(key)
        } catch (e: Exception) {
            return 1.0
        }

    }

}