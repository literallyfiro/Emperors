package me.onlyfire.emperors.bot;

import me.onlyfire.emperors.utils.Emoji;

public enum Language {

    /* Multi Space */
    WELCOME("""
            <b>Ciao! Benvenuto in @EmperorsRobot!</b>
                                    
            👑 <b>Cosa è Emperors?</b>
            Emperors è un bot per far divertire gli utenti del vostro gruppo.
            Scrivendo il nome di un imperatore, i vostri utenti potranno diventare l'imperatore del giorno.
                                    
            ❔ <b>Come funziona?</b>
            Gli amministratori del gruppo avranno il permesso di creare un imperatore (e di rimuoverlo), inserendo una foto e il suo identificativo (che gli utenti dovranno scrivere per diventare l'imperatore del giorno)
                                    
            ⚡️ <b>Quanti imperatori si possono aggiungere in un gruppo?</b>
            La risposta è semplice. Infiniti
            Sbizzarritevi a creare i vostri imperatori senza nessun tipo di limite!
                             
             
            ⚠️ <b>Nota, il bot è in uno stato chiamato BETA, ciò significa che non è la versione finale e ci potranno essere vari cambiamenti</b>
            """),

    SETTINGS("""
            ⚙️ <b>Impostazioni del gruppo %s</b>
                        
            <i>Usa la tastiera per navigare tra le impostazioni</i>
            """),

    SETTINGS_SPECIFIC("""
            <b>STATISTICHE:</b>
             %s: %s
            """),

    ADD_EMPEROR_FIRST_STEP("""
            <b>Perfetto</b>, adesso invia una foto (senza compressione di telegram) che identifica l'imperatore, con una didascalia contenente il suo nome (Gli utenti dovranno scrivere quel nome per diventare imperatori).
            <b>Esempio: https://imgur.com/a/chawtDS</b>

            Fai <code>/cancel</code> per annullare questa operazione
            """
    ),
    GENERAL_ERROR("<b>Non è stato possibile eseguire la tua azione!</b> " + Emoji.CRYING_FACE +
            "\n" +
            "<i>Mi dispiace averti disturbato, ma c'è stato un problema tecnico nel bot! Molto probabilmente (anzi, sicuramente) questo errore è stato generato per via dell'incapacità dello sviluppatore nel controllare le exceptions in java.</i>\n" +
            "\n\n" +
            "<b>ERROR CODE:</b> <code>%s</code>" +
            "\n" +
            "<b>MESSAGE:</b> <code>%s</code>"
    ),

    /* Simple messages */
    NEW_EMPEROR_OF_DAY("<a href=\"%s\">&#8205</a>" + Emoji.PARTY + " ||• <b>Congratulazioni</b> %s •|| " +
            Emoji.PARTY + "\n\n" + "➥ Sei il nuovo imperatore <code>%s</code> di oggi!"),
    MAX_EMPERORS(Emoji.RAILWAY_CAR + " Hai già conquistato %s imperatori! Il limite per utente è di %s imperatori."),
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

    final String s;

    Language(String lang) {
        this.s = lang;
    }

    @Override
    public String toString() {
        return s;
    }
}
