package com.jupalaja.calorieCounter.shared.constants

object MessageConstants {
    // Welcome message
    const val WELCOME_MESSAGE =
        "Hello! I can help you with nutrition information. Just send me what you ate and " +
            "I'll tell you the total protein."

    // Error messages
    const val VOICE_MESSAGE_PROCESSING_ERROR = "Sorry, I couldn't process your voice message."
    const val VOICE_MESSAGE_GENERAL_ERROR = "Sorry, an error occurred while processing your voice message."
    const val GENERAL_PROCESSING_ERROR = "Sorry, an error occurred while processing your request."
    const val BLANK_TEXT_MESSAGE_ERROR = "Sorry, I couldn't understand your message. Please try sending it again."
    const val NULL_VOICE_DATA_ERROR = "Sorry, I had trouble processing your voice message. Please try again."

    // Prompt templates
    val NATURAL_LANGUAGE_QUERY_PROMPT_TEMPLATE =
        """
        Summarize the following user request into a simple query in English for a nutrition API.
        The user wants to know the nutritional information of some food.
        Extract the food items and quantities, and translate them to English.
        For example, if the user asks 'what are the calories in 3 boiled eggs and a slice of bread', the output should 
        be '3 boiled eggs and a slice of bread'.If the user asks in another language, for example 'me comí 2 manzanas y 
        una banana', the output should be '2 apples and a banana'.
        Another example, if the user asks 'me comi un pan grande, de media libra', the output should be 'half a pound 
        of large bread'. Only return the query, with no other text, explanation, or formatting.
        The user request is: "%s"
        """.trimIndent()

    val PROTEIN_SUMMARY_PROMPT_TEMPLATE =
        """
        Generate a summary in Spanish for the provided list of foods and their protein content.
        The summary should start with "Claro, éste es el resumen".
        Then, list each food item with its protein content on a new line, using a bullet point. For example: 
        "- La pechuga de pollo contiene 50g de proteína".
        After the list, add a blank line. Finally, add a sentence with the total protein amount. For example: 
        "En total estarías consumiendo 60g de proteína".

        If the list of items is empty or the total protein is 0, the response should be "Los alimentos que 
        proporcionaste no parecen contener proteínas."

        Here is the list of food items and their protein content:
        %s

        The total protein is %sg.

        Only return the final formatted text. Do not add any other explanations or formatting.
        """.trimIndent()
}
