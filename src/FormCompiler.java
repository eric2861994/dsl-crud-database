import org.if4150.databasecruddsl.cRUDModel.Table;
import org.if4150.databasecruddsl.cRUDModel.TableEntry;
import org.if4150.databasecruddsl.cRUDModel.TableEntryWithoutParameter;

import java.io.PrintStream;

public class FormCompiler {

    private void println(String line, int nestLevel, PrintStream out) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < SPACES_PER_LEVEL * nestLevel; i++) {
            stringBuilder.append(' ');
        }

        stringBuilder.append(line);

        out.println(stringBuilder.toString());
    }

    public void compileCreateForm(Table table, PrintStream out) {
        String tablename = table.getTableName();

        println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"", 0, out);
        println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", 1, out);
        out.println();

        println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">", 0, out);
        out.println();

        println("<head>", 0, out);
        println("<title>Create for " + tablename + "</title>", 1, out);
        println("</head>", 0, out);
        out.println();

        println("<body>", 0, out);
        println("<form action=\"" + URL_STUB + "/" + tablename + ".php\" method=\"post\" class=\"" + FORM_CLASS + "\">", 1, out);
        println("<h2>Create for " + tablename + "</h2>", 2, out);

        for (TableEntry tableEntry : table.getTableEntries()) {
            String columnName = tableEntry.getColumnName();

            out.println();
            println("<div class=\"" + LABEL_CLASS + "\">", 2, out);
            println("<label for=\"" + ID_PREFIX + columnName + "\">" + columnName + ":</label>", 3, out);
            println("</div>", 2, out);
            println("<div class=\"" + INPUT_CLASS + "\">", 2, out);
            println(getInputTag(tableEntry), 3, out);
            println("</div>", 2, out);
        }

        out.println();
        println("<div class=\"" + SUBMIT_CLASS + "\">", 2, out);
        println("<input type=\"submit\" value=\"Submit\" />", 3, out);
        println("</div>", 2, out);
        println("</form>", 1, out);
        println("</body>", 0, out);
        out.println();

        println("</html>", 0, out);
    }

    private String getInputTag(TableEntry tableEntry) {
        String columnName = tableEntry.getColumnName();

        if (tableEntry instanceof TableEntryWithoutParameter) {
            TableEntryWithoutParameter downcasted = (TableEntryWithoutParameter) tableEntry;
            if (downcasted.getDatatype().equals(BOOLEAN_TYPE)) {
                return "<input id=\"" + ID_PREFIX + columnName + "\" name=\"" + columnName + "\" type=\"checkbox\" />";
            }
        }

        return "<input id=\"" + ID_PREFIX + columnName + "\" name=\"" + columnName + "\" type=\"text\" />";
    }

    private static final int SPACES_PER_LEVEL = 2;
    private static final String URL_STUB = "(INSERT_URL_HERE)";
    private static final String ID_PREFIX = "crud_";
    private static final String FORM_CLASS = "crud_form";
    private static final String INPUT_CLASS = "crud_input";
    private static final String LABEL_CLASS = "crud_label";
    private static final String SUBMIT_CLASS = "crud_submit";
    private static final String BOOLEAN_TYPE = "boolean";
}
