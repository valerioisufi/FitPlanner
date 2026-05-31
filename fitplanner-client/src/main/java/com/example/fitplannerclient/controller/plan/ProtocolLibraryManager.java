package com.example.fitplannerclient.controller.plan;

import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.controller.plan.factory.ProtocolBlockFactory;
import com.example.fitplannerclient.entity.plan.block.ProtocolBlock;
import com.example.fitplannerclient.serializer.PlanToBeanVisitor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ProtocolLibraryManager {

    private final List<ProtocolBlock> protocolBlockLibrary = new ArrayList<>();
    private final List<PlanNodeBean> protocolBlockLibraryCache = new ArrayList<>();

    public ProtocolLibraryManager() {
        buildProtocolBlockLibrary();
    }

    public List<PlanNodeBean> getProtocolBlockLibraryCache() {
        return protocolBlockLibraryCache;
    }

    public void buildProtocolBlockLibrary() {
        protocolBlockLibrary.clear();
        protocolBlockLibraryCache.clear();

        ProtocolBlockFactory factory = new ProtocolBlockFactory();

        protocolBlockLibrary.add(factory.createCircuit());
        protocolBlockLibrary.add(factory.createSuperSet());
        protocolBlockLibrary.add(factory.createDropSet());
        protocolBlockLibrary.add(factory.createGiantSet());
        protocolBlockLibrary.add(factory.createAMRAP());
        protocolBlockLibrary.add(factory.createEMOM());

        for (ProtocolBlock block : protocolBlockLibrary) {
            PlanToBeanVisitor visitor = new PlanToBeanVisitor();
            block.accept(visitor);

            protocolBlockLibraryCache.add(visitor.getCurrentPlanNodeBean());
        }
    }

    public ProtocolBlock getProtocolBlock(String protocolName) {
        ProtocolBlockFactory factory = new ProtocolBlockFactory();
        return switch (protocolName) {
            case "DROP_SET" -> factory.createDropSet();
            case "SUPER_SET" -> factory.createSuperSet();
            case "GIANT_SET" -> factory.createGiantSet();
            case "CIRCUIT" -> factory.createCircuit();
            case "AMRAP" -> factory.createAMRAP();
            case "EMOM" -> factory.createEMOM();
            default -> factory.createCircuit();
        };
    }

    public Map<String, String> getDefaultProtocolParameters(String protocolName) {
        return getProtocolBlock(protocolName).getParameters();
    }
}
