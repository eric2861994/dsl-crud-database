
import java.io.PrintStream;
import org.if4150.databasecruddsl.cRUDModel.Database;
import org.if4150.databasecruddsl.cRUDModel.Table;
import org.if4150.databasecruddsl.cRUDModel.TableEntry;
import org.if4150.databasecruddsl.cRUDModel.TableEntryWithoutParameter;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author ahmadshahab
 */
public class BackendCompiler {
    
    private void println(String line, int nestLevel, PrintStream out) {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < SPACES_PER_LEVEL * nestLevel; i++) {
            stringBuilder.append(' ');
        }

        stringBuilder.append(line);

        out.println(stringBuilder.toString());
    }
    
    public void compilerReadBackend(Database database, Table table, PrintStream out){
        System.out.println("masuk");
    }

    public void compileCreateBackend(Table table, PrintStream out) {
        String tablename = table.getTableName();

        println("<?php", 0, out);
        println("$mysqli = new mysqli(<location>, <user>, <password>, <database>);", 0, out);
        println("$headers = apache_request_headers();", 0, out);


        println("if ($mysqli->connect_error) {", 0, out);
        println("die('Connect Error (' . $mysqli->connect_errno . ') ' . $mysqli->connect_error);", 1, out);
        println("}", 0, out);

        println("if ($stmt = $mysqli->prepare(\"INSERT INTO " + tablename + " " + getColumnTuple(table) + " VALUES " +
                getQuestionTuple(table) + ";\")) {", 0, out);
        for (TableEntry tableEntry : table.getTableEntries()) {
            println("$stmt->bind_param(\"" + getEntryType(tableEntry) + "\", $headers[" +
                    tableEntry.getColumnName() + "]);", 1, out);
        }
        println("}", 0, out);

        println("$mysqli->close();", 0, out);

        println("?>", 0, out);
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
    
    private static final int SPACES_PER_LEVEL = 2;
}
