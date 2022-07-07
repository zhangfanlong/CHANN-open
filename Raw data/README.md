# Raw data
## Dataset specification
1. Download source code.
    Since the projects original datasets contain multiple versions, we cannot provide them online (too big) , but it can be downloaded on SourceForge according to the version of each project we present in the paper, for example, the jFreeChart project can be downloaded by following link: 
[https://sourceforge.net/projects/jfreechart/files/1.%20JFreeChart/](https://sourceforge.net/projects/jfreechart/files/1.%20JFreeChart/)

2. After download the source code, you can employ NiCad to detect all these code clones from each version of project. The Nicad can be found with the following link:
[https://www.txl.ca/txl-nicaddownload.html](https://www.txl.ca/txl-nicaddownload.html)

3. With these code clones, we build all the clone genalogies for each project following the Work of Zhang et al., that can be found under the "code clones" folder of [https://www.dropbox.com/scl/fo/o6y4igsl3hl0rla8r4n60/h?dl=0&rlkey=aza56jcoezl55xajb6kxnugtx](https://www.dropbox.com/scl/fo/o6y4igsl3hl0rla8r4n60/h?dl=0&rlkey=aza56jcoezl55xajb6kxnugtx)


4. Raw Data Description.

    * In the "code clones" folder. We provide xml files for all these code clones detected by NiCad. Each file contains all the clone groups and its clone fragments for the project with a specific version.

    * In the "clone genealogy dataset" folder. we provide xml files for clone genealogy in each project we have collected, which can be combined with the original data to obtain the corresponding cloned code snippets of the clone evolution process.

    * In the "arff dataset" folder, we provide data sets that are fed into the Baselines model. Details of the data are as follows:

        - Relation declaration** : The relation name is defined in the first valid line of the ARFF file in the format @relation <relation-name>;

        - Attribute declaration** : Attribute declarations are represented by a list of statements beginning with "@attribute". Each attribute in the dataset has a corresponding "@attribute" statement that defines its attribute name and data type. For example, @attribute <attribute-name> <datatype>. There are four types of datatype: numeric, nominal, string and date;

        - Data information**:The "@data" tag occupies a single line of data information, and the rest is data for each instance.Each instance is on a row. Instance attribute values are separated by commas (,).

## How to build the dataset
Start with step 1 if you want to build the data from the projects original datasets, otherwise start with step 4.
1. Download source code(projects original datasets).
2. Download NiCad, and then employ it to detect all these code clones from each version of project.
3. Run the **Feature Extractor** to collect the clone genealogy.
4. Run the **pipeline.py** to process the clone genealogy and build the preprocessed dataset.