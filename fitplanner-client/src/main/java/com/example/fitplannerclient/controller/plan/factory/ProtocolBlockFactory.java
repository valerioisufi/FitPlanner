package com.example.fitplannerclient.controller.plan.factory;

import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;

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

//    public ProtocolBlock createCircuit() {
//        ProtocolBlock circuitBlock = new ProtocolBlock(
//                ProtocolBlockTypes.CIRCUIT.getSemanticType(),
//
//        );
//    }
//
//    public ProtocolBlock createAMRAP() {
//
//    }
//
//    public ProtocolBlock createEMOM() {
//
//    }
}
