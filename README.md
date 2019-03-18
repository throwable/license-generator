# License Generator

A simple tool to protect your Java code with license restrictions.

## How does it work?

The tool generates a license key that contains product information and expiration restrictions.
This key must be placed in the product's binary directory.
During the compile time the tool injects a small piece of code into constructors of specified classes that
does verification of the provided license key and checks it's expiration period.
If the license is not valid the program exists.

### What the verification process exactly does?

* The verification occurs periodically during a constructor call on any of the enhanced classes
* At first invocation it searches and loads the ```license.key``` file. The file is searched in these locations:
    * A file specified with ```license.key``` system property (-Dlicense.key=...)
    * Current directory of the java process (user.dir)
    * Directory where is located the .jar file that contains current class (usually /lib)
    * Classpath root resource
* It verifies the license's signature and a correspondent productId value
* It checks that current timestamp is in range specified by the license
    * The process tries to detect and protect against tricks with system time manipulation
    * An external temporal hidden file is used to save the latest timestamp
* If the verification fails a ```license.fail``` file containing error code is generated in current directory and the 
process terminates with System.exit()
* Next time the verification will be done only after the some period of time specified at compile time

## Getting started

Download ```license-generator-all.jar``` from repository.  

1. First we need to generate a public/private key pair for your product that will be used to sign/verify license:

```
java -jar license-generator-all.jar keygen
```
That will generate two files: ```public.key``` and ```private.key```.

2. Now we must run the class enhancement tool to inject a license verification code into your classes. 
This step must be proceeded just right after the compile phase and before packaging files into a .jar file.

```
java -jar license-generator-all.jar -cp <project_classpath> enhance -p <product_id> -k <public.key> class1 class2...
```
where

```product_id``` any string that identifies your product (e.g. product's title + version) 

```project_classpath``` classpath line that contains a root directory (or a list of directories separated by path separator) 
where compiled .class files are located. It also needed to specify all .jar dependencies needed to compile your classes.

```public.key``` the generated public.key file

```class1..classN``` class names where the verification code will be injected.
Keep in mind that the verification only occurs on class instantiation.  

3. Create a file ```license.properties``` that contains these required properties:
* ```productId``` you product identifier (the same as specified at enhancement phase)
* ```validFrom``` date the license become valid from (yyyy-mm-dd)
* ```validUntil``` date when the license expires (yyyy-mm-dd) 

A file may contain any other properties like customer's name, generation date, etc...

4. Sign your license file:

```
java -jar license-generator.jar sign -pri <private.key file> -pub <public.key file> -l <license.properties file> [-o <output dir>]
```
That will generate a signed license.key file. Place this file into your product's installation directory or into jars directory.

## Build tool integration

The goal is to run the injection tool right after a compile task. 
In this case your tests may be affected if you have no a valid license.key in classpath or in the project's root folder.
To solve this problem simply generate a build-time perpetual license and place it to project's root or to src/test/resources forder.  

### Gradle

```
buildscript {
    repositories {
        maven {
            url  "https://dl.bintray.com/antkuranov/pub"
        }
    }
    dependencies {
        classpath 'es.minsait.tm.license:license-generator:0.1'
    }
}
task injectLicense(type:JavaExec) {
    doFirst {
        println("Injecting license verification code...")
    }
    classpath = buildscript.configurations.classpath
    main = 'es.minsait.tm.license.gen.Main'
    args = ['enhance',
            '-cp', sourceSets.main.runtimeClasspath.asPath,
            '-p', 'My Product ID',
            '-k', "${project.projectDir}/public.key",
            'my.product.SomeClass1',
            'my.product.SomeClass2',
            'my.product.SomeClass3',
            ...
            ]
}
compileKotlin.doLast {
    injectLicense.execute()
}
```

### Maven

TODO: ...
