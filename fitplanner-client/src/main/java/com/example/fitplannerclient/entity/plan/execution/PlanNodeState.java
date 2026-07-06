package com.example.fitplannerclient.entity.plan.execution;

public enum PlanNodeState {
    IDLE,      // Nodo non ancora iniziato o resettato
    RUNNING,   // Nodo in esecuzione (es. esercizio in corso)
    WAITING,   // Nodo in attesa (es. pausa o timer in corso)
    COMPLETED, // Nodo completato con successo
    REVERT,    // Segnale per riavvolgere l'esecuzione (es. skip previous)
    SKIPPED    // Nodo saltato dall'utente senza completarlo
}
