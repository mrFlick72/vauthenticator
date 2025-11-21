package com.vauthenticator.server.password.domain

import org.springframework.boot.context.properties.ConfigurationProperties
import kotlin.random.Random

@ConfigurationProperties(prefix = "password.generator-criteria")
data class PasswordGeneratorCriteria(
    val upperCaseCharactersSize: Int,
    val lowerCaseCharactersSize: Int,
    val specialCharactersSize: Int,
    val numberCharactersSize: Int
) {
    fun size() = upperCaseCharactersSize + specialCharactersSize + numberCharactersSize
}

class PasswordGenerator(private val passwordGeneratorCriteria: PasswordGeneratorCriteria) {

    fun generate(): String {
        val random = Random
        val builder = StringBuilder()
        val mutableListOf = mutableListOf<Char>()

        for (index in 1..passwordGeneratorCriteria.specialCharactersSize) {
            mutableListOf.add(specialCharactersAlphabet[random.nextInt(specialCharactersAlphabet.size - 1)])
        }
        for (index in 1..passwordGeneratorCriteria.numberCharactersSize) {
            mutableListOf.add(numberAlphabet[random.nextInt(numberAlphabet.size - 1)])
        }
        for (index in 1..passwordGeneratorCriteria.upperCaseCharactersSize) {
            mutableListOf.add(upperCaseAlphabet[random.nextInt(upperCaseAlphabet.size - 1)])
        }
        for (index in 1..passwordGeneratorCriteria.lowerCaseCharactersSize) {
            mutableListOf.add(lowerCaseAlphabet[random.nextInt(lowerCaseAlphabet.size - 1)])
        }

        for (index in 0..mutableListOf.size) {
            val nextInt = if (mutableListOf.size > 0) {
                random.nextInt(mutableListOf.size)
            } else {
                0
            }

            if (mutableListOf.isNotEmpty()) {
                builder.append(mutableListOf[nextInt])
                mutableListOf.removeAt(nextInt)
            }
        }

        return builder.toString()
    }
}


val specialCharactersAlphabet = charArrayOf(
    '!',
    '@',
    '#',
    '$',
    '%',
    '^',
    '&',
    '*',
    '(',
    ')',
    '-',
    '_',
    '=',
    '+',
    '[',
    ']',
    '{',
    '}',
    ';',
    ':',
    '"',
    '\'',
    '\\',
    '|',
    '~',
    '`',
    ',',
    '<',
    '.',
    '>',
    '/',
    '?',
)
val numberAlphabet = charArrayOf(
    '0',
    '1',
    '2',
    '3',
    '4',
    '5',
    '6',
    '7',
    '8',
    '9',
)
val lowerCaseAlphabet = charArrayOf(
    'a',
    'b',
    'c',
    'd',
    'e',
    'f',
    'g',
    'h',
    'i',
    'j',
    'k',
    'l',
    'm',
    'n',
    'o',
    'p',
    'q',
    'r',
    's',
    't',
    'u',
    'v',
    'w',
    'x',
    'y',
    'z'
)
val upperCaseAlphabet = charArrayOf(
    'A',
    'B',
    'C',
    'D',
    'E',
    'F',
    'G',
    'H',
    'I',
    'J',
    'K',
    'L',
    'M',
    'N',
    'O',
    'P',
    'Q',
    'R',
    'S',
    'T',
    'U',
    'V',
    'W',
    'X',
    'Y',
    'Z'
)