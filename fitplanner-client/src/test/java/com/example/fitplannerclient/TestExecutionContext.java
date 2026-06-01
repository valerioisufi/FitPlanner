package com.example.fitplannerclient;

import com.example.fitplannerclient.entity.plan.context.ControlSignal;
import com.example.fitplannerclient.entity.plan.context.ExecutionContext;
import com.example.fitplannerclient.entity.plan.exercise.ExerciseNode;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test per la classe ExecutionContext
 *
 * @author Dennis Imperia
 */
public class TestExecutionContext {

    @Test
    void testActiveNode() {
        ExecutionContext context = new ExecutionContext();
        ExerciseNode node = new ExerciseNode();

        context.setActiveNode(node);
        assertEquals(node, context.getActiveNode());
    }

    @Test
    void testSignalInjectionAndConsumption() {
        ExecutionContext context = new ExecutionContext();

        // Verifica stato iniziale
        assertEquals(ControlSignal.NONE, context.getCurrentSignal());

        // Inietta segnale
        context.injectSignal(ControlSignal.SKIP_NEXT);
        assertEquals(ControlSignal.SKIP_NEXT, context.getCurrentSignal());

        // Prova a consumare un segnale diverso (non deve consumarlo)
        assertFalse(context.consumeSignal(ControlSignal.DONE));
        assertEquals(ControlSignal.SKIP_NEXT, context.getCurrentSignal());

        // Consuma il segnale corretto
        assertTrue(context.consumeSignal(ControlSignal.SKIP_NEXT));
        // Verifica che sia stato resettato a NONE
        assertEquals(ControlSignal.NONE, context.getCurrentSignal());
    }

    @Test
    void testTickDelta() {
        ExecutionContext context = new ExecutionContext();

        context.setTickDelta(100);
        assertEquals(100, context.getTickDelta());

        context.consumeTickDelta(40);
        assertEquals(60, context.getTickDelta());

        // Non deve andare sotto lo zero
        context.consumeTickDelta(100);
        assertEquals(0, context.getTickDelta());
    }

    @Test
    void testResolveVariables() {
        ExecutionContext context = new ExecutionContext();
        context.setParameter("REST_TIME", "60");
        context.setParameter("EXERCISE_NAME", "Panca Piana");

        // Stringa senza variabili
        assertEquals("Nessuna variabile", context.resolveVariables("Nessuna variabile"));

        // Variabile singola
        assertEquals("60", context.resolveVariables("${REST_TIME}"));

        // Variabili multiple nel testo
        assertEquals("Esegui Panca Piana e riposa 60s",
                context.resolveVariables("Esegui ${EXERCISE_NAME} e riposa ${REST_TIME}s"));

        // Variabile mancante (deve lasciare il segnaposto)
        assertEquals("Valore: ${MISSING_VAR}", context.resolveVariables("Valore: ${MISSING_VAR}"));
    }

    @Test
    void testResolveAsInteger() {
        ExecutionContext context = new ExecutionContext();
        context.setParameter("REPS", "12");
        context.setParameter("INVALID", "testo_non_numerico");

        // Risoluzione corretta
        assertEquals(12, context.resolveAsInteger("${REPS}", 5));

        // Numero diretto senza variabili
        assertEquals(15, context.resolveAsInteger("15", 5));

        // Risoluzione fallita (variabile assente), usa fallback
        assertEquals(5, context.resolveAsInteger("${MISSING}", 5));

        // Risoluzione fallita (testo non convertibile in numero), usa fallback
        assertEquals(8, context.resolveAsInteger("${INVALID}", 8));

        // Stringa vuota o nulla
        assertEquals(10, context.resolveAsInteger("", 10));
        assertEquals(10, context.resolveAsInteger(null, 10));
    }

    @Test
    void testReset() {
        ExecutionContext context = new ExecutionContext();

        context.setActiveNode(new ExerciseNode());
        context.injectSignal(ControlSignal.DONE);
        context.setTickDelta(50);

        context.reset();

        assertNull(context.getActiveNode());
        assertEquals(ControlSignal.NONE, context.getCurrentSignal());
        assertEquals(0, context.getTickDelta());
    }
}