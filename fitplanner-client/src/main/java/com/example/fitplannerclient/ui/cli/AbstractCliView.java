package com.example.fitplannerclient.ui.cli;

import com.example.fitplannerclient.ui.cli.io.InputReader;
import com.example.fitplannerclient.ui.cli.io.OutputPrinter;

/**
 * Base per tutte le view CLI: espone engine/printer/reader e implementa
 * il ciclo di vita comune. Le sottoclassi implementano solo {@link #render()}.
 */
public abstract class AbstractCliView implements CliView {

    protected CliEngine engine;
    protected OutputPrinter printer;
    protected InputReader reader;

    @Override
    public final CliView execute(CliEngine engine) {
        this.engine = engine;
        this.printer = engine.getPrinter();
        this.reader = engine.getInput();
        return render();
    }

    /** Logica specifica della view; ritorna la prossima view (o null per uscire). */
    protected abstract CliView render();

    @Override
    public void stop() {
        // Default: nessuna risorsa da rilasciare. Override dove necessario.
    }
}
