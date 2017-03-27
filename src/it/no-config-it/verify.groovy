// From http://stackoverflow.com/questions/159148/groovy-executing-shell-commands
// a wrapper closure around executing a string
// can take either a string or a list of strings (for arguments with spaces)
// prints all output, complains and halts on error
def runCommand = { strList ->
    assert ( strList instanceof String ||
            ( strList instanceof List && strList.each{ it instanceof String } ) \
)
    def proc = strList.execute()
    proc.in.eachLine { line -> println line }
    proc.out.close()
    proc.waitFor()

    print "[INFO] ( "
    if(strList instanceof List) {
        strList.each { print "${it} " }
    } else {
        print strList
    }
    println " )"

    if (proc.exitValue()) {
        println "gave the following error: "
        println "[ERROR] ${proc.getErrorStream()}"
    }
    assert !proc.exitValue()
}

runCommand("find .");

class ExistenceChecker {
    private File basedir;
    private String prefix;

    ExistenceChecker(File basedir, String prefix) {
        this.basedir = basedir;
        this.prefix = prefix
    }

    public void assertExists(String f) {
        def file = new File(basedir, prefix + f)
        println("file: " + file)
        assert file.isFile();
    }
}

def generatedSourcesChecker = new ExistenceChecker(basedir, "target/generated-sources/rdl/")
def expectedFiles = Arrays.asList(
        "com/ApiAssetsRespObject.java",
        "com/ApiResourcesHandler.java",
        "com/ApiResourcesResources.java",
        "com/ApiResourcesSchema.java",
        "com/ApiResourcesServer.java",
        "com/ResourceError.java",
        "com/ResourceException.java",
        "com/ResourceContext.java");
for (String f : expectedFiles) {
    generatedSourcesChecker.assertExists(f);
}

def compiledClassesChecker = new ExistenceChecker(basedir, "target/classes/");
def expectedClassFiles = Arrays.asList(
        "com/ApiAssetsRespObject.class",
        "com/ApiResourcesHandler.class",
        "com/ApiResourcesResources.class",
        "com/ApiResourcesSchema.class",
        "com/ApiResourcesServer.class",
        "com/ResourceError.class",
        "com/ResourceException.class",
        "com/ResourceContext.class");
for (String f : expectedClassFiles) {
    compiledClassesChecker.assertExists(f);
}
