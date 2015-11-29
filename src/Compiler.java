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

import java.util.List;

public class Compiler {
    public static void main(String[] args) {

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
        processCRUDModel(crudModel);
    }

    public static void processCRUDModel(CRUDModel crudModel) {
        if (crudModel instanceof Database) {
            Database database = (Database) crudModel;
            processDatabase(database);

        } else {
            // TODO this is impossible
        }
    }

    public static void processDatabase(Database database) {
        System.out.println("database " + database.getDatabaseName() + " {");
        for (Table table : database.getTables()) {
            processTable(table);
        }
        System.out.println("}");
    }

    public static void processTable(Table table) {
        System.out.println("table " + table.getTableName() + " {");
        for (TableEntry tableEntry : table.getTableEntries()) {
            processTableEntry(tableEntry);
        }
        System.out.println("}");
    }

    public static void processTableEntry(TableEntry tableEntry) {
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

    public static void processTableEntryWithoutParameter(TableEntryWithoutParameter tableEntryWithoutParameter) {

        String datatype = tableEntryWithoutParameter.getDatatype();
        String columnName = tableEntryWithoutParameter.getColumnName();

        System.out.println(datatype + " " + columnName + ";");
    }

    public static void processTableEntryWithParameter(TableEntryWithParameter tableEntryWithParameter) {
        SQLTypeWithParameter sqlTypeWithParameter = tableEntryWithParameter.getDatatype();
        processSQLTypeWithParameter(sqlTypeWithParameter);

        System.out.println(tableEntryWithParameter.getColumnName() + ";");
    }

    public static void processSQLTypeWithParameter(SQLTypeWithParameter sqlTypeWithParameter) {
        if (sqlTypeWithParameter instanceof SQLString) {
            SQLString sqlString = (SQLString) sqlTypeWithParameter;
            processSQLString(sqlString);

        } else {
            // TODO this is impossible
        }
    }

    public static void processSQLString(SQLString sqlString) {
        System.out.print("string(" + sqlString.getStringLength() + ")");
    }
}
