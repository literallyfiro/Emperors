package me.onlyfire.emperors.essential;

import me.onlyfire.emperors.utils.Emoji;

public enum Language {

    /* Multi Space */
    ADD_EMPEROR_FIRST_STEP("""
            <b>Perfetto</b>, adesso invia una foto (senza compressione di telegram) che identifica l'imperatore, con una didascalia contenente il suo nome (Gli utenti dovranno scrivere quel nome per diventare imperatori).
            <b>Esempio: https://imgur.com/a/chawtDS</b>

            Fai <code>/cancel</code> per annullare questa operazione
            """
    ),
    GENERAL_ERROR("<b>Non è stato possibile eseguire la tua azione!</b> " + Emoji.CRYING_FACE +
            "\n" +
            "<i>Mi dispiace averti disturbato, ma c'è stato un problema tecnico nel bot! Molto probabilmente (anzi, sicuramente) questo errore è stato generato per via dell'incapacità dello sviluppatore nel controllare le exceptions in java.</i>\n" +
            "\n" +
            "(Ah e tranquillo, ho già taggato segretamente lo sviluppatore per incitarlo ad aggiustare questo problema" + Emoji.MOON + ")" +
            "\n\n" +
            "<b>ERROR CODE:</b> <code>%s</code>" +
            "\n" +
            "<b>MESSAGE:</b> <code>%s</code>"
    ),
    ERROR_EMPEROR_CREATION_LOG("""
            <b>Hey Hey Hey! Nuovo bug, altro fix!</b>

            Ho appena tirato un'eccezione mentre cercavo di creare un imperatore per un innocente utente, la prossima volta programmami meglio invece di fare altro...
                        
            <b>Informazioni utili:</b>
                - <b>Chat ID:</b> <code>%s</code>
                - <b>Error Code:</b> <code>%s</code>
                        
            Qui troverai l'exception tirata dal bot, muoviti a fixarmi :(
            """
    ),

    /* Simple messages */
    NEW_EMPEROR_OF_DAY("<a href=\"%s\">&#8205</a>" + Emoji.PARTY + " ||• <b>Congratulazioni</b> %s •|| " +
            Emoji.PARTY + "\n\n" + "➥ Sei il nuovo imperatore <code>%s</code> di oggi!"),
    REMOVE_EMPEROR_FIRST_STEP("Rispondi a questo messaggio con il nome dell'imperatore che vuoi eliminare"),
    ADDED_EMPEROR_SUCCESSFULLY("Complimenti, l'imperatore <code>%s</code> è ora disponibile a tutti gli utenti!"),
    ALREADY_HAS_EMPEROR(Emoji.CRYING_FACE + " <b>Mi dispiace!</b> %s ha già preso il posto di re <code>%s</code>!"),
    ALREADY_HAS_EMPEROR_SELF("<b>Hey!</b> Hai già preso il posto di questo re, te ne sei dimenticato? " + Emoji.THINKING
            + "\nSe vuoi controllare la lista degli imperatori, digita /listemperors."),
    CREATION_IN_PROGRESS(Emoji.TECHNOLOGIST + "Il tuo imperatore è ora in fase di creazione, questa operazione potrà richiedere qualche minuto."),
    ALREADY_EXIST_EMPEROR(Emoji.HEAVY_MULTIPLICATION_X + " Esiste già un imperatore con questo nome!"),
    REMOVED_EMPEROR_SUCCESSFULLY("L'imperatore <code>%s</code> è stato rimosso dal gruppo!"),
    THERE_ARE_NO_EMPERORS("<b>Non ci sono imperatori in questo gruppo!</b> " + Emoji.CRYING_FACE),
    NOT_EXIST_EMPEROR(Emoji.HEAVY_MULTIPLICATION_X + " Non esiste un imperatore con questo nome!");

    String language;

    Language(String it) {
        this.language = it;
    }

    @Override
    public String toString() {
        return language;
    }
}
