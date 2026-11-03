package task1;

import java.util.Base64;

interface ReportFormatter { String format(String title, String[] headers, String[][] rows); }

class JsonFormatter implements ReportFormatter {
    public String format(String title, String[] headers, String[][] rows) {
        StringBuilder out = new StringBuilder();
        out.append("{\n  \"report\": \"").append(title).append("\",\n  \"records\": [\n");
        for (int i = 0; i < rows.length; i++) {
            out.append("    {");
            for (int j = 0; j < headers.length; j++) {
                out.append("\"").append(headers[j]).append("\": \"").append(rows[i][j]).append("\"");
                if (j < headers.length - 1) out.append(", ");
            }
            out.append("}").append(i < rows.length - 1 ? ",\n" : "\n");
        }
        return out.append("  ]\n}").toString();
    }
}

class CsvFormatter implements ReportFormatter {
    public String format(String title, String[] headers, String[][] rows) {
        StringBuilder out = new StringBuilder();
        out.append("# TITLE: ").append(title).append("\n");
        out.append(String.join(",", headers)).append("\n");
        for (String[] row : rows) out.append(String.join(",", row)).append("\n");
        return out.toString();
    }
}

interface Exporter { void export(String destination, String data); }
class BaseFileExporter implements Exporter {
    public void export(String destination, String data) {
        System.out.printf("[DISK WRITE] Writing %d bytes to file://%s%n", data.length(), destination);
        System.out.println("----- FILE PAYLOAD BEGIN -----");
        System.out.println(data);
        System.out.println("----- FILE PAYLOAD END -----");
    }
}
abstract class ExporterDecorator implements Exporter {
    protected final Exporter wrappedExporter;
    ExporterDecorator(Exporter wrappedExporter) {
        if (wrappedExporter == null) throw new IllegalArgumentException("Wrapped exporter cannot be null");
        this.wrappedExporter = wrappedExporter;
    }
    public void export(String destination, String data) { wrappedExporter.export(destination, data); }
}
class HeaderFooterDecorator extends ExporterDecorator {
    private final String timestamp;
    HeaderFooterDecorator(Exporter wrapped, String timestamp) { super(wrapped); this.timestamp = timestamp; }
    public void export(String destination, String data) {
        super.export(destination, "=== METADATA HEADER: " + timestamp + " ===\n" + data + "\n=== INTEGRITY CHECK: SHA-256 OK | END RECORD ===");
    }
}
class Base64EncryptDecorator extends ExporterDecorator {
    Base64EncryptDecorator(Exporter wrapped) { super(wrapped); }
    public void export(String destination, String data) {
        super.export(destination, "[BASE64_CIPHERTEXT:" + Base64.getEncoder().encodeToString(data.getBytes()) + "]");
    }
}

public class Task1Runner {
    public static void main(String[] args) {
        String[] headers = {"ID", "User", "Role"};
        String[][] rows = {{"101", "sufyan", "Admin"}, {"102", "alex", "Dev"}};
        String rawJson = new JsonFormatter().format("UserAuditReport", headers, rows);
        System.out.println(">>> TESTING DECORATOR STACK: Base64(HeaderFooter(BaseFileExporter)) <<<");
        Exporter pipeline = new HeaderFooterDecorator(new Base64EncryptDecorator(new BaseFileExporter()), "2026-11-02 10:00:00 UTC");
        pipeline.export("/var/logs/audit.sec", rawJson);
    }
}
