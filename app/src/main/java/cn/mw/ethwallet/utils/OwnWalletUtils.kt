package cn.mw.ethwallet.utils

import org.web3j.crypto.*
import org.web3j.crypto.Keys.ADDRESS_LENGTH_IN_HEX
import org.web3j.crypto.Keys.PRIVATE_KEY_LENGTH_IN_HEX
import org.web3j.crypto.WalletUtils
import org.web3j.protocol.ObjectMapperFactory
import org.web3j.utils.Numeric
import java.io.File
import java.io.IOException
import java.security.InvalidAlgorithmParameterException
import java.security.NoSuchAlgorithmException
import java.security.NoSuchProviderException

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 18:23
 * @description
 */
object OwnWalletUtils  {

    // OVERRIDING THOSE METHODS BECAUSE OF CUSTOM WALLET NAMING (CUTING ALL THE TIMESTAMPTS FOR INTERNAL STORAGE)

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class, CipherException::class, IOException::class)
    fun generateFullNewWalletFile(password: String, destinationDirectory: File): String {

        return generateNewWalletFile(password, destinationDirectory, true)
    }

    @Throws(NoSuchAlgorithmException::class, NoSuchProviderException::class, InvalidAlgorithmParameterException::class, CipherException::class, IOException::class)
    fun generateLightNewWalletFile(password: String, destinationDirectory: File): String {

        return generateNewWalletFile(password, destinationDirectory, false)
    }

    @Throws(CipherException::class, IOException::class, InvalidAlgorithmParameterException::class, NoSuchAlgorithmException::class, NoSuchProviderException::class)
    fun generateNewWalletFile(
            password: String, destinationDirectory: File, useFullScrypt: Boolean): String {

        val ecKeyPair = Keys.createEcKeyPair()
        return generateWalletFile(password, ecKeyPair, destinationDirectory, useFullScrypt)
    }

    @Throws(CipherException::class, IOException::class)
    fun generateWalletFile(
            password: String, ecKeyPair: ECKeyPair, destinationDirectory: File, useFullScrypt: Boolean): String {

        val walletFile: WalletFile
        if (useFullScrypt) {
            walletFile = Wallet.createStandard(password, ecKeyPair)
        } else {
            walletFile = Wallet.createLight(password, ecKeyPair)
        }

        val fileName = getWalletFileName(walletFile)
        val destination = File(destinationDirectory, fileName)

        val objectMapper = ObjectMapperFactory.getObjectMapper()
        objectMapper.writeValue(destination, walletFile)

        return fileName
    }

    private fun getWalletFileName(walletFile: WalletFile): String {
        return walletFile.address
    }

    fun isValidPrivateKey(privateKey: String): Boolean {
        val cleanPrivateKey = Numeric.cleanHexPrefix(privateKey)
        return cleanPrivateKey.length == PRIVATE_KEY_LENGTH_IN_HEX
    }

    fun isValidAddress(address: String): Boolean {
        val addressNoPrefix = Numeric.cleanHexPrefix(address)
        return addressNoPrefix.length == ADDRESS_LENGTH_IN_HEX
    }
}