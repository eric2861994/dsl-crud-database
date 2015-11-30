import com.google.inject.Injector;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.eclipse.xtext.validation.Issue;
import org.if4150.databasecruddsl.CRUDModelStandaloneSetup;
import org.if4150.databasecruddsl.cRUDModel.*;

import java.io.*;
import java.util.List;

public class Compiler {
    private final FormCompiler formCompiler = new FormCompiler();
    private final BackendCompiler backendCompiler = new BackendCompiler();

    private Compiler() {

    }

    public static void main(String[] args) throws FileNotFoundException {
        new Compiler().run();
    }

    public void run() throws FileNotFoundException {
        Injector injector = new CRUDModelStandaloneSetup().createInjectorAndDoEMFRegistration();
        IResourceValidator iResourceValidator = injector.getInstance(IResourceValidator.class);
        XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
        resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
        Resource resource = resourceSet.getResource(URI.createURI("res/test.crud"), true);

        for (Resource.Diagnostic diagnostic : resource.getErrors()) {
            System.err.println(diagnostic);
        }

        List<Issue> issues = iResourceValidator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
        if (issues.size() != 0) {
            for (Issue issue : issues) {
                System.err.println(issue);
            }
            System.exit(1);
        }

        CRUDModel crudModel = (CRUDModel) resource.getContents().get(0);

        generateForm((Database) crudModel);
        generateBackend((Database) crudModel);
    }

    private void generateForm(Database database) throws FileNotFoundException {
        File formsDirectory = new File(FORMS_OUTPUT_DIRECTORY);

        // create directories if it doesn't exist
        if (!formsDirectory.exists()) {
            formsDirectory.mkdir();
        } else if (!formsDirectory.isDirectory()) {
            throw new FileNotFoundException(FORMS_OUTPUT_DIRECTORY + " is not a directory! I don't understand...");
        }

        for (Table table : database.getTables()) {
            generateForm(table);
        }
    }
    
    public void generateBackend(Database database) throws FileNotFoundException{
        File formsDirectory = new File(BACKEND_OUTPUT_DIRECTORY);

        // create directories if it doesn't exist
        if (!formsDirectory.exists()) {
            formsDirectory.mkdir();
        } else if (!formsDirectory.isDirectory()) {
            throw new FileNotFoundException(BACKEND_OUTPUT_DIRECTORY + " is not a directory! I don't understand...");
        }

        for (Table table : database.getTables()) {
            generateBackend(database, table);
        }
    }

    public void generateForm(Table table) throws FileNotFoundException {
        String tablename = table.getTableName();

        String completePath = FORMS_OUTPUT_DIRECTORY + "/" + tablename;
        File tableDirectory = new File(completePath);

        // create directories if it doesn't exist
        if (!tableDirectory.exists()) {
            tableDirectory.mkdir();
        } else if (!tableDirectory.isDirectory()) {
            throw new FileNotFoundException(completePath + " is not a directory! I don't understand...");
        }

        File createFormFile = new File(completePath + "/create.xhtml");

        // no need to close on exception because nothing can be closed anyway
        OutputStream outputStream = new FileOutputStream(createFormFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        PrintStream createFormStream = new PrintStream(bufferedOutputStream);

        // compile create form
        formCompiler.compileCreateForm(table, createFormStream);

        createFormStream.close();
    }
    
    public void generateBackend(Database database, Table table) throws FileNotFoundException {
        String tablename = table.getTableName();

        String completePath = BACKEND_OUTPUT_DIRECTORY + "/" + tablename;
        File tableDirectory = new File(completePath);

        // create directories if it doesn't exist
        if (!tableDirectory.exists()) {
            tableDirectory.mkdir();
        } else if (!tableDirectory.isDirectory()) {
            throw new FileNotFoundException(completePath + " is not a directory! I don't understand...");
        }

        generateCreateBackend(completePath, table);

//        File createBackendFile = new File(completePath + "/backend.php");
//
//        // no need to close on exception because nothing can be closed anyway
//        OutputStream outputStream = new FileOutputStream(createBackendFile);
//        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
//        PrintStream createFormStream = new PrintStream(bufferedOutputStream);
//
//        // compile create form
//        backendCompiler.compilerReadBackend(database, table, createFormStream);
//
//        createFormStream.close();
    }

    public void generateCreateBackend(String completePath, Table table) throws FileNotFoundException {
        File mCreateBackendFile = new File(completePath + "/create.php");

        // no need to close on exception because nothing can be closed anyway
        OutputStream outputStream = new FileOutputStream(mCreateBackendFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        PrintStream createFormStream = new PrintStream(bufferedOutputStream);

        // compile create form
        backendCompiler.compileCreateBackend(table, createFormStream);

        createFormStream.close();
    }

    public void processDatabase(Database database) {
        System.out.println("database " + database.getDatabaseName() + " {");
        for (Table table : database.getTables()) {
            processTable(table);
        }
        System.out.println("}");
    }

    public void processTable(Table table) {
        System.out.println("table " + table.getTableName() + " {");
        for (TableEntry tableEntry : table.getTableEntries()) {
            processTableEntry(tableEntry);
        }
        System.out.println("}");
    }

    public void processTableEntry(TableEntry tableEntry) {
        if (tableEntry instanceof TableEntryWithoutParameter) {
            TableEntryWithoutParameter tableEntryWithoutParameter = (TableEntryWithoutParameter) tableEntry;
            processTableEntryWithoutParameter(tableEntryWithoutParameter);

        } else if (tableEntry instanceof TableEntryWithParameter) {
            TableEntryWithParameter tableEntryWithParameter = (TableEntryWithParameter) tableEntry;
            processTableEntryWithParameter(tableEntryWithParameter);

        } else {
            // TODO this is impossible
        }
    }

    public void processTableEntryWithoutParameter(TableEntryWithoutParameter tableEntryWithoutParameter) {

        String datatype = tableEntryWithoutParameter.getDatatype();
        String columnName = tableEntryWithoutParameter.getColumnName();

        System.out.println(datatype + " " + columnName + ";");
    }

    public void processTableEntryWithParameter(TableEntryWithParameter tableEntryWithParameter) {
        SQLTypeWithParameter sqlTypeWithParameter = tableEntryWithParameter.getDatatype();
        processSQLTypeWithParameter(sqlTypeWithParameter);

        System.out.println(tableEntryWithParameter.getColumnName() + ";");
    }

    public void processSQLTypeWithParameter(SQLTypeWithParameter sqlTypeWithParameter) {
        if (sqlTypeWithParameter instanceof SQLString) {
            SQLString sqlString = (SQLString) sqlTypeWithParameter;
            processSQLString(sqlString);

        } else {
            // TODO this is impossible
        }
    }

    public void processSQLString(SQLString sqlString) {
        System.out.print("string(" + sqlString.getStringLength() + ")");
    }

    public static String getCreateFormLocation(String tablename) {
        return "<INSERT URL HERE>/" + FORMS_OUTPUT_DIRECTORY + '/' + tablename + '/' + CREATE_FRAGMENT + ".xhtml";
    }

    public static final String FORMS_OUTPUT_DIRECTORY = "forms";
    public static final String BACKEND_OUTPUT_DIRECTORY = "backend";
    public static final String CREATE_FRAGMENT = "create";
    public static final String READ_FRAGMENT = "read";
    public static final String UPDATE_FRAGMENT = "update";
    public static final String DELETE_FRAGMENT = "delete";
}
