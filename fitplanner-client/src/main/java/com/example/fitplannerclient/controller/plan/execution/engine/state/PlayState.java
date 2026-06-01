package com.example.fitplannerclient.controller.plan.execution.engine.state;

import com.example.fitplannerclient.controller.plan.execution.engine.WorkoutEngineImpl;
import com.example.fitplannerclient.entity.plan.context.*;

public class PlayState extends EngineState {

    private Thread engineThread;

    @Override
    public WorkoutStatus getStatus() {
        return WorkoutStatus.PLAYING;
    }

    @Override
    public boolean isPlaying() {
        return true;
    }

    @Override
    public void entry(WorkoutEngineImpl engine) {
        ExecutionContext context = engine.getContext();

        this.engineThread = Thread.startVirtualThread(() -> {
            long lastWakeUpTime = System.currentTimeMillis();

            while (engine.getState().isPlaying()) {
                Thread.interrupted();
                ExecutionResult result = engine.execute(context);
                
                engine.notifyUpdate(this, context.getActiveNode(), result.getRequestedSleepMillis());

                if (result.getState() == PlanNodeState.COMPLETED) {
                    engine.stop();
                    break;
                }

                int sleepTime = result.getRequestedSleepMillis();
                
                if (sleepTime == -1) {
                    // Nessun limite di tempo -> dorme finché non riceve un segnale (es. DONE o SKIP)
                    try {
                        Thread.sleep(Integer.MAX_VALUE);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        long now = System.currentTimeMillis();
                        context.setTickDelta((int) (now - lastWakeUpTime));
                        lastWakeUpTime = now;
                    }
                } else if (sleepTime > 0) {
                    // Dorme per il tempo esatto richiesto dal nodo (es. RestDecorator o TimeLimit)
                    try {
                        Thread.sleep(sleepTime);

                        context.setTickDelta(sleepTime);
                        lastWakeUpTime = System.currentTimeMillis();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        long now = System.currentTimeMillis();
                        context.setTickDelta((int) (now - lastWakeUpTime));
                        lastWakeUpTime = now;
                    }
                } else {
                    // sleepTime == 0 -> il nodo ha chiesto di non dormire affatto (tick immediato)
                    context.setTickDelta(0);
                    lastWakeUpTime = System.currentTimeMillis();
                }
            }
        });
    }

    @Override
    public void pause(WorkoutEngineImpl engine) {
        engine.changeToState(new PauseState());
    }

    @Override
    public void stop(WorkoutEngineImpl engine) {
        engine.changeToState(new StopState());
    }

    @Override
    public void exit(WorkoutEngineImpl engine) {
        if (engineThread != null && engineThread.isAlive()) {
            engineThread.interrupt();
        }
    }

    @Override
    public void skipNext(WorkoutEngineImpl engine) {
        engine.getContext().injectSignal(ControlSignal.SKIP_NEXT);
        if (engineThread != null && engineThread.isAlive()) {
            engineThread.interrupt();
        }
    }

    @Override
    public void skipPrevious(WorkoutEngineImpl engine) {
        engine.getContext().injectSignal(ControlSignal.SKIP_PREVIOUS);
        if (engineThread != null && engineThread.isAlive()) {
            engineThread.interrupt();
        }
    }

    @Override
    public void done(WorkoutEngineImpl engine) {
        engine.getContext().injectSignal(ControlSignal.DONE);
        if (engineThread != null && engineThread.isAlive()) {
            engineThread.interrupt();
        }
    }
}
