import org.if4150.databasecruddsl.cRUDModel.*;

import java.io.PrintStream;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

public class BackendCompiler {

    private final Compiler compiler;

    public BackendCompiler(Compiler compiler) {
        this.compiler = compiler;
    }

    public void compileCreateBackend(Table table, PrintStream out) {
        String tablename = table.getTableName();

        println("<?php", 0, out);
        println("$posted = $_POST;", 0, out);
        checkStringLength(table, out);
        changeBooleanValue(table, out);
        out.println();

        println("$mysqli = new mysqli(\"" + compiler.getDatabaseAddress() + "\", \"" + compiler.getDatabaseUser() +
                "\", \"" + compiler.getDatabasePassword() + "\", \"" + compiler.getDatabaseName() + "\");", 0, out);
        println("if ($mysqli->connect_error) {", 0, out);
        println("die(\"Connect Error (\" . $mysqli->connect_errno . \") \" . $mysqli->connect_error);", 1, out);
        println("}", 0, out);
        out.println();

        println("if ($stmt = $mysqli->prepare(\"INSERT INTO " + tablename + " " + getColumnTuple(table) + " VALUES " +
                getQuestionTuple(table) + ";\")) {", 0, out);

        // --------- intermezzo -----------
        // get value types
        StringBuilder types = new StringBuilder();
        for (TableEntry tableEntry : table.getTableEntries()) {
            types.append(getEntryType(tableEntry));
        }

        // get values
        StringBuilder values = new StringBuilder();
        for (TableEntry tableEntry : table.getTableEntries()) {
            values.append("$posted[\"");
            values.append(tableEntry.getColumnName());
            values.append("\"], ");
        }

        int valueSize = values.length();
        if (valueSize > 0) {
            values.delete(valueSize - 2, valueSize);
        }
        // --------- resume compiling ----------

        println("$stmt->bind_param(\"" + types.toString() + "\", " + values.toString() + ");", 1, out);
        println("$stmt->execute();", 1, out);
        println("$stmt->close();", 1, out);
        println("}", 0, out);
        out.println();

        println("$mysqli->close();", 0, out);
        println("header(\"Location: " + compiler.getCreateFormLocation(tablename) + "\");", 0, out);
        println("die();", 0, out);
        println("?>", 0, out);
    }

    public void compileUpdateBackend(Table table, PrintStream out) {
        String tablename = table.getTableName();

        println("<?php", 0, out);
        println("$posted = $_POST;", 0, out);
        checkStringLength(table, out);
        changeBooleanValue(table, out);
        out.println();

        println("$mysqli = new mysqli(\"" + compiler.getDatabaseAddress() + "\", \"" + compiler.getDatabaseUser() +
                "\", \"" + compiler.getDatabasePassword() + "\", \"" + compiler.getDatabaseName() + "\");", 0, out);
        println("if ($mysqli->connect_error) {", 0, out);
        println("die(\"Connect Error (\" . $mysqli->connect_errno . \") \" . $mysqli->connect_error);", 1, out);
        println("}", 0, out);
        out.println();

        // --------- intermezzo -----------

        StringBuilder columnQuestion = new StringBuilder();
        for (TableEntry tableEntry : table.getTableEntries()) {
            columnQuestion.append(tableEntry.getColumnName());
            columnQuestion.append(" = ?, ");
        }

        int columnQuestionLength = columnQuestion.length();
        if (columnQuestionLength > 0) {
            columnQuestion.delete(columnQuestionLength - 2, columnQuestionLength);
        }

        // --------- resume compiling ----------

        println("if ($stmt = $mysqli->prepare(\"UPDATE " + tablename + " SET " + columnQuestion.toString() +
                " WHERE ID = \" . $posted[\"id\"])) {", 0, out);

        // --------- intermezzo -----------
        // get value types
        StringBuilder types = new StringBuilder();
        for (TableEntry tableEntry : table.getTableEntries()) {
            types.append(getEntryType(tableEntry));
        }

        // get values
        StringBuilder values = new StringBuilder();
        for (TableEntry tableEntry : table.getTableEntries()) {
            values.append("$posted[\"");
            values.append(tableEntry.getColumnName());
            values.append("\"], ");
        }

        int valueSize = values.length();
        if (valueSize > 0) {
            values.delete(valueSize - 2, valueSize);
        }
        // --------- resume compiling ----------

        println("$stmt->bind_param(\"" + types.toString() + "\", " + values.toString() + ");", 1, out);
        println("$stmt->execute();", 1, out);
        println("$stmt->close();", 1, out);
        println("}", 0, out);
        out.println();

        println("$mysqli->close();", 0, out);
        println("header(\"Location: " + compiler.getReadFormLocation(tablename) + "\");", 0, out);
        println("die();", 0, out);
        println("?>", 0, out);
    }

