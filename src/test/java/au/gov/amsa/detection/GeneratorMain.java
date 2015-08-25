package au.gov.amsa.detection;

import java.io.File;

import xuml.tools.miuml.metamodel.jaxb.Domains;
import xuml.tools.miuml.metamodel.jaxb.Marshaller;
import xuml.tools.model.compiler.CodeGeneratorJava;

public class GeneratorMain {

    public static void main(String[] args) {

        Domains domains = new Marshaller()
                .unmarshal(GeneratorMain.class.getResourceAsStream("/domains.xml"));

        String domain = "detection-domain";
        String packageName = "au.gov.amsa.detection.model";
        String schema = "DETECTION";
        File outputSourceDirectory = new File("target/generated-sources");
        File resourcesDirectory = new File("target/generated-resources");
        String implementationPackageName = "au.gov.amsa.detection.model";
        File implementationSourceDirectory = new File("target/generated-sources");
        boolean generatePersistenceXml = false;
        boolean implementationOverwrite = true;
        new CodeGeneratorJava(domains, domain, packageName, schema, outputSourceDirectory,
                resourcesDirectory, implementationPackageName, implementationSourceDirectory,
                generatePersistenceXml, implementationOverwrite).generate();
    }

}
