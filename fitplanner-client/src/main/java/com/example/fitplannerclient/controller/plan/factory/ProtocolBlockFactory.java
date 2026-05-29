package com.example.fitplannerclient.controller.plan.factory;

import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.entity.plan.block.strategy.composition.WrapWithDecoratorRule;
import com.example.fitplannerclient.entity.plan.block.strategy.validation.OnlyExercisesAllowedRule;
import com.example.fitplannerclient.entity.plan.decorator.LoopDecorator;

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
                null,
                List.of(new WrapWithDecoratorRule(new LoopDecorator(null, "${CIRCUIT_ROUNDS}")))
        );
        circuitBlock.setParameter("CIRCUIT_ROUNDS", "3");
        return circuitBlock;
    }

//    public ProtocolBlock createAMRAP() {
//
//    }
//
//    public ProtocolBlock createEMOM() {
//
//    }
}
