package cn.mw.ethwallet.interfaces

/**
 * @author Aaron
 * @email aaron@magicwindow.cn
 * @date 08/03/2018 17:11
 * @description
 */
interface PasswordDialogCallback {

    fun success(password: String)

    fun canceled()
}