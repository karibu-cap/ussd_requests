/*
 * Copyright (c) 2020. BoostTag E.I.R.L. Romell D.Z.
 * All rights reserved
 * porfile.romellfudi.com
 */

/**
 * BoostTag E.I.R.L. All Copyright Reserved
 * www.boosttag.com
 */
package com.karibu_cap.ussd_requests

/**
 * Kotlin library extensions
 *
 * @author Romell Dominguez
 * @version 1.1.i 2019/04/18
 * @since 1.1.i
 * @suppress
 */
inline fun CharSequence.isEmpty(): Boolean = length == 0
fun String.replace(oldChar: String, newChar: String): String {
    return (this as java.lang.String).replace(oldChar, newChar)
}
fun String.contains(sequence: String): Boolean {
    return (this as java.lang.String).contains(sequence)
}
fun String.substring(indexA: Int): String {
    return (this as java.lang.String).substring(indexA)
}