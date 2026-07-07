package com.example.fitplannerclient.ui.cli.io;

import java.util.List;
import com.example.fitplannerclient.bean.plan.PlanNodeBean;
import com.example.fitplannerclient.bean.plan.ExerciseModifierBean;
import com.example.fitplannerclient.bean.plan.FlowDecoratorBean;

public class OutputPrinter {

    public void printHeader(String title) {
        System.out.println("\n========================================");
        System.out.println("  " + title.toUpperCase());
        System.out.println("========================================");
    }

    public void printMenu(String title, List<String> options) {
        if (title != null && !title.isEmpty()) {
            System.out.println("\n--- " + title + " ---");
        } else {
            System.out.println();
        }
        
        for (int i = 0; i < options.size(); i++) {
            System.out.println((i + 1) + ". " + options.get(i));
        }
    }

    public void printTitle(String title) {
        System.out.println("\n--- " + title + " ---");
    }

    public void printError(String message) {
        System.out.println("[ERRORE] " + message);
    }

    public void printException(String message, Throwable ex) {
        String errorMsg = ex.getCause() != null ? ex.getCause().getMessage() : ex.getMessage();
        System.out.println("[ERRORE] " + message + errorMsg);
    }

    public void printSuccess(String message) {
        System.out.println("[OK] " + message);
    }

    public void printInfo(String message) {
        System.out.println("[INFO] " + message);
    }

    public void printLn(String message) {
        System.out.println(message);
    }

    /**
     * Stampa i dati in formato tabellare.
     */
    public void printTable(List<String> headers, List<List<String>> data) {
        if (headers == null || headers.isEmpty()) return;
        int[] colWidths = calculateColumnWidths(headers, data);

        String format = buildFormatString(colWidths);
        printHeaderAndSeparator(headers, colWidths, format);

        if (data != null) {
            for (List<String> row : data) {
                System.out.printf(format, row.toArray());
            }
        }
    }

    private int[] calculateColumnWidths(List<String> headers, List<List<String>> data) {
        int[] colWidths = new int[headers.size()];
        for (int i = 0; i < headers.size(); i++) {
            colWidths[i] = headers.get(i).length();
        }

        if (data != null) {
            for (List<String> row : data) {
                for (int i = 0; i < row.size(); i++) {
                    if (row.get(i) != null && row.get(i).length() > colWidths[i]) {
                        colWidths[i] = row.get(i).length();
                    }
                }
            }
        }

        return colWidths;
    }

    private String buildFormatString(int[] colWidths) {
        StringBuilder formatBuilder = new StringBuilder();

        for (int width : colWidths) {
            formatBuilder.append("%-").append(width + 2).append("s");
        }

        formatBuilder.append("%n");
        return formatBuilder.toString();
    }

    private void printHeaderAndSeparator(List<String> headers, int[] colWidths, String format) {
        System.out.printf(format, headers.toArray());
        StringBuilder separator = new StringBuilder();

        for (int width : colWidths) {
            separator.append("-".repeat(width + 2));
        }
        System.out.println(separator.toString());
    }

    public void printPlan(PlanNodeBean root) {
        if (root == null) {
            System.out.println("Plan is empty.");
            return;
        }
        System.out.println();
        printPlanRecursive(root, "", true);
        System.out.println();
    }

    private void printPlanRecursive(PlanNodeBean node, String prefix, boolean isTail) {
        StringBuilder nodeDisplay = new StringBuilder();
        nodeDisplay.append(node.getName());
        
        if (node.getType() != null) {
            nodeDisplay.append(" [").append(node.getType()).append("]");
        }

        if (node.getModifiers() != null && !node.getModifiers().isEmpty()) {
            nodeDisplay.append(" (");
            for (int i = 0; i < node.getModifiers().size(); i++) {
                ExerciseModifierBean mod = node.getModifiers().get(i);
                nodeDisplay.append(mod.getName()).append(": ").append(mod.getValue());
                if (i < node.getModifiers().size() - 1) nodeDisplay.append(", ");
            }
            nodeDisplay.append(")");
        }

        if (node.getFlowDecorators() != null && !node.getFlowDecorators().isEmpty()) {
            nodeDisplay.append(" {");
            for (int i = 0; i < node.getFlowDecorators().size(); i++) {
                FlowDecoratorBean dec = node.getFlowDecorators().get(i);
                nodeDisplay.append(dec.getType()).append(": ").append(dec.getValue());
                if (i < node.getFlowDecorators().size() - 1) nodeDisplay.append(", ");
            }
            nodeDisplay.append("}");
        }

        System.out.println(prefix + (isTail ? "└── " : "├── ") + nodeDisplay);

        if (node.getChildren() != null) {
            for (int i = 0; i < node.getChildren().size() - 1; i++) {
                printPlanRecursive(node.getChildren().get(i), prefix + (isTail ? "    " : "│   "), false);
            }
            if (!node.getChildren().isEmpty()) {
                printPlanRecursive(node.getChildren().getLast(), prefix + (isTail ? "    " : "│   "), true);
            }
        }
    }
}
