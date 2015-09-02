package au.gov.amsa.detection;

import xuml.tools.miuml.metamodel.jaxb.Domains;
import xuml.tools.miuml.metamodel.jaxb.Marshaller;
import xuml.tools.model.compiler.CodeGeneratorJava;

public class GeneratorMain {

    public static void main(String[] args) {

        Domains domains = new Marshaller()
                .unmarshal(GeneratorMain.class.getResourceAsStream("/domains.xml"));

        CodeGeneratorJava.builder().domains(domains).domainName("detection-domain")
                .domainSchema("DETECTION").domainPackageName("au.gov.amsa.detection.model")
                .generatedSourcesDirectory("target/generated-sources")
                .generatedResourcesDirectory("target/generated-resources")
                .generatePersistenceXml(true).build().generate();
    }

}
