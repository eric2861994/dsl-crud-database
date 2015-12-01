import com.google.inject.Injector;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextResourceSet;
import org.if4150.databasecruddsl.CRUDModelStandaloneSetup;
import org.if4150.databasecruddsl.cRUDModel.CRUDModel;
import org.if4150.databasecruddsl.cRUDModel.Database;
import org.if4150.databasecruddsl.cRUDModel.Table;

import java.io.*;

public class Compiler {
    private final FormCompiler formCompiler = new FormCompiler(this);
    private final BackendCompiler backendCompiler = new BackendCompiler(this);
    private final String databaseAddress;
    private final String databaseUser;
    private final String databasePassword;
    private final String baseURL;

    private String databaseName;

    private Compiler(String[] args) {
        databaseAddress = args[0];
        databaseUser = args[1];
        databasePassword = args[2];
        baseURL = args[3];
    }

    public static void main(String[] args) throws FileNotFoundException {
        new Compiler(args).run();
    }

    public void run() throws FileNotFoundException {
        Injector injector = new CRUDModelStandaloneSetup().createInjectorAndDoEMFRegistration();
        XtextResourceSet resourceSet = injector.getInstance(XtextResourceSet.class);
        resourceSet.addLoadOption(XtextResource.OPTION_RESOLVE_ALL, Boolean.TRUE);
        Resource resource = resourceSet.getResource(URI.createURI("res/test.crud"), true);

        for (Resource.Diagnostic diagnostic : resource.getErrors()) {
            System.err.println(diagnostic);
        }

        CRUDModel crudModel = (CRUDModel) resource.getContents().get(0);
        Database database = (Database) crudModel;
        databaseName = database.getDatabaseName();

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

        generateCreateForm(completePath, table);
        generateReadForm(completePath, table);
        generateUpdateForm(completePath, table);
    }

    private void generateCreateForm(String completePath, Table table) throws FileNotFoundException {
        File createFormFile = new File(completePath + '/' + CREATE_FRAGMENT + ".php");

        // no need to close on exception because nothing can be closed anyway
        OutputStream outputStream = new FileOutputStream(createFormFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        PrintStream createFormStream = new PrintStream(bufferedOutputStream);

        // compile create form
        formCompiler.compileCreateForm(table, createFormStream);

        createFormStream.close();
    }

    private void generateReadForm(String completePath, Table table) throws FileNotFoundException {
        File readFormFile = new File(completePath + '/' + READ_FRAGMENT + ".php");

        // no need to close on exception because nothing can be closed anyway
        OutputStream outputStream = new FileOutputStream(readFormFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        PrintStream readFormStream = new PrintStream(bufferedOutputStream);

        // compile create form
        formCompiler.compileReadForm(table, readFormStream);

        readFormStream.close();
    }

    private void generateUpdateForm(String completePath, Table table) throws FileNotFoundException {
        File updateFromFile = new File(completePath + '/' + UPDATE_FRAGMENT + ".php");

        // no need to close on exception because nothing can be closed anyway
        OutputStream outputStream = new FileOutputStream(updateFromFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        PrintStream updateFormStream = new PrintStream(bufferedOutputStream);

        // compile create form
        formCompiler.compileUpdateForm(table, updateFormStream);

        updateFormStream.close();
    }

    public void generateBackend(Database database) throws FileNotFoundException {
        File backendDirectory = new File(BACKEND_OUTPUT_DIRECTORY);

        // create directories if it doesn't exist
        if (!backendDirectory.exists()) {
            backendDirectory.mkdir();
        } else if (!backendDirectory.isDirectory()) {
            throw new FileNotFoundException(BACKEND_OUTPUT_DIRECTORY + " is not a directory! I don't understand...");
        }

        for (Table table : database.getTables()) {
            generateBackend(table);
        }
    }

    public void generateBackend(Table table) throws FileNotFoundException {
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
        generateUpdateBackend(completePath, table);
        generateDeleteBackend(completePath, table);
    }

    public void generateCreateBackend(String completePath, Table table) throws FileNotFoundException {
        File createBackendFile = new File(completePath + '/' + CREATE_FRAGMENT + ".php");

        // no need to close on exception because nothing can be closed anyway
        OutputStream outputStream = new FileOutputStream(createBackendFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        PrintStream createFormStream = new PrintStream(bufferedOutputStream);

        // compile create form
        backendCompiler.compileCreateBackend(table, createFormStream);

        createFormStream.close();
    }

    public void generateUpdateBackend(String completePath, Table table) throws FileNotFoundException {
        File updateBackendFile = new File(completePath + '/' + UPDATE_FRAGMENT + ".php");

        // no need to close on exception because nothing can be closed anyway
        OutputStream outputStream = new FileOutputStream(updateBackendFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        PrintStream updateFormStream = new PrintStream(bufferedOutputStream);

        // compile create form
        backendCompiler.compileUpdateBackend(table, updateFormStream);

        updateFormStream.close();
    }

    public void generateDeleteBackend(String completePath, Table table) throws FileNotFoundException {
        File deleteBackendFile = new File(completePath + '/' + DELETE_FRAGMENT + ".php");

        // no need to close on exception because nothing can be closed anyway
        OutputStream outputStream = new FileOutputStream(deleteBackendFile);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(outputStream);
        PrintStream deleteFormStream = new PrintStream(bufferedOutputStream);

        // compile create form
        backendCompiler.compileDeleteBackend(table, deleteFormStream);

        deleteFormStream.close();
    }

    public String getCreateFormLocation(String tablename) {
        return '/' + BASE_DIRECTORY + '/' + FORMS_OUTPUT_DIRECTORY + '/' + tablename + '/' + CREATE_FRAGMENT + ".php";
    }

    public String getReadFormLocation(String tablename) {
        return '/' + BASE_DIRECTORY + '/' + FORMS_OUTPUT_DIRECTORY + '/' + tablename + '/' + READ_FRAGMENT + ".php";
    }

    public String getUpdateFormLocation(String tablename) {
        return '/' + BASE_DIRECTORY + '/' + FORMS_OUTPUT_DIRECTORY + '/' + tablename + '/' + UPDATE_FRAGMENT + ".php";
    }

    public String getCreateBackendLocation(String tablename) {
        return '/' + BASE_DIRECTORY + '/' + BACKEND_OUTPUT_DIRECTORY + '/' + tablename + '/' + CREATE_FRAGMENT + ".php";
    }

    public String getUpdateBackendLocation(String tablename) {
        return '/' + BASE_DIRECTORY + '/' + BACKEND_OUTPUT_DIRECTORY + '/' + tablename + '/' + UPDATE_FRAGMENT + ".php";
    }

    public String getDeleteBackendLocation(String tablename) {
        return '/' + BASE_DIRECTORY + '/' + BACKEND_OUTPUT_DIRECTORY + '/' + tablename + '/' + DELETE_FRAGMENT + ".php";
    }

    public String getDatabaseAddress() {
        return databaseAddress;
    }

    public String getDatabaseUser() {
        return databaseUser;
    }

    public String getDatabasePassword() {
        return databasePassword;
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public static final String FORMS_OUTPUT_DIRECTORY = "forms";
    public static final String BACKEND_OUTPUT_DIRECTORY = "backend";
    public static final String CREATE_FRAGMENT = "create";
    public static final String READ_FRAGMENT = "read";
    public static final String UPDATE_FRAGMENT = "update";
    public static final String DELETE_FRAGMENT = "delete";
    public static final String BASE_DIRECTORY = "rplsd";
}
