package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.block.strategy.composition.WrapWithDecoratorRule;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.NodeCountRangeRule;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.OnlyExercisesAllowedRule;
import com.example.fitplannerclient.entity.plan.decorator.*;

import java.util.List;

public class ProtocolBlockFactory {

    public ProtocolBlock create(String semanticType) {
        return switch (semanticType) {
            case "DROP_SET" -> createDropSet();
            case "SUPER_SET" -> createSuperSet();
            case "GIANT_SET" -> createGiantSet();
            case "CIRCUIT" -> createCircuit();
            case "AMRAP" -> createAMRAP();
            case "EMOM" -> createEMOM();
            default -> createCircuit();
        };
    }

    public ProtocolBlock createCircuit() {
        ProtocolBlock circuitBlock = new ProtocolBlock(
                ProtocolType.CIRCUIT,
                List.of(new OnlyExercisesAllowedRule()),
                List.of(new WrapWithDecoratorRule(new RestDecorator(null, "${CIRCUIT_REST_BETWEEN_EXERCISES}"))),
                List.of(
                        new WrapWithDecoratorRule(new RestDecorator(null, "${CIRCUIT_REST_BETWEEN_ROUNDS}")),
                        new WrapWithDecoratorRule(new LoopDecorator(null, "${CIRCUIT_ROUNDS}"))
                )
        );
        circuitBlock.setParameter("CIRCUIT_ROUNDS", "3");
        circuitBlock.setParameter("CIRCUIT_REST_BETWEEN_EXERCISES", "0");
        circuitBlock.setParameter("CIRCUIT_REST_BETWEEN_ROUNDS", "120"); // 2 min
        return circuitBlock;
    }

    public ProtocolBlock createDropSet() {
        ProtocolBlock block = new ProtocolBlock(
                ProtocolType.DROP_SET,
                List.of(new OnlyExercisesAllowedRule(), new NodeCountRangeRule(1, 1)),
                null,
                List.of(new WrapWithDecoratorRule(new ProgressionDecorator(null, "${DROP_SET_PROGRESSION}")))
        );
        block.setParameter("DROP_SET_PROGRESSION", "WEIGHT: 20, 15, 10; REPS: 10, 10, 10");
        return block;
    }

    public ProtocolBlock createSuperSet() {
        ProtocolBlock block = new ProtocolBlock(
                ProtocolType.SUPER_SET,
                List.of(new OnlyExercisesAllowedRule(), new NodeCountRangeRule(2, 2)),
                null,
                List.of(new WrapWithDecoratorRule(new LoopDecorator(null, "${SUPER_SET_ROUNDS}")))
        );
        block.setParameter("SUPER_SET_ROUNDS", "3");
        return block;
    }

    public ProtocolBlock createGiantSet() {
        ProtocolBlock block = new ProtocolBlock(
                ProtocolType.GIANT_SET,
                List.of(new OnlyExercisesAllowedRule(), new NodeCountRangeRule(3, -1)),
                null,
                List.of(new WrapWithDecoratorRule(new LoopDecorator(null, "${GIANT_SET_ROUNDS}")))
        );
        block.setParameter("GIANT_SET_ROUNDS", "3");
        return block;
    }

    public ProtocolBlock createAMRAP() {
        ProtocolBlock block = new ProtocolBlock(
                ProtocolType.AMRAP,
                List.of(new OnlyExercisesAllowedRule()),
                null,
                List.of(
                        new WrapWithDecoratorRule(new LoopDecorator(null, "9999")), // round "infiniti"
                        new WrapWithDecoratorRule(new TimeLimitDecorator(null, "${AMRAP_TIME}"))
                )
        );
        block.setParameter("AMRAP_TIME", "600"); // 10 min
        return block;
    }

    public ProtocolBlock createEMOM() {
        ProtocolBlock block = new ProtocolBlock(
                ProtocolType.EMOM,
                List.of(new OnlyExercisesAllowedRule()),
                null,
                List.of(
                        new WrapWithDecoratorRule(new IntervalDecorator(null, "${EMOM_INTERVAL}")),
                        new WrapWithDecoratorRule(new LoopDecorator(null, "${EMOM_ROUNDS}"))
                )
        );
        block.setParameter("EMOM_INTERVAL", "60"); // 1 min
        block.setParameter("EMOM_ROUNDS", "10");
        return block;
    }
}
