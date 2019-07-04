# Source To Image Example 

A very simple example that demonstrates how to use `@OpenshiftApplication`.
Check the [Main.java](src/main/java/io/dekorate/examples/openshift/Main.java) which bears the annotation.
To access the `@OpenshiftApplication` annotation you just need to have the following dependency in your
class path:

    <dependency>
      <groupId>io.dekorate</groupId>
      <artifactId>openshift-annotations</artifactId>
      <version>${project.version}</version>
    </dependency>
    
Compile the project using:

    mvn clean install
    
You can find the generated deployment under: `target/classes/META-INF/dekorate/openshift.yml`.

The generated list should now contain the following item:

    ---
    - apiVersion: "image.openshift.io/v1"
      kind: "ImageStream"
      metadata:
        name: "s2i-java"
      spec:
        dockerImageRepository: "fabric8/s2i-java"
    - apiVersion: "image.openshift.io/v1"
      kind: "ImageStream"
      metadata:
        name: "source-to-image-example"
    - apiVersion: "build.openshift.io/v1"
      kind: "BuildConfig"
      metadata:
        name: "source-to-image-example"
      spec:
        output:
          to:
            kind: "ImageStreamTag"
            name: "source-to-image-example:1.0-SNAPSHOT"
        source:
          binary: {}
        strategy:
          sourceStrategy:
            from:
              kind: "ImageStreamTag"
              name: "s2i-java:2.3"
    
