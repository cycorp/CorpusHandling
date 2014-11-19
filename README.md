CorpusHandling
==============


Included
--------

* All source files
* A suite of tests including integration tests using a sample corpus
* A sample corpus consisting of 40 PMC IDs of freely available PubMed articles
* An example config.json file
* Resource files related to generating code using the JAXB plugin
* This README file

Requirements
------------

* This library requires Java 1.8.
* Additional requirements are described below.
* **_This code has not yet been tested on Windows._**

Description and Usage
---------------------

The CorpusHandling project is a Java library that pulls articles from PubMed
using a PMC identification number.  The library does two things:

1. Given a list of PMC IDs, the library can create subcorpora of any number of
   articles, making sure that there is no overlap across the subcorpora.

2. The library generates classes based on an xsd using the jaxb plugin.  These
   generated classes are used to extract information from the nxml
   representations of the articles.  The following information is currently
   extracted:
    * Title
    * Abstract
    * Information related to referencing the paper such as the author list and
      publication date

To use the library, you will have to install it to your local Maven
repository.  This will generate the necessary sources for creating subcorpora
and extracting information from the articles in a subcorpus.  (Note that when
you first open the project, it will have errors in it because these sources
are required.  The errors should go away after you install the project.)

The library depends on the existence of certain directories on your file
system.  Before using the library, you should create the following items
somewhere in your local file system:

* A base directory for your corpus;
* Directories for the DTDs that you want to use (see below).  To get the
  `PubMedOpenAccessPaperIT.java` tests to work, you will need the following
  directories:
      * `[base-directory]/NLMOpenAccess/DTD/JATS`
      * `[base-directory]/NLMOpenAccess/DTD/NLM2.3`
* A directory inside the the base directory for your subcorpora;
* An empty file inside your subcorpus directory called:  
  `[corpus-name]_used_articles.txt`

You will need to download the following DTDs to get the tests in
`PubMedOpenAccessPaperIT.java` to pass:

* ftp://ftp.ncbi.nlm.nih.gov/pub/jats/archiving/1.0/jats-archiving-dtd-1.0.zip
    * Extract into `[base-directory]/NLMOpenAccess/DTD/JATS`
* ftp://ftp.ncbi.nih.gov/pub/archive_dtd/archiving/2.3/archive-interchange-dtd-2.3.zip
    * Extract into `[base-directory]/NLMOpenAccess/DTD/NLM2.3`

If you need additional DTDs, look in http://jats.nlm.nih.gov/versions.html.
    
Once you have created these items, you should create a `config.json` file and
save it to the `src/main/resources` directory of the CorpusHandling project. 
You will have to specify the paths to your base directory and subcorpus
directory in the config file.  See `config.json.example` for more information.

There are two places in the code that you will need to edit based on the name
of your corpus:

1. `InitialCollection.java` -- Change `resourceName` to the name of your
   corpus list file.  This file should live in the `com.cyc.corpus.papers`
   package in `src/main/resources`.
2. `Subcollection.java` -- Change `usedArticleListFileName` to the name of the
   file you created above.

Once all of this is in place, you can install the library and all errors
should go away.

There are a few tests that you can look at to get an idea of how to use the
library:

1. `SubcollectionIT.java` -- The tests in this class demonstrate how to create
   and retrieve a subcorpus.
2. `PubMedOpenAccessPaperIT.java` -- The tests in this class demonstrate how
   to extract information from a particular article.  The tests are based on
   articles from the included `SamplePMCCorpus.txt` corpus.

