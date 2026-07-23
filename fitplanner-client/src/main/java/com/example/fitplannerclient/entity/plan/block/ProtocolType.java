package com.example.fitplannerclient.entity.plan.block;

public enum ProtocolType {
    DROP_SET("DROP_SET"),
    SUPER_SET("SUPER_SET"),
    GIANT_SET("GIANT_SET"),
    CIRCUIT("CIRCUIT"),
    AMRAP("AMRAP"),
    EMOM("EMOM");

    private final String semanticType;
    ProtocolType(String value) {
        this.semanticType = value;
    }

    public String toString() {
        return this.semanticType.replace("_", " ");
    }
}
