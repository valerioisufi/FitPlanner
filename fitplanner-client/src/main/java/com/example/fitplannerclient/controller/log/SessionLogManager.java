package com.example.fitplannerclient.controller.log;


import com.example.fitplannerclient.bean.log.ExerciseLogBean;
import com.example.fitplannerclient.service.facade.SessionLogFacade;
import com.example.fitplannercommon.SessionLogDTO;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public class SessionLogManager {
    private final SessionLogFacade facade;

    public SessionLogManager(SessionLogFacade facade){
        this.facade= facade;
    }

//    public CompletableFuture<Void> getFilteredSessionLogs(String userId, long start, long end){
//
//    }
}


/**
 * interfaccia progressi/statistiche
 * si seleziona l'intervallo di tempo, il manager recupera i session log tramite la facade
 *
 * creiamo le entity, calcoliamo attraverso le entity il volume totale delle singole sessioni
 * che visualizzeremo su un grafico (ascissa = giorno)
 *
 * entity per session log
 * fare interfaccia grafica (modificare quella della scheda)
 */