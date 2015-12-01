import org.if4150.databasecruddsl.cRUDModel.Table;
import org.if4150.databasecruddsl.cRUDModel.TableEntry;
import org.if4150.databasecruddsl.cRUDModel.TableEntryWithoutParameter;

import java.io.PrintStream;

public class FormCompiler {

    private final Compiler compiler;

    public FormCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    public void compileCreateForm(Table table, PrintStream out) {

        println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"", 0, out);
        println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", 1, out);
        out.println();

        println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">", 0, out);
        out.println();

        // -------- intermezzo -----------
        String tablename = table.getTableName();
        // -------- resume compiling ---------

        println("<head>", 0, out);
        println("<title>Create for " + tablename + "</title>", 1, out);
        println("</head>", 0, out);
        out.println();

        println("<body>", 0, out);
        println("<form action=\"" + compiler.getCreateBackendLocation(tablename) + "\" method=\"post\" class=\"" + FORM_CLASS + "\">", 1, out);
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

    public void compileReadForm(Table table, PrintStream out) {
        String DIV_READ = "read_table";
        String TABLE_CLASS = "table table-striped";

        String tablename = table.getTableName();

        println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"", 0, out);
        println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", 1, out);
        out.println();

        println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">", 0, out);
        out.println();

        println("<head>", 0, out);
        println("<title>Data " + tablename + "</title>", 1, out);
        println("<?php", 1, out);
        out.println();
        println("$mysqli = new mysqli(\"" + compiler.getDatabaseAddress() + "\", \"" + compiler.getDatabaseUser() +
                "\", \"" + compiler.getDatabasePassword() + "\", \"" + compiler.getDatabaseName() + "\");", 1, out);


        println("/* check connection */", 1, out);
        println("if (mysqli_connect_errno()) {", 1, out);
        println("printf(\"Connect failed: %s\\n\", mysqli_connect_error());", 2, out);
        println("exit();", 2, out);
        println("}", 1, out);
        out.println();

        println("$sql = \"SELECT * FROM " + tablename + "\";", 1, out);
        println("$result = $mysqli->query($sql);", 1, out);
        println("?>", 1, out);
        out.println();

        println("</head>", 0, out);
        out.println();

        println("<body>", 0, out);
        println("<div class=\"" + DIV_READ + "\">", 1, out);
        println("<h1>" + tablename + "</h1>", 2, out);

        println("<?php if($result->num_rows > 0){", 2, out);
        println("// output data of each row ?>", 3, out);

        println("<table class=\"" + TABLE_CLASS + "\">", 2, out);

        println("<tr>", 3, out);
        for (TableEntry tableEntry : table.getTableEntries()) {
            String columnName = tableEntry.getColumnName();
            println("<th>" + columnName + "</th>", 4, out);
        }
        println("</tr>", 3, out);

        println("<?php while($row = $result->fetch_assoc()) { ?>", 3, out);
        println("<tr>", 3, out);

        for (TableEntry tableEntry : table.getTableEntries()) {
            String columnName = tableEntry.getColumnName();
            println("<td><?php echo $row[\"" + columnName + "\"] ?></td>", 4, out);
        }

        println("<td><a href=\"" + compiler.getUpdateFormLocation(tablename) + "?id=<?php echo $row[\"id\"]; ?>\">update</a></td>", 4, out);
        println("<td><a href=\"" + compiler.getDeleteBackendLocation(tablename) + "?id=<?php echo $row[\"id\"]; ?>\">delete</a></td>", 4, out);
        println("</tr>", 3, out);
        println("<?php } ?>", 3, out);
        println("</table>", 2, out);
        println("<?php } else{", 2, out);
        println("echo \"0 Results\";", 3, out);
        println("} ?>", 2, out);

        println("</div>", 1, out);
        println("</body>", 0, out);
        println("</html>", 0, out);
    }

    public void compileUpdateForm(Table table, PrintStream out) {
        String tablename = table.getTableName();

        println("<?php", 0, out);
        println("$mysqli = new mysqli(\"" + compiler.getDatabaseAddress() + "\", \"" + compiler.getDatabaseUser() +
                "\", \"" + compiler.getDatabasePassword() + "\", \"" + compiler.getDatabaseName() + "\");", 0, out);
        println("if ($mysqli->connect_error) {", 0, out);
        println("die(\"Connect Error (\" . $mysqli->connect_errno . \") \" . $mysqli->connect_error);", 1, out);
        println("}", 0, out);
        out.println();

        println("$sqlresult = $mysqli->query(\"SELECT * FROM " + tablename + "  WHERE id = \" . $_GET[\"id\"]);", 0, out);
        println("if (!$sqlresult) {", 0, out);
        println("die(\"entry didn't exist!\");", 1, out);
        println("}", 0, out);
        out.println();

        println("$mysqli->close();", 0, out);
        println("$result = $sqlresult->fetch_assoc();", 0, out);
        println("?>", 0, out);

        println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\"", 0, out);
        println("\"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">", 1, out);
        out.println();

        println("<html xmlns=\"http://www.w3.org/1999/xhtml\" lang=\"en\" xml:lang=\"en\">", 0, out);
        out.println();

        println("<head>", 0, out);
        println("<title>Update for " + tablename + "</title>", 1, out);
        println("</head>", 0, out);
        out.println();

        println("<body>", 0, out);
        println("<form action=\"" + compiler.getUpdateBackendLocation(tablename) + "\" method=\"post\" class=\"" +
                FORM_CLASS + "\">", 1, out);
        println("<h2>Update for " + tablename + "</h2>", 2, out);
        out.println();
        println("<input name=\"id\" type=\"hidden\" value=\"<?php echo $_GET[\"id\"]; ?>\" />", 2, out);


        for (TableEntry tableEntry : table.getTableEntries()) {
            String columnName = tableEntry.getColumnName();

            out.println();
            println("<div class=\"" + LABEL_CLASS + "\">", 2, out);
            println("<label for=\"" + ID_PREFIX + columnName + "\">" + columnName + ":</label>", 3, out);
            println("</div>", 2, out);
            println("<div class=\"" + INPUT_CLASS + "\">", 2, out);
            println(getInputTagWithValue(tableEntry), 3, out);
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

    private void println(String line, int nestLevel, PrintStream out) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < SPACES_PER_LEVEL * nestLevel; i++) {
            stringBuilder.append(' ');
        }

        stringBuilder.append(line);

        out.println(stringBuilder.toString());
    }

    private String getInputTag(TableEntry tableEntry) {
        String columnName = tableEntry.getColumnName();

        if (tableEntry instanceof TableEntryWithoutParameter) {
            TableEntryWithoutParameter downcasted = (TableEntryWithoutParameter) tableEntry;
            if (downcasted.getDatatype().equals(BOOLEAN_TYPE)) {
                return "<input id=\"" + ID_PREFIX + columnName + "\" name=\"" + columnName +
                        "\" type=\"checkbox\" />";
            }
        }

        return "<input id=\"" + ID_PREFIX + columnName + "\" name=\"" + columnName + "\" type=\"text\" />";
    }

    private String getInputTagWithValue(TableEntry tableEntry) {
        String columnName = tableEntry.getColumnName();

        if (tableEntry instanceof TableEntryWithoutParameter) {
            TableEntryWithoutParameter downcasted = (TableEntryWithoutParameter) tableEntry;
            if (downcasted.getDatatype().equals(BOOLEAN_TYPE)) {
                return "<input id=\"" + ID_PREFIX + columnName + "\" name=\"" + columnName +
                        "\" type=\"checkbox\" <?php if ($result[\"" + columnName +
                        "\"]) { echo \"checked=\\\"checked\\\"\"; } ?> />";
            }
        }

        return "<input id=\"" + ID_PREFIX + columnName + "\" name=\"" + columnName +
                "\" type=\"text\" value=\"<?php echo $result[\"" + columnName + "\"] ?>\" />";
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