    public void compileDeleteBackend(Table table, PrintStream out) {
        String tablename = table.getTableName();
        String databaseName = "INSERT DATABASE NAME HERE";

        println("<?php", 0, out);
        out.println();
        println("$mysqli = new mysqli(\"" + compiler.getDatabaseAddress() + "\", \"" + compiler.getDatabaseUser() +
                "\", \"" + compiler.getDatabasePassword() + "\", \"" + compiler.getDatabaseName() + "\");", 0, out);

        println("/* check connection */", 0, out);
        println("if (mysqli_connect_errno()) {", 0, out);
        println("printf(\"Connect failed: %s\\n\", mysqli_connect_error());", 1, out);
        println("exit();", 1, out);
        println("}", 0, out);
        out.println();

        println("$id = $_GET[\"id\"];", 0, out);
        println("$sql = \"DELETE FROM " + tablename + " WHERE id = '\".$id.\"'\";", 0, out);
        println("if ($mysqli->query($sql) == TRUE ) {", 0, out);
        println("}", 0, out);

        println("$mysqli->close();", 0, out);
        println("header(\"Location: " + compiler.getReadFormLocation(tablename) + "\");", 0, out);
        println("die();", 0, out);
        println("?>", 0, out);
    }

    private void checkStringLength(Table table, PrintStream out) {
        for (TableEntry tableEntry : table.getTableEntries()) {
            if (tableEntry instanceof TableEntryWithParameter) {
                TableEntryWithParameter downcasted = (TableEntryWithParameter) tableEntry;
                SQLTypeWithParameter sqlTypeWithParameter = downcasted.getDatatype();
                if (sqlTypeWithParameter instanceof SQLString) {
                    int stringLength = ((SQLString) sqlTypeWithParameter).getStringLength();
                    String columnName = tableEntry.getColumnName();

                    println("if (strlen($posted[\"" + columnName + "\"]) > " + stringLength + ") {", 0, out);
                    println("die(\"" + columnName + " is too long!\");", 1, out);
                    println("}", 0, out);
                }
            }
        }
    }

    private void changeBooleanValue(Table table, PrintStream out) {
        for (TableEntry tableEntry : table.getTableEntries()) {
            String columnName = tableEntry.getColumnName();

            if (isBooleanEntry(tableEntry)) {
                println("$posted[\"" + columnName + "\"] = $_POST[\"" + columnName + "\"] === \"on\";", 0, out);
            }
        }
    }

    private char getEntryType(TableEntry tableEntry) {
        if (tableEntry instanceof TableEntryWithoutParameter) {
            TableEntryWithoutParameter downcasted = (TableEntryWithoutParameter) tableEntry;

            String datatype = downcasted.getDatatype();
            if (datatype.equals("integer")) {
                return 'i';

            } else if (datatype.equals("float")) {
                return 'd';
            }
        }

        return 's';
    }

    private String getColumnTuple(Table table) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append('(');

        for (TableEntry tableEntry : table.getTableEntries()) {
            stringBuilder.append(tableEntry.getColumnName());
            stringBuilder.append(", ");
        }

        int lastIdx = stringBuilder.length() - 1;
        if (stringBuilder.charAt(lastIdx) != '(') {
            stringBuilder.delete(lastIdx - 1, lastIdx + 1);
        }

        stringBuilder.append(')');

        return stringBuilder.toString();
    }

    private String getQuestionTuple(Table table) {
        StringBuilder stringBuilder = new StringBuilder();

        stringBuilder.append('(');

        for (TableEntry tableEntry : table.getTableEntries()) {
            stringBuilder.append("?, ");
        }

        int lastIdx = stringBuilder.length() - 1;
        if (stringBuilder.charAt(lastIdx) != '(') {
            stringBuilder.delete(lastIdx - 1, lastIdx + 1);
        }

        stringBuilder.append(')');

        return stringBuilder.toString();
    }

    private boolean isBooleanEntry(TableEntry entry) {
        if (entry instanceof TableEntryWithoutParameter) {
            TableEntryWithoutParameter downcasted = (TableEntryWithoutParameter) entry;
            if (downcasted.getDatatype().equals("boolean")) {
                return true;
            }
        }

        return false;
    }

    private void println(String line, int nestLevel, PrintStream out) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < SPACES_PER_LEVEL * nestLevel; i++) {
            stringBuilder.append(' ');
        }

        stringBuilder.append(line);

        out.println(stringBuilder.toString());
    }

    private static final int SPACES_PER_LEVEL = 2;
}
