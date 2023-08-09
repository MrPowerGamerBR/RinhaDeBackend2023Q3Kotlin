package com.mrpowergamerbr.rinhadebackend2023q3.utils

object RegexUtils {
    /**
     * Escapes a RegEx based on the [input], to be used when querying PostgreSQL
     *
     * Sadly, Java's [Pattern.quote] result doesn't work in PostgreSQL, which is why we need to manually escape it.
     *
     * @param input the RegEx input
     */
    // Thanks ChatGPT!
    fun escapeRegex(input: String): String {
        // List of special characters in regex
        val specialCharacters = ".^$*+?()[{\\|"

        // Initialize a StringBuilder to store the escaped regex
        val escapedRegex = StringBuilder()

        // Iterate through each character in the input
        for (c in input.toCharArray()) {
            if (specialCharacters.indexOf(c) != -1) {
                // If the character is special, escape it by adding a backslash
                escapedRegex.append("\\")
            }
            escapedRegex.append(c)
        }
        return escapedRegex.toString()
    }
}