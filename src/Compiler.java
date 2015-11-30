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
    }

    private void generateForm(Database database) throws FileNotFoundException {
        File formsDirectory = new File(FORMS_OUTPUT_DIRECTORY);

        // create directories if it doesn't exist
        if (!formsDirectory.exists()) {
            formsDirectory.mkdirs();
        } else if (!formsDirectory.isDirectory()) {
            throw new FileNotFoundException(FORMS_OUTPUT_DIRECTORY + " is not a directory! I don't understand...");
        }

        for (Table table : database.getTables()) {
            generateForm(table);
        }
    }

    public void generateForm(Table table) throws FileNotFoundException {



        File createFormFile = new File(FORMS_OUTPUT_DIRECTORY + "/create.xhtml");

        // no need to close on exception because nothing can be closed anyway
        OutputStream outputStream = new FileOutputStream(createFormFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        PrintStream createFormStream = new PrintStream(bufferedOutputStream);

        // compile create form
        formCompiler.compileCreateForm(table, createFormStream);

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

    private static final String FORMS_OUTPUT_DIRECTORY = "forms";
    
}
