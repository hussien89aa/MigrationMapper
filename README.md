## MigrationMapper
<p align="justified">
<b>MigrationMapper</b> is an open-source tool that provides the developer with an easy-to-use and comprehensive way of extracting, from a given list of input projects, existing method mapping between two third-party libraries using program analysis based on Abstract Syntax Tree (AST) code representation. In a nutshell, MigrationMiner (i) detects, (ii) extracts, (iii) filters, and (iv) collects code changes related to any performed migration, and (v) generates mappings between APIs of two libraries. A demo video of MigrationMapper is available on [Youtube](https://www.youtube.com/watch?v=D-01g2GjuTg)

 ### When you use this tool, please cite this paper.
<pre>
@inproceedings{alrubaye2019use,
  title={On the use of information retrieval to automate the detection of third-party java library migration at the method level},
  author={Alrubaye, Hussein and Mkaouer, Mohamed Wiem and Ouni, Ali},
  booktitle={2019 IEEE/ACM 27th International Conference on Program Comprehension (ICPC)},
  pages={347--357},
  year={2019},
  organization={IEEE}
}
</pre>


## Prerequisites

* Install java JDK >= 8 from [here](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html).
* Install  MYSQL Server from [here](https://dev.mysql.com/downloads/installer/).
* Install Python 3.8 or later from [here](https://www.python.org/downloads/).
* Tool supports only MacOS and Linux based OS (ex, Ubuntu). If you are using Ubuntu, please install curl by running the following commands "sudo apt install curl".


## How to install and run the tool

#### To run the project on your local machine you can follow one of these two tutorials:
 
 #### A- Setup database
* First you need to build the dataset, by running the following script Database/MigrationMapperDBSQL.sql.
Open a terminal and run the following commands
```sql
 mysql -u root -p
 source ./MigrationMapperDBSQL.sql
```

After running the commands, the database should be created with all tables and views.

#### B- Setup Python server

* Install the following packages

```
% pip3 install Django
% pip3 install jellyfish
% pip3 install --user -U nltk
% pip3 install numpy
% pip3 install pandas
% pip3 install sklearn
```

* download NLP  words
```
% python3

>>> import nltk 
>>> nltk.download('punkt')
>>> nltk.download('stopwords')
>>> nltk.download('wordnet')
```

* Run python server for NLP text processing by going to folder 'python-service' and run 
```
% python3 manage.py runserver

```

#### C- Setup MigrationMapper
* Update [MigrationMiner/data/gitRepositories.csv](https://github.com/hussien89aa/MigrationMapper/blob/master/MigrationMapper/data/gitRepositories.csv) with the list of git repositories that you want to use as input (they will be searched for potential library migrations).
* Update [MigrationMiner/data/config.json](https://github.com/hussien89aa/MigrationMapper/blob/master/MigrationMapper/data/config.json) to set your local MYSQL username and password  and github developer token in. to get github developer token Go to your github account under Settings > Developer Settings > Personal Access Tokens, add new token. Your token will be used so that Migration Miner can search a large number of GitHub projects without authentication issues.

#### D- Run MigrationMapper: There are two ways to run the tool either using IDE or terminal which are:

##### 1- Run MigrationMapper from the source code uisng IDE
* Install Any Java IDE that support Maven Java project such as (Eclipse, IntelliJ IDEA, etc).
* Open eclipse IDE then go to File-> import-> Maven-> existing Maven Projects-> Select MigrationMiner directory.
* (Optional) We print alot of logs, to avoid console buffer overflow. In eclipse IDE go to   preferences-> console-> limit console buffer size to small number such as 10000.
* Run the [Main.java](https://github.com/hussien89aa/MigrationMapper/blob/master/MigrationMapper/src/main/java/com/main/parse/Main.java).

##### 2- Run MigrationMapper using the Jar file (MigrationMapper.jar)
* Copy MigrationMapper.jar and [data](https://github.com/hussien89aa/MigrationMapper/blob/master/MigrationMapper/data/) folder that has gitRepositories.csv and config.json, which you need to update, to same directory.
```
--|
  |- MigrationMapper.jar
  |- data
       |- gitRepositories.csv
       |- config.json
```

* from terminal run
```
% java -jar MigrationMapper.jar

```

## Tool output

##### A- Ouput as Relational Database
* After running Main.java, the database Tables will be filled with any migration infomation found. For each potential migration, the following information can be found in database, whose schema is as follows:
 
   * Repositories: List of projects that are scanned by the tool.
   * AppCommits: List of projects' commits information (Commit Id, developer name, Commit text, and commit date).
   * ProjectLibraries: List of libraries that were added or removed at every commit.
   * MigrationRules:  List of migration Rules that were detected from the Dataset.
   * MigrationSegments: List Of migration Fragments that were extracted from software migration.
   * LibraryDocumenation: Library documentation associated with every library version that has been involved in any migration.
   * MigrationMapping: List of method mapping between two APIs.
##### B- Ouput as HTML
   There will be a generated HTML file named "MigrationMapperOutput.html" that has the summary of all migrations detected, and for each migration, all its corresponding method mapping along with their Library documentation. An illutrative example of this file is in the following picture:
   
![main](http://attach.alruabye.net/migrationminer/methodMapping.jpg)


##### C- Ouput as Objects
After running Main.java, You could read the output as objects by writing the following code. or run [TestClient.java](https://github.com/hussien89aa/MigrationMapper/blob/master/MigrationMapper/src/main/java/com/main/parse/TestClient.java). That could help you to integrate the tool with your code.

```java
 
//Return list of migrations between two pairs of libraries( added/removed)
LinkedList<MigrationRule> migrationRules= new MigrationRuleDB().getMigrationRulesWithoutVersion(1);

for (MigrationRule migrationRule : migrationRules) {
 System.out.println("== Migration Rule "+ migrationRule.FromLibrary +
      " <==> "+  migrationRule.ToLibrary +"==");

 /*
 *  For every migrations, retrieve list of detected Method mapping
 *  Between Two APIs
 */
 ArrayList<Segment> segmentList = new MigrationMappingDB().getFunctionMapping(String.valueOf(migrationRule.ID), false, false);

 for (Segment segment : segmentList) {

  segment.print();

  // Print all removed method signatures With Docs
  printMethodWithDocs( migrationRule.FromLibrary,segment.removedCode);  

  // Print all added method signatures With Docs
  printMethodWithDocs( migrationRule.ToLibrary,segment.addedCode);

 } // End fragment for every migration

}  // End library migration


/* 
* This method takes list of methods signatures with library that methods belong to.
* It will print the signatures and Docs for every method
*/
void printMethodWithDocs(String libraryName,ArrayList<String> listOfMethods ) {

 // For every add method print the Docs
 for(String methodSignature: listOfMethods){

  // Convert  method signatures as String to Object
  MethodObj methodFormObj= MethodObj.GenerateSignature(methodSignature);

  //retrieve Docs from the library for method has that name
  ArrayList<MethodDocs>  toLibrary = new LibraryDocumentationDB()
                                           .getDocs( libraryName,methodFormObj.methodName);

  //Map method signatures to docs
  MethodDocs methodFromDocs = MethodDocs.GetObjDocs(toLibrary, methodFormObj);

  if(methodFromDocs.methodObj== null) {
   System.err.println("Cannot find Docs for: "+ methodSignature);
   continue;
  }
  methodFromDocs.print();      
 }
}
```
 
## MigrationMapper has been used so far in the following papers:

* Alrubaye, H., & Mkaouer, M. W. (2018, October). [Automating the detection of third-party Java library migration at the function level](https://dl.acm.org/citation.cfm?id=3291299). In Proceedings of the 28th Annual International Conference on Computer Science and Software Engineering (pp. 60-71). IBM Corp.
* Alrubaye, H., Mkaouer, & M. W., Ali, O (2019).[ On the Use of Information Retrieval to Automate the Detection of Third-Party Java Library Migration At The Function Level](https://dl.acm.org/citation.cfm?id=3339129), 27th IEEE/ACM International Conference on Program Comprehension 2019.
* Alrubaye, H (2020) [Towards the Automation of Migration and Safety of Third-Party Libraries](https://scholarworks.rit.edu/theses/10614/), Rochester Institute of Technology
 
## License

This software is licensed under the [MIT license](https://opensource.org/licenses/MIT).
