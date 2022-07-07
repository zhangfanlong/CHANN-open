# FE4CCP
## How to run
1. Run **Select Directory** to load the historical version of the software system and the clone result
   * Import source code folder: The source code path gives the path to the folder containing the source code of various historical versions. For example, the multi-version source code path of the dnsJava system is stored in the SubjectSys_dnsJava folder, and the source code path is given to the SubjectSys_dnsJava folder.
   * Import the clone result file: Load the clone detection result path clone, and the detection result path is given to the upper level path of the detection result. For example, when the dnsJava system uses Nicad to perform detection by block, the storage format is that there is a block folder under the dnsJava folder, and the block folder stores the clone detection results, so the path is given to the dnsJava folder instead of the block folder.

2. Run **CRD**(Clone Region Discription) to create a clone region descriptor
3. Run **Map** to construct the mapping relationship of cloned code between adjacent versions
4. Run **Create Genealogy** to generate clone family
   1. Run **Write Genealogy** to save the genealogy XML file locally
5. Run **Extract Feature (extract creating data, namely Type1, 29-dimensional data) / Temp Extract Feature (extract changing data, namely Type2, 105-dimensional data)** to extract feature files