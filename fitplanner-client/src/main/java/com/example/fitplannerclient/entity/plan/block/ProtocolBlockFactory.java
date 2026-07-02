package com.example.fitplannerclient.entity.plan.block;

import com.example.fitplannerclient.entity.plan.block.strategy.composition.WrapWithDecoratorRule;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.NodeCountRangeRule;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.OnlyExercisesAllowedRule;
import com.example.fitplannerclient.entity.plan.decorator.*;

import java.util.List;

public class ProtocolBlockFactory {

    private enum ProtocolBlockTypes {
        DROP_SET("DROP_SET"),
        SUPER_SET("SUPER_SET"),
        GIANT_SET("GIANT_SET"),
        CIRCUIT("CIRCUIT"),
        AMRAP("AMRAP"),
        EMOM("EMOM");

        private final String semanticType;

        ProtocolBlockTypes(String semanticType) {
            this.semanticType = semanticType;
        }

        public String getSemanticType() {
            return semanticType;
        }
    }

    public ProtocolBlock createCircuit() {
        ProtocolBlock circuitBlock = new ProtocolBlock(
                ProtocolBlockTypes.CIRCUIT.getSemanticType(),
                List.of(new OnlyExercisesAllowedRule()),
                List.of(new WrapWithDecoratorRule(new RestDecorator(null, "${CIRCUIT_REST_BETWEEN_EXERCISES}"))),
                List.of(
                        new WrapWithDecoratorRule(new RestDecorator(null, "${CIRCUIT_REST_BETWEEN_ROUNDS}")),
                        new WrapWithDecoratorRule(new LoopDecorator(null, "${CIRCUIT_ROUNDS}"))
                )
        );
        circuitBlock.setParameter("CIRCUIT_ROUNDS", "3");
        circuitBlock.setParameter("CIRCUIT_REST_BETWEEN_EXERCISES", "0");
        circuitBlock.setParameter("CIRCUIT_REST_BETWEEN_ROUNDS", "120000"); // 2 min in ms
        return circuitBlock;
    }

    public ProtocolBlock createDropSet() {
        ProtocolBlock block = new ProtocolBlock(
                ProtocolBlockTypes.DROP_SET.getSemanticType(),
                List.of(new OnlyExercisesAllowedRule(), new NodeCountRangeRule(1, 1)),
                null,
                List.of(new WrapWithDecoratorRule(new ProgressionDecorator(null, "${DROP_SET_PROGRESSION}")))
        );
        block.setParameter("DROP_SET_PROGRESSION", "WEIGHT: 20, 15, 10; REPS: 10, 10, 10");
        return block;
    }

    public ProtocolBlock createSuperSet() {
        ProtocolBlock block = new ProtocolBlock(
                ProtocolBlockTypes.SUPER_SET.getSemanticType(),
                List.of(new OnlyExercisesAllowedRule(), new NodeCountRangeRule(2, 2)),
                null,
                List.of(new WrapWithDecoratorRule(new LoopDecorator(null, "${SUPER_SET_ROUNDS}")))
        );
        block.setParameter("SUPER_SET_ROUNDS", "3");
        return block;
    }

    public ProtocolBlock createGiantSet() {
        ProtocolBlock block = new ProtocolBlock(
                ProtocolBlockTypes.GIANT_SET.getSemanticType(),
                List.of(new OnlyExercisesAllowedRule(), new NodeCountRangeRule(3, Integer.MAX_VALUE)),
                null,
                List.of(new WrapWithDecoratorRule(new LoopDecorator(null, "${GIANT_SET_ROUNDS}")))
        );
        block.setParameter("GIANT_SET_ROUNDS", "3");
        return block;
    }

    public ProtocolBlock createAMRAP() {
        ProtocolBlock block = new ProtocolBlock(
                ProtocolBlockTypes.AMRAP.getSemanticType(),
                List.of(new OnlyExercisesAllowedRule()),
                null,
                List.of(
                        new WrapWithDecoratorRule(new LoopDecorator(null, "9999")), // round "infiniti"
                        new WrapWithDecoratorRule(new TimeLimitDecorator(null, "${AMRAP_TIME}"))
                )
        );
        block.setParameter("AMRAP_TIME", "600000"); // 10 min in ms
        return block;
    }

    public ProtocolBlock createEMOM() {
        ProtocolBlock block = new ProtocolBlock(
                ProtocolBlockTypes.EMOM.getSemanticType(),
                List.of(new OnlyExercisesAllowedRule()),
                null,
                List.of(
                        new WrapWithDecoratorRule(new IntervalDecorator(null, "${EMOM_INTERVAL}")),
                        new WrapWithDecoratorRule(new LoopDecorator(null, "${EMOM_ROUNDS}"))
                )
        );
        block.setParameter("EMOM_INTERVAL", "60000"); // 1 min in ms
        block.setParameter("EMOM_ROUNDS", "10");
        return block;
    }
}
